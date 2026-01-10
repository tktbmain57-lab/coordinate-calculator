package jp.dev.tanaka.coordinatecalculator.ui.result

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.ui.ToolPathViewModel
import jp.dev.tanaka.coordinatecalculator.ui.common.ChamferCanvasView
import jp.dev.tanaka.coordinatecalculator.util.RoundingUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {

    private val viewModel: ToolPathViewModel by activityViewModels {
        ToolPathViewModel.Factory((requireActivity().application as CoordinateCalculatorApp).repository)
    }

    private lateinit var chamferCanvas: ChamferCanvasView
    private lateinit var resultX: TextView
    private lateinit var resultY: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chamferCanvas = view.findViewById(R.id.chamferCanvas)
        resultX = view.findViewById(R.id.resultX)
        resultY = view.findViewById(R.id.resultY)

        // コピーボタン
        view.findViewById<ImageButton>(R.id.btnCopy).setOnClickListener {
            copyResultToClipboard()
        }

        // 新しい計算ボタン
        view.findViewById<MaterialButton>(R.id.btnNewCalculation).setOnClickListener {
            viewModel.resetToStart()
            findNavController().popBackStack(R.id.scenarioSelectFragment, false)
        }

        // 保存ボタン
        view.findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            showSaveDialog()
        }

        // 結果の表示
        viewLifecycleOwner.lifecycleScope.launch {
            combine(
                viewModel.calculationResult,
                viewModel.settings,
                viewModel.chamferParameters,
                viewModel.selectedDirection
            ) { result, settings, params, direction ->
                Triple(result, settings.decimalPlaces, params to direction)
            }.collectLatest { (result, decimalPlaces, paramsAndDirection) ->
                result?.let {
                    val (params, direction) = paramsAndDirection
                    displayResult(it, decimalPlaces)

                    // キャンバスに描画データを設定
                    if (params != null && direction != null) {
                        chamferCanvas.setData(
                            cornerPoint = it.cornerPoint,
                            approachPoint = it.approachPoint,
                            chamferAngle = it.chamferAngle,
                            direction = direction
                        )
                    }
                }
            }
        }
    }

    private fun displayResult(result: ChamferResult, decimalPlaces: Int) {
        resultX.text = RoundingUtil.format(result.approachPoint.x, decimalPlaces)
        resultY.text = RoundingUtil.format(result.approachPoint.y, decimalPlaces)
    }

    private fun copyResultToClipboard() {
        val result = viewModel.calculationResult.value ?: return
        val decimalPlaces = viewModel.settings.value.decimalPlaces

        val text = "X: ${RoundingUtil.format(result.approachPoint.x, decimalPlaces)}, " +
                   "Y: ${RoundingUtil.format(result.approachPoint.y, decimalPlaces)}"

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("座標", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun showSaveDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_save_history, null)

        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.input_title)
        val memoInput = dialogView.findViewById<TextInputEditText>(R.id.input_memo)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_save_title)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_save) { _, _ ->
                val title = titleInput.text.toString().ifBlank {
                    getString(R.string.default_history_title)
                }
                val memo = memoInput.text.toString()
                viewModel.saveToHistory(title, memo)
                Toast.makeText(requireContext(), R.string.saved_to_history, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
}
