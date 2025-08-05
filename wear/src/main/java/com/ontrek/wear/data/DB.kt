package com.ontrek.wear.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "tracks")
data class Track(
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

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Int): Track?

    @Insert
    suspend fun insertTrack(track: Track)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Int)
}

@Database(entities = [Track::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}

object DatabaseProvider {
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "ontrek-database"
            ).build()
            instance = db
            db
        }
    }
}
