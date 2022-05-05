package net.davegoddin.trigbag.model

import android.media.Image
import androidx.room.BuiltInTypeConverters
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime
import java.util.*
@Entity
data class Visit(
    @PrimaryKey
    val id: Long,
    val dateTime: Int?,
    val rating: Int?,
    val comment: String?
)
