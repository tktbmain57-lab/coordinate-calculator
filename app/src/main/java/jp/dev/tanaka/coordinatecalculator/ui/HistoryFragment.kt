package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private val viewModel: ToolPathViewModel by activityViewModels {
        ToolPathViewModel.Factory((requireActivity().application as CoordinateCalculatorApp).repository)
    }

    private lateinit var adapter: HistoryAdapter
    private lateinit var historyList: RecyclerView
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyList = view.findViewById(R.id.history_list)
        emptyText = view.findViewById(R.id.empty_text)

        adapter = HistoryAdapter(
            decimalPlaces = 2,
            onDeleteClick = { history ->
                showDeleteConfirmation(history.id)
            }
        )

        historyList.layoutManager = LinearLayoutManager(requireContext())
        historyList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                viewModel.allHistory,
                viewModel.settings
            ) { history, settings ->
                Pair(history, settings.decimalPlaces)
            }.collectLatest { (historyData, decimalPlaces) ->
                adapter.decimalPlaces = decimalPlaces
                adapter.submitList(historyData)
                emptyText.visibility = if (historyData.isEmpty()) View.VISIBLE else View.GONE
                historyList.visibility = if (historyData.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showDeleteConfirmation(id: Long) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteHistory(id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
