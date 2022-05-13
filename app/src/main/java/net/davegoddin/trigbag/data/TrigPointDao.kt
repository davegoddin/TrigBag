package net.davegoddin.trigbag.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.google.android.gms.maps.model.LatLngBounds
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.TrigPointDisplay
import net.davegoddin.trigbag.model.Visit


@Dao
interface TrigPointDao {

    @Transaction
    @Query("SELECT * FROM TrigPoint")
    suspend fun getAll() : List<TrigPointDisplay>

    @Transaction
    @Query("SELECT * FROM TrigPoint" +
            " WHERE latitude <= :neLat AND latitude >= :swLat AND longitude <= :neLong AND longitude >= :swLong AND condition != 'Moved' AND condition !='Destroyed' AND condition != 'Possibly missing'")
    suspend fun getWithinBounds(neLat: Double, neLong: Double, swLat: Double, swLong: Double) : List<TrigPointDisplay>

    @Transaction
    @Query ("SELECT DISTINCT * FROM TrigPoint LEFT JOIN (SELECT DISTINCT trigPointId FROM Visit) WHERE trigPointId = id")
    suspend fun getVisited() : List<TrigPointDisplay>


    @Insert
    suspend fun insertAll(vararg points: TrigPoint)
}