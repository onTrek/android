package com.ontrek.wear.screens.track

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class TrackScreenViewModel : ViewModel() {
    private val _gpxData = MutableStateFlow<Gpx?>(null)
    val gpxData: StateFlow<Gpx?> = _gpxData

    fun loadGpx(context: Context, fileName: String) {
        val parser = GPXParser()
        viewModelScope.launch {
            try {
                val gpxFile = context.openFileInput(fileName)
                val parsedGpx: Gpx? = parser.parse(gpxFile)
                parsedGpx?.let {
                    Log.d("TrackScreenViewModel", "GPX file parsed successfully: ${it.metadata?.name}")
                    _gpxData.value = parsedGpx
                } ?: {
                    Log.e("TrackScreenViewModel", "Generic GPX parsing error")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }
        }
    }
}
