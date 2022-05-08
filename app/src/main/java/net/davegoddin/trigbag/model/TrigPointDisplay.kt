package net.davegoddin.trigbag.model

import androidx.room.Embedded
import androidx.room.Relation

data class TrigPointDisplay(
    @Embedded val trigPoint: TrigPoint,
    @Relation(
        parentColumn = "id",
        entityColumn = "trigPointId"
    )
    val visits: MutableList<Visit>,
    var distance: Double?) {

}