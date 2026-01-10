package jp.dev.tanaka.coordinatecalculator.ui.scenario

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import jp.dev.tanaka.coordinatecalculator.R

/**
 * 加工シナリオの定義
 */
sealed class Scenario(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val enabled: Boolean = true
) {
    /** 外角面取り */
    data object OuterChamfer : Scenario(
        id = "CHAMFER_OUTER",
        titleRes = R.string.scenario_outer_chamfer_title,
        descriptionRes = R.string.scenario_outer_chamfer_desc,
        iconRes = R.drawable.ic_chamfer_outer,
        enabled = true
    )

    /** 内角面取り（将来実装） */
    data object InnerChamfer : Scenario(
        id = "CHAMFER_INNER",
        titleRes = R.string.scenario_inner_chamfer_title,
        descriptionRes = R.string.scenario_inner_chamfer_desc,
        iconRes = R.drawable.ic_chamfer_inner,
        enabled = false
    )

    /** 円弧進入（将来実装） */
    data object ArcApproach : Scenario(
        id = "ARC_APPROACH",
        titleRes = R.string.scenario_arc_approach_title,
        descriptionRes = R.string.scenario_arc_approach_desc,
        iconRes = R.drawable.ic_arc_approach,
        enabled = false
    )

    companion object {
        /** 全シナリオのリスト */
        val all: List<Scenario> = listOf(
            OuterChamfer,
            InnerChamfer,
            ArcApproach
        )

        /** IDからシナリオを取得 */
        fun fromId(id: String): Scenario? = all.find { it.id == id }
    }
}
