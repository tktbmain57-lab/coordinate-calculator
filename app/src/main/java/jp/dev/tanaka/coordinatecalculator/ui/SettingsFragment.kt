package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe decimal places
        viewModel.decimalPlaces.observe(viewLifecycleOwner) { places ->
            when (places) {
                1 -> binding.radioDecimal1.isChecked = true
                2 -> binding.radioDecimal2.isChecked = true
                3 -> binding.radioDecimal3.isChecked = true
            }
        }

        // Radio group listener
        binding.decimalPlacesGroup.setOnCheckedChangeListener { _, checkedId ->
            val places = when (checkedId) {
                R.id.radio_decimal_1 -> 1
                R.id.radio_decimal_2 -> 2
                R.id.radio_decimal_3 -> 3
                else -> 2
            }
            viewModel.updateDecimalPlaces(places)
        }

        // Delete all history button
        binding.btnDeleteAllHistory.setOnClickListener {
            showDeleteAllConfirmation()
        }
    }

    private fun showDeleteAllConfirmation() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.delete_all_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteAllHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
