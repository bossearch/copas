package copas.app

import android.service.quicksettings.TileService
import android.content.Context
import android.content.ClipboardManager
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushTileService : TileService() {

    override fun onClick() {
        super.onClick()

        val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip == null || clip.itemCount == 0) {
            showToast("Clipboard empty")
            return
        }

        val text = clip.getItemAt(0).text?.toString() ?: ""
        if (text.isBlank()) {
            showToast("Not text content")
            return
        }

        showToast("Pushing to PC...")

        Handler(Looper.getMainLooper()).post {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = SettingsManager(this@PushTileService)
                    if (!settings.isConfigured) {
                        showToastOnMainThread("Copas not configured")
                        return@launch
                    }

                    val client = ApiClient(settings.serverUrl, settings.authToken)
                    client.pushClipboard(text)
                    showToastOnMainThread("Pushed to PC")
                } catch (e: Exception) {
                    showToastOnMainThread("Push failed: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToastOnMainThread(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
