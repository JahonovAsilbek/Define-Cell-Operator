package uz.jahonov.defineoperator

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner for Hilt instrumentation tests.
 *
 * This runner replaces the default Application with HiltTestApplication during
 * instrumentation tests. This allows Hilt to:
 * - Provide test-specific dependencies
 * - Replace production modules with test modules
 * - Enable dependency injection in test classes
 *
 * **Usage:**
 * Configure this runner in app/build.gradle.kts:
 * ```kotlin
 * defaultConfig {
 *     testInstrumentationRunner = "uz.jahonov.defineoperator.HiltTestRunner"
 * }
 * ```
 *
 * **How it works:**
 * 1. During test execution, Android creates an Application instance
 * 2. This runner intercepts the creation and substitutes HiltTestApplication
 * 3. HiltTestApplication sets up the test dependency graph
 * 4. Test classes annotated with @HiltAndroidTest can inject dependencies
 *
 * **Example test:**
 * ```kotlin
 * @HiltAndroidTest
 * class MyInstrumentedTest {
 *     @get:Rule
 *     val hiltRule = HiltAndroidRule(this)
 *
 *     @Inject
 *     lateinit var repository: NetworkRepository
 *
 *     @Before
 *     fun init() {
 *         hiltRule.inject()
 *     }
 *
 *     @Test
 *     fun testRepository() {
 *         // Test with injected repository
 *     }
 * }
 * ```
 */
class HiltTestRunner : AndroidJUnitRunner() {
    /**
     * Creates the Application instance for testing.
     *
     * Overrides the default behavior to return HiltTestApplication instead of the
     * production Application class. This enables Hilt dependency injection in tests.
     *
     * @param cl ClassLoader to use for loading the Application class
     * @param className Original Application class name (ignored)
     * @param context Context to pass to the Application
     * @return HiltTestApplication instance for testing
     */
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
