package de.datlag.burningseries.ui.connector

import com.fede987.statusbaralert.StatusBarAlert
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
interface StatusBarAlertProvider {
    val statusBarAlert: StatusBarAlert
}