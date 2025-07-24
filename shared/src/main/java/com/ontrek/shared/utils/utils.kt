package com.ontrek.shared.utils

import android.util.Log
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDate(dateString: String, time: Boolean = false): String {
    return try {
        val instant = Instant.parse(dateString)
        val pattern = if (time) {
            "dd/MM/yyyy HH:mm"
        } else {
            "dd/MM/yyyy"
        }

        DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(instant)
    } catch (e: Exception) {
        Log.e("Utils", "Error formatting date: ${e.message}")
        dateString
    }
}

// Formatta la durata per la visualizzazione
fun formatDuration(duration: String): String {
    return try {
        val parts = duration.split(":")

        if ((parts[0] == "00" && parts[1] == "00") || (parts.size < 2)) {
            "--:--"
        } else {
            "${parts[0]}h ${parts[1]}m"
        }
    } catch (e: Exception) {
        Log.e("Utils", "Error formatting duration: ${e.message}")
        duration
    }
}