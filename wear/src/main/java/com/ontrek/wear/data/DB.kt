package com.ontrek.wear.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "tracks")
data class Track (
    @PrimaryKey val id: Int,
    val title: String,
    val filename: String = "$id.gpx",
    val uploadedAt: Long,
    val size: Long,  // size in Bytes
    var downloadedAt: Long,
)

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY downloadedAt DESC")
    suspend fun getAllTracks(): List<Track>

     @Insert
     suspend fun insertTrack(track: Track)

     @Delete
     suspend fun deleteTrack(track: Track)
}

@Database(entities = [Track::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
