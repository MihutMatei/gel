package kronos.project.util

import java.util.Locale

actual fun changeLanguage(languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
}
