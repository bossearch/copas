package copas.app

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import android.os.Handler
import android.os.Looper

class PushTileService : TileService() {

    companion object {
        const val TAG = "PushTileService"
        const val REQUEST_CODE_PUSH_TILE = 1001
        const val ACTION_PUSH_FROM_TILE = "copas.app.PUSH_FROM_TILE_ACTION"
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Push tile clicked.")

        showToast("Launching Copas...")

        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_PUSH_FROM_TILE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("PUSH_FROM_TILE", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE_PUSH_TILE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            startActivityAndCollapse(pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MainActivity from tile using PendingIntent: ${e.message}", e)
            showToastOnMainThread("Failed to launch Copas: ${e.message}")
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
