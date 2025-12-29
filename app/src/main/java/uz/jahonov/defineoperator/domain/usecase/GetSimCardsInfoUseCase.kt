package uz.jahonov.defineoperator.domain.usecase

import uz.jahonov.defineoperator.domain.model.SimCardInfo
import uz.jahonov.defineoperator.domain.repository.NetworkRepository
import javax.inject.Inject

/**
 * Use case for retrieving SIM card information.
 *
 * This use case encapsulates the business logic for fetching SIM card details from the device.
 * It handles multi-SIM scenarios, filters out invalid entries, and ensures consistent ordering.
 *
 * **Business rules:**
 * - Only return SIM cards with valid subscription IDs (subscriptionId != -1)
 * - Sort SIM cards by slot index for consistent ordering (SIM 1, SIM 2, etc.)
 * - Return empty list if permissions are denied or no SIMs are installed
 *
 * **Use cases:**
 * - Display all available SIM cards to the user
 * - Allow user to select which SIM to use for a specific operation
 * - Log SIM card information for debugging or analytics
 *
 * @property networkRepository Repository providing access to network and telephony data
 */
class GetSimCardsInfoUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    /**
     * Executes the use case to get all available SIM cards.
     *
     * This method retrieves SIM card information from the repository, filters out invalid
     * subscriptions, and sorts by slot index to ensure consistent ordering.
     *
     * **Invalid subscriptions:**
     * A subscriptionId of -1 indicates an invalid or inactive subscription. This can occur when:
     * - A SIM slot is empty
     * - A SIM card is present but not activated
     * - The SIM card is being initialized
     *
     * **Sorting:**
     * SIM cards are sorted by slotIndex (0, 1, 2...) so that:
     * - SIM 1 (slot 0) always appears first
     * - SIM 2 (slot 1) appears second
     * - And so on for multi-SIM devices
     *
     * @return List of valid SIM card information, sorted by slot index.
     *         Empty list if no SIMs, permissions denied, or all subscriptions invalid.
     */
    suspend operator fun invoke(): List<SimCardInfo> {
        return networkRepository.getSimCardsInfo()
            .filter { it.subscriptionId != -1 } // Filter out invalid subscriptions
            .sortedBy { it.slotIndex } // Sort by slot index for consistent ordering
    }
}
