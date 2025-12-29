package uz.jahonov.defineoperator.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.SubscriptionManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Data source for network connectivity information.
 *
 * This class uses Android's ConnectivityManager and NetworkCapabilities APIs to determine
 * which connection (Wi-Fi or Mobile) is currently active for internet access. It also provides
 * real-time network monitoring capabilities through network callbacks.
 *
 * **Android APIs used:**
 * - [ConnectivityManager]: Manages network connectivity
 * - [NetworkCapabilities]: Provides detailed information about network capabilities and transports
 * - [Network]: Represents a network connection
 * - [ConnectivityManager.NetworkCallback]: Callback for monitoring network changes
 *
 * **How active connection is determined:**
 * 1. Get the active network using ConnectivityManager.getActiveNetwork()
 * 2. Query NetworkCapabilities for that network
 * 3. Check which transport is being used (TRANSPORT_WIFI or TRANSPORT_CELLULAR)
 * 4. For cellular, determine which SIM is providing data
 *
 * **Connection priority:**
 * Android automatically selects the preferred connection based on:
 * 1. Wi-Fi (highest priority when available and connected)
 * 2. Cellular data (used when Wi-Fi unavailable)
 * 3. Ethernet, Bluetooth, VPN (other transports, context-dependent priority)
 *
 * @property context Application context for accessing system services
 */
class ConnectivityDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    /**
     * Gets the currently active network.
     *
     * On Android 6.0+ (API 23+), this method uses NetworkCapabilities to determine which
     * network is actively being used for internet traffic. This properly handles scenarios
     * where multiple networks are available but only one is routing internet traffic.
     *
     * **VPN handling:**
     * When a VPN is active, getActiveNetwork() typically returns the VPN network. The VPN
     * network's NetworkCapabilities will indicate which underlying transport (Wi-Fi or cellular)
     * is being used.
     *
     * **Example scenarios:**
     * - Wi-Fi connected with internet: Returns Wi-Fi network
     * - Wi-Fi connected without internet + Cellular with internet: Returns cellular network
     * - Both disabled: Returns null
     * - Airplane mode: Returns null
     *
     * @return Active network or null if no network is available
     */
    fun getActiveNetwork(): Network? {
        return connectivityManager.activeNetwork
    }

    /**
     * Gets network capabilities for the specified network.
     *
     * NetworkCapabilities provides detailed information about a network including:
     * - **Transports:** WIFI, CELLULAR, BLUETOOTH, ETHERNET, VPN, etc.
     * - **Capabilities:** INTERNET, NOT_METERED, NOT_ROAMING, VALIDATED, etc.
     * - **Link properties:** Bandwidth, signal strength, etc.
     *
     * **Key capabilities:**
     * - NET_CAPABILITY_INTERNET: Network has internet connectivity
     * - NET_CAPABILITY_VALIDATED: Internet connectivity has been verified
     * - NET_CAPABILITY_NOT_ROAMING: Network is not roaming (for cellular)
     * - NET_CAPABILITY_NOT_METERED: Network is not metered (typically Wi-Fi)
     *
     * **Use cases:**
     * - Determine transport type: hasTransport(TRANSPORT_WIFI)
     * - Check if internet available: hasCapability(NET_CAPABILITY_INTERNET)
     * - Check roaming status: !hasCapability(NET_CAPABILITY_NOT_ROAMING)
     *
     * @param network The network to query
     * @return NetworkCapabilities or null if network is invalid or unavailable
     */
    fun getNetworkCapabilities(network: Network): NetworkCapabilities? {
        return connectivityManager.getNetworkCapabilities(network)
    }

    /**
     * Determines if Wi-Fi is the active transport.
     *
     * This method checks if the currently active network is using Wi-Fi as its transport.
     * Returns true only if:
     * 1. An active network exists
     * 2. The network has valid capabilities
     * 3. The network uses TRANSPORT_WIFI
     *
     * **Important:**
     * This checks the *active* transport, not just whether Wi-Fi is enabled.
     * - Wi-Fi enabled but not connected: Returns false
     * - Wi-Fi connected but cellular active (no internet on Wi-Fi): Returns false
     * - Wi-Fi connected with internet: Returns true
     *
     * @return true if Wi-Fi is the active transport, false otherwise
     */
    fun isWiFiActive(): Boolean {
        return try {
            val network = getActiveNetwork() ?: return false
            val capabilities = getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Wi-Fi active status", e)
            false
        }
    }

    /**
     * Determines if cellular (mobile data) is the active transport.
     *
     * This method checks if the currently active network is using cellular data as its transport.
     * Returns true only if:
     * 1. An active network exists
     * 2. The network has valid capabilities
     * 3. The network uses TRANSPORT_CELLULAR
     *
     * **Important:**
     * This checks the *active* transport, not just whether mobile data is enabled.
     * - Mobile data enabled but Wi-Fi active: Returns false
     * - Mobile data enabled but no signal: Returns false
     * - Mobile data active for internet: Returns true
     *
     * **Dual-SIM devices:**
     * On dual-SIM devices, this returns true if *any* SIM is providing cellular data.
     * Use getActiveDataSubscriptionId() to determine *which* SIM is active.
     *
     * @return true if cellular is the active transport, false otherwise
     */
    fun isCellularActive(): Boolean {
        return try {
            val network = getActiveNetwork() ?: return false
            val capabilities = getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking cellular active status", e)
            false
        }
    }

    /**
     * Gets the subscription ID of the currently active mobile data connection.
     *
     * On dual-SIM devices, multiple SIM cards can be active simultaneously, but only one
     * provides mobile data at a time. This method identifies which SIM is currently being
     * used for data.
     *
     * **How it works:**
     * - Android 7.0+ (API 24+): Uses SubscriptionManager.getDefaultDataSubscriptionId()
     * - This returns the subscription ID of the SIM configured for data in Settings
     * - The returned ID matches the subscriptionId in SubscriptionInfo
     *
     * **Return values:**
     * - Positive number (0, 1, 2...): Valid subscription ID of active data SIM
     * - -1 (SubscriptionManager.INVALID_SUBSCRIPTION_ID): No valid data subscription
     *   - Can occur in airplane mode
     *   - Can occur if no SIM is configured for data
     *   - Can occur on devices without mobile data
     *
     * **Example:**
     * ```kotlin
     * val dataSubId = getActiveDataSubscriptionId()
     * val subscriptions = telephonyDataSource.getActiveSubscriptions()
     * val dataSim = subscriptions.find { it.subscriptionId == dataSubId }
     * // dataSim now contains info about the SIM providing data
     * ```
     *
     * @return Subscription ID of active data SIM, or -1 if not available
     */
    @SuppressLint("MissingPermission")
    fun getActiveDataSubscriptionId(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // getDefaultDataSubscriptionId() is a static method
                SubscriptionManager.getDefaultDataSubscriptionId()
            } else {
                SubscriptionManager.INVALID_SUBSCRIPTION_ID
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting active data subscription ID", e)
            SubscriptionManager.INVALID_SUBSCRIPTION_ID
        }
    }

    /**
     * Registers a network callback to observe network changes.
     *
     * This method allows real-time monitoring of network state changes. The callback will
     * be invoked whenever network state changes occur, such as:
     * - Network becomes available (connection established)
     * - Network is lost (connection dropped)
     * - Network capabilities change (e.g., Wi-Fi → Cellular switch)
     * - Bandwidth changes
     *
     * **NetworkRequest:**
     * The method creates a NetworkRequest that filters for networks with internet capability.
     * This ensures callbacks are only triggered for networks that can provide internet access.
     *
     * **Callback methods:**
     * - onAvailable(network): Called when a network becomes available
     * - onLost(network): Called when a network is lost
     * - onCapabilitiesChanged(network, capabilities): Called when capabilities change
     * - onLinkPropertiesChanged(network, properties): Called when link properties change
     *
     * **Important:**
     * You MUST call unregisterNetworkCallback() when done to avoid memory leaks and
     * unnecessary callback invocations.
     *
     * **Example usage:**
     * ```kotlin
     * val callback = object : ConnectivityManager.NetworkCallback() {
     *     override fun onAvailable(network: Network) {
     *         // Network connected - update UI
     *     }
     *     override fun onLost(network: Network) {
     *         // Network disconnected - show offline message
     *     }
     * }
     * registerNetworkCallback(callback)
     * // Later: unregisterNetworkCallback(callback)
     * ```
     *
     * @param callback The callback to invoke on network changes
     */
    fun registerNetworkCallback(callback: ConnectivityManager.NetworkCallback) {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering network callback", e)
        }
    }

    /**
     * Unregisters a previously registered network callback.
     *
     * This method removes a network callback that was registered using registerNetworkCallback().
     * Always call this method when you no longer need network updates to:
     * - Prevent memory leaks
     * - Avoid unnecessary callback invocations
     * - Reduce battery consumption
     *
     * **When to call:**
     * - In ViewModel: When the ViewModel is cleared (onCleared())
     * - In Activity/Fragment: In onStop() or onDestroy()
     * - In Flow: In awaitClose() block of callbackFlow
     *
     * **Safe to call:**
     * - Calling with an unregistered callback is safe (no-op)
     * - Calling multiple times with same callback is safe (subsequent calls are no-op)
     *
     * @param callback The callback to unregister
     */
    fun unregisterNetworkCallback(callback: ConnectivityManager.NetworkCallback) {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            // Safe to ignore - callback might already be unregistered
            Log.d(TAG, "Error unregistering network callback (may already be unregistered)", e)
        }
    }

    companion object {
        private const val TAG = "ConnectivityDataSource"
    }
}
