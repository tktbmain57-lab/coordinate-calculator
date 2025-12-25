package jp.dev.tanaka.coordinatecalculator.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val historyDao: HistoryDao,
    private val settingsDao: SettingsDao
) {
    // History
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun getHistoryById(id: Long): HistoryEntity? = historyDao.getHistoryById(id)

    suspend fun insertHistory(history: HistoryEntity): Long = historyDao.insert(history)

    suspend fun deleteHistory(id: Long) = historyDao.deleteById(id)

    suspend fun deleteAllHistory() = historyDao.deleteAll()

    // Settings
    val settings: Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun getSettingsSync(): SettingsEntity =
        settingsDao.getSettingsSync() ?: SettingsEntity()

    suspend fun updateSettings(settings: SettingsEntity) =
        settingsDao.insertOrUpdate(settings)
}
