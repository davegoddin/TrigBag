package net.davegoddin.trigbag.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.google.android.gms.maps.model.LatLngBounds
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.Visit


@Dao
interface TrigPointDao {
    @Query("SELECT * FROM TrigPoint" +
            " LEFT JOIN Visit on TrigPoint.id = Visit.id")
    suspend fun getAll(): Map<TrigPoint, List<Visit>>

    @Query("SELECT * FROM TrigPoint" +
            " LEFT JOIN Visit on TrigPoint.id = Visit.id" +
            " LIMIT :number")
    suspend fun getRows(number: Int): Map<TrigPoint, List<Visit>>

    @Query("SELECT * FROM TrigPoint" +
            " LEFT JOIN Visit on TrigPoint.id = Visit.id" +
            " WHERE TrigPoint.id = :id")
    suspend fun getById(id: Int) : Map<TrigPoint, List<Visit>>

    @Query("SELECT * FROM TrigPoint" +
            " LEFT JOIN Visit on TrigPoint.id = Visit.id" +
            " WHERE name LIKE :searchTerm OR country LIKE :searchTerm")
    suspend fun search(searchTerm: String) : Map<TrigPoint, List<Visit>>

    @Query("SELECT * FROM TrigPoint" +
            " LEFT JOIN Visit on TrigPoint.id = Visit.id" +
            " WHERE latitude <= :neLat AND latitude >= :swLat AND longitude <= :neLong AND longitude >= :swLong AND condition != 'Moved'")
    suspend fun getWithinBounds(neLat: Double, neLong: Double, swLat: Double, swLong: Double) : Map<TrigPoint, List<Visit>>

    @Insert
    suspend fun insertAll(vararg points: TrigPoint)
}