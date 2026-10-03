package com.valueappsolutions.prayertimes.ui.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SunTrajectoryUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        // Only proceed if it's the 10-minute alarm OR the user just unlocked their phone
        if (action == Intent.ACTION_USER_PRESENT || action == SunTrajectoryUpdateScheduler.ACTION_UPDATE_SUN_WIDGET) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // We want to force an immediate update if the device just woke up, 
                    // or if the alarm fired (and device is active)
                    SunTrajectoryUpdateScheduler.scheduleNextUpdateIfActive(context, forceImmediateUpdate = true)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
