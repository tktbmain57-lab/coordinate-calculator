package jp.dev.tanaka.coordinatecalculator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.data.*
import jp.dev.tanaka.coordinatecalculator.util.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as CoordinateCalculatorApp).repository

    // Settings
    val decimalPlaces: LiveData<Int> = repository.settings.map { it?.decimalPlaces ?: 2 }.asLiveData()

    // History
    val historyList: LiveData<List<HistoryEntity>> = repository.allHistory.asLiveData()

    // Current shape inputs
    private val _shapeAType = MutableLiveData(ShapeType.LINE_TWO_POINTS)
    val shapeAType: LiveData<ShapeType> = _shapeAType

    private val _shapeBType = MutableLiveData(ShapeType.CIRCLE)
    val shapeBType: LiveData<ShapeType> = _shapeBType

    // Calculation result
    private val _calculationResult = MutableLiveData<CalculationResult?>()
    val calculationResult: LiveData<CalculationResult?> = _calculationResult

    // Current inputs (for canvas drawing)
    private val _currentInputA = MutableLiveData<ShapeInput?>()
    val currentInputA: LiveData<ShapeInput?> = _currentInputA

    private val _currentInputB = MutableLiveData<ShapeInput?>()
    val currentInputB: LiveData<ShapeInput?> = _currentInputB

    // Selected intersection point
    private val _selectedPoint = MutableLiveData<IntersectionResult?>()
    val selectedPoint: LiveData<IntersectionResult?> = _selectedPoint

    fun setShapeAType(type: ShapeType) {
        _shapeAType.value = type
    }

    fun setShapeBType(type: ShapeType) {
        _shapeBType.value = type
    }

    fun calculate(shapeA: ShapeInput?, shapeB: ShapeInput?) {
        if (shapeA == null || shapeB == null) {
            _calculationResult.value = CalculationResult.Error("入力値が不正です")
            return
        }

        _currentInputA.value = shapeA
        _currentInputB.value = shapeB

        val result = when {
            // 直線 × 直線
            shapeA.toLine() != null && shapeB.toLine() != null -> {
                IntersectionCalculator.lineLineIntersection(shapeA.toLine()!!, shapeB.toLine()!!)
            }
            // 直線 × 円
            shapeA.toLine() != null && shapeB.toCircle() != null -> {
                IntersectionCalculator.lineCircleIntersection(shapeA.toLine()!!, shapeB.toCircle()!!)
            }
            // 円 × 直線
            shapeA.toCircle() != null && shapeB.toLine() != null -> {
                IntersectionCalculator.lineCircleIntersection(shapeB.toLine()!!, shapeA.toCircle()!!)
            }
            // 円 × 円
            shapeA.toCircle() != null && shapeB.toCircle() != null -> {
                IntersectionCalculator.circleCircleIntersection(shapeA.toCircle()!!, shapeB.toCircle()!!)
            }
            else -> CalculationResult.Error("計算できない組み合わせです")
        }

        _calculationResult.value = result

        // 結果が1つなら自動選択
        if (result is CalculationResult.Success && result.points.size == 1) {
            _selectedPoint.value = result.points[0]
        } else {
            _selectedPoint.value = null
        }
    }

    fun selectPoint(point: IntersectionResult) {
        _selectedPoint.value = point
    }

    fun clearResult() {
        _calculationResult.value = null
        _currentInputA.value = null
        _currentInputB.value = null
        _selectedPoint.value = null
    }

    fun saveToHistory(title: String, memo: String) {
        val inputA = _currentInputA.value ?: return
        val inputB = _currentInputB.value ?: return
        val result = _calculationResult.value as? CalculationResult.Success ?: return

        viewModelScope.launch {
            val history = HistoryEntity(
                title = title.ifEmpty { "計算結果" },
                memo = memo,
                inputsJson = CalculationInput(inputA, inputB).toJson(),
                resultPointsJson = ResultSerializer.toJson(result.points)
            )
            repository.insertHistory(history)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            repository.deleteAllHistory()
        }
    }

    fun updateDecimalPlaces(places: Int) {
        viewModelScope.launch {
            repository.updateSettings(SettingsEntity(decimalPlaces = places))
        }
    }
}
