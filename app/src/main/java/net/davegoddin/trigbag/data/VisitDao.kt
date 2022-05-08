package net.davegoddin.trigbag.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.Visit

@Dao
interface VisitDao {

    @Query("SELECT COUNT(*) FROM Visit")
    suspend fun getTotalVisits() : Int

    @Insert
    suspend fun insert(visit: Visit)
}