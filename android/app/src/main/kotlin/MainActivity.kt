package copas.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
  private var pushFromTileRequest by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)

    val settings = SettingsManager(this)

    setContent {
      CopasTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainScreen(
                  settings = settings,
                  shouldPushFromTile = pushFromTileRequest,
                  onPushFromTileHandled = { pushFromTileRequest = false }
          )
        }
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
fun CopasTheme(content: @Composable () -> Unit) {
  val darkTheme = isSystemInDarkTheme()
  val brandColor = Color(0xFF7E7EFF)

  MaterialTheme(
          colorScheme =
                  if (darkTheme) {
                    darkColorScheme(
                            primary = brandColor,
                            background = Color(0xFF121212),
                            surface = Color(0xFF1E1E1E),
                            error = Color(0xFFF44336),
                            onPrimary = Color.White,
                            onBackground = Color.White,
                            onSurface = Color.White,
                            onError = Color.White
                    )
                  } else {
                    lightColorScheme(
                            primary = brandColor,
                            background = Color(0xFFF5F5F5),
                            surface = Color.White,
                            error = Color(0xFFD32F2F),
                            onPrimary = Color.Black,
                            onBackground = Color.Black,
                            onSurface = Color.Black,
                            onError = Color.White
                    )
                  },
          typography =
                  Typography(
                          bodyLarge = TextStyle(fontSize = 16.sp),
                          titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                          labelMedium = TextStyle(fontSize = 14.sp)
                  ),
          content = content
  )
}

