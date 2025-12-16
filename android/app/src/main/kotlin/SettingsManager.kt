package copas.app

import android.content.Context
import androidx.core.content.edit

class SettingsManager(context: Context) {
  private val prefs =
          context.applicationContext.getSharedPreferences("copas_prefs", Context.MODE_PRIVATE)

  var serverUrl: String
    get() = prefs.getString("server_url", "") ?: ""
    set(value) = prefs.edit().putString("server_url", value).apply()

  var authToken: String
    get() = prefs.getString("auth_token", "") ?: ""
    set(value) = prefs.edit().putString("auth_token", value).apply()

  val isConfigured: Boolean
    get() = serverUrl.isNotBlank() && authToken.isNotBlank()
}
