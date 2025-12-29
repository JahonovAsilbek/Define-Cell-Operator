package uz.jahonov.defineoperator.data.mapper

import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.jahonov.defineoperator.domain.model.NetworkType

/**
 * Unit tests for ConnectionMapper.
 *
 * These tests verify that network type constants from TelephonyManager are correctly
 * mapped to domain NetworkType enum values.
 *
 * **Testing approach:**
 * - Test all supported network types
 * - Test unknown/unsupported network types
 * - These are pure functions, no mocking needed
 */
class ConnectionMapperTest {

    @Test
    fun `toDomainNetworkType maps 2G technologies correctly`() {
        assertEquals(
            NetworkType.GPRS,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_GPRS)
        )
        assertEquals(
            NetworkType.EDGE,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_EDGE)
        )
    }

    @Test
    fun `toDomainNetworkType maps 3G technologies correctly`() {
        assertEquals(
            NetworkType.UMTS,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_UMTS)
        )
        assertEquals(
            NetworkType.HSDPA,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA)
        )
        assertEquals(
            NetworkType.HSUPA,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA)
        )
        assertEquals(
            NetworkType.HSPA,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_HSPA)
        )
    }

    @Test
    fun `toDomainNetworkType maps 4G LTE correctly`() {
        assertEquals(
            NetworkType.LTE,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_LTE)
        )
    }

    @Test
    fun `toDomainNetworkType maps 5G NR correctly`() {
        assertEquals(
            NetworkType.NR,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_NR)
        )
    }

    @Test
    fun `toDomainNetworkType returns UNKNOWN for unrecognized types`() {
        assertEquals(
            NetworkType.UNKNOWN,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_UNKNOWN)
        )
        assertEquals(
            NetworkType.UNKNOWN,
            ConnectionMapper.toDomainNetworkType(999) // Invalid type
        )
    }

    @Test
    fun `toDomainNetworkType returns UNKNOWN for CDMA types not explicitly mapped`() {
        // These CDMA types are not explicitly mapped in our mapper
        // They should return UNKNOWN
        assertEquals(
            NetworkType.UNKNOWN,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_CDMA)
        )
        assertEquals(
            NetworkType.UNKNOWN,
            ConnectionMapper.toDomainNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT)
        )
    }
}
