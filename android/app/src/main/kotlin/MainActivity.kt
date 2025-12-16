package copas.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private var pushFromTileRequest by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        val settings = SettingsManager(this)
        val clipboardMonitor = ClipboardMonitor(this)

        setContent {
            val (localPushRequest, setLocalPushRequest) = remember { mutableStateOf(pushFromTileRequest) }
            LaunchedEffect(pushFromTileRequest) {
                 setLocalPushRequest(pushFromTileRequest)
            }

            CopasTheme {
                MainScreen(
                    settings = settings,
                    clipboardMonitor = clipboardMonitor,
                    shouldPushFromTile = localPushRequest,
                    onPushFromTileHandled = { success ->
                        if (pushFromTileRequest) {
                            Log.d(TAG, "onPushFromTileHandled called, resetting activity flag. Success: $success")
                            pushFromTileRequest = false
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called with intent: $intent")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.extras?.getBoolean("PUSH_FROM_TILE", false) == true) {
            Log.d(TAG, "Intent received with PUSH_FROM_TILE flag.")
            pushFromTileRequest = true
        }
    }

    private fun showToastOnUiThread(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun MainScreen(
    settings: SettingsManager,
    clipboardMonitor: ClipboardMonitor,
    shouldPushFromTile: Boolean,
    onPushFromTileHandled: (Boolean) -> Unit
) {
    var serverUrl by remember { mutableStateOf(settings.serverUrl) }
    var token by remember { mutableStateOf(settings.authToken) }
    var status by remember { mutableStateOf("Ready") }
    var configured by remember { mutableStateOf(settings.isConfigured) }
    var isPerformingPushFromTile by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(shouldPushFromTile) {
        if (shouldPushFromTile && !isPerformingPushFromTile && configured) {
            Log.d(MainActivity.TAG, "LaunchedEffect: Attempting push from tile...")
            isPerformingPushFromTile = true


            status = "Pushing clipboard from tile..."


            Log.d(MainActivity.TAG, "LaunchedEffect: Starting 3000ms delay...")
            delay(3000)
            Log.d(MainActivity.TAG, "LaunchedEffect: Delay finished, attempting to read clipboard now.")

            var pushSuccess = false
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = clipboard.primaryClip

                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text.toString()
                    if (text.isNotBlank()) {
                        val client = ApiClient(serverUrl, token)
                        client.pushClipboard(text)

                        status = "Pushed from tile: ${text.take(20)}${if (text.length > 20) "..." else ""}"
                        Log.d(MainActivity.TAG, "Successfully pushed clipboard content from tile: ${text.take(50)}...")
                        pushSuccess = true

                    } else {
                        status = "Clipboard item is blank - nothing to push from tile"
                        Log.w(MainActivity.TAG, "Clipboard item was blank when trying to push from tile.")
                    }
                } else {
                    status = "No clipboard content found after delay - nothing to push from tile"
                    Log.w(MainActivity.TAG, "Clipboard was empty or had no items after delay when trying to push from tile.")
                }
            } catch (e: Exception) {
                Log.e(MainActivity.TAG, "Failed to push clipboard from tile: ${e.message}", e)
                status = "Push from tile failed: ${e.message ?: "Unknown error"}"
                pushSuccess = false
            } finally {
                isPerformingPushFromTile = false

                onPushFromTileHandled(pushSuccess)
            }
        } else if (shouldPushFromTile && !configured) {
             Log.w(MainActivity.TAG, "Received PUSH_FROM_TILE flag but app is not configured.")
             status = "Cannot push from tile: Not configured"

             onPushFromTileHandled(false)
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
            status = if (configured) "Configuration saved" else "Invalid configuration"
        }) {
            Text("Save Configuration")
        }

        if (configured) {

            Button(
                onClick = {
                    scope.launch {
                        try {

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
                },
                enabled = configured && !isPerformingPushFromTile
            ) {
                Text("Push to PC")
            }

            Button(onClick = {
                scope.launch {
                    try {
                        val client = ApiClient(serverUrl, token)
                        val text = client.pullClipboard()

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

        if (isPerformingPushFromTile) {
             Text(
                 "Pushing from tile...",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.primary
             )
        }
    }
}
