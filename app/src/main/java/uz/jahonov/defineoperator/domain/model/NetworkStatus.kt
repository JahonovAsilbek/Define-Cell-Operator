package uz.jahonov.defineoperator.domain.model

/**
 * Complete network status including all SIM cards and active connection.
 *
 * This data class provides a comprehensive snapshot of the device's network state at a
 * specific point in time. It combines information about all available SIM cards with
 * the current active internet connection.
 *
 * **Use cases:**
 * - Display complete network information to the user
 * - Log network state for debugging or analytics
 * - Monitor network changes over time
 * - Make routing decisions based on active connection
 *
 * @property simCards List of all available SIM cards in the device. The list is:
 *                    - Empty if no SIM cards are installed
 *                    - Empty if READ_PHONE_STATE permission is not granted
 *                    - Sorted by slot index for consistent ordering
 *                    Each entry contains detailed information about the SIM including
 *                    carrier name, subscription ID, roaming status, etc.
 * @property activeConnection Current active internet connection. Indicates whether the device
 *                            is using Wi-Fi, mobile data (and from which SIM), or has no
 *                            connection. This represents the actual connection being used
 *                            for internet traffic, not just what's enabled.
 * @property timestamp When this status snapshot was captured, in milliseconds since epoch
 *                     (System.currentTimeMillis()). Useful for tracking when network
 *                     changes occurred and ensuring data freshness.
 */
data class NetworkStatus(
    val simCards: List<SimCardInfo>,
    val activeConnection: ConnectionState,
    val timestamp: Long = System.currentTimeMillis()
)
