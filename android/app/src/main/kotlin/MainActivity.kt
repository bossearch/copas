package copas.app

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = SettingsManager(this)
        val clipboardMonitor = ClipboardMonitor(this)

        setContent {
            CopasTheme {
                MainScreen(settings, clipboardMonitor)
            }
        }
    }
}

@Composable
fun MainScreen(
    settings: SettingsManager,
    clipboardMonitor: ClipboardMonitor
) {
    var serverUrl by remember { mutableStateOf(settings.serverUrl) }
    var token by remember { mutableStateOf(settings.authToken) }
    var status by remember { mutableStateOf("Not synced") }
    var configured by remember { mutableStateOf(settings.isConfigured) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(configured) {
        if (configured) {
            clipboardMonitor.startMonitoring { text ->
                if (text.isNotBlank()) {
                    scope.launch {
                        try {
                            val client = ApiClient(serverUrl, token)
                            client.pushClipboard(text)
                            status = "Pushed: ${text.take(20)}"
                        } catch (e: Exception) {
                            status = "Push failed: ${e.message}"
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Copas - Copy Paste Sync", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Server URL (e.g., http://192.168.1.42:6669)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Auth Token") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            settings.serverUrl = serverUrl
            settings.authToken = token
            configured = settings.isConfigured
            status = "Saved"
        }) {
            Text("Save")
        }

        if (configured) {
            Button(onClick = {
                scope.launch {
                    try {
                        val client = ApiClient(serverUrl, token)
                        val text = client.pullClipboard()

                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("copas", text)
                        clipboard.setPrimaryClip(clip)

                        status = "Pulled: ${text.take(20)}..."
                    } catch (e: Exception) {
                        status = "Pull failed: ${e.message}"
                    }
                }
            }) {
                Text("Pull from PC")
            }
        }

        Text(status, style = MaterialTheme.typography.bodyMedium)
    }
}
