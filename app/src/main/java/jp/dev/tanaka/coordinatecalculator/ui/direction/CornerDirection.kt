package jp.dev.tanaka.coordinatecalculator.ui.direction

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.dev.tanaka.coordinatecalculator.R

/**
 * 角の向き（4方向）
 */
enum class CornerDirection(
    val id: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val angleOffset: Double  // 計算時の角度オフセット（度）
) {
    /** 左上: ┌── */
    TOP_LEFT(
        id = "TOP_LEFT",
        labelRes = R.string.direction_top_left,
        iconRes = R.drawable.ic_corner_top_left,
        angleOffset = 0.0
    ),

    /** 右上: ──┐ */
    TOP_RIGHT(
        id = "TOP_RIGHT",
        labelRes = R.string.direction_top_right,
        iconRes = R.drawable.ic_corner_top_right,
        angleOffset = 90.0
    ),

    /** 左下: └── */
    BOTTOM_LEFT(
        id = "BOTTOM_LEFT",
        labelRes = R.string.direction_bottom_left,
        iconRes = R.drawable.ic_corner_bottom_left,
        angleOffset = 270.0
    ),

    /** 右下: ──┘ */
    BOTTOM_RIGHT(
        id = "BOTTOM_RIGHT",
        labelRes = R.string.direction_bottom_right,
        iconRes = R.drawable.ic_corner_bottom_right,
        angleOffset = 180.0
    );

    companion object {
        fun fromId(id: String): CornerDirection? = entries.find { it.id == id }
    }
}
