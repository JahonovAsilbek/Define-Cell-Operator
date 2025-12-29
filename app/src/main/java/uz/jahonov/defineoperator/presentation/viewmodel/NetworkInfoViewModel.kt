package uz.jahonov.defineoperator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import uz.jahonov.defineoperator.domain.model.NetworkStatus
import uz.jahonov.defineoperator.domain.usecase.CheckPermissionsUseCase
import uz.jahonov.defineoperator.domain.usecase.GetActiveConnectionUseCase
import uz.jahonov.defineoperator.domain.usecase.GetSimCardsInfoUseCase
import uz.jahonov.defineoperator.domain.usecase.ObserveNetworkChangesUseCase
import uz.jahonov.defineoperator.domain.repository.PermissionRepository
import uz.jahonov.defineoperator.presentation.state.NetworkUiState
import uz.jahonov.defineoperator.util.Logger
import javax.inject.Inject

/**
 * ViewModel for managing network information UI state.
 *
 * This ViewModel coordinates permission checking, network data fetching, and real-time
 * network observation. It exposes UI state as StateFlow for the Compose UI to observe
 * and react to changes.
 *
 * **Architecture pattern: MVVM**
 * - View (Compose UI) observes [uiState] StateFlow
 * - ViewModel executes use cases and updates state
 * - Use cases execute business logic via repository
 *
 * **State management:**
 * - Uses MutableStateFlow internally for state updates
 * - Exposes immutable StateFlow to UI
 * - UI state follows sealed class pattern (Loading, Success, Error, PermissionDenied)
 *
 * **Lifecycle:**
 * - Created when UI enters composition
 * - Survives configuration changes (screen rotation, etc.)
 * - Cleared when UI is permanently removed
 * - Network observation automatically cancelled in onCleared()
 *
 * @property getSimCardsInfoUseCase Use case for retrieving SIM card information
 * @property getActiveConnectionUseCase Use case for determining active connection
 * @property observeNetworkChangesUseCase Use case for real-time network monitoring
 * @property checkPermissionsUseCase Use case for checking permission status
 * @property permissionRepository Repository for permission management
 * @property logger Logger for debugging and error tracking
 */
