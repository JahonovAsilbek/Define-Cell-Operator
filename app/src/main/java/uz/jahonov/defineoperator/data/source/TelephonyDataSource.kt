package uz.jahonov.defineoperator.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Data source for telephony-related information using Android APIs.
 *
 * This class wraps Android's TelephonyManager and SubscriptionManager APIs to provide
 * SIM card and mobile network information. It handles multi-SIM scenarios and provides
 * subscription-specific telephony managers for detailed per-SIM information.
 *
 * **Android APIs used:**
 * - [TelephonyManager]: Primary API for telephony operations
 * - [SubscriptionManager]: Manages multiple SIM subscriptions (Android 5.1+, API 22+)
 * - [SubscriptionInfo]: Contains detailed information about each SIM card
 *
 * **Multi-SIM support:**
 * On devices with multiple SIM slots (dual-SIM, triple-SIM, etc.), SubscriptionManager
 * provides information about all active subscriptions. Each SIM has:
 * - Unique subscriptionId: Persists across reboots, identifies the subscription
 * - Physical slotIndex: 0, 1, 2... indicating physical slot position
 * - Carrier information: Name, country, network operator code
 *
 * **Permissions required:**
 * - READ_PHONE_STATE: Required for accessing subscription information
 * - On Android 10+, READ_PRECISE_PHONE_STATE may be required for some operations
 *
 * @property context Application context for accessing system services
 */
class TelephonyDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val telephonyManager: TelephonyManager by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val subscriptionManager: SubscriptionManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        } else {
            null
        }
    }

    /**
     * Retrieves all active subscriptions (SIM cards).
     *
     * This method uses SubscriptionManager.getActiveSubscriptionInfoList() which requires
     * READ_PHONE_STATE permission. On Android 9+ (API 28+), this method provides detailed
     * information about each SIM including carrier name, slot index, country code, etc.
     *
     * **How it works:**
     * 1. Checks if READ_PHONE_STATE permission is granted
     * 2. Calls SubscriptionManager.getActiveSubscriptionInfoList()
     * 3. Returns list of SubscriptionInfo objects, one per active SIM
     *
     * **Multi-SIM behavior:**
     * - Dual-SIM device with 2 SIMs: Returns list of 2 SubscriptionInfo objects
     * - Single-SIM device: Returns list with 1 SubscriptionInfo object
     * - No SIM: Returns empty list
     * - Permission denied: Returns empty list
     *
     * **SubscriptionInfo contains:**
     * - subscriptionId: Unique ID for the subscription
     * - simSlotIndex: Physical slot (0, 1, 2...)
     * - carrierName: Operator name (e.g., "Beeline", "Ucell")
     * - displayName: User-assigned name (e.g., "Personal", "Work")
     * - countryIso: ISO country code (e.g., "UZ")
     * - number: Phone number (if available on SIM)
     *
     * @return List of SubscriptionInfo objects, or empty list if no permissions/SIMs
     */
    @SuppressLint("MissingPermission")
    fun getActiveSubscriptions(): List<SubscriptionInfo> {
        if (!hasPermissions()) {
            Log.w(TAG, "READ_PHONE_STATE permission not granted, returning empty subscription list")
            return emptyList()
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for reading subscriptions", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading subscriptions", e)
            emptyList()
        }
    }

    /**
     * Gets telephony manager for a specific subscription.
     *
     * This method creates a subscription-specific TelephonyManager that provides information
     * for a single SIM card. This allows us to get network-specific information like roaming
     * status, network type, operator code, etc. for each SIM independently.
     *
     * **Android version support:**
     * - Android 7.0+ (API 24+): Uses TelephonyManager.createForSubscriptionId()
     * - Below Android 7.0: Returns default TelephonyManager (single SIM support)
     *
     * **Use cases:**
     * - Check roaming status for specific SIM: manager.isNetworkRoaming
     * - Get network type for specific SIM: manager.dataNetworkType
     * - Get operator code for specific SIM: manager.networkOperator
     *
     * **Example:**
     * ```kotlin
     * val subscriptions = getActiveSubscriptions()
     * subscriptions.forEach { subscription ->
     *     val manager = getTelephonyManagerForSubscription(subscription.subscriptionId)
     *     val isRoaming = manager.isNetworkRoaming
     *     val networkType = manager.dataNetworkType
     * }
     * ```
     *
     * @param subscriptionId The subscription ID from SubscriptionInfo
     * @return TelephonyManager instance for this subscription
     */
    fun getTelephonyManagerForSubscription(subscriptionId: Int): TelephonyManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            telephonyManager.createForSubscriptionId(subscriptionId)
        } else {
            telephonyManager
        }
    }

    /**
     * Checks if the device is currently roaming on the default subscription.
     *
     * Roaming occurs when the device is connected to a network other than the home network.
     * This typically happens when:
     * - Traveling abroad (international roaming)
     * - In areas without home network coverage (national roaming)
     *
     * **Note:** On dual-SIM devices, this returns roaming status for the default data SIM.
     * Use getTelephonyManagerForSubscription() for per-SIM roaming status.
     *
     * @return true if currently roaming, false otherwise
     */
    fun isRoaming(): Boolean {
        return telephonyManager.isNetworkRoaming
    }

    /**
     * Gets network operator for a specific subscription.
     *
     * Returns the MCC+MNC (Mobile Country Code + Mobile Network Code) as a string.
     * This uniquely identifies the mobile operator.
     *
     * **Format:** MCCMNC (5-6 digits)
     * - MCC: 3 digits identifying the country (e.g., 434 = Uzbekistan, 310 = USA)
     * - MNC: 2-3 digits identifying the operator within the country
     *
     * **Examples:**
     * - "43405" = Beeline Uzbekistan (434 = Uzbekistan, 05 = Beeline)
     * - "43407" = Ucell Uzbekistan (434 = Uzbekistan, 07 = Ucell)
     * - "310260" = T-Mobile USA (310 = USA, 260 = T-Mobile)
     *
     * **Use cases:**
     * - Identify carrier programmatically
     * - Detect if user changed SIM card
     * - Apply carrier-specific configurations
     *
     * @param subscriptionId The subscription ID
     * @return Operator code string (MCC+MNC), or empty string if unavailable
     */
    @SuppressLint("MissingPermission")
    fun getNetworkOperator(subscriptionId: Int): String {
        return try {
            val manager = getTelephonyManagerForSubscription(subscriptionId)
            manager.networkOperator ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network operator for subscription $subscriptionId", e)
            ""
        }
    }

    /**
     * Gets the current data network type (4G, 5G, etc.).
     *
     * This method returns the network type currently being used for mobile data.
     * The returned value is one of the TelephonyManager.NETWORK_TYPE_* constants:
     * - NETWORK_TYPE_GPRS (2G)
     * - NETWORK_TYPE_EDGE (2G)
     * - NETWORK_TYPE_UMTS (3G)
     * - NETWORK_TYPE_HSDPA (3G)
     * - NETWORK_TYPE_LTE (4G)
     * - NETWORK_TYPE_NR (5G)
     * - etc.
     *
     * **Android version differences:**
     * - Android 7.0+ (API 24+): Uses dataNetworkType (more accurate)
     * - Below Android 7.0: Uses networkType (fallback)
     *
     * **Note:** Requires permission and active mobile data connection.
     *
     * @return Network type constant from TelephonyManager, or NETWORK_TYPE_UNKNOWN if unavailable
     */
    @SuppressLint("MissingPermission")
    fun getDataNetworkType(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                telephonyManager.dataNetworkType
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.networkType
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting data network type", e)
            TelephonyManager.NETWORK_TYPE_UNKNOWN
        }
    }

    /**
     * Checks if READ_PHONE_STATE permission is granted.
     *
     * This permission is required for accessing subscription information and telephony data.
     * Without this permission, most methods in this class will return empty/default values.
     *
     * @return true if permission is granted, false otherwise
     */
    private fun hasPermissions(): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "TelephonyDataSource"
    }
}
