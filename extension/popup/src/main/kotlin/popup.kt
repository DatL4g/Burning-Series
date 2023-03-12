import common.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.dom.addClass
import org.w3c.dom.Element
import kotlin.js.Promise

var settingToggle = false
fun main() {
    val saveColor: MutableStateFlow<dynamic> = MutableStateFlow(null)

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
            val darkMode = (darkModeItem as? Boolean) ?: it.asDynamic().darkMode.unsafeCast<Boolean?>()

            modeChanged(darkMode, defaultColorSchemeDark, toggle)
        }

        browser.storage.sync.get("color").collect {
            val colorItem = runCatching {
                it as? String
            }.getOrNull() ?: runCatching {
                it.unsafeCast<String?>()
            }.getOrNull()
            val savedColor = (colorItem as? String) ?: it.asDynamic().color.unsafeCast<String?>() ?: "#f5cd67"

            val options = objectOf<dynamic> {
                color = savedColor
            }

            picker = runCatching {
                iro.ColorPicker("#picker", options)
            }.getOrNull() ?: iro.default.ColorPicker("#picker", options)

            runCatching {
                picker.on("color:change") { color -> saveColor.tryEmit(color) } as Unit
            }
        }

        toggle?.addEventListener("change", { event ->
            val checked = event.currentTarget?.asDynamic().checked.unsafeCast<Boolean>()

            if (!settingToggle) {
                if (checked) {
                    browser.storage.sync.set(objectOf<dynamic> { darkMode = true } as Any)
                } else {
                    browser.storage.sync.set(objectOf<dynamic> { darkMode = false } as Any)
                }
            }
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

    GlobalScope.launch(Dispatchers.Default) {
        saveColor.debounce(1000).collect {
            withContext(Dispatchers.Main) {
                onColorChange(it)
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

private fun onColorChange(changeColor: dynamic) {
    if (changeColor.unsafeCast<Any?>().isNullOrEmpty()) {
        return
    }
    browser.storage.sync.set(objectOf<dynamic> { color = changeColor.hexString } as Any)
}

@JsModule("@jaames/iro")
@JsNonModule
external val iro: dynamic