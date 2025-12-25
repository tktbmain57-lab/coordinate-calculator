package jp.dev.tanaka.coordinatecalculator

import android.app.Application
import jp.dev.tanaka.coordinatecalculator.data.AppDatabase
import jp.dev.tanaka.coordinatecalculator.data.Repository

class CoordinateCalculatorApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { Repository(database.historyDao(), database.settingsDao()) }
}
