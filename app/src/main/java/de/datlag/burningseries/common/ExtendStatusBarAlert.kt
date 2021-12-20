package de.datlag.burningseries.common

import com.fede987.statusbaralert.StatusBarAlert
import java.util.concurrent.TimeUnit

fun StatusBarAlert.showLoading(loadingText: String, backgroundColor: Int, textColor: Int) = this.hide {
    this.setText(loadingText)
    this.setAutoHide(false)
    this.setAlertColor(backgroundColor)
    this.setTextColor(textColor)
    this.showProgress()
    this.setProgressBarColor(textColor)
    this.show()
}

fun StatusBarAlert.showError(errorText: String, backgroundColor: Int, textColor: Int) = this.hide {
    this.setText(errorText)
    this.setAutoHide(true)
    this.setDuration(3, TimeUnit.SECONDS)
    this.setAlertColor(backgroundColor)
    this.setTextColor(textColor)
    this.hideProgress()
    this.show()
}

fun StatusBarAlert.showSuccess(successText: String, backgroundColor: Int, textColor: Int) = showError(successText, backgroundColor, textColor)