package app.ninesevennine.twofactorauthenticator.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle


object Clipboard {
    fun copy(context: Context, label: String, text: String, isSensitive: Boolean) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)

        if (isSensitive) {
            val extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }

            clip.description.extras = extras
        }

        clipboard.setPrimaryClip(clip)
    }
}