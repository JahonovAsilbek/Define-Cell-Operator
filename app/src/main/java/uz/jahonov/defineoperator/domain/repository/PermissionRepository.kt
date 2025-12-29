package uz.jahonov.defineoperator.domain.repository

/**
 * Repository for managing runtime permissions.
 *
 * This interface provides methods for checking and managing Android runtime permissions
 * required by the application. Since Android 6.0 (API 23), dangerous permissions must be
 * requested at runtime rather than just declared in the manifest.
 *
 * **Required permissions for this app:**
 * - READ_PHONE_STATE: Required to access SIM card and telephony information
 * - ACCESS_NETWORK_STATE: Required to read network connection state
 * - ACCESS_WIFI_STATE: Required to read Wi-Fi connection details
 *
 * **Permission flow:**
 * 1. Check if permissions are granted using [hasRequiredPermissions]
 * 2. If not granted, request using Activity Result API with [getRequiredPermissions]
 * 3. If user denies, check [shouldShowRationale] to determine if explanation needed
 * 4. Show rationale and request again, or direct user to settings
 */
interface PermissionRepository {
    /**
     * Checks if all required permissions are granted.
     *
     * This method checks the current permission status for all permissions required by the app.
     * It uses Context.checkSelfPermission() to verify each permission.
     *
     * **Implementation note:**
     * This method should check all permissions returned by [getRequiredPermissions] and
     * return true only if ALL permissions are granted.
     *
     * **Usage:**
     * Call this method before attempting to access protected resources (SIM info, network state).
     * If false, use [getRequiredPermissions] to request the missing permissions.
     *
     * @return true if all required permissions are granted, false if any are missing
     */
    fun hasRequiredPermissions(): Boolean

    /**
     * Gets the array of permissions that need to be requested.
     *
     * This method returns the complete list of runtime permissions required by the app.
     * The returned array can be passed directly to the Activity Result API for permission
     * requests.
     *
     * **Example usage with Jetpack Compose:**
     * ```kotlin
     * val permissionLauncher = rememberLauncherForActivityResult(
     *     ActivityResultContracts.RequestMultiplePermissions()
     * ) { permissions ->
     *     if (permissions.values.all { it }) {
     *         // All permissions granted
     *     }
     * }
     *
     * val requiredPermissions = permissionRepository.getRequiredPermissions()
     * permissionLauncher.launch(requiredPermissions)
     * ```
     *
     * @return Array of permission strings (e.g., ["android.permission.READ_PHONE_STATE", ...])
     */
    fun getRequiredPermissions(): Array<String>

    /**
     * Checks if we should show rationale for a permission.
     *
     * According to Android guidelines, you should show an explanation (rationale) if:
     * 1. The user previously denied the permission
     * 2. The user hasn't selected "Don't ask again"
     *
     * This method wraps Activity.shouldShowRequestPermissionRationale() which returns true
     * in these cases. Use this to determine whether to show an explanation dialog before
     * requesting the permission again.
     *
     * **Permission request flow:**
     * ```
     * 1. First request: shouldShowRationale = false → Request directly
     * 2. User denies: shouldShowRationale = true → Show explanation, then request
     * 3. User denies with "Don't ask again": shouldShowRationale = false → Direct to settings
     * ```
     *
     * **Implementation note:**
     * This method requires access to an Activity, so implementations typically need an
     * Activity context. If not available, return false as a safe default.
     *
     * @param permission The permission to check (e.g., "android.permission.READ_PHONE_STATE")
     * @return true if rationale should be shown, false otherwise
     */
    fun shouldShowRationale(permission: String): Boolean
}
