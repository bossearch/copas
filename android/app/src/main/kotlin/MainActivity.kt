package copas.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private var pushFromTileRequest by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)

    val settings = SettingsManager(this)

    setContent {
      CopasTheme {
        MainScreen(
                settings = settings,
                shouldPushFromTile = pushFromTileRequest,
                onPushFromTileHandled = { pushFromTileRequest = false }
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    if (intent?.getBooleanExtra("PUSH_FROM_TILE", false) == true) {
      pushFromTileRequest = true
      intent.removeExtra("PUSH_FROM_TILE")
    }
  }
}

@Composable
fun MainScreen(
        settings: SettingsManager,
        shouldPushFromTile: Boolean,
        onPushFromTileHandled: () -> Unit
) {
  var serverUrl by remember { mutableStateOf(settings.serverUrl) }
  var token by remember { mutableStateOf(settings.authToken) }
  var status by remember { mutableStateOf("Ready") }
  var configured by remember { mutableStateOf(settings.isConfigured) }
  var isPerformingPush by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  LaunchedEffect(shouldPushFromTile) {
    if (!shouldPushFromTile || isPerformingPush || !configured) return@LaunchedEffect

    isPerformingPush = true
    status = "Pushing clipboard..."

    try {
      val text = waitForClipboardText(context)

      if (!text.isNullOrBlank()) {
        val client = ApiClient(serverUrl, token)
        client.pushClipboard(text)
        status = "Pushed: ${text.take(20)}${if (text.length > 20) "..." else ""}"
      } else {
        status = "Clipboard empty - nothing to push"
      }
    } catch (e: Exception) {
      status = "Push failed: ${e.message ?: "Unknown error"}"
    } finally {
      isPerformingPush = false
      onPushFromTileHandled()
    }
  }

  Column(
          modifier = Modifier.fillMaxSize().padding(16.dp),
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

    Button(
            onClick = {
              settings.serverUrl = serverUrl
              settings.authToken = token
              configured = settings.isConfigured
              status = if (configured) "Configuration saved" else "Invalid configuration"
            }
    ) { Text("Save Configuration") }

    if (configured) {
      Button(
              onClick = {
                scope.launch {
                  isPerformingPush = true
                  status = "Pushing clipboard..."

                  try {
                    val text = waitForClipboardText(context)

                    if (!text.isNullOrBlank()) {
                      val client = ApiClient(serverUrl, token)
                      client.pushClipboard(text)
                      status = "Pushed: ${text.take(20)}${if (text.length > 20) "..." else ""}"
                    } else {
                      status = "Clipboard empty"
                    }
                  } catch (e: Exception) {
                    status = "Push failed: ${e.message ?: "Unknown error"}"
                  } finally {
                    isPerformingPush = false
                  }
                }
              },
              enabled = !isPerformingPush
      ) { Text("Push to PC") }

      Button(
              onClick = {
                scope.launch {
                  try {
                    val client = ApiClient(serverUrl, token)
                    val text = client.pullClipboard()

                    val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("copas", text))

                    status = "Pulled: ${text.take(20)}${if (text.length > 20) "..." else ""}"
                  } catch (e: Exception) {
                    status = "Pull failed: ${e.message ?: "Unknown error"}"
                  }
                }
              }
      ) { Text("Pull from PC") }
    }

    Text(status)

    if (!configured) {
      Text("Please save configuration first", color = MaterialTheme.colorScheme.error)
    }
  }
}

private suspend fun waitForClipboardText(
        context: Context,
        timeoutMs: Long = 1000,
        intervalMs: Long = 50
): String? {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val start = System.currentTimeMillis()

  while (System.currentTimeMillis() - start < timeoutMs) {
    val clip = clipboard.primaryClip
    if (clip != null && clip.itemCount > 0) {
      val text = clip.getItemAt(0).coerceToText(context)?.toString()
      if (!text.isNullOrBlank()) return text
    }
    delay(intervalMs)
  }
  return null
}
