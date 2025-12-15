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
import android.content.ClipData

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
    var status by remember { mutableStateOf("Ready") }
    var configured by remember { mutableStateOf(settings.isConfigured) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
            status = if (configured) "Configuration saved" else "Invalid configuration"
        }) {
            Text("Save Configuration")
        }

        if (configured) {
            Button(onClick = {
                scope.launch {
                    try {
                        // Get current clipboard content
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = clipboard.primaryClip

                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).text.toString()
                            if (text.isNotBlank()) {
                                val client = ApiClient(serverUrl, token)
                                client.pushClipboard(text)
                                status = "Pushed to PC: ${text.take(20)}${if (text.length > 20) "..." else ""}"
                            } else {
                                status = "Clipboard is empty"
                            }
                        } else {
                            status = "No clipboard content found"
                        }
                    } catch (e: Exception) {
                        status = "Push failed: ${e.message ?: "Unknown error"}"
                    }
                }
            }, enabled = configured) {
                Text("Push to PC")
            }

            Button(onClick = {
                scope.launch {
                    try {
                        val client = ApiClient(serverUrl, token)
                        val text = client.pullClipboard()

                        // Set to Android clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("copas", text)
                        clipboard.setPrimaryClip(clip)

                        status = "Pulled from PC: ${text.take(20)}${if (text.length > 20) "..." else ""}"
                    } catch (e: Exception) {
                        status = "Pull failed: ${e.message ?: "Unknown error"}"
                    }
                }
            }, enabled = configured) {
                Text("Pull from PC")
            }
        }

        Text(status, style = MaterialTheme.typography.bodyMedium)

        if (!configured) {
            Text(
                "Please save configuration first",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
