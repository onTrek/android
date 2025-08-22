package com.ontrek.shared.utils

import android.util.Log
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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

// Formats a timestamp to a human-readable "time ago" format
fun formatTimeAgo(timestamp: String): String {
    return try {
        val time = Instant.parse(timestamp)
        val now = Instant.now()
        val diff =  Duration.between(time, now).toMillis()

        when {
            diff < 60_000 -> "Now"
            diff < 3_600_000 -> "${diff / 60_000} minutes ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            else -> "${diff / 86_400_000} days ago"
        }
    } catch (e: DateTimeParseException) {
        Log.e("Utils", "Error formatting duration: ${e.message}")
        ""
    }
}