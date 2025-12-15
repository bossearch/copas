package copas.app

import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClipboardMonitor(context: Context) {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val _lastText = MutableStateFlow("")
    val lastText = _lastText.asStateFlow()

    fun startMonitoring(onChange: (String) -> Unit) {
        clipboard.addPrimaryClipChangedListener {
            val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            if (text != _lastText.value) {
                _lastText.value = text
                onChange(text)
            }
        }
    }
}
