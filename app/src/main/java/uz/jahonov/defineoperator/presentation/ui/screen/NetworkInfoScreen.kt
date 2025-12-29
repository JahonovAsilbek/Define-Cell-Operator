package uz.jahonov.defineoperator.presentation.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.jahonov.defineoperator.domain.model.ConnectionState
import uz.jahonov.defineoperator.domain.model.NetworkStatus
import uz.jahonov.defineoperator.domain.model.SimCardInfo
import uz.jahonov.defineoperator.presentation.state.NetworkUiState
import uz.jahonov.defineoperator.presentation.viewmodel.NetworkInfoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NetworkInfoScreen(
    viewModel: NetworkInfoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.onPermissionsGranted()
        }
    }

    when (val state = uiState) {
        is NetworkUiState.Loading -> LoadingContent()
        is NetworkUiState.Success -> NetworkInfoContent(
            networkStatus = state.networkStatus,
            onRefresh = { viewModel.refresh() },
            modifier = modifier
        )
        is NetworkUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = { viewModel.refresh() }
        )
        is NetworkUiState.PermissionDenied -> PermissionDeniedContent(
            onRequestPermissions = {
                permissionLauncher.launch(state.requiredPermissions.toTypedArray())
            }
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading network information...")
        }
    }
}

@Composable
private fun NetworkInfoContent(
    networkStatus: NetworkStatus,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ConnectionStatusCard(connectionState = networkStatus.activeConnection)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIM Cards (${networkStatus.simCards.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        if (networkStatus.simCards.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No SIM cards detected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(networkStatus.simCards) { simCard ->
                SimCardItem(simCardInfo = simCard)
            }
        }

        item {
            Text(
                text = "Last updated: ${formatTimestamp(networkStatus.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is ConnectionState.Mobile -> MaterialTheme.colorScheme.primaryContainer
                is ConnectionState.WiFi -> MaterialTheme.colorScheme.secondaryContainer
                is ConnectionState.None -> MaterialTheme.colorScheme.errorContainer
                is ConnectionState.Unknown -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (connectionState) {
                        is ConnectionState.Mobile -> Icons.Default.SignalCellularAlt
                        is ConnectionState.WiFi -> Icons.Default.Wifi
                        is ConnectionState.None -> Icons.Default.SignalCellularOff
                        is ConnectionState.Unknown -> Icons.Default.Help
                    },
                    contentDescription = null,
                    tint = when (connectionState) {
                        is ConnectionState.Mobile -> MaterialTheme.colorScheme.onPrimaryContainer
                        is ConnectionState.WiFi -> MaterialTheme.colorScheme.onSecondaryContainer
                        is ConnectionState.None -> MaterialTheme.colorScheme.onErrorContainer
                        is ConnectionState.Unknown -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Text(
                    text = "Active Connection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when (connectionState) {
                is ConnectionState.Mobile -> {
                    InfoRow("Carrier", connectionState.carrierName)
                    InfoRow("Type", connectionState.networkType.displayName)
                    InfoRow("Roaming", if (connectionState.isRoaming) "Yes" else "No")
                    if (connectionState.isRoaming) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Roaming Active",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                is ConnectionState.WiFi -> {
                    InfoRow("Network", connectionState.ssid ?: "Connected")
                }

                is ConnectionState.None -> {
                    Text("No internet connection", style = MaterialTheme.typography.bodyMedium)
                }

                is ConnectionState.Unknown -> {
                    Text("Connection state unknown", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SimCardItem(
    simCardInfo: SimCardInfo,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIM ${simCardInfo.slotIndex + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (simCardInfo.isEmbedded) {
                    AssistChip(
                        onClick = { },
                        label = { Text("eSIM") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.SimCard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Text(
                text = simCardInfo.carrierName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            simCardInfo.displayName?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            InfoRow("Operator Code", simCardInfo.networkOperator)
            InfoRow("Country", simCardInfo.countryIso.uppercase())
            simCardInfo.phoneNumber?.let {
                InfoRow("Number", it)
            }
            InfoRow("Subscription ID", simCardInfo.subscriptionId.toString())
            InfoRow("Roaming Enabled", if (simCardInfo.isDataRoaming) "Yes" else "No")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermissions: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "This app needs permission to access phone state and network information to display SIM card details and connection status.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Grant Permissions")
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
