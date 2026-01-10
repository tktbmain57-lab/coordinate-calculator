package jp.dev.tanaka.coordinatecalculator.util

import jp.dev.tanaka.coordinatecalculator.ui.direction.CornerDirection
import jp.dev.tanaka.coordinatecalculator.ui.parameter.ChamferParameters
import jp.dev.tanaka.coordinatecalculator.ui.result.ChamferResult
import kotlin.math.cos
import kotlin.math.sin

/**
 * 面取り加工の座標計算
 */
object ChamferCalculator {

    /**
     * 外角面取りの進入点を計算する
     *
     * @param params 面取りパラメータ
     * @return 計算結果
     */
    fun calculateOuterChamferApproach(params: ChamferParameters): ChamferResult {
        val cornerPoint = Point(params.cornerX, params.cornerY)

        // 面取り方向の角度を計算（向きに応じて調整）
        val chamferDirectionAngle = calculateChamferDirectionAngle(
            params.chamferAngle,
            params.direction
        )

        // 進入方向 = 面取り方向の逆（面取りラインに沿って外側から内側へ進入）
        val approachAngleRad = Math.toRadians(chamferDirectionAngle + 180.0)

        // 進入開始点を計算
        val approachPoint = Point(
            x = cornerPoint.x + params.approachDistance * cos(approachAngleRad),
            y = cornerPoint.y + params.approachDistance * sin(approachAngleRad)
        )

        return ChamferResult(
            approachPoint = approachPoint,
            cornerPoint = cornerPoint,
            chamferAngle = params.chamferAngle,
            approachDistance = params.approachDistance
        )
    }

    /**
     * 角の向きと面取り角度から、実際の面取り方向の角度を計算
     *
     * 角の向きによって、面取りラインの方向が変わる:
     * - 左上 (┌──): 面取りは右下方向へ → 角度 = -(chamferAngle)
     * - 右上 (──┐): 面取りは左下方向へ → 角度 = 180 + chamferAngle
     * - 左下 (└──): 面取りは右上方向へ → 角度 = chamferAngle
     * - 右下 (──┘): 面取りは左上方向へ → 角度 = 180 - chamferAngle
     *
     * @param chamferAngle 面取り角度（度、0-90）
     * @param direction 角の向き
     * @return 面取り方向の角度（度、標準座標系で反時計回りが正）
     */
    private fun calculateChamferDirectionAngle(
        chamferAngle: Double,
        direction: CornerDirection
    ): Double {
        return when (direction) {
            // 左上の角: 面取りは右下方向（第4象限へ）
            CornerDirection.TOP_LEFT -> -chamferAngle

            // 右上の角: 面取りは左下方向（第3象限へ）
            CornerDirection.TOP_RIGHT -> 180.0 + chamferAngle

            // 左下の角: 面取りは右上方向（第1象限へ）
            CornerDirection.BOTTOM_LEFT -> chamferAngle

            // 右下の角: 面取りは左上方向（第2象限へ）
            CornerDirection.BOTTOM_RIGHT -> 180.0 - chamferAngle
        }
    }
}
