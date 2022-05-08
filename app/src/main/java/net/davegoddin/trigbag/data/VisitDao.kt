package net.davegoddin.trigbag.data

import androidx.room.Dao
import androidx.room.Insert
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.Visit

@Dao
interface VisitDao {

    @Insert
    suspend fun insert(visit: Visit)
}