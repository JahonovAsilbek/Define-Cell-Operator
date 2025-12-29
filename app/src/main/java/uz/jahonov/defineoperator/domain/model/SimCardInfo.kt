package uz.jahonov.defineoperator.domain.model

/**
 * Represents information about a single SIM card slot.
 *
 * This data class contains comprehensive information about a SIM card installed in the device,
 * including carrier details, subscription information, and roaming status. It supports both
 * physical SIM cards and eSIMs.
 *
 * @property slotIndex The physical slot index (0 for SIM 1, 1 for SIM 2, etc.)
 * @property subscriptionId The unique subscription ID assigned by Android. This ID persists
 *                          across reboots and uniquely identifies this subscription.
 * @property carrierName The carrier/operator name (e.g., "Beeline", "Ucell", "T-Mobile").
 *                       Read from the SIM card or carrier configuration.
 * @property displayName User-assigned name for the SIM card (e.g., "Personal", "Work").
 *                       This is customizable by the user in device settings.
 * @property phoneNumber The phone number associated with this SIM. May be null if the number
 *                       is not stored on the SIM card or is not accessible.
 * @property countryIso The ISO country code (e.g., "UZ" for Uzbekistan, "US" for United States).
 *                      Represents the country of the mobile operator.
 * @property isDataRoaming Whether data roaming is currently active for this SIM.
 *                         True if the device is connected to a foreign network.
 * @property networkOperator The MCC+MNC code identifying the network operator.
 *                           Format: MCCMNC (e.g., "43405" = Beeline Uzbekistan, where
 *                           434 is the Mobile Country Code for Uzbekistan and 05 is the
 *                           Mobile Network Code for Beeline).
 * @property isEmbedded Whether this is an eSIM (embedded SIM) rather than a physical SIM card.
 *                      Available on Android 9.0 (API 28) and higher.
 */
data class SimCardInfo(
    val slotIndex: Int,
    val subscriptionId: Int,
    val carrierName: String,
    val displayName: String?,
    val phoneNumber: String?,
    val countryIso: String,
    val isDataRoaming: Boolean,
    val networkOperator: String,
    val isEmbedded: Boolean
)
