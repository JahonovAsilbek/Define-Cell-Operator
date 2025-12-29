package uz.jahonov.defineoperator.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uz.jahonov.defineoperator.domain.model.SimCardInfo
import uz.jahonov.defineoperator.domain.repository.NetworkRepository

/**
 * Unit tests for GetSimCardsInfoUseCase.
 *
 * These tests verify the business logic of the use case:
 * - Filtering out invalid subscriptions (subscriptionId == -1)
 * - Sorting SIM cards by slot index
 * - Delegating to repository correctly
 *
 * **Testing approach:**
 * - Use MockK to mock the NetworkRepository dependency
 * - Use kotlinx-coroutines-test for testing suspend functions
 * - Verify behavior, not implementation details
 */
class GetSimCardsInfoUseCaseTest {

    private lateinit var networkRepository: NetworkRepository
    private lateinit var useCase: GetSimCardsInfoUseCase

    @Before
    fun setup() {
        networkRepository = mockk()
        useCase = GetSimCardsInfoUseCase(networkRepository)
    }

    @Test
    fun `invoke returns sorted valid SIM cards`() = runTest {
        // Given: Repository returns unsorted list with one invalid subscription
        val simCards = listOf(
            createSimCard(slotIndex = 1, subscriptionId = 2),
            createSimCard(slotIndex = 0, subscriptionId = 1),
            createSimCard(slotIndex = 2, subscriptionId = -1) // Invalid - should be filtered
        )
        coEvery { networkRepository.getSimCardsInfo() } returns simCards

        // When: Use case is invoked
        val result = useCase()

        // Then: Returns valid SIM cards sorted by slot index
        assertEquals(2, result.size)
        assertEquals(0, result[0].slotIndex) // SIM in slot 0 comes first
        assertEquals(1, result[1].slotIndex) // SIM in slot 1 comes second
        coVerify(exactly = 1) { networkRepository.getSimCardsInfo() }
    }

    @Test
    fun `invoke returns empty list when no valid SIM cards`() = runTest {
        // Given: Repository returns only invalid subscriptions
        val simCards = listOf(
            createSimCard(slotIndex = 0, subscriptionId = -1),
            createSimCard(slotIndex = 1, subscriptionId = -1)
        )
        coEvery { networkRepository.getSimCardsInfo() } returns simCards

        // When: Use case is invoked
        val result = useCase()

        // Then: Returns empty list
        assertEquals(0, result.size)
        coVerify(exactly = 1) { networkRepository.getSimCardsInfo() }
    }

    @Test
    fun `invoke returns empty list when repository returns empty list`() = runTest {
        // Given: Repository returns empty list (no SIMs or no permissions)
        coEvery { networkRepository.getSimCardsInfo() } returns emptyList()

        // When: Use case is invoked
        val result = useCase()

        // Then: Returns empty list
        assertEquals(0, result.size)
        coVerify(exactly = 1) { networkRepository.getSimCardsInfo() }
    }

    @Test
    fun `invoke maintains sorting order for multiple SIMs`() = runTest {
        // Given: Repository returns SIM cards in random order
        val simCards = listOf(
            createSimCard(slotIndex = 3, subscriptionId = 4),
            createSimCard(slotIndex = 0, subscriptionId = 1),
            createSimCard(slotIndex = 1, subscriptionId = 2),
            createSimCard(slotIndex = 2, subscriptionId = 3)
        )
        coEvery { networkRepository.getSimCardsInfo() } returns simCards

        // When: Use case is invoked
        val result = useCase()

        // Then: Returns SIM cards sorted by slot index (0, 1, 2, 3)
        assertEquals(4, result.size)
        assertEquals(0, result[0].slotIndex)
        assertEquals(1, result[1].slotIndex)
        assertEquals(2, result[2].slotIndex)
        assertEquals(3, result[3].slotIndex)
    }

    /**
     * Helper function to create test SIM card data.
     */
    private fun createSimCard(slotIndex: Int, subscriptionId: Int) = SimCardInfo(
        slotIndex = slotIndex,
        subscriptionId = subscriptionId,
        carrierName = "Test Carrier",
        displayName = "Test SIM",
        phoneNumber = "+998901234567",
        countryIso = "UZ",
        isDataRoaming = false,
        networkOperator = "43405",
        isEmbedded = false
    )
}
