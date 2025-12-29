package uz.jahonov.defineoperator.data.repository

import uz.jahonov.defineoperator.data.source.PermissionDataSource
import uz.jahonov.defineoperator.domain.repository.PermissionRepository
import javax.inject.Inject

/**
 * Implementation of PermissionRepository.
 *
 * This repository provides permission checking functionality by delegating to
 * PermissionDataSource. It implements the domain layer's permission repository interface
 * and translates between data and domain layers.
 *
 * **Design note:**
 * While this class currently acts as a simple pass-through to the data source, having
 * a repository layer provides:
 * - Consistent architecture across all data access
 * - Easy extension for future permission-related business logic
 * - Testability through interface abstraction
 * - Potential for combining multiple permission sources
 *
 * @property permissionDataSource Data source for permission checking
 */
class PermissionRepositoryImpl @Inject constructor(
    private val permissionDataSource: PermissionDataSource
) : PermissionRepository {

    /**
     * Checks if all required permissions are granted.
     *
     * Delegates to [PermissionDataSource.hasAllPermissions] to check permission status.
     *
     * @return true if all required permissions are granted, false if any are missing
     */
    override fun hasRequiredPermissions(): Boolean {
        return permissionDataSource.hasAllPermissions()
    }

    /**
     * Gets the array of permissions that need to be requested.
     *
     * Delegates to [PermissionDataSource.getRequiredPermissions] to get the permission list.
     *
     * @return Array of permission strings
     */
    override fun getRequiredPermissions(): Array<String> {
        return permissionDataSource.getRequiredPermissions()
    }

    /**
     * Checks if we should show rationale for a permission.
     *
     * **Note:** This implementation always returns false because determining whether to show
     * rationale requires an Activity context (Activity.shouldShowRequestPermissionRationale).
     * In a clean architecture, the repository layer shouldn't have access to Activity.
     *
     * **Recommended approach:**
     * Handle permission rationale in the UI layer (Activity/Fragment/Composable) where you
     * have access to the Activity context:
     * ```kotlin
     * val activity = LocalContext.current as? Activity
     * val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(permission) ?: false
     * ```
     *
     * @param permission The permission to check
     * @return false (rationale checking should be done in UI layer)
     */
    override fun shouldShowRationale(permission: String): Boolean {
        // Rationale checking requires Activity context, which repository shouldn't access.
        // This should be handled in the UI layer.
        return false
    }
}
