package uz.jahonov.defineoperator.data.mapper

import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.TelephonyManager
import uz.jahonov.defineoperator.domain.model.SimCardInfo

/**
 * Mapper for converting Android SubscriptionInfo to domain SimCardInfo model.
 *
 * This object provides mapping functions to convert Android framework classes (SubscriptionInfo,
 * TelephonyManager) into domain layer models (SimCardInfo). This separation allows:
 * - Domain layer to remain independent of Android framework
 * - Easy testing of domain logic without Android dependencies
 * - Centralized conversion logic
 * - Type-safe domain models
 *
 * **Mapping strategy:**
 * - Extract data from Android SubscriptionInfo
 * - Query TelephonyManager for additional per-SIM information
 * - Convert to domain model with appropriate null handling
 * - Handle Android version differences gracefully
 */
object SimCardMapper {
    /**
     * Converts Android SubscriptionInfo to domain SimCardInfo model.
     *
     * This method extracts all relevant information from Android's SubscriptionInfo and
     * TelephonyManager objects and maps them to the domain model. It handles null values
     * and provides sensible defaults when information is unavailable.
     *
     * **Data mapping:**
     * - slotIndex: From SubscriptionInfo.simSlotIndex
     * - subscriptionId: From SubscriptionInfo.subscriptionId
     * - carrierName: From SubscriptionInfo.carrierName (or "Unknown" if null)
     * - displayName: From SubscriptionInfo.displayName (user-assigned name)
     * - phoneNumber: From SubscriptionInfo.number (may be null if not on SIM)
     * - countryIso: From SubscriptionInfo.countryIso (e.g., "UZ", "US")
     * - isDataRoaming: From TelephonyManager.isNetworkRoaming
     * - networkOperator: From TelephonyManager.networkOperator (MCC+MNC code)
     * - isEmbedded: From SubscriptionInfo.isEmbedded (eSIM indicator, API 28+)
     *
     * **Roaming detection:**
     * Uses the subscription-specific TelephonyManager to check if that particular SIM is
     * currently roaming. This is important on dual-SIM devices where one SIM might be
     * roaming while the other is not.
     *
     * **eSIM support:**
     * The isEmbedded property is only available on Android 9.0+ (API 28). On older versions,
     * it defaults to false, assuming physical SIM cards.
     *
     * **Example:**
     * ```kotlin
     * val subscriptionInfo: SubscriptionInfo = ...
     * val telephonyManager: TelephonyManager = ...
     * val simCardInfo = SimCardMapper.toDomain(subscriptionInfo, telephonyManager)
     * // simCardInfo is now a domain model ready for business logic
     * ```
     *
     * @param subscriptionInfo Android's SubscriptionInfo object containing SIM details
     * @param telephonyManager Subscription-specific TelephonyManager for this SIM.
     *                         Should be created using TelephonyManager.createForSubscriptionId()
     * @return Domain model SimCardInfo with all available SIM information
     */
    fun toDomain(
        subscriptionInfo: SubscriptionInfo,
        telephonyManager: TelephonyManager
    ): SimCardInfo {
        return SimCardInfo(
            slotIndex = subscriptionInfo.simSlotIndex,
            subscriptionId = subscriptionInfo.subscriptionId,
            carrierName = subscriptionInfo.carrierName?.toString() ?: "Unknown",
            displayName = subscriptionInfo.displayName?.toString(),
            phoneNumber = subscriptionInfo.number,
            countryIso = subscriptionInfo.countryIso ?: "",
            isDataRoaming = telephonyManager.isNetworkRoaming,
            networkOperator = telephonyManager.networkOperator ?: "",
            isEmbedded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                subscriptionInfo.isEmbedded
            } else {
                false
            }
        )
    }
}
