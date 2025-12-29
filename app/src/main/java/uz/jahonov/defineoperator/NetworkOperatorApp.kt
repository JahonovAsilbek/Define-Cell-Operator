package uz.jahonov.defineoperator

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Hilt initialization.
 *
 * This class serves as the entry point for Hilt dependency injection. The @HiltAndroidApp
 * annotation triggers Hilt's code generation and sets up the application-level dependency
 * injection container.
 *
 * **Hilt initialization:**
 * - @HiltAndroidApp generates a Hilt component for the Application
 * - This component serves as the root of the dependency graph
 * - All other components (Activity, ViewModel, etc.) are children of this component
 *
 * **Component hierarchy:**
 * ```
 * ApplicationComponent (singleton)
 *   └── ActivityComponent (activity scope)
 *         └── ViewModelComponent (viewmodel scope)
 * ```
 *
 * **Lifecycle:**
 * - Created when app process starts
 * - Lives for entire app lifetime
 * - Provides singleton dependencies
 *
 * **Usage:**
 * Reference this class in AndroidManifest.xml:
 * ```xml
 * <application
 *     android:name=".NetworkOperatorApp"
 *     ...>
 * ```
 */
@HiltAndroidApp
class NetworkOperatorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // App initialization code can go here if needed
    }
}
