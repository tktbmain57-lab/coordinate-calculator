package jp.dev.tanaka.coordinatecalculator.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.data.HistoryEntity
import jp.dev.tanaka.coordinatecalculator.ui.result.ChamferResult
import jp.dev.tanaka.coordinatecalculator.ui.scenario.Scenario
import jp.dev.tanaka.coordinatecalculator.util.RoundingUtil
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    var decimalPlaces: Int,
    private val onDeleteClick: (HistoryEntity) -> Unit
) : ListAdapter<HistoryEntity, HistoryAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.history_title)
        private val resultView: TextView = itemView.findViewById(R.id.history_result)
        private val memoView: TextView = itemView.findViewById(R.id.history_memo)
        private val dateView: TextView = itemView.findViewById(R.id.history_date)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(history: HistoryEntity) {
            titleView.text = history.title

            // シナリオタイプに応じて結果を表示
            val resultText = try {
                if (history.resultJson.isNotEmpty()) {
                    val json = JSONObject(history.resultJson)
                    val x = RoundingUtil.format(json.getDouble("approachX"), decimalPlaces)
                    val y = RoundingUtil.format(json.getDouble("approachY"), decimalPlaces)
                    "進入点: X=$x, Y=$y"
                } else {
                    "結果なし"
                }
            } catch (e: Exception) {
                "結果なし"
            }
            resultView.text = resultText

            // メモ
            if (history.memo.isNotEmpty()) {
                memoView.text = history.memo
                memoView.visibility = View.VISIBLE
            } else {
                memoView.visibility = View.GONE
            }

            // 日時
            dateView.text = dateFormat.format(Date(history.createdAt))

            // 削除ボタン
            deleteButton.setOnClickListener {
                onDeleteClick(history)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryEntity>() {
        override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
