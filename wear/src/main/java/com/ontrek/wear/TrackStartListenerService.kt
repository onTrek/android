package com.ontrek.wear

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class TrackStartListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            Log.d("WATCH_CONNECTION", "${event}")
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/track-start") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val trackID = dataMap.getInt("trackId")
                val sessionID = dataMap.getInt("sessionId")
                val trackName = dataMap.getString("trackName") ?: ""
                if (!MainActivity.isInForeground) {
                    Log.d("WATCH_CONNECTION", "App not in foreground, starting MainActivity")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("trackId", trackID)
                    intent.putExtra("sessionId", sessionID)
                    intent.putExtra("trackName", trackName)
                    startActivity(intent)
                } else {
                    Log.d("WATCH_CONNECTION", "App in foreground, not starting MainActivity")
                }
            }
        }
    }
}
