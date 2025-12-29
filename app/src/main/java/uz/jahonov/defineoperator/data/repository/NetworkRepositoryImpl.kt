package uz.jahonov.defineoperator.data.repository

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.jahonov.defineoperator.data.mapper.ConnectionMapper
import uz.jahonov.defineoperator.data.mapper.SimCardMapper
import uz.jahonov.defineoperator.data.source.ConnectivityDataSource
import uz.jahonov.defineoperator.data.source.TelephonyDataSource
import uz.jahonov.defineoperator.di.IoDispatcher
import uz.jahonov.defineoperator.domain.model.ConnectionState
import uz.jahonov.defineoperator.domain.model.NetworkStatus
import uz.jahonov.defineoperator.domain.model.SimCardInfo
import uz.jahonov.defineoperator.domain.repository.NetworkRepository
import uz.jahonov.defineoperator.util.Logger
import javax.inject.Inject

/**
 * Implementation of NetworkRepository.
 *
 * This repository coordinates between TelephonyDataSource and ConnectivityDataSource to provide
 * complete network status information. It implements the business logic for determining active
 * connections and handling multi-SIM scenarios.
 *
 * **Architecture:**
 * ```
 * NetworkRepositoryImpl
 *     ├── TelephonyDataSource (SIM card information)
 *     ├── ConnectivityDataSource (Network connectivity)
 *     ├── SimCardMapper (SubscriptionInfo → SimCardInfo)
 *     └── ConnectionMapper (Network type int → NetworkType enum)
 * ```
 *
 * **Thread safety:**
 * All suspend functions run on the IO dispatcher to avoid blocking the main thread.
 * The Flow returned by observeNetworkChanges() uses callbackFlow and flowOn to ensure
 * thread safety.
 *
 * @property telephonyDataSource Data source for SIM card and telephony information
 * @property connectivityDataSource Data source for network connectivity information
 * @property logger Logger for debugging and error reporting
 * @property ioDispatcher IO dispatcher for background operations
 */
class NetworkRepositoryImpl @Inject constructor(
    private val telephonyDataSource: TelephonyDataSource,
    private val connectivityDataSource: ConnectivityDataSource,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkRepository {

    override suspend fun getSimCardsInfo(): List<SimCardInfo> = withContext(ioDispatcher) {
        try {
            val subscriptions = telephonyDataSource.getActiveSubscriptions()
            logger.d(TAG, "Found ${subscriptions.size} active subscriptions")

            subscriptions.map { subscription ->
                val telephonyManager = telephonyDataSource
                    .getTelephonyManagerForSubscription(subscription.subscriptionId)
                SimCardMapper.toDomain(subscription, telephonyManager).also {
                    logger.d(
                        TAG,
                        "SIM ${it.slotIndex}: ${it.carrierName} (${it.networkOperator}, Roaming: ${it.isDataRoaming})"
                    )
                }
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error getting SIM cards info", e)
            emptyList()
        }
    }

    override suspend fun getActiveConnection(): ConnectionState = withContext(ioDispatcher) {
        try {
            when {
                connectivityDataSource.isWiFiActive() -> {
                    logger.d(TAG, "Active connection: Wi-Fi")
                    ConnectionState.WiFi(ssid = null) // SSID requires location permission
                }

                connectivityDataSource.isCellularActive() -> {
                    val subscriptionId = connectivityDataSource.getActiveDataSubscriptionId()
                    val subscriptions = telephonyDataSource.getActiveSubscriptions()
                    val activeSubscription = subscriptions.find { it.subscriptionId == subscriptionId }

                    if (activeSubscription != null) {
                        val telephonyManager = telephonyDataSource
                            .getTelephonyManagerForSubscription(subscriptionId)
                        val isRoaming = telephonyManager.isNetworkRoaming
                        val networkType = ConnectionMapper.toDomainNetworkType(
                            telephonyDataSource.getDataNetworkType()
                        )

                        logger.d(
                            TAG,
                            "Active connection: Mobile (${activeSubscription.carrierName}, " +
                                    "Roaming: $isRoaming, Type: ${networkType.displayName})"
                        )

                        ConnectionState.Mobile(
                            subscriptionId = subscriptionId,
                            carrierName = activeSubscription.carrierName?.toString() ?: "Unknown",
                            isRoaming = isRoaming,
                            networkType = networkType
                        )
                    } else {
                        logger.w(TAG, "Cellular active but no matching subscription found")
                        ConnectionState.Unknown
                    }
                }

                else -> {
                    logger.d(TAG, "No active connection")
                    ConnectionState.None
                }
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error getting active connection", e)
            ConnectionState.Unknown
        }
    }

    override fun observeNetworkChanges(): Flow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                logger.d(TAG, "Network available: $network")
                launch {
                    val status = getCurrentNetworkStatus()
                    send(status)
                }
            }

            override fun onLost(network: Network) {
                logger.d(TAG, "Network lost: $network")
                launch {
                    val status = getCurrentNetworkStatus()
                    send(status)
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                logger.d(TAG, "Network capabilities changed: $network")
                launch {
                    val status = getCurrentNetworkStatus()
                    send(status)
                }
            }
        }

        try {
            connectivityDataSource.registerNetworkCallback(callback)
            logger.d(TAG, "Network callback registered")

            // Send initial state
            val initialStatus = getCurrentNetworkStatus()
            send(initialStatus)
            logger.d(TAG, "Initial network status sent")
        } catch (e: Exception) {
            logger.e(TAG, "Error in observeNetworkChanges", e)
        }

        awaitClose {
            connectivityDataSource.unregisterNetworkCallback(callback)
            logger.d(TAG, "Network callback unregistered")
        }
    }.flowOn(ioDispatcher)

    /**
     * Gets the current network status by combining SIM card info and active connection.
     *
     * This is a helper method used by observeNetworkChanges() to create NetworkStatus snapshots.
     * It's called whenever network state changes or when the initial state is needed.
     *
     * @return Current NetworkStatus with SIM cards and active connection
     */
    private suspend fun getCurrentNetworkStatus(): NetworkStatus {
        return NetworkStatus(
            simCards = getSimCardsInfo(),
            activeConnection = getActiveConnection()
        )
    }

    companion object {
        private const val TAG = "NetworkRepositoryImpl"
    }
}
