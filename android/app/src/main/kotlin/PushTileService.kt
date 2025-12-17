package copas.app

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService

class PushTileService : TileService() {

  override fun onClick() {
    super.onClick()

    val intent =
            Intent(this, MainActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
              putExtra("PUSH_FROM_TILE", true)
            }

    val pendingIntent =
            PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

    startActivityAndCollapse(pendingIntent)
  }
}
