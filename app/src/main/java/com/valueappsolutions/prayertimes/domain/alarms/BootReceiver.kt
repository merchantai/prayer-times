package com.valueappsolutions.prayertimes.domain.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // We could trigger a WorkManager job here to reschedule all alarms
            // Or just start a service.
            // For now, we will leave it ready to be hooked up.
            // When the app is opened, alarms will be re-scheduled anyway by the ViewModel.
        }
    }
}