@HiltViewModel
class NetworkInfoViewModel @Inject constructor(
    private val getSimCardsInfoUseCase: GetSimCardsInfoUseCase,
    private val getActiveConnectionUseCase: GetActiveConnectionUseCase,
    private val observeNetworkChangesUseCase: ObserveNetworkChangesUseCase,
    private val checkPermissionsUseCase: CheckPermissionsUseCase,
    private val permissionRepository: PermissionRepository,
    private val logger: Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkUiState>(NetworkUiState.Loading)

    /**
     * Current UI state.
     *
     * This StateFlow emits UI state changes that the Compose UI observes and reacts to.
     * The UI should collect this flow and render appropriate screens based on the state.
     *
     * **State transitions:**
     * ```
     * Loading (initial) → PermissionDenied (if no permissions)
     *                  → Success (if permissions granted and data loaded)
     *                  → Error (if data loading fails)
     *
     * PermissionDenied → Loading → Success (after user grants permissions)
     * Success → Success (on network changes or manual refresh)
     * Error → Loading → Success/Error (on retry)
     * ```
     *
     * **Example usage in Compose:**
     * ```kotlin
     * val uiState by viewModel.uiState.collectAsStateWithLifecycle()
     * when (uiState) {
     *     is NetworkUiState.Loading -> LoadingScreen()
     *     is NetworkUiState.Success -> SuccessScreen(uiState.networkStatus)
     *     is NetworkUiState.Error -> ErrorScreen(uiState.message)
     *     is NetworkUiState.PermissionDenied -> PermissionScreen()
     * }
     * ```
     */
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    init {
        checkPermissionsAndLoadData()
    }

    /**
     * Checks permissions and loads network data if granted.
     *
     * This method is called:
     * - On ViewModel initialization (init block)
     * - After user grants permissions (onPermissionsGranted)
     * - When user triggers manual check (if exposed via UI)
     *
     * **Flow:**
     * 1. Check if required permissions are granted
     * 2. If granted: Start observing network changes
     * 3. If not granted: Set PermissionDenied state with required permission list
     */
    fun checkPermissionsAndLoadData() {
        if (checkPermissionsUseCase()) {
            logger.d(TAG, "Permissions granted, starting network observation")
            observeNetworkChanges()
        } else {
            logger.w(TAG, "Permissions not granted")
            _uiState.value = NetworkUiState.PermissionDenied(
                requiredPermissions = permissionRepository.getRequiredPermissions().toList()
            )
        }
    }

    /**
     * Called when permissions are granted by the user.
     *
     * This method should be called from the UI after the permission request is completed
     * and the user has granted all required permissions. It triggers permission check
     * and data loading.
     *
     * **Example usage in Compose:**
     * ```kotlin
     * val permissionLauncher = rememberLauncherForActivityResult(
     *     ActivityResultContracts.RequestMultiplePermissions()
     * ) { permissions ->
     *     if (permissions.values.all { it }) {
     *         viewModel.onPermissionsGranted()
     *     }
     * }
     * ```
     */
    fun onPermissionsGranted() {
        logger.d(TAG, "Permissions granted, reloading network data")
        checkPermissionsAndLoadData()
    }

    /**
     * Manually refreshes network data.
     *
     * This method performs a one-time refresh of network data without using the continuous
     * observation. It's useful for:
     * - Pull-to-refresh functionality
     * - Retry after error
     * - Manual refresh button
     *
     * **Note:** This is different from observeNetworkChanges() which provides continuous updates.
     * Use this when you want a single refresh rather than ongoing observation.
     *
     * **State transitions:**
     * Loading → Success (on successful fetch)
     *       → Error (on failure)
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                logger.d(TAG, "Manual refresh triggered")
                _uiState.value = NetworkUiState.Loading

                val simCards = getSimCardsInfoUseCase()
                val connection = getActiveConnectionUseCase()
                val status = NetworkStatus(simCards, connection)

                _uiState.value = NetworkUiState.Success(status)
                logger.d(TAG, "Manual refresh completed successfully")
                logNetworkStatus(status)
            } catch (e: Exception) {
                logger.e(TAG, "Error during manual refresh", e)
                _uiState.value = NetworkUiState.Error(
                    message = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Observes network changes in real-time.
     *
     * This method starts continuous monitoring of network state using Flow. It automatically:
     * - Emits initial network state immediately
     * - Emits updates whenever network state changes (Wi-Fi ↔ Cellular, connection/disconnection)
     * - Handles errors by emitting Error state
     * - Cancels observation when ViewModel is cleared
     *
     * **Network changes monitored:**
     * - SIM card insertion/removal
     * - Wi-Fi connection/disconnection
     * - Mobile data connection/disconnection
     * - Switching between Wi-Fi and cellular
     * - Roaming status changes
     * - Network type changes (3G → 4G, etc.)
     *
     * **Implementation:**
     * Uses viewModelScope.launch to ensure the Flow collection is cancelled when the
     * ViewModel is cleared, preventing memory leaks.
     */
    private fun observeNetworkChanges() {
        viewModelScope.launch {
            observeNetworkChangesUseCase()
                .catch { e ->
                    logger.e(TAG, "Error observing network changes", e)
                    _uiState.value = NetworkUiState.Error(
                        message = e.message ?: "Failed to monitor network changes"
                    )
                }
                .collect { status ->
                    logger.d(TAG, "Network status update received")
                    _uiState.value = NetworkUiState.Success(status)
                    logNetworkStatus(status)
                }
        }
    }

    /**
     * Logs network status for debugging.
     *
     * This method logs detailed network information to Logcat for debugging and monitoring.
     * It's called whenever network status is updated (initial load or change).
     *
     * **Log format:**
     * ```
     * [NetworkInfoViewModel] === Network Status ===
     * [NetworkInfoViewModel] SIM Cards: 2
     * [NetworkInfoViewModel]   Slot 0: Beeline (43405, Roaming: false)
     * [NetworkInfoViewModel]   Slot 1: Ucell (43407, Roaming: false)
     * [NetworkInfoViewModel] Active Connection: Mobile(Beeline, 4G LTE, Not Roaming)
     * [NetworkInfoViewModel] =====================
     * ```
     *
     * @param status Network status to log
     */
    private fun logNetworkStatus(status: NetworkStatus) {
        logger.d(TAG, "=== Network Status ===")
        logger.d(TAG, "SIM Cards: ${status.simCards.size}")
        status.simCards.forEach { sim ->
            logger.d(
                TAG,
                "  Slot ${sim.slotIndex}: ${sim.carrierName} " +
                        "(${sim.networkOperator}, Roaming: ${sim.isDataRoaming})"
            )
        }
        logger.d(TAG, "Active Connection: ${status.activeConnection}")
        logger.d(TAG, "=====================")
    }

    companion object {
        private const val TAG = "NetworkInfoViewModel"
    }
}
