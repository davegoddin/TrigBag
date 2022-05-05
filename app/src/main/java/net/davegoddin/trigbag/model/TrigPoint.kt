package net.davegoddin.trigbag.model

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity
data class TrigPoint(
    @PrimaryKey val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val condition: String,
    val country: String
)
