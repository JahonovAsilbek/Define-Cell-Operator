package uz.jahonov.defineoperator.domain.usecase

import uz.jahonov.defineoperator.domain.model.ConnectionState
import uz.jahonov.defineoperator.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for determining the active internet connection.
 *
 * This use case implements the logic to determine which connection (Mobile or Wi-Fi) is
 * currently being used for internet access. It provides a simple interface to the presentation
 * layer without exposing repository implementation details.
 *
 * **Connection priority (Android system behavior):**
 * When multiple networks are available:
 * 1. Wi-Fi takes priority over cellular (when connected and has internet)
 * 2. Cellular data is used when Wi-Fi is unavailable or disabled
 * 3. VPN connections can override both, but are not tracked in this use case
 *
 * **Business logic:**
 * - If Wi-Fi is active: return ConnectionState.WiFi
 * - If cellular is active: return ConnectionState.Mobile with SIM details
 * - If neither: return ConnectionState.None
 * - If error/unknown: return ConnectionState.Unknown
 *
 * @property networkRepository Repository providing access to network connectivity data
 */
class GetActiveConnectionUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    /**
     * Executes the use case to get the current active connection.
     *
     * This method queries the current network state and returns a [ConnectionState] indicating
     * which connection is active. For mobile connections, it includes details about which SIM
     * is providing data, the carrier name, roaming status, and network type (4G, 5G, etc.).
     *
     * **Example results:**
     * - WiFi: `ConnectionState.WiFi(ssid = "MyWiFi")`
     * - Mobile: `ConnectionState.Mobile(subscriptionId = 1, carrierName = "Beeline",
     *            isRoaming = false, networkType = NetworkType.LTE)`
     * - No connection: `ConnectionState.None`
     * - Error: `ConnectionState.Unknown`
     *
     * @return Current connection state. Never null.
     */
    suspend operator fun invoke(): ConnectionState {
        return networkRepository.getActiveConnection()
    }
}
