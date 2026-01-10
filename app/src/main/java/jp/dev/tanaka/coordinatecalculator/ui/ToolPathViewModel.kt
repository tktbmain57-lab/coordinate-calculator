package jp.dev.tanaka.coordinatecalculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.dev.tanaka.coordinatecalculator.data.HistoryEntity
import jp.dev.tanaka.coordinatecalculator.data.Repository
import jp.dev.tanaka.coordinatecalculator.data.SettingsEntity
import jp.dev.tanaka.coordinatecalculator.ui.direction.CornerDirection
import jp.dev.tanaka.coordinatecalculator.ui.parameter.ChamferParameters
import jp.dev.tanaka.coordinatecalculator.ui.result.ChamferResult
import jp.dev.tanaka.coordinatecalculator.ui.scenario.Scenario
import jp.dev.tanaka.coordinatecalculator.util.ChamferCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ツールパス座標計算のViewModel
 */
class ToolPathViewModel(private val repository: Repository) : ViewModel() {

    // ========== シナリオ選択 ==========
    private val _selectedScenario = MutableStateFlow<Scenario?>(null)
    val selectedScenario: StateFlow<Scenario?> = _selectedScenario.asStateFlow()

    // ========== 向き選択 ==========
    private val _selectedDirection = MutableStateFlow<CornerDirection?>(null)
    val selectedDirection: StateFlow<CornerDirection?> = _selectedDirection.asStateFlow()

    // ========== パラメータ ==========
    private val _chamferParameters = MutableStateFlow<ChamferParameters?>(null)
    val chamferParameters: StateFlow<ChamferParameters?> = _chamferParameters.asStateFlow()

    // ========== 計算結果 ==========
    private val _calculationResult = MutableStateFlow<ChamferResult?>(null)
    val calculationResult: StateFlow<ChamferResult?> = _calculationResult.asStateFlow()

    // ========== 設定 ==========
    val settings: StateFlow<SettingsEntity> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        ) as StateFlow<SettingsEntity>

    // ========== 履歴 ==========
    val allHistory: Flow<List<HistoryEntity>> = repository.allHistory

    // ========== アクション ==========

    /**
     * シナリオを選択
     */
    fun selectScenario(scenario: Scenario) {
        _selectedScenario.value = scenario
        // 選択時に次のステップの状態をリセット
        _selectedDirection.value = null
        _chamferParameters.value = null
        _calculationResult.value = null
    }

    /**
     * 角の向きを選択
     */
    fun selectDirection(direction: CornerDirection) {
        _selectedDirection.value = direction
        // 選択時に次のステップの状態をリセット
        _chamferParameters.value = null
        _calculationResult.value = null
    }

    /**
     * 面取り計算を実行
     */
    fun calculate(params: ChamferParameters) {
        _chamferParameters.value = params

        val result = when (_selectedScenario.value) {
            is Scenario.OuterChamfer -> {
                ChamferCalculator.calculateOuterChamferApproach(params)
            }
            else -> null
        }

        _calculationResult.value = result
    }

    /**
     * 計算結果を履歴に保存
     */
    fun saveToHistory(title: String, memo: String = "") {
        val scenario = _selectedScenario.value ?: return
        val params = _chamferParameters.value ?: return
        val result = _calculationResult.value ?: return

        viewModelScope.launch {
            val history = HistoryEntity(
                scenarioType = scenario.id,
                title = title,
                memo = memo,
                inputsJson = params.toJson().toString(),
                resultJson = result.toJson().toString(),
                createdAt = System.currentTimeMillis()
            )
            repository.insertHistory(history)
        }
    }

    /**
     * 履歴を削除
     */
    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    /**
     * 全履歴を削除
     */
    fun deleteAllHistory() {
        viewModelScope.launch {
            repository.deleteAllHistory()
        }
    }

    /**
     * 設定を更新
     */
    fun updateDecimalPlaces(decimalPlaces: Int) {
        viewModelScope.launch {
            repository.updateSettings(SettingsEntity(decimalPlaces = decimalPlaces))
        }
    }

    /**
     * 状態をリセット（新しい計算を開始）
     */
    fun reset() {
        _selectedScenario.value = null
        _selectedDirection.value = null
        _chamferParameters.value = null
        _calculationResult.value = null
    }

    /**
     * 結果画面から戻る（シナリオ選択からやり直し）
     */
    fun resetToStart() {
        reset()
    }

    // ========== Factory ==========

    class Factory(private val repository: Repository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ToolPathViewModel::class.java)) {
                return ToolPathViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
