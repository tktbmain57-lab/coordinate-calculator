package jp.dev.tanaka.coordinatecalculator.ui.result

import jp.dev.tanaka.coordinatecalculator.util.Point
import org.json.JSONObject

/**
 * 面取り計算の結果
 */
data class ChamferResult(
    /** 進入開始点 */
    val approachPoint: Point,
    /** 面取り開始点（基準点） */
    val cornerPoint: Point,
    /** 面取り角度（度） */
    val chamferAngle: Double,
    /** 進入距離（mm） */
    val approachDistance: Double
) {
    /** JSONシリアライズ */
    fun toJson(): JSONObject = JSONObject().apply {
        put("approachX", approachPoint.x)
        put("approachY", approachPoint.y)
        put("cornerX", cornerPoint.x)
        put("cornerY", cornerPoint.y)
        put("chamferAngle", chamferAngle)
        put("approachDistance", approachDistance)
    }

    companion object {
        /** JSONからデシリアライズ */
        fun fromJson(json: JSONObject): ChamferResult {
            return ChamferResult(
                approachPoint = Point(
                    json.getDouble("approachX"),
                    json.getDouble("approachY")
                ),
                cornerPoint = Point(
                    json.getDouble("cornerX"),
                    json.getDouble("cornerY")
                ),
                chamferAngle = json.getDouble("chamferAngle"),
                approachDistance = json.getDouble("approachDistance")
            )
        }
    }
}
