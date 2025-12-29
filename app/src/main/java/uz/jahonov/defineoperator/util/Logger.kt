package uz.jahonov.defineoperator.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized logging utility for the application.
 *
 * This class provides a unified interface for logging throughout the app. It wraps Android's
 * Log class and can be easily extended to:
 * - Add additional logging destinations (file, analytics, crash reporting)
 * - Filter logs by build type (debug vs release)
 * - Format log messages consistently
 * - Add contextual information to all logs
 *
 * **Logging levels:**
 * - Debug (d): Development-time information, disabled in release builds
 * - Warning (w): Potentially harmful situations
 * - Error (e): Error events that might still allow the app to continue
 *
 * **Benefits of centralized logging:**
 * - Easy to modify logging behavior globally
 * - Can be mocked in tests
 * - Can add analytics or crash reporting integration
 * - Consistent log format across the app
 *
 * @constructor Creates a Logger instance. Injected as singleton by Hilt.
 */
@Singleton
class Logger @Inject constructor() {
    /**
     * Logs a debug message.
     *
     * Debug messages are used for detailed diagnostic information useful during development.
     * These logs should provide insight into the application's execution flow and state.
     *
     * **When to use:**
     * - Tracking method entry/exit points
     * - Logging variable values during debugging
     * - Recording flow through conditional logic
     * - Documenting state changes
     *
     * **Example:**
     * ```kotlin
     * logger.d("NetworkRepository", "Found ${subscriptions.size} active subscriptions")
     * logger.d("NetworkRepository", "Active connection: $connectionState")
     * ```
     *
     * @param tag Used to identify the source of a log message. Typically the class name.
     * @param message The message to log
     */
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    /**
     * Logs a warning message.
     *
     * Warning messages indicate potentially harmful situations that don't prevent the app
     * from functioning but might lead to problems or unexpected behavior.
     *
     * **When to use:**
     * - Deprecated API usage
     * - Recoverable errors (e.g., network timeout with retry)
     * - Unexpected but handled situations
     * - Performance issues
     *
     * **Example:**
     * ```kotlin
     * logger.w("NetworkRepository", "No SIM cards found, returning empty list")
     * logger.w("ConnectivityDataSource", "Active network has no internet capability")
     * ```
     *
     * @param tag Used to identify the source of a log message. Typically the class name.
     * @param message The message to log
     */
    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    /**
     * Logs an error message.
     *
     * Error messages indicate error events that might still allow the application to
     * continue running. These should be used for unexpected exceptions or error conditions.
     *
     * **When to use:**
     * - Caught exceptions
     * - Failed operations (network errors, permission denials)
     * - Invalid state conditions
     * - Resource access failures
     *
     * **With throwable:**
     * When logging an exception, pass the throwable to include stack trace in logs.
     *
     * **Example:**
     * ```kotlin
     * logger.e("NetworkRepository", "Error fetching SIM cards", exception)
     * logger.e("TelephonyDataSource", "Permission denied for reading subscriptions")
     * ```
     *
     * @param tag Used to identify the source of a log message. Typically the class name.
     * @param message The message to log
     * @param throwable Optional throwable to log with stack trace
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
