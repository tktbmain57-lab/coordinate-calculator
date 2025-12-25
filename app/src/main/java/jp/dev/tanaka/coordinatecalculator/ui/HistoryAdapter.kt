package jp.dev.tanaka.coordinatecalculator.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.dev.tanaka.coordinatecalculator.data.HistoryEntity
import jp.dev.tanaka.coordinatecalculator.data.ResultSerializer
import jp.dev.tanaka.coordinatecalculator.databinding.ItemHistoryBinding
import jp.dev.tanaka.coordinatecalculator.util.RoundingUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    var decimalPlaces: Int,
    private val onDeleteClick: (HistoryEntity) -> Unit
) : ListAdapter<HistoryEntity, HistoryAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(history: HistoryEntity) {
            binding.historyTitle.text = history.title

            // Parse and display result points
            val results = ResultSerializer.fromJson(history.resultPointsJson)
            val resultText = results.joinToString("\n") { result ->
                val rounded = RoundingUtil.roundPoint(result.point, decimalPlaces)
                rounded.format(decimalPlaces)
            }
            binding.historyResult.text = resultText

            // Memo
            if (history.memo.isNotEmpty()) {
                binding.historyMemo.text = history.memo
                binding.historyMemo.visibility = View.VISIBLE
            } else {
                binding.historyMemo.visibility = View.GONE
            }

            // Date
            binding.historyDate.text = dateFormat.format(Date(history.createdAt))

            // Delete button
            binding.btnDelete.setOnClickListener {
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
