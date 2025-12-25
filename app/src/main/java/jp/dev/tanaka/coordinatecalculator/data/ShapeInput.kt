package jp.dev.tanaka.coordinatecalculator.data

import jp.dev.tanaka.coordinatecalculator.util.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 図形の入力タイプ
 */
enum class ShapeType {
    LINE_TWO_POINTS,    // 直線（2点指定）
    LINE_POINT_ANGLE,   // 直線（1点+角度）
    CIRCLE              // 円
}

/**
 * 図形の入力データ
 */
sealed class ShapeInput {
    abstract fun toLine(): Line?
    abstract fun toCircle(): Circle?
    abstract fun toJson(): JSONObject

    data class LineTwoPoints(
        val p1: Point,
        val p2: Point
    ) : ShapeInput() {
        override fun toLine(): Line? = Line.fromTwoPoints(p1, p2)
        override fun toCircle(): Circle? = null
        override fun toJson(): JSONObject = JSONObject().apply {
            put("type", "LINE_TWO_POINTS")
            put("p1", JSONArray(listOf(p1.x, p1.y)))
            put("p2", JSONArray(listOf(p2.x, p2.y)))
        }
    }

    data class LinePointAngle(
        val point: Point,
        val angleDegrees: Double
    ) : ShapeInput() {
        override fun toLine(): Line = Line.fromPointAndAngle(point, angleDegrees)
        override fun toCircle(): Circle? = null
        override fun toJson(): JSONObject = JSONObject().apply {
            put("type", "LINE_POINT_ANGLE")
            put("point", JSONArray(listOf(point.x, point.y)))
            put("angle", angleDegrees)
        }
    }

    data class CircleInput(
        val center: Point,
        val radius: Double
    ) : ShapeInput() {
        override fun toLine(): Line? = null
        override fun toCircle(): Circle = Circle(center, radius)
        override fun toJson(): JSONObject = JSONObject().apply {
            put("type", "CIRCLE")
            put("center", JSONArray(listOf(center.x, center.y)))
            put("radius", radius)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ShapeInput? {
            return when (json.optString("type")) {
                "LINE_TWO_POINTS" -> {
                    val p1Arr = json.getJSONArray("p1")
                    val p2Arr = json.getJSONArray("p2")
                    LineTwoPoints(
                        Point(p1Arr.getDouble(0), p1Arr.getDouble(1)),
                        Point(p2Arr.getDouble(0), p2Arr.getDouble(1))
                    )
                }
                "LINE_POINT_ANGLE" -> {
                    val pArr = json.getJSONArray("point")
                    LinePointAngle(
                        Point(pArr.getDouble(0), pArr.getDouble(1)),
                        json.getDouble("angle")
                    )
                }
                "CIRCLE" -> {
                    val cArr = json.getJSONArray("center")
                    CircleInput(
                        Point(cArr.getDouble(0), cArr.getDouble(1)),
                        json.getDouble("radius")
                    )
                }
                else -> null
            }
        }
    }
}

/**
 * 計算入力のペア
 */
data class CalculationInput(
    val shapeA: ShapeInput,
    val shapeB: ShapeInput
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("shapeA", shapeA.toJson())
            put("shapeB", shapeB.toJson())
        }.toString()
    }

    companion object {
        fun fromJson(jsonString: String): CalculationInput? {
            return try {
                val json = JSONObject(jsonString)
                val shapeA = ShapeInput.fromJson(json.getJSONObject("shapeA"))
                val shapeB = ShapeInput.fromJson(json.getJSONObject("shapeB"))
                if (shapeA != null && shapeB != null) {
                    CalculationInput(shapeA, shapeB)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * 結果のシリアライズ
 */
object ResultSerializer {
    fun toJson(results: List<IntersectionResult>): String {
        val arr = JSONArray()
        results.forEach { result ->
            arr.put(JSONObject().apply {
                put("x", result.point.x)
                put("y", result.point.y)
                put("type", result.type.name)
            })
        }
        return arr.toString()
    }

    fun fromJson(jsonString: String): List<IntersectionResult> {
        return try {
            val arr = JSONArray(jsonString)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                IntersectionResult(
                    Point(obj.getDouble("x"), obj.getDouble("y")),
                    IntersectionType.valueOf(obj.getString("type"))
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
