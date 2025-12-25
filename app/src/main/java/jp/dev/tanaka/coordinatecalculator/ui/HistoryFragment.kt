package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter(
            decimalPlaces = viewModel.decimalPlaces.value ?: 2,
            onDeleteClick = { history ->
                showDeleteConfirmation(history.id)
            }
        )

        binding.historyList.layoutManager = LinearLayoutManager(requireContext())
        binding.historyList.adapter = adapter

        viewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            adapter.submitList(historyList)
            binding.emptyText.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
            binding.historyList.visibility = if (historyList.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.decimalPlaces.observe(viewLifecycleOwner) { places ->
            adapter.decimalPlaces = places
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
