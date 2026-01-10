package jp.dev.tanaka.coordinatecalculator.ui.parameter

import jp.dev.tanaka.coordinatecalculator.ui.direction.CornerDirection
import org.json.JSONObject

/**
 * 面取り加工のパラメータ
 */
data class ChamferParameters(
    /** 角の座標 X (mm) */
    val cornerX: Double,
    /** 角の座標 Y (mm) */
    val cornerY: Double,
    /** 面取り角度 (度) */
    val chamferAngle: Double,
    /** 進入距離 (mm) */
    val approachDistance: Double,
    /** 角の向き */
    val direction: CornerDirection
) {
    /** JSONシリアライズ */
    fun toJson(): JSONObject = JSONObject().apply {
        put("cornerX", cornerX)
        put("cornerY", cornerY)
        put("chamferAngle", chamferAngle)
        put("approachDistance", approachDistance)
        put("direction", direction.id)
    }

    companion object {
        /** デフォルトの進入距離 */
        const val DEFAULT_APPROACH_DISTANCE = 2.0

        /** プリセット角度 */
        val PRESET_ANGLES = listOf(30.0, 45.0, 60.0)

        /** JSONからデシリアライズ */
        fun fromJson(json: JSONObject): ChamferParameters {
            return ChamferParameters(
                cornerX = json.getDouble("cornerX"),
                cornerY = json.getDouble("cornerY"),
                chamferAngle = json.getDouble("chamferAngle"),
                approachDistance = json.getDouble("approachDistance"),
                direction = CornerDirection.fromId(json.getString("direction"))
                    ?: CornerDirection.TOP_LEFT
            )
        }
    }
}
