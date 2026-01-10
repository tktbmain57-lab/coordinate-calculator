package jp.dev.tanaka.coordinatecalculator.ui.parameter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.ui.ToolPathViewModel
import jp.dev.tanaka.coordinatecalculator.ui.direction.CornerDirection
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ParameterInputFragment : Fragment() {

    private val viewModel: ToolPathViewModel by activityViewModels {
        ToolPathViewModel.Factory((requireActivity().application as CoordinateCalculatorApp).repository)
    }

    private lateinit var inputCornerX: TextInputEditText
    private lateinit var inputCornerY: TextInputEditText
    private lateinit var inputAngle: TextInputEditText
    private lateinit var inputApproachDistance: TextInputEditText
    private lateinit var btnAngle30: MaterialButton
    private lateinit var btnAngle45: MaterialButton
    private lateinit var btnAngle60: MaterialButton
    private lateinit var directionImage: ImageView
    private lateinit var directionLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_parameter_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View の初期化
        inputCornerX = view.findViewById(R.id.inputCornerX)
        inputCornerY = view.findViewById(R.id.inputCornerY)
        inputAngle = view.findViewById(R.id.inputAngle)
        inputApproachDistance = view.findViewById(R.id.inputApproachDistance)
        btnAngle30 = view.findViewById(R.id.btnAngle30)
        btnAngle45 = view.findViewById(R.id.btnAngle45)
        btnAngle60 = view.findViewById(R.id.btnAngle60)
        directionImage = view.findViewById(R.id.directionImage)
        directionLabel = view.findViewById(R.id.directionLabel)

        // プリセット角度ボタン
        btnAngle30.setOnClickListener { setAngle(30.0) }
        btnAngle45.setOnClickListener { setAngle(45.0) }
        btnAngle60.setOnClickListener { setAngle(60.0) }

        // 計算ボタン
        view.findViewById<MaterialButton>(R.id.btnCalculate).setOnClickListener {
            calculate()
        }

        // 選択された向きを表示
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDirection.collectLatest { direction ->
                direction?.let { updateDirectionDisplay(it) }
            }
        }
    }

    private fun updateDirectionDisplay(direction: CornerDirection) {
        directionImage.setImageResource(direction.iconRes)
        directionLabel.setText(direction.labelRes)
    }

    private fun setAngle(angle: Double) {
        inputAngle.setText(angle.toInt().toString())
        updateAngleButtonStates(angle)
    }

    private fun updateAngleButtonStates(selectedAngle: Double) {
        btnAngle30.isChecked = selectedAngle == 30.0
        btnAngle45.isChecked = selectedAngle == 45.0
        btnAngle60.isChecked = selectedAngle == 60.0
    }

    private fun calculate() {
        val direction = viewModel.selectedDirection.value
        if (direction == null) {
            Toast.makeText(requireContext(), R.string.error_no_direction, Toast.LENGTH_SHORT).show()
            return
        }

        // 入力値の取得と検証
        val cornerX = inputCornerX.text.toString().toDoubleOrNull()
        val cornerY = inputCornerY.text.toString().toDoubleOrNull()
        val angle = inputAngle.text.toString().toDoubleOrNull()
        val approachDistance = inputApproachDistance.text.toString().toDoubleOrNull()

        if (cornerX == null || cornerY == null) {
            Toast.makeText(requireContext(), R.string.error_invalid_coordinate, Toast.LENGTH_SHORT).show()
            return
        }

        if (angle == null || angle <= 0 || angle >= 90) {
            Toast.makeText(requireContext(), R.string.error_invalid_angle, Toast.LENGTH_SHORT).show()
            return
        }

        if (approachDistance == null || approachDistance <= 0) {
            Toast.makeText(requireContext(), R.string.error_invalid_approach, Toast.LENGTH_SHORT).show()
            return
        }

        // パラメータを作成して計算実行
        val params = ChamferParameters(
            cornerX = cornerX,
            cornerY = cornerY,
            chamferAngle = angle,
            approachDistance = approachDistance,
            direction = direction
        )

        viewModel.calculate(params)

        // 結果画面へ遷移
        findNavController().navigate(R.id.action_parameter_to_result)
    }
}
