package uz.jahonov.defineoperator.presentation.state

import uz.jahonov.defineoperator.domain.model.NetworkStatus

/**
 * UI state for network information screen.
 *
 * This sealed class represents all possible states of the network information UI. Using a
 * sealed class provides type-safe state management and ensures the UI handles all possible
 * states explicitly.
 *
 * **State flow:**
 * ```
 * Loading → Success (if permissions granted and data loaded)
 *   ↓
 * PermissionDenied (if permissions not granted)
 *   ↓
 * Error (if data loading fails)
 * ```
 *
 * **Benefits of sealed class for UI state:**
 * - Exhaustive when statements ensure all states are handled
 * - Type-safe state data (each state has specific properties)
 * - Clear representation of UI state machine
 * - Easy to add new states without breaking existing code
 */
sealed class NetworkUiState {
    /**
     * Loading state - fetching network information.
     *
     * This state is shown when:
     * - App is first launched
     * - User manually refreshes the data
     * - Network state is being queried
     *
     * **UI should display:**
     * - Loading spinner or skeleton screen
     * - "Loading network information..." message
     * - Disabled refresh button
     */
    data object Loading : NetworkUiState()

    /**
     * Success state - network information available.
     *
     * This state indicates that network data has been successfully retrieved and is ready
     * to display. The UI should show all SIM cards, active connection, and allow user
     * interaction.
     *
     * **UI should display:**
     * - List of SIM cards with carrier names, slot numbers, roaming status
     * - Active connection card (Wi-Fi or Mobile with details)
     * - Refresh button (enabled)
     * - Last updated timestamp
     *
     * @property networkStatus The current network status with SIM cards and active connection
     */
    data class Success(val networkStatus: NetworkStatus) : NetworkUiState()

    /**
     * Error state - failed to fetch network information.
     *
     * This state is shown when an error occurs during data retrieval. Errors can include:
     * - Exception while accessing Android APIs
     * - Unexpected errors in data processing
     * - System service unavailable
     *
     * **Note:** Permission errors are handled separately in [PermissionDenied] state.
     *
     * **UI should display:**
     * - Error icon
     * - Error message from [message] property
     * - "Retry" button to attempt loading again
     * - "Settings" button if the error might be resolved by user action
     *
     * @property message Error message to display to the user. Should be user-friendly
     *                   and actionable when possible.
     */
    data class Error(val message: String) : NetworkUiState()

    /**
     * Permission denied state - user needs to grant permissions.
     *
     * This state is shown when required runtime permissions are not granted. The app cannot
     * access network or SIM information without these permissions.
     *
     * **Required permissions:**
     * - READ_PHONE_STATE: For SIM card information
     * - ACCESS_NETWORK_STATE: For network state (usually granted automatically)
     * - ACCESS_WIFI_STATE: For Wi-Fi information (usually granted automatically)
     *
     * **UI should display:**
     * - Permission rationale explaining why permissions are needed
     * - List of required permissions
     * - "Grant Permissions" button to launch permission request
     * - Optional "Why is this needed?" expandable section with detailed explanation
     *
     * **Transition to Success:**
     * After user grants permissions, the ViewModel should automatically transition to
     * Loading state and then Success state once data is loaded.
     *
     * @property requiredPermissions List of permission strings that need to be granted.
     *                                Can be passed directly to permission request APIs.
     */
    data class PermissionDenied(val requiredPermissions: List<String>) : NetworkUiState()
}
