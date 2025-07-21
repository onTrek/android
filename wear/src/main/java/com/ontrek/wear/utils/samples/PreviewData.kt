package com.ontrek.wear.utils.samples

import com.ontrek.shared.data.Track
import com.ontrek.shared.data.TrackStats

val sampleTrackList: List<Track> = listOf(
    Track(
        id = 1,
        filename = "track1.gpx",
        stats = TrackStats(
            km = 5.2f,
            duration = "00:45:00",
            ascent = 120.0,
            descent = 110.0,
            max_altitude = 450,
            min_altitude = 320
        ),
        title = "Morning Hike",
        upload_date = "2024-06-01",
        size = 0
    ),
    Track(
        id = 3,
        filename = "track3.gpx",
        stats = TrackStats(
            km = 8.7f,
            duration = "01:20:00",
            ascent = 200.0,
            descent = 195.0,
            max_altitude = 600,
            min_altitude = 400
        ),
        title = "Mount Everest Expedition",
        upload_date = "2024-06-02",
        size = 0
    ),
    Track(
        id = 2,
        filename = "track2.gpx",
        stats = TrackStats(
            km = 8.7f,
            duration = "01:20:00",
            ascent = 200.0,
            descent = 195.0,
            max_altitude = 600,
            min_altitude = 400
        ),
        title = "Evening Trail",
        upload_date = "2024-06-02",
        size = 0
    )
)
