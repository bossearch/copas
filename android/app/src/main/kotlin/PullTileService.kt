package copas.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PullTileService : TileService() {

  override fun onClick() {
    super.onClick()

    Handler(Looper.getMainLooper()).post {
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val settings = SettingsManager(this@PullTileService)
          if (!settings.isConfigured) {
            showToastOnMainThread("Copas not configured")
            return@launch
          }

          val client = ApiClient(settings.serverUrl, settings.authToken)
          val text = client.pullClipboard()

          val clipboard =
                  applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          clipboard.setPrimaryClip(ClipData.newPlainText("copas", text))

          showToastOnMainThread("Pulled from PC")
        } catch (e: Exception) {
          showToastOnMainThread("Pull failed: ${e.message ?: "Unknown error"}")
        }
      }
    }
  }

  private fun showToastOnMainThread(message: String) {
    Handler(Looper.getMainLooper()).post {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
  }
}
