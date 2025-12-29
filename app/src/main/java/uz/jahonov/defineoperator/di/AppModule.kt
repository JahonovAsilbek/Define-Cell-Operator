package uz.jahonov.defineoperator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import uz.jahonov.defineoperator.util.Logger
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module providing application-wide dependencies.
 *
 * This module provides dependencies that have application scope (singleton) and are used
 * throughout the app. It's installed in the SingletonComponent, meaning the provided
 * dependencies live for the entire app lifetime.
 *
 * **Provided dependencies:**
 * - Logger: Centralized logging utility
 * - IoDispatcher: Coroutine dispatcher for IO operations
 *
 * **Hilt component:**
 * SingletonComponent - App-level dependencies that live for entire app lifetime
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides singleton Logger instance.
     *
     * The Logger is used throughout the app for consistent logging. Being a singleton
     * ensures all classes use the same Logger instance, which could be useful for:
     * - Maintaining consistent log configuration
     * - Aggregating logs for analytics
     * - Switching log destinations globally
     *
     * @return Logger instance
     */
    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return Logger()
    }

    /**
     * Provides IO dispatcher for background operations.
     *
     * This dispatcher is used for IO-bound operations like:
     * - Reading from Android system services
     * - Network operations
     * - Database operations
     * - File operations
     *
     * Using a custom qualifier (@IoDispatcher) allows us to:
     * - Inject different dispatchers for different purposes (IO, Main, Default)
     * - Easily swap dispatchers in tests
     * - Make threading behavior explicit
     *
     * @return Coroutine dispatcher for IO operations
     */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

/**
 * Qualifier annotation for IO dispatcher.
 *
 * This qualifier distinguishes the IO dispatcher from other dispatcher types
 * (Main, Default, etc.) during dependency injection.
 *
 * **Usage:**
 * ```kotlin
 * class MyRepository @Inject constructor(
 *     @IoDispatcher private val ioDispatcher: CoroutineDispatcher
 * ) {
 *     suspend fun loadData() = withContext(ioDispatcher) {
 *         // IO operations
 *     }
 * }
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
