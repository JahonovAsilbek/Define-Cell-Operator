package uz.jahonov.defineoperator.domain.model

/**
 * Represents the current active internet connection state.
 *
 * This sealed class provides a type-safe representation of the different connection states
 * a device can be in. It uses Android's ConnectivityManager and NetworkCapabilities APIs
 * to determine which transport (Wi-Fi or Cellular) is currently active for internet access.
 *
 * On devices with multiple network interfaces (Wi-Fi + Cellular), Android automatically
 * selects the preferred connection (typically Wi-Fi when available).
 */
sealed class ConnectionState {
    /**
     * Mobile data connection is active.
     *
     * This state indicates that cellular data is currently being used for internet access.
     * On dual-SIM devices, the [subscriptionId] identifies which SIM card is providing
     * the data connection.
     *
     * **How it's detected:**
     * - Uses ConnectivityManager.getActiveNetwork() to get the active network
     * - Checks NetworkCapabilities.hasTransport(TRANSPORT_CELLULAR)
     * - Uses SubscriptionManager.getDefaultDataSubscriptionId() to identify the SIM
     * - Uses TelephonyManager.isNetworkRoaming to check roaming status
     *
     * @property subscriptionId The subscription ID of the SIM providing data.
     *                          Matches the subscriptionId in [SimCardInfo].
     * @property carrierName The carrier name providing the connection (e.g., "Beeline", "Ucell")
     * @property isRoaming Whether the connection is in roaming mode. True if the device is
     *                     connected to a network other than the home network (e.g., when
     *                     traveling abroad or in areas without home network coverage).
     * @property networkType The type of cellular network (2G, 3G, 4G LTE, 5G NR, etc.)
     */
    data class Mobile(
        val subscriptionId: Int,
        val carrierName: String,
        val isRoaming: Boolean,
        val networkType: NetworkType
    ) : ConnectionState()

    /**
     * Wi-Fi connection is active.
     *
     * This state indicates that Wi-Fi is currently being used for internet access.
     * When both Wi-Fi and mobile data are enabled, Wi-Fi typically takes priority.
     *
     * **How it's detected:**
     * - Uses ConnectivityManager.getActiveNetwork() to get the active network
     * - Checks NetworkCapabilities.hasTransport(TRANSPORT_WIFI)
     *
     * @property ssid The Wi-Fi network name (Service Set Identifier). May be null if the
     *               SSID is not available or location permission is not granted (required
     *               on Android 10+ to access SSID).
     */
    data class WiFi(val ssid: String?) : ConnectionState()

    /**
     * No internet connection available.
     *
     * This state indicates that the device is not connected to any network with internet
     * capability. This can occur when:
     * - Airplane mode is enabled
     * - Mobile data and Wi-Fi are both disabled
     * - Connected to a network without internet access
     * - In an area with no signal coverage
     */
    data object None : ConnectionState()

    /**
     * Connection state is unknown.
     *
     * This state indicates that the connection status could not be determined. This can
     * occur when:
     * - Required permissions (ACCESS_NETWORK_STATE) are not granted
     * - An error occurred while querying network state
     * - The device is in an unexpected network configuration
     */
    data object Unknown : ConnectionState()
}
