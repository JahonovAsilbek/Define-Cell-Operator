package uz.jahonov.defineoperator.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.jahonov.defineoperator.domain.model.ConnectionState
import uz.jahonov.defineoperator.domain.model.NetworkStatus
import uz.jahonov.defineoperator.domain.model.SimCardInfo

/**
 * Repository for accessing network and telephony information.
 *
 * This interface defines the contract for retrieving network and SIM card data from the system.
 * It abstracts the underlying Android APIs (TelephonyManager, SubscriptionManager,
 * ConnectivityManager) and provides a clean, testable interface for the domain layer.
 *
 * **Implementation notes:**
 * - All suspend functions should be called from a background dispatcher (typically Dispatchers.IO)
 * - Implementations must handle SecurityException when permissions are denied
 * - Implementations should log errors appropriately
 * - Flows should use callbackFlow to convert callback-based APIs to Flow
 *
 * **Required permissions:**
 * - android.permission.READ_PHONE_STATE (for SIM card information)
 * - android.permission.ACCESS_NETWORK_STATE (for network connection state)
 */
interface NetworkRepository {
    /**
     * Gets information about all available SIM cards.
     *
     * This method retrieves detailed information about each SIM card installed in the device,
     * including carrier name, subscription ID, slot index, and roaming status.
     *
     * **Android APIs used:**
     * - SubscriptionManager.getActiveSubscriptionInfoList(): Returns list of active SIM cards
     * - TelephonyManager.createForSubscriptionId(): Creates subscription-specific manager
     *
     * **Multi-SIM support:**
     * On devices with multiple SIM card slots (dual-SIM, triple-SIM, etc.), this method
     * returns information for all active subscriptions. Each SIM has a unique subscriptionId
     * and a physical slotIndex (0, 1, 2, etc.).
     *
     * **Error handling:**
     * - Returns empty list if READ_PHONE_STATE permission is not granted
     * - Returns empty list if no SIM cards are installed
     * - Returns empty list if a SecurityException occurs
     *
     * @return List of SIM card information. Empty list if no SIMs or permissions denied.
     *         The list is typically sorted by slot index for consistent ordering.
     */
    suspend fun getSimCardsInfo(): List<SimCardInfo>

    /**
     * Gets the current active internet connection state.
     *
     * This method determines which connection (Wi-Fi or Cellular) is currently being used
     * for internet access. On devices with both Wi-Fi and cellular enabled, Android typically
     * routes internet traffic through Wi-Fi.
     *
     * **Android APIs used:**
     * - ConnectivityManager.getActiveNetwork(): Gets the currently active network
     * - NetworkCapabilities.hasTransport(): Determines transport type (WIFI or CELLULAR)
     * - SubscriptionManager.getDefaultDataSubscriptionId(): For cellular, identifies which SIM
     * - TelephonyManager.isNetworkRoaming: Checks if the cellular connection is roaming
     *
     * **Connection priority:**
     * When multiple networks are available, Android selects based on:
     * 1. Wi-Fi (highest priority when available and connected)
     * 2. Cellular data (used when Wi-Fi unavailable or disabled)
     * 3. Other transports (Ethernet, Bluetooth, VPN, etc.)
     *
     * **Error handling:**
     * - Returns ConnectionState.Unknown if ACCESS_NETWORK_STATE permission not granted
     * - Returns ConnectionState.None if no active internet connection
     * - Returns ConnectionState.Unknown if an error occurs during detection
     *
     * @return Current connection state. One of: Mobile, WiFi, None, or Unknown.
     */
    suspend fun getActiveConnection(): ConnectionState

    /**
     * Observes network changes in real-time.
     *
     * This method returns a Flow that emits [NetworkStatus] whenever the network state changes.
     * Changes include:
     * - SIM card insertion/removal
     * - Network connection/disconnection (Wi-Fi, cellular)
     * - Switching between Wi-Fi and cellular
     * - Roaming status changes
     * - Network type changes (3G → 4G, etc.)
     *
     * **Android APIs used:**
     * - ConnectivityManager.registerNetworkCallback(): Registers for network change callbacks
     * - NetworkCallback.onAvailable(): Called when a network becomes available
     * - NetworkCallback.onLost(): Called when a network is disconnected
     * - NetworkCallback.onCapabilitiesChanged(): Called when network capabilities change
     *
     * **Flow behavior:**
     * - Emits initial state immediately upon collection
     * - Emits new state whenever network changes occur
     * - Uses callbackFlow to convert callbacks to Flow
     * - Automatically unregisters callback when Flow is cancelled
     * - Runs on background dispatcher (typically Dispatchers.IO)
     *
     * **Example usage:**
     * ```kotlin
     * viewModelScope.launch {
     *     repository.observeNetworkChanges()
     *         .collect { networkStatus ->
     *             // Update UI with new network status
     *             _uiState.value = UiState.Success(networkStatus)
     *         }
     * }
     * ```
     *
     * @return Flow that emits NetworkStatus whenever network state changes.
     *         The Flow is cold - it only starts monitoring when collected.
     */
    fun observeNetworkChanges(): Flow<NetworkStatus>
}
