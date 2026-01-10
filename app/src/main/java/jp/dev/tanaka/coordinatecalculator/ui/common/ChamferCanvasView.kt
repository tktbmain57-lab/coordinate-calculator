package jp.dev.tanaka.coordinatecalculator.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import jp.dev.tanaka.coordinatecalculator.ui.direction.CornerDirection
import jp.dev.tanaka.coordinatecalculator.util.Point
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * 面取り結果を表示するキャンバスView
 */
class ChamferCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // データ
    private var cornerPoint: Point? = null
    private var approachPoint: Point? = null
    private var chamferAngle: Double = 45.0
    private var direction: CornerDirection = CornerDirection.TOP_LEFT

    // 描画用Paint
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val axisPaint = Paint().apply {
        color = Color.parseColor("#9E9E9E")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val workpiecePaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val chamferLinePaint = Paint().apply {
        color = Color.parseColor("#FF9800")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val approachLinePaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val cornerPointPaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
    }

    private val approachPointPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#333333")
        textSize = 32f
        isAntiAlias = true
    }

    // 変換用パラメータ
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    fun setData(
        cornerPoint: Point,
        approachPoint: Point,
        chamferAngle: Double,
        direction: CornerDirection
    ) {
        this.cornerPoint = cornerPoint
        this.approachPoint = approachPoint
        this.chamferAngle = chamferAngle
        this.direction = direction
        calculateTransform()
        invalidate()
    }

    private fun calculateTransform() {
        val corner = cornerPoint ?: return
        val approach = approachPoint ?: return

        // 描画範囲を計算（余白を含む）
        val margin = 50.0
        val minX = min(corner.x, approach.x) - margin
        val maxX = max(corner.x, approach.x) + margin
        val minY = min(corner.y, approach.y) - margin
        val maxY = max(corner.y, approach.y) + margin

        val dataWidth = maxX - minX
        val dataHeight = maxY - minY

        // スケールを計算（アスペクト比を維持）
        val scaleX = width / dataWidth
        val scaleY = height / dataHeight
        scale = min(scaleX, scaleY).toFloat() * 0.8f

        // 中心にオフセット
        offsetX = width / 2f - ((minX + maxX) / 2 * scale).toFloat()
        offsetY = height / 2f + ((minY + maxY) / 2 * scale).toFloat()
    }

    private fun worldToScreen(point: Point): Pair<Float, Float> {
        val x = point.x * scale + offsetX
        val y = -point.y * scale + offsetY  // Y軸は反転
        return Pair(x.toFloat(), y.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateTransform()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val corner = cornerPoint ?: return
        val approach = approachPoint ?: return

        // グリッドを描画
        drawGrid(canvas)

        // ワークピース（角）を描画
        drawWorkpiece(canvas, corner)

        // 面取りラインを描画
        drawChamferLine(canvas, corner)

        // 進入ラインを描画
        drawApproachLine(canvas, corner, approach)

        // 点を描画
        drawPoints(canvas, corner, approach)

        // ラベルを描画
        drawLabels(canvas, corner, approach)
    }

    private fun drawGrid(canvas: Canvas) {
        val gridSpacing = 10f * scale
        if (gridSpacing < 20f) return  // グリッドが細かすぎる場合はスキップ

        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSpacing
        }

        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSpacing
        }
    }

    private fun drawWorkpiece(canvas: Canvas, corner: Point) {
        val (cx, cy) = worldToScreen(corner)
        val length = 80f * scale

        val path = Path()

        when (direction) {
            CornerDirection.TOP_LEFT -> {
                // ┌──
                path.moveTo(cx, cy + length)
                path.lineTo(cx, cy)
                path.lineTo(cx + length, cy)
            }
            CornerDirection.TOP_RIGHT -> {
                // ──┐
                path.moveTo(cx - length, cy)
                path.lineTo(cx, cy)
                path.lineTo(cx, cy + length)
            }
            CornerDirection.BOTTOM_LEFT -> {
                // └──
                path.moveTo(cx, cy - length)
                path.lineTo(cx, cy)
                path.lineTo(cx + length, cy)
            }
            CornerDirection.BOTTOM_RIGHT -> {
                // ──┘
                path.moveTo(cx - length, cy)
                path.lineTo(cx, cy)
                path.lineTo(cx, cy - length)
            }
        }

        canvas.drawPath(path, workpiecePaint)
    }

    private fun drawChamferLine(canvas: Canvas, corner: Point) {
        val (cx, cy) = worldToScreen(corner)
        val length = 60f * scale
        val angleRad = Math.toRadians(chamferAngle)

        val (endX, endY) = when (direction) {
            CornerDirection.TOP_LEFT -> {
                Pair(cx + (length * cos(angleRad)).toFloat(), cy + (length * sin(angleRad)).toFloat())
            }
            CornerDirection.TOP_RIGHT -> {
                Pair(cx - (length * cos(angleRad)).toFloat(), cy + (length * sin(angleRad)).toFloat())
            }
            CornerDirection.BOTTOM_LEFT -> {
                Pair(cx + (length * cos(angleRad)).toFloat(), cy - (length * sin(angleRad)).toFloat())
            }
            CornerDirection.BOTTOM_RIGHT -> {
                Pair(cx - (length * cos(angleRad)).toFloat(), cy - (length * sin(angleRad)).toFloat())
            }
        }

        canvas.drawLine(cx, cy, endX, endY, chamferLinePaint)
    }

    private fun drawApproachLine(canvas: Canvas, corner: Point, approach: Point) {
        val (cx, cy) = worldToScreen(corner)
        val (ax, ay) = worldToScreen(approach)
        canvas.drawLine(ax, ay, cx, cy, approachLinePaint)
    }

    private fun drawPoints(canvas: Canvas, corner: Point, approach: Point) {
        val (cx, cy) = worldToScreen(corner)
        val (ax, ay) = worldToScreen(approach)

        // 角の点
        canvas.drawCircle(cx, cy, 12f, cornerPointPaint)

        // 進入点
        canvas.drawCircle(ax, ay, 14f, approachPointPaint)
    }

    private fun drawLabels(canvas: Canvas, corner: Point, approach: Point) {
        val (cx, cy) = worldToScreen(corner)
        val (ax, ay) = worldToScreen(approach)

        // 角の点ラベル
        canvas.drawText("面取り点", cx + 20f, cy - 20f, textPaint)

        // 進入点ラベル
        canvas.drawText("進入点", ax + 20f, ay - 20f, textPaint)
    }
}
