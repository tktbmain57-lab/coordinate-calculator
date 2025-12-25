package jp.dev.tanaka.coordinatecalculator.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.data.ShapeInput
import jp.dev.tanaka.coordinatecalculator.util.IntersectionResult
import jp.dev.tanaka.coordinatecalculator.util.Point
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CoordinateCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val gridPaint = Paint().apply {
        color = context.getColor(R.color.grid_color)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val axisPaint = Paint().apply {
        color = context.getColor(R.color.axis_color)
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val linePaint = Paint().apply {
        color = context.getColor(R.color.line_color)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val circlePaint = Paint().apply {
        color = context.getColor(R.color.circle_color)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        color = context.getColor(R.color.intersection_color)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val selectedPointPaint = Paint().apply {
        color = context.getColor(R.color.intersection_color)
        strokeWidth = 4f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 32f
        isAntiAlias = true
    }

    // Transform
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var baseScale = 1f  // mm to pixels

    // Data
    private var shapeA: ShapeInput? = null
    private var shapeB: ShapeInput? = null
    private var intersectionPoints: List<IntersectionResult> = emptyList()
    private var selectedPoint: IntersectionResult? = null

    // Gesture detection
    private var onPointClickListener: ((IntersectionResult) -> Unit)? = null

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = scale.coerceIn(0.1f, 10f)
            invalidate()
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            offsetX -= distanceX
            offsetY -= distanceY
            invalidate()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            handleTap(e.x, e.y)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            resetView()
            return true
        }
    })

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetView()
    }

    fun resetView() {
        scale = 1f
        offsetX = width / 2f
        offsetY = height / 2f
        baseScale = min(width, height) / 200f  // 200mm range initially
        invalidate()
    }

    fun setShapes(a: ShapeInput?, b: ShapeInput?) {
        shapeA = a
        shapeB = b
        invalidate()
    }

    fun setIntersectionPoints(points: List<IntersectionResult>) {
        intersectionPoints = points
        invalidate()
    }

    fun setSelectedPoint(point: IntersectionResult?) {
        selectedPoint = point
        invalidate()
    }

    fun setOnPointClickListener(listener: (IntersectionResult) -> Unit) {
        onPointClickListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun handleTap(x: Float, y: Float) {
        val worldPoint = screenToWorld(x, y)

        // Find closest intersection point within tap distance
        val tapRadius = 48f / (baseScale * scale)  // 48dp in world coords

        val closest = intersectionPoints.minByOrNull {
            val dx = it.point.x - worldPoint.x
            val dy = it.point.y - worldPoint.y
            dx * dx + dy * dy
        }

        if (closest != null) {
            val dx = closest.point.x - worldPoint.x
            val dy = closest.point.y - worldPoint.y
            if (dx * dx + dy * dy < tapRadius * tapRadius) {
                onPointClickListener?.invoke(closest)
            }
        }
    }

    private fun worldToScreen(wx: Double, wy: Double): PointF {
        val sx = offsetX + wx.toFloat() * baseScale * scale
        val sy = offsetY - wy.toFloat() * baseScale * scale  // Y flipped
        return PointF(sx, sy)
    }

    private fun screenToWorld(sx: Float, sy: Float): Point {
        val wx = (sx - offsetX) / (baseScale * scale)
        val wy = -(sy - offsetY) / (baseScale * scale)  // Y flipped
        return Point(wx.toDouble(), wy.toDouble())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawGrid(canvas)
        drawAxes(canvas)

        shapeA?.let { drawShape(canvas, it, linePaint) }
        shapeB?.let { drawShape(canvas, it, circlePaint) }

        drawIntersectionPoints(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        val gridSpacing = 10f * baseScale * scale  // 10mm grid
        if (gridSpacing < 20f) return  // Don't draw if too dense

        // Vertical lines
        var x = offsetX % gridSpacing
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSpacing
        }

        // Horizontal lines
        var y = offsetY % gridSpacing
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSpacing
        }
    }

    private fun drawAxes(canvas: Canvas) {
        // X axis
        canvas.drawLine(0f, offsetY, width.toFloat(), offsetY, axisPaint)
        // Y axis
        canvas.drawLine(offsetX, 0f, offsetX, height.toFloat(), axisPaint)

        // Origin label
        canvas.drawText("O", offsetX + 8, offsetY - 8, textPaint)
    }

    private fun drawShape(canvas: Canvas, shape: ShapeInput, paint: Paint) {
        when (shape) {
            is ShapeInput.LineTwoPoints -> {
                drawLineExtended(canvas, shape.p1, shape.p2, paint)
            }
            is ShapeInput.LinePointAngle -> {
                val rad = Math.toRadians(shape.angleDegrees)
                val dx = cos(rad) * 1000
                val dy = sin(rad) * 1000
                val p1 = Point(shape.point.x - dx, shape.point.y - dy)
                val p2 = Point(shape.point.x + dx, shape.point.y + dy)
                drawLineExtended(canvas, p1, p2, paint)
            }
            is ShapeInput.CircleInput -> {
                val center = worldToScreen(shape.center.x, shape.center.y)
                val radius = shape.radius.toFloat() * baseScale * scale
                canvas.drawCircle(center.x, center.y, radius, paint)
                // Draw center point
                canvas.drawCircle(center.x, center.y, 6f, pointPaint)
            }
        }
    }

    private fun drawLineExtended(canvas: Canvas, p1: Point, p2: Point, paint: Paint) {
        // Extend line beyond canvas
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        if (len < 0.0001) return

        val scale = 10000.0 / len
        val extP1 = Point(p1.x - dx * scale, p1.y - dy * scale)
        val extP2 = Point(p2.x + dx * scale, p2.y + dy * scale)

        val s1 = worldToScreen(extP1.x, extP1.y)
        val s2 = worldToScreen(extP2.x, extP2.y)
        canvas.drawLine(s1.x, s1.y, s2.x, s2.y, paint)
    }

    private fun drawIntersectionPoints(canvas: Canvas) {
        intersectionPoints.forEach { result ->
            val screen = worldToScreen(result.point.x, result.point.y)
            val isSelected = result == selectedPoint

            // Draw point
            canvas.drawCircle(screen.x, screen.y, if (isSelected) 16f else 12f, pointPaint)

            // Draw selection ring
            if (isSelected) {
                canvas.drawCircle(screen.x, screen.y, 24f, selectedPointPaint)
            }
        }
    }
}