@Composable
fun MainScreen(
        settings: SettingsManager,
        shouldPushFromTile: Boolean,
        onPushFromTileHandled: () -> Unit
) {
  var serverHost by remember { mutableStateOf(settings.serverHost) }
  var serverPort by remember { mutableStateOf(settings.serverPort) }
  var token by remember { mutableStateOf(settings.authToken) }
  var status by remember {
    mutableStateOf(if (settings.isConfigured) "Ready" else "Not configured")
  }
  var configured by remember { mutableStateOf(settings.isConfigured) }
  var isPushing by remember { mutableStateOf(false) }
  var isPulling by remember { mutableStateOf(false) }
  var isEditing by remember { mutableStateOf(!settings.isConfigured) }

  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val scrollState = rememberScrollState()
  val keyboardController = LocalSoftwareKeyboardController.current
  val darkTheme = isSystemInDarkTheme()

  LaunchedEffect(shouldPushFromTile) {
    if (shouldPushFromTile && configured) {
      isPushing = true
      val host = serverHost
      val port = serverPort.ifBlank { "6669" }
      val tok = token
      val ctxt = context

      try {
        // status = "Pushing clipboard..."
        val text = waitForClipboardText(ctxt)
        if (!text.isNullOrBlank()) {
          val client = ApiClient("http://$host:$port", tok)
          client.pushClipboard(text)
          status = "Pushed to PC"
        } else {
          status = "Clipboard empty"
        }
      } catch (e: Exception) {
        status = "Push failed: ${e.message ?: "Unknown error"}"
      } finally {
        isPushing = false
        onPushFromTileHandled()
        keyboardController?.hide()
        if (shouldPushFromTile && context is ComponentActivity) {
          context.finishAndRemoveTask()
        }
      }
    }
  }

  // UI
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
    ) {
      // App Header
      Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.fillMaxWidth()
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    "Copas",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7E7EFF)
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
          Text(
                  "Copy • Paste • Send",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Server Configuration
      Card(
              modifier = Modifier.fillMaxWidth(),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
                  "Server Configuration",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
                  value = serverHost,
                  onValueChange = { if (isEditing) serverHost = it },
                  placeholder = { Text("e.g., 192.168.1.42") },
                  singleLine = true,
                  enabled = isEditing,
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                          OutlinedTextFieldDefaults.colors(
                                  focusedBorderColor = Color(0xFF7E7EFF),
                                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                  cursorColor = Color(0xFF7E7EFF)
                          )
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
                  value = serverPort,
                  onValueChange = { newValue ->
                    if (isEditing) serverPort = newValue.filter { char -> char.isDigit() }
                  },
                  placeholder = { Text("6669") },
                  singleLine = true,
                  enabled = isEditing,
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                          OutlinedTextFieldDefaults.colors(
                                  focusedBorderColor = Color(0xFF7E7EFF),
                                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                  cursorColor = Color(0xFF7E7EFF)
                          )
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
                  value = token,
                  onValueChange = { if (isEditing) token = it },
                  placeholder = { Text("Enter your auth token") },
                  singleLine = true,
                  enabled = isEditing,
                  modifier = Modifier.fillMaxWidth(),
                  colors =
                          OutlinedTextFieldDefaults.colors(
                                  focusedBorderColor = Color(0xFF7E7EFF),
                                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                  cursorColor = Color(0xFF7E7EFF)
                          )
          )

          Spacer(modifier = Modifier.height(16.dp))

          if (isEditing) {
            Button(
                    onClick = {
                      settings.serverHost = serverHost
                      settings.serverPort = serverPort.ifBlank { "6669" }
                      settings.authToken = token
                      configured = settings.isConfigured
                      isEditing = false
                      status = if (configured) "Configuration saved" else "Invalid configuration"
                    },
                    enabled = serverHost.isNotBlank() && token.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E7EFF))
            ) { Text("Save Configuration", color = MaterialTheme.colorScheme.onPrimary) }
          } else {
            Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E7EFF))
            ) { Text("Edit Configuration", color = MaterialTheme.colorScheme.onPrimary) }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Status indicator
      val (statusIcon, statusTint, statusBg) =
              when {
                status == "Pushed to PC" || status == "Pulled from PC" ->
                        Triple(
                                Icons.Default.CheckCircle,
                                Color.Green,
                                Color.Green.copy(alpha = 0.1f)
                        )
                status.contains("failed") ->
                        Triple(Icons.Default.Error, Color.Red, Color.Red.copy(alpha = 0.1f))
                status == "Clipboard empty" ->
                        Triple(
                                Icons.Default.Info,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                else ->
                        Triple(
                                Icons.Default.Info,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surface
                        )
              }

      Card(
              colors = CardDefaults.cardColors(containerColor = statusBg),
      ) {
        Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(statusIcon, contentDescription = null, tint = statusTint)
          Text(
                  text = status,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Pull or Push buttons and footer
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      if (configured && !isEditing) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
                  onClick = {
                    scope.launch {
                      isPulling = true
                      // status = "Pulling from PC..."
                      try {
                        val url = "http://$serverHost:${serverPort.ifBlank { "6669" }}"
                        val client = ApiClient(url, token)
                        val text = client.pullClipboard()
                        val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as
                                        ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("copas", text))
                        status = "Pulled from PC"
                      } catch (e: Exception) {
                        status = "Pull failed: ${e.message ?: "Unknown error"}"
                      } finally {
                        isPulling = false
                      }
                    }
                  },
                  enabled = !isPushing && !isPulling,
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                  colors =
                          ButtonDefaults.buttonColors(
                                  containerColor =
                                          MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                          )
          ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.height(IntrinsicSize.Min)
            ) {
              if (isPulling) {
                CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (darkTheme) Color.White else Color.Black,
                        strokeWidth = 2.dp
                )
              } else {
                Icon(
                        painter = painterResource(id = R.drawable.ic_pull),
                        contentDescription = "Pull",
                        modifier = Modifier.size(24.dp),
                        tint = if (darkTheme) Color.White else Color.Black
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Pull from PC", color = if (darkTheme) Color.White else Color.Black)
            }
          }
          Button(
                  onClick = {
                    scope.launch {
                      isPushing = true
                      // status = "Pushing to PC..."
                      try {
                        val text = waitForClipboardText(context)
                        if (!text.isNullOrBlank()) {
                          val url = "http://$serverHost:${serverPort.ifBlank { "6669" }}"
                          val client = ApiClient(url, token)
                          client.pushClipboard(text)
                          status = "Pushed to PC"
                        } else {
                          status = "Clipboard empty"
                        }
                      } catch (e: Exception) {
                        status = "Push failed: ${e.message ?: "Unknown error"}"
                      } finally {
                        isPushing = false
                      }
                    }
                  },
                  enabled = !isPushing && !isPulling,
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(12.dp),
                  colors =
                          ButtonDefaults.buttonColors(
                                  containerColor =
                                          MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                          )
          ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.height(IntrinsicSize.Min)
            ) {
              if (isPushing) {
                CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (darkTheme) Color.White else Color.Black,
                        strokeWidth = 2.dp
                )
              } else {
                Icon(
                        painter = painterResource(id = R.drawable.ic_push),
                        contentDescription = "Push",
                        modifier = Modifier.size(24.dp),
                        tint = if (darkTheme) Color.White else Color.Black
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("Push to PC", color = if (darkTheme) Color.White else Color.Black)
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      ClickableText(
              text = AnnotatedString("Made with ❤️ by bossearch"),
              style =
                      MaterialTheme.typography.labelSmall.copy(
                              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                      ),
              onClick = { _ ->
                val uri = android.net.Uri.parse("https://github.com/bossearch/copas")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
              }
      )
    }
  }
}

private suspend fun waitForClipboardText(
        context: Context,
        timeoutMs: Long = 500,
        intervalMs: Long = 30
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
