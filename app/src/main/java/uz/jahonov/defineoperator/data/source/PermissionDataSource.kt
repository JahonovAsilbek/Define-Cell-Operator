package uz.jahonov.defineoperator.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Data source for checking and managing permissions.
 *
 * This class provides methods for checking the status of runtime permissions required by
 * the application. It uses Android's permission system APIs to determine if permissions
 * are granted and which permissions need to be requested.
 *
 * **Required permissions for this app:**
 * - READ_PHONE_STATE: Required to access SIM card and telephony information
 * - ACCESS_NETWORK_STATE: Required to read network connection state
 * - ACCESS_WIFI_STATE: Required to read Wi-Fi connection details
 *
 * **Permission types:**
 * - READ_PHONE_STATE: Dangerous permission (requires runtime request on Android 6.0+)
 * - ACCESS_NETWORK_STATE: Normal permission (granted automatically)
 * - ACCESS_WIFI_STATE: Normal permission (granted automatically)
 *
 * **Note:** While ACCESS_NETWORK_STATE and ACCESS_WIFI_STATE are normal permissions and
 * don't require runtime requests, they're included in the required permissions list for
 * completeness and to ensure they're declared in the manifest.
 *
 * @property context Application context for checking permissions
 */
class PermissionDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Array of all permissions required by the application.
     *
     * This array contains both dangerous and normal permissions. While normal permissions
     * are granted automatically, they must still be declared in the manifest.
     *
     * **Dangerous permissions (require runtime request):**
     * - READ_PHONE_STATE: Access to telephony information
     *
     * **Normal permissions (granted automatically):**
     * - ACCESS_NETWORK_STATE: Access to network state
     * - ACCESS_WIFI_STATE: Access to Wi-Fi state
     */
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE
    )

    /**
     * Checks if all required permissions are granted.
     *
     * This method iterates through all required permissions and checks if each one is granted.
     * It returns true only if ALL permissions are granted.
     *
     * **How it works:**
     * 1. Iterates through [requiredPermissions] array
     * 2. Calls Context.checkSelfPermission() for each permission
     * 3. Compares result with PackageManager.PERMISSION_GRANTED
     * 4. Returns true only if all permissions are granted
     *
     * **Permission states:**
     * - PERMISSION_GRANTED: Permission is currently granted
     * - PERMISSION_DENIED: Permission is not granted (never asked, or user denied)
     *
     * **Use cases:**
     * - Check before accessing protected resources (SIM info, network state)
     * - Determine if permission request UI should be shown
     * - Validate permissions after user responds to permission request
     *
     * @return true if all required permissions are granted, false if any are missing
     */
    fun hasAllPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Gets the array of required permissions.
     *
     * This method returns the array of all permissions required by the app. The returned
     * array can be used directly with permission request APIs.
     *
     * **Usage with Activity Result API:**
     * ```kotlin
     * val permissionLauncher = rememberLauncherForActivityResult(
     *     ActivityResultContracts.RequestMultiplePermissions()
     * ) { permissions ->
     *     if (permissions.values.all { it }) {
     *         // All permissions granted
     *     }
     * }
     *
     * val requiredPermissions = permissionDataSource.getRequiredPermissions()
     * permissionLauncher.launch(requiredPermissions)
     * ```
     *
     * @return Array of permission strings (e.g., "android.permission.READ_PHONE_STATE")
     */
    fun getRequiredPermissions(): Array<String> = requiredPermissions
}
