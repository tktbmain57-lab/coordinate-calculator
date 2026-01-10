package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val viewModel: ToolPathViewModel by activityViewModels {
        ToolPathViewModel.Factory((requireActivity().application as CoordinateCalculatorApp).repository)
    }

    private lateinit var decimalPlacesGroup: RadioGroup
    private lateinit var radioDecimal1: RadioButton
    private lateinit var radioDecimal2: RadioButton
    private lateinit var radioDecimal3: RadioButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        decimalPlacesGroup = view.findViewById(R.id.decimal_places_group)
        radioDecimal1 = view.findViewById(R.id.radio_decimal_1)
        radioDecimal2 = view.findViewById(R.id.radio_decimal_2)
        radioDecimal3 = view.findViewById(R.id.radio_decimal_3)

        // 設定を監視
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collectLatest { settings ->
                when (settings.decimalPlaces) {
                    1 -> radioDecimal1.isChecked = true
                    2 -> radioDecimal2.isChecked = true
                    3 -> radioDecimal3.isChecked = true
                }
            }
        }

        // ラジオグループのリスナー
        decimalPlacesGroup.setOnCheckedChangeListener { _, checkedId ->
            val places = when (checkedId) {
                R.id.radio_decimal_1 -> 1
                R.id.radio_decimal_2 -> 2
                R.id.radio_decimal_3 -> 3
                else -> 2
            }
            viewModel.updateDecimalPlaces(places)
        }

        // 全履歴削除ボタン
        view.findViewById<MaterialButton>(R.id.btn_delete_all_history).setOnClickListener {
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
}
