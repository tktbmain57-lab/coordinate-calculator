package jp.dev.tanaka.coordinatecalculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val memo: String,
    val inputsJson: String,
    val resultPointsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
