package uz.jahonov.defineoperator.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import uz.jahonov.defineoperator.data.repository.NetworkRepositoryImpl
import uz.jahonov.defineoperator.data.repository.PermissionRepositoryImpl
import uz.jahonov.defineoperator.data.source.ConnectivityDataSource
import uz.jahonov.defineoperator.data.source.PermissionDataSource
import uz.jahonov.defineoperator.data.source.TelephonyDataSource
import uz.jahonov.defineoperator.domain.repository.NetworkRepository
import uz.jahonov.defineoperator.domain.repository.PermissionRepository
import uz.jahonov.defineoperator.util.Logger
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies.
 *
 * This module provides all dependencies related to network and telephony functionality.
 * It's installed in the SingletonComponent, so all provided dependencies are singletons
 * that live for the entire app lifetime.
 *
 * **Dependency graph:**
 * ```
 * NetworkRepository ← NetworkRepositoryImpl
 *   ├── TelephonyDataSource
 *   ├── ConnectivityDataSource
 *   ├── Logger
 *   └── IoDispatcher
 *
 * PermissionRepository ← PermissionRepositoryImpl
 *   └── PermissionDataSource
 * ```
 *
 * **Why singleton?**
 * - Data sources wrap Android system services (singleton by nature)
 * - Repositories maintain no mutable state, safe to share
 * - Reduces object allocation overhead
 * - Enables caching if needed in the future
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides singleton TelephonyDataSource.
     *
     * TelephonyDataSource wraps TelephonyManager and SubscriptionManager system services.
     * Being a singleton is appropriate because:
     * - System services are singletons
     * - No mutable state in the data source
     * - Efficient reuse across the app
     *
     * @param context Application context for accessing system services
     * @return TelephonyDataSource instance
     */
    @Provides
    @Singleton
    fun provideTelephonyDataSource(
        @ApplicationContext context: Context
    ): TelephonyDataSource {
        return TelephonyDataSource(context)
    }

    /**
     * Provides singleton ConnectivityDataSource.
     *
     * ConnectivityDataSource wraps ConnectivityManager system service. Being a singleton
     * ensures that network callbacks are managed centrally and not duplicated.
     *
     * @param context Application context for accessing system services
     * @return ConnectivityDataSource instance
     */
    @Provides
    @Singleton
    fun provideConnectivityDataSource(
        @ApplicationContext context: Context
    ): ConnectivityDataSource {
        return ConnectivityDataSource(context)
    }

    /**
     * Provides singleton PermissionDataSource.
     *
     * PermissionDataSource provides permission checking functionality. Being a singleton
     * is appropriate as it has no mutable state.
     *
     * @param context Application context for checking permissions
     * @return PermissionDataSource instance
     */
    @Provides
    @Singleton
    fun providePermissionDataSource(
        @ApplicationContext context: Context
    ): PermissionDataSource {
        return PermissionDataSource(context)
    }

    /**
     * Provides singleton NetworkRepository implementation.
     *
     * This binds the NetworkRepositoryImpl implementation to the NetworkRepository interface.
     * The repository coordinates data sources and implements business logic.
     *
     * @param telephonyDataSource Data source for telephony information
     * @param connectivityDataSource Data source for connectivity information
     * @param logger Logger for debugging
     * @param ioDispatcher Dispatcher for background operations
     * @return NetworkRepository implementation
     */
    @Provides
    @Singleton
    fun provideNetworkRepository(
        telephonyDataSource: TelephonyDataSource,
        connectivityDataSource: ConnectivityDataSource,
        logger: Logger,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): NetworkRepository {
        return NetworkRepositoryImpl(
            telephonyDataSource,
            connectivityDataSource,
            logger,
            ioDispatcher
        )
    }

    /**
     * Provides singleton PermissionRepository implementation.
     *
     * This binds the PermissionRepositoryImpl to the PermissionRepository interface.
     *
     * @param permissionDataSource Data source for permission checking
     * @return PermissionRepository implementation
     */
    @Provides
    @Singleton
    fun providePermissionRepository(
        permissionDataSource: PermissionDataSource
    ): PermissionRepository {
        return PermissionRepositoryImpl(permissionDataSource)
    }
}
