package uz.jahonov.defineoperator.domain.usecase

import uz.jahonov.defineoperator.domain.repository.PermissionRepository
import javax.inject.Inject

/**
 * Use case for checking permission status.
 *
 * This use case provides a simple, testable way to check if all required runtime permissions
 * are granted. It encapsulates the permission checking logic and provides a clean interface
 * for the presentation layer.
 *
 * **Required permissions:**
 * - READ_PHONE_STATE: For accessing SIM card and telephony information
 * - ACCESS_NETWORK_STATE: For reading network connection state
 * - ACCESS_WIFI_STATE: For reading Wi-Fi connection details
 *
 * **Usage in ViewModel:**
 * ```kotlin
 * class MyViewModel @Inject constructor(
 *     private val checkPermissionsUseCase: CheckPermissionsUseCase
 * ) : ViewModel() {
 *     fun init() {
 *         if (checkPermissionsUseCase()) {
 *             // Permissions granted - proceed with network monitoring
 *             loadNetworkData()
 *         } else {
 *             // Permissions not granted - show permission request UI
 *             _uiState.value = UiState.PermissionDenied
 *         }
 *     }
 * }
 * ```
 *
 * @property permissionRepository Repository providing permission checking capabilities
 */
class CheckPermissionsUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    /**
     * Checks if all required permissions are granted.
     *
     * This method checks the current permission status for all permissions required by the app.
     * It returns true only if ALL required permissions are granted.
     *
     * **Permission states:**
     * - true: All required permissions are granted → App can access protected resources
     * - false: At least one permission is missing → Must request permissions before proceeding
     *
     * **Note:**
     * This method does NOT request permissions - it only checks current status.
     * Use PermissionRepository.getRequiredPermissions() to request missing permissions.
     *
     * @return true if all required permissions are granted, false if any are missing
     */
    operator fun invoke(): Boolean {
        return permissionRepository.hasRequiredPermissions()
    }
}
