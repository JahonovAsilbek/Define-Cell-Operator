package uz.jahonov.defineoperator.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.jahonov.defineoperator.domain.model.NetworkStatus
import uz.jahonov.defineoperator.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for observing real-time network changes.
 *
 * This use case provides a Flow that emits network status updates whenever the network state
 * changes. It's designed for use in ViewModels or other components that need to react to
 * network changes in real-time.
 *
 * **Network changes monitored:**
 * - SIM card insertion/removal
 * - Wi-Fi connection/disconnection
 * - Mobile data connection/disconnection
 * - Switching between Wi-Fi and cellular
 * - Roaming status changes
 * - Network type changes (3G → 4G, 4G → 5G, etc.)
 * - Active data SIM changes (on dual-SIM devices)
 *
 * **Flow characteristics:**
 * - Cold Flow: Starts monitoring only when collected
 * - Emits initial state immediately upon collection
 * - Emits new NetworkStatus on each network change
 * - Automatically cleans up when Flow is cancelled
 * - Thread-safe: Can be collected from multiple coroutines
 *
 * **Example usage in ViewModel:**
 * ```kotlin
 * class MyViewModel @Inject constructor(
 *     private val observeNetworkChangesUseCase: ObserveNetworkChangesUseCase
 * ) : ViewModel() {
 *     val networkStatus: StateFlow<NetworkStatus> = observeNetworkChangesUseCase()
 *         .stateIn(
 *             scope = viewModelScope,
 *             started = SharingStarted.WhileSubscribed(5000),
 *             initialValue = NetworkStatus(emptyList(), ConnectionState.Unknown)
 *         )
 * }
 * ```
 *
 * @property networkRepository Repository providing network observation capabilities
 */
class ObserveNetworkChangesUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    /**
     * Executes the use case to observe network changes.
     *
     * Returns a Flow that emits [NetworkStatus] updates whenever the network state changes.
     * The Flow emits immediately with the current state, then continues emitting on each change.
     *
     * **Flow behavior:**
     * - First emission: Current network status (immediate)
     * - Subsequent emissions: Only when network state changes
     * - Cancellation: Automatically unregisters network callbacks
     * - Errors: Emitted via Flow.catch() or try-catch in collector
     *
     * **Performance notes:**
     * - Uses Android's NetworkCallback mechanism (efficient, system-level monitoring)
     * - No polling - only reacts to actual system events
     * - Minimal battery impact (system manages callbacks efficiently)
     *
     * @return Flow of network status updates. Emits on collection and on each network change.
     */
    operator fun invoke(): Flow<NetworkStatus> {
        return networkRepository.observeNetworkChanges()
    }
}
