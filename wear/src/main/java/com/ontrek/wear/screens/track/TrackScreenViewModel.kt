package com.ontrek.wear.screens.track

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.data.TrackPoint
import com.ontrek.shared.data.toSimplePoint
import com.ontrek.wear.utils.functions.distanceToTrack
import com.ontrek.wear.utils.functions.getDistanceTo
import com.ontrek.wear.utils.objects.NeighbouringTrackPoints
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import kotlin.collections.isNotEmpty
import kotlin.collections.maxOfOrNull
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class TrackScreenViewModel : ViewModel() {

    private val trackPoints = MutableStateFlow(listOf<TrackPoint>())
    val trackPointListState: StateFlow<List<TrackPoint>> = trackPoints
    private val parsingError = MutableStateFlow<String>("")
    val parsingErrorState: StateFlow<String> = parsingError
    private val isNearTrack = MutableStateFlow<Boolean?>(null)
    val isNearTrackState: StateFlow<Boolean?> = isNearTrack
    private val arrowDirection = MutableStateFlow<Float>(0F)
    val arrowDirectionState: StateFlow<Float> = arrowDirection
    private val onTrak = MutableStateFlow<Boolean>(false) //fa ride scrive Trak
    val onTrakState: StateFlow<Boolean> = onTrak
    private val progress = MutableStateFlow(0F) // Progress along the track
    val progressState: StateFlow<Float> = progress

    // States only used inside the viewModel functions
    private val directionTrackPoint = MutableStateFlow<TrackPoint?>(null) // Track point for direction calculation
    private val position = MutableStateFlow<Location?>(null) // Current position of the user
    private val totalLength = MutableStateFlow(0F)

    fun loadGpx(context: Context, fileName: String) {
        val parser = GPXParser()
        viewModelScope.launch {
            try {
                val gpxFile = context.openFileInput(fileName)
                val parsedGpx: Gpx? = parser.parse(gpxFile)
                parsedGpx?.let {
                    Log.d("TrackScreenViewModel", "GPX file parsed successfully: ${it.metadata?.name}")
                    trackPoints.value = it.tracks.flatMap { track ->
                            track.trackSegments.flatMap { segment ->
                                segment.trackPoints.mapIndexed { index, point ->
                                    val distance = if (index > 0) {
                                        getDistanceTo(point.toSimplePoint(), segment.trackPoints[index - 1].toSimplePoint())
                                    } else {
                                        0.0
                                    }
                                    TrackPoint(
                                        latitude = point.latitude,
                                        longitude = point.longitude,
                                        elevation = point.elevation,
                                        distance = distance
                                    )
                                }
                            }
                        }
                    totalLength.value = (trackPoints.value.sumOf { it.distance }).toFloat()
                } ?: {
                    Log.e("TrackScreenViewModel", "Generic GPX parsing error")
                    parsingError.value = "Error parsing GPX file: No data found"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                parsingError.value = "Error reading GPX file: ${e.message}"
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                parsingError.value = "Error parsing GPX file: ${e.message}"
            }
        }
    }

    // Checks the distance to the track and initializes the direction track point
    fun checkTrackDistanceAndInitialize(currentLocation: Location, direction: Float) {
        position.value = currentLocation
        if (trackPoints.value.isNotEmpty()) {
            val nearestPoint = distanceToTrack(
                currentLocation,
                trackPoints.value
            )
            val isNearTrackValue = nearestPoint.distance < (trackPoints.value.maxOfOrNull { it.distance } ?: Double.MIN_VALUE)
            var firstPointIndex = nearestPoint.index
            Log.d("TRACK_SCREEN_VIEW_MODEL", "Distance to track: ${nearestPoint.distance}, isNearTrack: $isNearTrackValue")
            if (isNearTrackValue) {
                if (firstPointIndex+1 >= trackPoints.value.size) {
                    // Circular track check
                    val distanceToFirstPoint = getDistanceTo(
                        trackPoints.value[0].toSimplePoint(),
                        trackPoints.value[firstPointIndex].toSimplePoint()
                    )
                    Log.d("TRACK_SCREEN_VIEW_MODEL", "Circular track detected, $distanceToFirstPoint")
                    if (distanceToFirstPoint < 50) {
                        // This is a circular track, starting from the first point (50m tolerance)
                        firstPointIndex = 1
                        directionTrackPoint.value = trackPoints.value[firstPointIndex]
                    } else {
                        //We set the last point as the direction track point
                        //Basically we are saying that the user is already at the end of the track...
                        directionTrackPoint.value = trackPoints.value[firstPointIndex]
                    }
                } else {
                    //In all other cases, we can safely get the next point as the first point
                    //Check IRL if this is the best way to do it
                    directionTrackPoint.value = trackPoints.value[firstPointIndex+1]
                }
                Log.d("TRACK_SCREEN_VIEW_MODEL", "Starting from $firstPointIndex")
                progress.value = (trackPoints.value.filterIndexed { index, _ -> index <= firstPointIndex }.sumOf { it.distance } / totalLength.value).toFloat()
                getNeighbouringTrackPoints(currentLocation)
                //Accuracy may be low, since this code may be running while the user is in the "improve accuracy screen"
                //but this is a first approximation, more accurate results will be obtained when accuracy improves
                elaborateDirection(direction)
            }
            //Change UI screen state
            isNearTrack.value = isNearTrackValue
        }
    }

    // Elaborates the distance to the track based on the current location and the current track points
    fun elaboratePosition(currentLocation: Location) {
        position.value = currentLocation
        val neighbouringTrackPoints = getNeighbouringTrackPoints(currentLocation)
        if (neighbouringTrackPoints?.nextPoint == null) {
            //currentTrackPoint is the last point of the track
            //TODO()
        }
        //
        //TODO()

        // If the user is getting out of the track, return false
        onTrak.value = true
    }


    fun elaborateDirection(compassDirection: Float) {
        val threadSafePosition = position.value
        val threadSafeNextPoint = directionTrackPoint.value
        if (threadSafePosition == null || threadSafeNextPoint == null) {
            Log.w("TRACK_SCREEN_VIEW_MODEL", "Position is null, cannot calculate direction")
            return
        }
        Log.d("TRACK_SCREEN_VIEW_MODEL", "Calculating direction from position: " +
                "${threadSafePosition.latitude}  ${threadSafePosition.longitude} to next point: " +
                "${threadSafeNextPoint.latitude}  ${threadSafeNextPoint.longitude}")

        val lat1Rad = Math.toRadians(threadSafePosition.latitude)
        val lat2Rad = Math.toRadians(threadSafeNextPoint.latitude)
        val deltaLonRad = Math.toRadians(threadSafeNextPoint.longitude - threadSafePosition.longitude)

        val y = sin(deltaLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)

        val initialBearing = atan2(y, x)
        val targetBearing = (Math.toDegrees(initialBearing) + 360) % 360

        val angle = (compassDirection - targetBearing + 360) % 360
        Log.d("TRACK_SCREEN_VIEW_MODEL", "Angle: $angle")
        arrowDirection.value = angle.toFloat()
    }

    fun reset() {
        Log.d("TRACK_SCREEN_VIEW_MODEL", "Resetting track data")
        trackPoints.value = emptyList()
        totalLength.value = 0F
        parsingError.value = ""
        isNearTrack.value = null
        directionTrackPoint.value = null
        arrowDirection.value = 0F
        position.value = null
    }


    private fun getNeighbouringTrackPoints(currentLocation: Location): NeighbouringTrackPoints? {
        //TODO()
        directionTrackPoint.value = trackPoints.value[0]
        return NeighbouringTrackPoints(trackPoints.value[0], trackPoints.value[1])
    }
}
