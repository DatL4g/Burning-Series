package de.datlag.burningseries.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.datetime.Clock

@Obfuscate
data class AppUsageTracker(
    val previousTime: Long,
    val saveListener: (time: Long) -> Unit
) : Application.ActivityLifecycleCallbacks {
    private var startingTime: Long = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        initOnNoChange()
    }

    override fun onActivityStarted(activity: Activity) {
        initOnNoChange()
    }

    override fun onActivityResumed(activity: Activity) {
        initOnNoChange()
    }

    override fun onActivityPaused(activity: Activity) {
        saveOnNoChange()
    }

    override fun onActivityStopped(activity: Activity) {
        saveOnNoChange()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        saveOnNoChange()
    }

    override fun onActivityDestroyed(activity: Activity) {
        saveOnNoChange()
    }


    private fun initOnNoChange() {
        if (startingTime <= 0L) {
            startingTime = Clock.System.now().epochSeconds
        }
    }

    private fun saveOnNoChange() {
        if (startingTime <= 0L) {
            startingTime = Clock.System.now().epochSeconds
        }
        saveListener.invoke(previousTime + (Clock.System.now().epochSeconds - startingTime))
    }
}
