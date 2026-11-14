package com.keal.playarea

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SessionStore.initialize(context)
                SessionStore.all(context)
                    .filter { it.status == SessionStatus.ACTIVE }
                    .forEach { SessionAlarmReceiver.schedule(context, it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
