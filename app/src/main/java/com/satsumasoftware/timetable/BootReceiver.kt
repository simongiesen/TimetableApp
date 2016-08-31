package com.satsumasoftware.timetable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver() : BroadcastReceiver() {

    companion object {
        private const val LOG_TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(LOG_TAG, "@onReceive() - ACTION_BOOT_COMPLETED - now refreshing alarms...")

            val application = context!!.applicationContext as TimetableApplication
            application.refreshAlarms(context)
        }
    }

}
