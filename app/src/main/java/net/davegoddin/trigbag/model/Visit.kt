package net.davegoddin.trigbag.model

import android.media.Image
import androidx.room.*
import java.time.LocalDateTime
import java.util.*
@Entity
data class Visit(
    val dateTime: Long?,
    val trigPointId: Long,
    val rating: Float?,
    val comment: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}
