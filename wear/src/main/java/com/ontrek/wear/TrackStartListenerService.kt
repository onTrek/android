package com.ontrek.wear

import android.app.ActivityManager
import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class TrackStartListenerService : WearableListenerService() {

    private fun isAppRunning(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        val packageName = packageName
        return runningProcesses.any { it.processName == packageName }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            Log.d("WATCH_CONNECTION", "${event}")
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/track-start") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val trackID = dataMap.getInt("trackId")
                val sessionID = dataMap.getInt("sessionId")
                val trackName = dataMap.getString("trackName") ?: ""
                if (!isAppRunning()) {
                    Log.d("WATCH_CONNECTION", "App non attiva, avvio MainActivity")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("trackId", trackID)
                    intent.putExtra("sessionId", sessionID)
                    intent.putExtra("trackName", trackName)
                    startActivity(intent)
                } else {
                    Log.d("WATCH_CONNECTION", "App già attiva, non avvio MainActivity")
                }
            }
        }
    }
}
