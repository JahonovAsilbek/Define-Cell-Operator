@file:OptIn(ExperimentalMaterial3Api::class)

package uz.jahonov.defineoperator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import uz.jahonov.defineoperator.presentation.ui.screen.NetworkInfoScreen
import uz.jahonov.defineoperator.ui.theme.DefineOperatorTestTheme

/**
 * Main activity hosting the network information screen.
 *
 * This activity is annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 * It uses Jetpack Compose for the UI and follows Material Design 3 guidelines.
 *
 * **Hilt integration:**
 * @AndroidEntryPoint enables:
 * - Dependency injection in this Activity
 * - ViewModels to be injected with @HiltViewModel
 * - Access to dependencies provided in Hilt modules
 *
 * **Architecture:**
 * - Activity: Hosts the Compose UI (minimal logic)
 * - Screen: NetworkInfoScreen (Composable, observes ViewModel)
 * - ViewModel: NetworkInfoViewModel (manages state, executes use cases)
 * - Use Cases: Business logic
 * - Repositories: Data access
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DefineOperatorTestTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Network Operator Info") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                ) { innerPadding ->
                    NetworkInfoScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}