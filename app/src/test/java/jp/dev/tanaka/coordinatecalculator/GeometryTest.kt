package jp.dev.tanaka.coordinatecalculator

import jp.dev.tanaka.coordinatecalculator.util.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class GeometryTest {

    private val DELTA = 0.0001

    @Test
    fun `line from two points - basic`() {
        val p1 = Point(0.0, 0.0)
        val p2 = Point(10.0, 10.0)
        val line = Line.fromTwoPoints(p1, p2)

        assertNotNull(line)
    }

    @Test
    fun `line from two points - same point returns null`() {
        val p1 = Point(5.0, 5.0)
        val p2 = Point(5.0, 5.0)
        val line = Line.fromTwoPoints(p1, p2)

        assertNull(line)
    }

    @Test
    fun `line line intersection - perpendicular`() {
        // Horizontal line y = 5
        val line1 = Line.fromTwoPoints(Point(0.0, 5.0), Point(10.0, 5.0))!!
        // Vertical line x = 3
        val line2 = Line.fromTwoPoints(Point(3.0, 0.0), Point(3.0, 10.0))!!

        val result = IntersectionCalculator.lineLineIntersection(line1, line2)

        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(1, success.points.size)
        assertEquals(3.0, success.points[0].point.x, DELTA)
        assertEquals(5.0, success.points[0].point.y, DELTA)
    }

    @Test
    fun `line line intersection - parallel lines`() {
        // y = x
        val line1 = Line.fromTwoPoints(Point(0.0, 0.0), Point(1.0, 1.0))!!
        // y = x + 5
        val line2 = Line.fromTwoPoints(Point(0.0, 5.0), Point(1.0, 6.0))!!

        val result = IntersectionCalculator.lineLineIntersection(line1, line2)

        assertTrue(result is CalculationResult.NoIntersection)
    }

    @Test
    fun `line circle intersection - two points`() {
        // Horizontal line y = 0
        val line = Line.fromTwoPoints(Point(-10.0, 0.0), Point(10.0, 0.0))!!
        // Circle centered at origin with radius 5
        val circle = Circle(Point(0.0, 0.0), 5.0)

        val result = IntersectionCalculator.lineCircleIntersection(line, circle)

        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(2, success.points.size)
        // Points should be at (-5, 0) and (5, 0)
        assertTrue(success.points.any { abs(it.point.x - 5.0) < DELTA && abs(it.point.y) < DELTA })
        assertTrue(success.points.any { abs(it.point.x + 5.0) < DELTA && abs(it.point.y) < DELTA })
    }

    @Test
    fun `line circle intersection - tangent`() {
        // Horizontal line y = 5
        val line = Line.fromTwoPoints(Point(-10.0, 5.0), Point(10.0, 5.0))!!
        // Circle centered at origin with radius 5
        val circle = Circle(Point(0.0, 0.0), 5.0)

        val result = IntersectionCalculator.lineCircleIntersection(line, circle)

        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(1, success.points.size)
        assertEquals(IntersectionType.TANGENT, success.points[0].type)
        assertEquals(0.0, success.points[0].point.x, DELTA)
        assertEquals(5.0, success.points[0].point.y, DELTA)
    }

    @Test
    fun `line circle intersection - no intersection`() {
        // Horizontal line y = 10
        val line = Line.fromTwoPoints(Point(-10.0, 10.0), Point(10.0, 10.0))!!
        // Circle centered at origin with radius 5
        val circle = Circle(Point(0.0, 0.0), 5.0)

        val result = IntersectionCalculator.lineCircleIntersection(line, circle)

        assertTrue(result is CalculationResult.NoIntersection)
    }

    @Test
    fun `circle circle intersection - two points`() {
        // Two circles that intersect at two points
        val circle1 = Circle(Point(0.0, 0.0), 5.0)
        val circle2 = Circle(Point(6.0, 0.0), 5.0)

        val result = IntersectionCalculator.circleCircleIntersection(circle1, circle2)

        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(2, success.points.size)
    }

    @Test
    fun `circle circle intersection - external tangent`() {
        // Two circles that touch externally
        val circle1 = Circle(Point(0.0, 0.0), 5.0)
        val circle2 = Circle(Point(10.0, 0.0), 5.0)

        val result = IntersectionCalculator.circleCircleIntersection(circle1, circle2)

        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(1, success.points.size)
        assertEquals(IntersectionType.TANGENT, success.points[0].type)
        assertEquals(5.0, success.points[0].point.x, DELTA)
        assertEquals(0.0, success.points[0].point.y, DELTA)
    }

    @Test
    fun `circle circle intersection - internal tangent`() {
        // Two circles that touch internally
        val circle1 = Circle(Point(0.0, 0.0), 10.0)
        val circle2 = Circle(Point(5.0, 0.0), 5.0)

        val result = IntersectionCalculator.circleCircleIntersection(circle1, circle2)

        assertTrue(result is CalculationResult.Success)
        val success = result as CalculationResult.Success
        assertEquals(1, success.points.size)
        assertEquals(IntersectionType.TANGENT, success.points[0].type)
    }

    @Test
    fun `circle circle intersection - no intersection (too far apart)`() {
        val circle1 = Circle(Point(0.0, 0.0), 5.0)
        val circle2 = Circle(Point(20.0, 0.0), 5.0)

        val result = IntersectionCalculator.circleCircleIntersection(circle1, circle2)

        assertTrue(result is CalculationResult.NoIntersection)
    }

    @Test
    fun `circle circle intersection - no intersection (one inside other)`() {
        val circle1 = Circle(Point(0.0, 0.0), 10.0)
        val circle2 = Circle(Point(2.0, 0.0), 3.0)

        val result = IntersectionCalculator.circleCircleIntersection(circle1, circle2)

        assertTrue(result is CalculationResult.NoIntersection)
    }

    @Test
    fun `circle circle intersection - concentric circles`() {
        val circle1 = Circle(Point(0.0, 0.0), 5.0)
        val circle2 = Circle(Point(0.0, 0.0), 10.0)

        val result = IntersectionCalculator.circleCircleIntersection(circle1, circle2)

        assertTrue(result is CalculationResult.NoIntersection)
    }

    @Test
    fun `rounding utility - basic`() {
        assertEquals(1.23, RoundingUtil.round(1.234, 2), DELTA)
        assertEquals(1.235, RoundingUtil.round(1.2345, 3), DELTA)
        assertEquals(1.2, RoundingUtil.round(1.24, 1), DELTA)
    }

    @Test
    fun `line from point and angle`() {
        val point = Point(0.0, 0.0)
        val line = Line.fromPointAndAngle(point, 45.0)

        // Should create a line at 45 degrees
        assertNotNull(line)
    }
}
