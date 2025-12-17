package copas.app

import android.content.Context
import androidx.core.content.edit

class SettingsManager(context: Context) {

  private val prefs =
    context.getSharedPreferences("copas_settings", Context.MODE_PRIVATE)

  var serverUrl: String
    get() = prefs.getString("server_url", BuildConfig.DEFAULT_SERVER_URL) ?: ""
    set(value) = prefs.edit().putString("server_url", value).apply()

  var authToken: String
    get() = prefs.getString("auth_token", BuildConfig.DEFAULT_AUTH_TOKEN) ?: ""
    set(value) = prefs.edit().putString("auth_token", value).apply()

  val isConfigured: Boolean
    get() = serverUrl.isNotBlank() && authToken.isNotBlank()
}

