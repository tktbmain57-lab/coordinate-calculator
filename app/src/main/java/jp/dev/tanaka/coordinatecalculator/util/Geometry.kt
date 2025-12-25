package jp.dev.tanaka.coordinatecalculator.util

import kotlin.math.*

/**
 * 2D座標を表すデータクラス
 */
data class Point(val x: Double, val y: Double) {
    fun distanceTo(other: Point): Double {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }

    fun format(decimalPlaces: Int): String {
        val format = "%.${decimalPlaces}f"
        return "(${format.format(x)}, ${format.format(y)})"
    }
}

/**
 * 直線を表すクラス（ax + by + c = 0 の形式）
 */
data class Line(val a: Double, val b: Double, val c: Double) {

    companion object {
        private const val EPSILON = 1e-10

        /**
         * 2点から直線を生成
         */
        fun fromTwoPoints(p1: Point, p2: Point): Line? {
            if (p1.distanceTo(p2) < EPSILON) return null
            val a = p2.y - p1.y
            val b = p1.x - p2.x
            val c = -a * p1.x - b * p1.y
            return Line(a, b, c)
        }

        /**
         * 1点と角度（度数法）から直線を生成
         */
        fun fromPointAndAngle(point: Point, angleDegrees: Double): Line {
            val angleRad = Math.toRadians(angleDegrees)
            // 方向ベクトル (cos, sin)
            val dx = cos(angleRad)
            val dy = sin(angleRad)
            // 法線ベクトル (-sin, cos) -> a = -sin, b = cos
            val a = -dy
            val b = dx
            val c = -a * point.x - b * point.y
            return Line(a, b, c)
        }
    }

    /**
     * 直線が平行かどうか
     */
    fun isParallelTo(other: Line): Boolean {
        return abs(a * other.b - b * other.a) < EPSILON
    }
}

/**
 * 円を表すクラス
 */
data class Circle(val center: Point, val radius: Double) {
    init {
        require(radius > 0) { "半径は正の値である必要があります" }
    }
}

/**
 * 交点の種類
 */
enum class IntersectionType {
    INTERSECTION,  // 通常の交点
    TANGENT        // 接点
}

/**
 * 交点結果
 */
data class IntersectionResult(
    val point: Point,
    val type: IntersectionType
)

/**
 * 交差計算の結果
 */
sealed class CalculationResult {
    data class Success(val points: List<IntersectionResult>) : CalculationResult()
    data class NoIntersection(val reason: String) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}

/**
 * 交差計算ユーティリティ
 */
object IntersectionCalculator {
    private const val EPSILON = 1e-10

    /**
     * 直線と直線の交点
     */
    fun lineLineIntersection(line1: Line, line2: Line): CalculationResult {
        val det = line1.a * line2.b - line2.a * line1.b

        if (abs(det) < EPSILON) {
            return CalculationResult.NoIntersection("直線が平行です")
        }

        val x = (line1.b * line2.c - line2.b * line1.c) / det
        val y = (line2.a * line1.c - line1.a * line2.c) / det

        return CalculationResult.Success(
            listOf(IntersectionResult(Point(x, y), IntersectionType.INTERSECTION))
        )
    }

    /**
     * 直線と円の交点
     */
    fun lineCircleIntersection(line: Line, circle: Circle): CalculationResult {
        // 直線: ax + by + c = 0
        // 円: (x - cx)^2 + (y - cy)^2 = r^2

        val a = line.a
        val b = line.b
        val c = line.c
        val cx = circle.center.x
        val cy = circle.center.y
        val r = circle.radius

        // 円の中心から直線までの距離
        val denom = sqrt(a * a + b * b)
        val distance = abs(a * cx + b * cy + c) / denom

        if (distance > r + EPSILON) {
            return CalculationResult.NoIntersection("交点がありません")
        }

        // 直線上で円の中心に最も近い点
        val t = -(a * cx + b * cy + c) / (a * a + b * b)
        val nearestX = cx + a * t
        val nearestY = cy + b * t

        if (abs(distance - r) < EPSILON) {
            // 接する場合
            return CalculationResult.Success(
                listOf(IntersectionResult(Point(nearestX, nearestY), IntersectionType.TANGENT))
            )
        }

        // 2点で交わる場合
        val halfChord = sqrt(r * r - distance * distance)
        val dx = b / denom * halfChord
        val dy = -a / denom * halfChord

        val p1 = Point(nearestX + dx, nearestY + dy)
        val p2 = Point(nearestX - dx, nearestY - dy)

        return CalculationResult.Success(
            listOf(
                IntersectionResult(p1, IntersectionType.INTERSECTION),
                IntersectionResult(p2, IntersectionType.INTERSECTION)
            )
        )
    }

    /**
     * 円と円の交点
     */
    fun circleCircleIntersection(circle1: Circle, circle2: Circle): CalculationResult {
        val c1 = circle1.center
        val c2 = circle2.center
        val r1 = circle1.radius
        val r2 = circle2.radius

        val d = c1.distanceTo(c2)

        // 同心円
        if (d < EPSILON) {
            return if (abs(r1 - r2) < EPSILON) {
                CalculationResult.NoIntersection("同一の円です")
            } else {
                CalculationResult.NoIntersection("同心円で交点がありません")
            }
        }

        // 離れすぎている
        if (d > r1 + r2 + EPSILON) {
            return CalculationResult.NoIntersection("円が離れていて交点がありません")
        }

        // 一方が他方の内側にある
        if (d < abs(r1 - r2) - EPSILON) {
            return CalculationResult.NoIntersection("円が内包されていて交点がありません")
        }

        // 外接または内接
        val isTangent = abs(d - (r1 + r2)) < EPSILON || abs(d - abs(r1 - r2)) < EPSILON

        // 交点の計算
        val a = (r1 * r1 - r2 * r2 + d * d) / (2 * d)
        val h2 = r1 * r1 - a * a

        // 中心を結ぶ線上で、c1からaの距離にある点
        val px = c1.x + a * (c2.x - c1.x) / d
        val py = c1.y + a * (c2.y - c1.y) / d

        if (isTangent || h2 < EPSILON) {
            return CalculationResult.Success(
                listOf(IntersectionResult(Point(px, py), IntersectionType.TANGENT))
            )
        }

        val h = sqrt(h2)
        val dx = h * (c2.y - c1.y) / d
        val dy = h * (c2.x - c1.x) / d

        val p1 = Point(px + dx, py - dy)
        val p2 = Point(px - dx, py + dy)

        return CalculationResult.Success(
            listOf(
                IntersectionResult(p1, IntersectionType.INTERSECTION),
                IntersectionResult(p2, IntersectionType.INTERSECTION)
            )
        )
    }
}

/**
 * 数値の丸めユーティリティ
 */
object RoundingUtil {
    fun round(value: Double, decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return (value * factor).roundToLong() / factor
    }

    fun roundPoint(point: Point, decimalPlaces: Int): Point {
        return Point(
            round(point.x, decimalPlaces),
            round(point.y, decimalPlaces)
        )
    }
}
