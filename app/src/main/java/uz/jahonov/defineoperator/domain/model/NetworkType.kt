package uz.jahonov.defineoperator.domain.model

/**
 * Represents the type of mobile network connection.
 *
 * This enum categorizes different cellular network technologies based on their generation
 * and specific technology standards. It provides a human-readable display name for each type.
 *
 * @property displayName Human-readable name for the network type
 */
enum class NetworkType(val displayName: String) {
    /**
     * GPRS (General Packet Radio Service) - 2G network technology.
     * Typical speeds: 56-114 kbps
     */
    GPRS("2G (GPRS)"),

    /**
     * EDGE (Enhanced Data rates for GSM Evolution) - Enhanced 2G network.
     * Typical speeds: 120-384 kbps
     */
    EDGE("2G (EDGE)"),

    /**
     * UMTS (Universal Mobile Telecommunications System) - 3G network technology.
     * Typical speeds: 384 kbps - 2 Mbps
     */
    UMTS("3G (UMTS)"),

    /**
     * HSDPA (High-Speed Downlink Packet Access) - Enhanced 3G with faster downloads.
     * Typical speeds: 1.8-14.4 Mbps
     */
    HSDPA("3G (HSDPA)"),

    /**
     * HSUPA (High-Speed Uplink Packet Access) - Enhanced 3G with faster uploads.
     * Typical speeds: 1.4-5.76 Mbps uplink
     */
    HSUPA("3G (HSUPA)"),

    /**
     * HSPA (High-Speed Packet Access) - Combined HSDPA and HSUPA.
     * Typical speeds: 14-42 Mbps
     */
    HSPA("3G (HSPA)"),

    /**
     * LTE (Long-Term Evolution) - 4G network technology.
     * Typical speeds: 10-100 Mbps, up to 1 Gbps
     */
    LTE("4G (LTE)"),

    /**
     * NR (New Radio) - 5G network technology.
     * Typical speeds: 100-10,000+ Mbps
     */
    NR("5G (NR)"),

    /**
     * Unknown or unrecognized network type.
     */
    UNKNOWN("Unknown")
}
