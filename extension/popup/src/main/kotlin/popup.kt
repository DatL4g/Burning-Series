import common.collect
import common.objectOf
import common.onReady
import common.removeAllClasses
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import org.w3c.dom.Element

var settingToggle = false
fun main() {
    document.onReady {
        val defaultColorSchemeDark = window.matchMedia("(prefers-color-scheme: dark)").matches
        val toggle = document.getElementById("toggleDark")
        var picker: dynamic = objectOf {  }

        browser.storage.sync.get("darkMode").collect {
            val darkModeItem = runCatching {
                it as? Boolean
            }.getOrNull() ?: runCatching {
                it.unsafeCast<Boolean?>()
            }.getOrNull()
            val darkMode = (darkModeItem as? Boolean) ?: it.asDynamic()["darkMode"].unsafeCast<Boolean?>()

            modeChanged(darkMode, defaultColorSchemeDark, toggle)
        }

        browser.storage.sync.get("color").collect {
            val colorItem = runCatching {
                it as? String
            }.getOrNull() ?: runCatching {
                it.unsafeCast<String?>()
            }.getOrNull()
            val savedColor = (colorItem as? String) ?: it.asDynamic()["color"].unsafeCast<String?>() ?: "#f5cd67"

            val options = objectOf<dynamic> {
                color = savedColor
            }

            picker = runCatching {
                iro.ColorPicker("#picker", options)
            }.getOrNull() ?: iro.default.ColorPicker("#picker", options)
        }


        toggle?.addEventListener("change", { event ->
            val checked = event.currentTarget?.asDynamic()["checked"].unsafeCast<Boolean>()

            if (!settingToggle) {
                if (checked) {
                    browser.storage.sync.set(objectOf<dynamic> { darkMode = true } as Any)
                } else {
                    browser.storage.sync.set(objectOf<dynamic> { darkMode = false } as Any)
                }
            }
        })

        document.getElementById("save")?.addEventListener("click", {
            browser.storage.sync.set(objectOf<dynamic> { color = picker.color.hexString } as Any)
        })

        browser.storage.onChanged.addListener {
            val changed = runCatching {
                val item = it.changes.asDynamic().darkMode.newValue
                runCatching {
                    item as? Boolean
                }.getOrNull() ?: runCatching {
                    item.unsafeCast<Boolean?>()
                }.getOrNull()
            }.getOrNull() ?: runCatching {
                val item = it.asDynamic().darkMode.newValue
                runCatching {
                    item as? Boolean
                }.getOrNull() ?: runCatching {
                    item.unsafeCast<Boolean?>()
                }.getOrNull()
            }.getOrNull()

            if (changed != null) {
                modeChanged(changed, toggle)
            }
        }
    }
}

private fun modeChanged(dark: Boolean, toggle: Element?) = modeChanged(dark, dark, toggle)

private fun modeChanged(dark: Boolean?, fallback: Boolean, toggle: Element?) {
    document.body?.removeAllClasses()
    val github = document.getElementById("github")
    github?.removeAllClasses()
    if (dark == null) {
        if (fallback) {
            document.body?.addClass("dark")
            github?.addClass("github-dark")
        } else {
            document.body?.addClass("light")
            github?.addClass("github-light")
        }
    } else if (dark == true) {
        document.body?.addClass("dark")
        github?.addClass("github-dark")
    } else {
        document.body?.addClass("light")
        github?.addClass("github-light")
    }
    settingToggle = true
    toggle?.asDynamic()["checked"] = dark ?: fallback
    settingToggle = false
}

@JsModule("@jaames/iro")
@JsNonModule
external val iro: dynamic