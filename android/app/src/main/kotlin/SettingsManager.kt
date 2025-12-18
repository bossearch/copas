package copas.app

import android.content.Context
import androidx.core.content.edit

class SettingsManager(context: Context) {
  private val prefs = context.getSharedPreferences("copas_prefs", Context.MODE_PRIVATE)

  var serverHost: String
    get() = prefs.getString("server_host", "") ?: ""
    // get() = prefs.getString("server_url", BuildConfig.DEFAULT_SERVER_URL) ?: ""
    set(value) = prefs.edit().putString("server_host", value).apply()

  var serverPort: String
    get() = prefs.getString("server_port", "6669") ?: "6669"
    set(value) = prefs.edit().putString("server_port", value.ifBlank { "6669" }).apply()

  var authToken: String
    get() = prefs.getString("auth_token", "") ?: ""
    // get() = prefs.getString("auth_token", BuildConfig.DEFAULT_AUTH_TOKEN) ?: ""
    set(value) = prefs.edit().putString("auth_token", value).apply()

  val isConfigured: Boolean
    get() = serverHost.isNotBlank() && authToken.isNotBlank()
}
