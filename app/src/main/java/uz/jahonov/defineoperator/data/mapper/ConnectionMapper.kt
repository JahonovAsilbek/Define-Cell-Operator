package uz.jahonov.defineoperator.data.mapper

import android.telephony.TelephonyManager
import uz.jahonov.defineoperator.domain.model.NetworkType

/**
 * Mapper for converting network type constants to domain NetworkType enum.
 *
 * This object provides mapping functions to convert Android framework network type constants
 * (from TelephonyManager) into domain layer enums. This provides:
 * - Type-safe network type representation in domain layer
 * - Human-readable network type names
 * - Centralized mapping logic
 * - Easy extension for new network types
 *
 * **Android network type constants:**
 * TelephonyManager provides NETWORK_TYPE_* constants to identify cellular technologies:
 * - NETWORK_TYPE_GPRS, NETWORK_TYPE_EDGE (2G)
 * - NETWORK_TYPE_UMTS, NETWORK_TYPE_HSDPA, NETWORK_TYPE_HSPA (3G)
 * - NETWORK_TYPE_LTE (4G)
 * - NETWORK_TYPE_NR (5G)
 * - NETWORK_TYPE_UNKNOWN (unrecognized or unavailable)
 */
object ConnectionMapper {
    /**
     * Maps TelephonyManager network type constant to domain NetworkType enum.
     *
     * This method converts the integer network type constants from TelephonyManager
     * (NETWORK_TYPE_*) into our type-safe NetworkType enum. This provides better
     * type safety and human-readable network type names.
     *
     * **Supported network types:**
     *
     * **2G Technologies:**
     * - NETWORK_TYPE_GPRS → NetworkType.GPRS
     * - NETWORK_TYPE_EDGE → NetworkType.EDGE
     *
     * **3G Technologies:**
     * - NETWORK_TYPE_UMTS → NetworkType.UMTS
     * - NETWORK_TYPE_HSDPA → NetworkType.HSDPA (Enhanced 3G, faster downloads)
     * - NETWORK_TYPE_HSUPA → NetworkType.HSUPA (Enhanced 3G, faster uploads)
     * - NETWORK_TYPE_HSPA → NetworkType.HSPA (Combined HSDPA/HSUPA)
     *
     * **4G Technologies:**
     * - NETWORK_TYPE_LTE → NetworkType.LTE
     *
     * **5G Technologies:**
     * - NETWORK_TYPE_NR → NetworkType.NR (New Radio - 5G standard)
     *
     * **Unknown/Unsupported:**
     * - Any other value → NetworkType.UNKNOWN
     *
     * **Additional 3G types not mapped (can be added if needed):**
     * - NETWORK_TYPE_CDMA (CDMA 2G/3G)
     * - NETWORK_TYPE_EVDO_* (EVDO variants - 3G)
     * - NETWORK_TYPE_1xRTT (CDMA 2G)
     *
     * **Additional 4G types not mapped:**
     * - NETWORK_TYPE_LTE_CA (LTE with Carrier Aggregation)
     * - NETWORK_TYPE_IWLAN (Wi-Fi calling, not cellular)
     *
     * **Example:**
     * ```kotlin
     * val telephonyManager: TelephonyManager = ...
     * val rawNetworkType = telephonyManager.dataNetworkType
     * val networkType = ConnectionMapper.toDomainNetworkType(rawNetworkType)
     * // networkType is now a type-safe enum: NetworkType.LTE, NetworkType.NR, etc.
     * ```
     *
     * @param networkType TelephonyManager.NETWORK_TYPE_* constant (e.g., NETWORK_TYPE_LTE = 13)
     * @return Corresponding domain NetworkType enum value
     */
    fun toDomainNetworkType(networkType: Int): NetworkType {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> NetworkType.GPRS
            TelephonyManager.NETWORK_TYPE_EDGE -> NetworkType.EDGE
            TelephonyManager.NETWORK_TYPE_UMTS -> NetworkType.UMTS
            TelephonyManager.NETWORK_TYPE_HSDPA -> NetworkType.HSDPA
            TelephonyManager.NETWORK_TYPE_HSUPA -> NetworkType.HSUPA
            TelephonyManager.NETWORK_TYPE_HSPA -> NetworkType.HSPA
            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.LTE
            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.NR
            else -> NetworkType.UNKNOWN
        }
    }
}
