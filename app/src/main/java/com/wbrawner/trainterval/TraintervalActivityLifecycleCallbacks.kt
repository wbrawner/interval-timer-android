package com.wbrawner.trainterval

import android.app.Activity
import android.app.Application
import android.os.Bundle
import timber.log.Timber

private const val TAG = "ActivityLifecycle"

class TraintervalActivityLifecycleCallbacks(
    private val logger: Timber.Tree = Timber.tag(TAG)
) : Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logger.v("onActivityCreated: $activity")
    }

    override fun onActivityStarted(activity: Activity) {
        logger.v("onActivityStarted: $activity")
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        logger.v("onActivityResumed: $activity")
    }

    override fun onActivityPaused(activity: Activity) {
        logger.v("onActivityPaused: $activity")
    }

    override fun onActivityStopped(activity: Activity) {
        logger.v("onActivityStopped: $activity")
        currentActivity = null
    }

    override fun onActivityDestroyed(activity: Activity) {
        logger.v("onActivityDestroyed: $activity")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        logger.v("onActivitySaveInstanceState: $activity")
    }
}
