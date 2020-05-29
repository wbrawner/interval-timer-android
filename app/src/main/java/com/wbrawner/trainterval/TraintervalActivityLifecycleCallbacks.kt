package com.wbrawner.trainterval

import android.app.Activity
import android.app.Application
import android.os.Bundle

private const val TAG = "ActivityLifecycleCallbacks"

class TraintervalActivityLifecycleCallbacks(
    private val logger: Logger
) : Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logger.v(TAG, "onActivityCreated: $activity")
    }

    override fun onActivityStarted(activity: Activity) {
        logger.v(TAG, "onActivityStarted: $activity")
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        logger.v(TAG, "onActivityResumed: $activity")
    }

    override fun onActivityPaused(activity: Activity) {
        logger.v(TAG, "onActivityPaused: $activity")
    }

    override fun onActivityStopped(activity: Activity) {
        logger.v(TAG, "onActivityStopped: $activity")
        currentActivity = null
    }

    override fun onActivityDestroyed(activity: Activity) {
        logger.v(TAG, "onActivityDestroyed: $activity")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        logger.v(TAG, "onActivitySaveInstanceState: $activity")
    }
}
