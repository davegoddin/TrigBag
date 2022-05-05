package net.davegoddin.trigbag.data

import androidx.room.Dao
import androidx.room.Insert
import net.davegoddin.trigbag.model.TrigPoint

@Dao
interface VisitDao {

    @Insert
    suspend fun insertAll(vararg points: TrigPoint)
}