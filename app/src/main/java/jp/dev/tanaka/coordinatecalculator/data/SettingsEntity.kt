package jp.dev.tanaka.coordinatecalculator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val decimalPlaces: Int = 2  // 1-3
)
