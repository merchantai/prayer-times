package com.valueappsolutions.prayertimes.ui.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
            "WidgetUpdateOneTime",
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
