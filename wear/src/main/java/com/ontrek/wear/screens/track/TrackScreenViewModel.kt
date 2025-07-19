package com.ontrek.wear.screens.track

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.data.TrackPoint
import com.ontrek.wear.utils.functions.getDistanceTo
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class TrackScreenViewModel : ViewModel() {

    private val trackPoints = MutableStateFlow(listOf<TrackPoint>())
    val trackPointListState: StateFlow<List<TrackPoint>> = trackPoints
    private val parsingError = MutableStateFlow<String>("")
    val parsingErrorState: StateFlow<String> = parsingError
    private val totalLength = MutableStateFlow(0.0)
    val totalLengthState: StateFlow<Double> = totalLength

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
                                        getDistanceTo(point, segment.trackPoints[index - 1])
                                    } else {
                                        0.0
                                    }
                                    TrackPoint(
                                        latitude = point.latitude,
                                        longitude = point.longitude,
                                        altitude = point.elevation,
                                        distance = distance
                                    )
                                }
                            }
                        }
                    totalLength.value = trackPoints.value.sumOf { it.distance }
                    Log.d("TrackScreenViewModel", "Track Lenght $totalLengthState")
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
}
