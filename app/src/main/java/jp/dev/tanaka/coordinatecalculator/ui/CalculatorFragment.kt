package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.data.ShapeInput
import jp.dev.tanaka.coordinatecalculator.data.ShapeType
import jp.dev.tanaka.coordinatecalculator.databinding.DialogSaveHistoryBinding
import jp.dev.tanaka.coordinatecalculator.databinding.FragmentCalculatorBinding
import jp.dev.tanaka.coordinatecalculator.databinding.InputShapeBinding
import jp.dev.tanaka.coordinatecalculator.util.CalculationResult
import jp.dev.tanaka.coordinatecalculator.util.IntersectionType
import jp.dev.tanaka.coordinatecalculator.util.Point
import jp.dev.tanaka.coordinatecalculator.util.RoundingUtil

class CalculatorFragment : Fragment() {
    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var shapeAInputBinding: InputShapeBinding
    private lateinit var shapeBInputBinding: InputShapeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        shapeAInputBinding = InputShapeBinding.bind(binding.shapeAInput.root)
        shapeBInputBinding = InputShapeBinding.bind(binding.shapeBInput.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupShapeTypeSelectors()
        setupButtons()
        setupCanvas()
        observeViewModel()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        showSaveDialog()
                        true
                    }
                    R.id.action_history -> {
                        findNavController().navigate(R.id.action_calculator_to_history)
                        true
                    }
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_calculator_to_settings)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupShapeTypeSelectors() {
        binding.shapeATypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val type = when (checkedId) {
                    R.id.btn_shape_a_line_2p -> ShapeType.LINE_TWO_POINTS
                    R.id.btn_shape_a_line_angle -> ShapeType.LINE_POINT_ANGLE
                    R.id.btn_shape_a_circle -> ShapeType.CIRCLE
                    else -> ShapeType.LINE_TWO_POINTS
                }
                viewModel.setShapeAType(type)
                updateInputVisibility(shapeAInputBinding, type)
            }
        }

        binding.shapeBTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val type = when (checkedId) {
                    R.id.btn_shape_b_line_2p -> ShapeType.LINE_TWO_POINTS
                    R.id.btn_shape_b_line_angle -> ShapeType.LINE_POINT_ANGLE
                    R.id.btn_shape_b_circle -> ShapeType.CIRCLE
                    else -> ShapeType.CIRCLE
                }
                viewModel.setShapeBType(type)
                updateInputVisibility(shapeBInputBinding, type)
            }
        }

        // Set initial selection
        binding.btnShapeALine2p.isChecked = true
        binding.btnShapeBCircle.isChecked = true
    }

    private fun updateInputVisibility(inputBinding: InputShapeBinding, type: ShapeType) {
        inputBinding.inputLineTwoPoints.visibility =
            if (type == ShapeType.LINE_TWO_POINTS) View.VISIBLE else View.GONE
        inputBinding.inputLinePointAngle.visibility =
            if (type == ShapeType.LINE_POINT_ANGLE) View.VISIBLE else View.GONE
        inputBinding.inputCircle.visibility =
            if (type == ShapeType.CIRCLE) View.VISIBLE else View.GONE
    }

    private fun setupButtons() {
        binding.btnCalculate.setOnClickListener {
            calculate()
        }

        binding.btnClear.setOnClickListener {
            clearAll()
        }
    }

    private fun setupCanvas() {
        binding.canvasView.setOnPointClickListener { point ->
            viewModel.selectPoint(point)
        }
    }

    private fun observeViewModel() {
        viewModel.shapeAType.observe(viewLifecycleOwner) { type ->
            updateInputVisibility(shapeAInputBinding, type)
        }

        viewModel.shapeBType.observe(viewLifecycleOwner) { type ->
            updateInputVisibility(shapeBInputBinding, type)
        }

        viewModel.calculationResult.observe(viewLifecycleOwner) { result ->
            updateResultDisplay(result)
        }

        viewModel.currentInputA.observe(viewLifecycleOwner) { shape ->
            binding.canvasView.setShapes(shape, viewModel.currentInputB.value)
        }

        viewModel.currentInputB.observe(viewLifecycleOwner) { shape ->
            binding.canvasView.setShapes(viewModel.currentInputA.value, shape)
        }

        viewModel.selectedPoint.observe(viewLifecycleOwner) { point ->
            binding.canvasView.setSelectedPoint(point)
            updateSelectedPointDisplay(point)
        }

        viewModel.decimalPlaces.observe(viewLifecycleOwner) { _ ->
            // Refresh display if result exists
            viewModel.calculationResult.value?.let { updateResultDisplay(it) }
        }
    }

    private fun calculate() {
        val shapeA = parseShapeInput(shapeAInputBinding, viewModel.shapeAType.value!!)
        val shapeB = parseShapeInput(shapeBInputBinding, viewModel.shapeBType.value!!)

        if (shapeA == null || shapeB == null) {
            Toast.makeText(requireContext(), R.string.error_invalid_input, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.calculate(shapeA, shapeB)
    }

    private fun parseShapeInput(inputBinding: InputShapeBinding, type: ShapeType): ShapeInput? {
        return try {
            when (type) {
                ShapeType.LINE_TWO_POINTS -> {
                    val p1x = inputBinding.inputP1X.text.toString().toDouble()
                    val p1y = inputBinding.inputP1Y.text.toString().toDouble()
                    val p2x = inputBinding.inputP2X.text.toString().toDouble()
                    val p2y = inputBinding.inputP2Y.text.toString().toDouble()

                    val p1 = Point(p1x, p1y)
                    val p2 = Point(p2x, p2y)

                    if (p1.distanceTo(p2) < 0.0001) {
                        Toast.makeText(requireContext(), R.string.error_same_points, Toast.LENGTH_SHORT).show()
                        return null
                    }

                    ShapeInput.LineTwoPoints(p1, p2)
                }
                ShapeType.LINE_POINT_ANGLE -> {
                    val px = inputBinding.inputAnglePX.text.toString().toDouble()
                    val py = inputBinding.inputAnglePY.text.toString().toDouble()
                    val angle = inputBinding.inputAngle.text.toString().toDouble()
                    ShapeInput.LinePointAngle(Point(px, py), angle)
                }
                ShapeType.CIRCLE -> {
                    val cx = inputBinding.inputCenterX.text.toString().toDouble()
                    val cy = inputBinding.inputCenterY.text.toString().toDouble()
                    val r = inputBinding.inputRadius.text.toString().toDouble()

                    if (r <= 0) {
                        Toast.makeText(requireContext(), R.string.error_zero_radius, Toast.LENGTH_SHORT).show()
                        return null
                    }

                    ShapeInput.CircleInput(Point(cx, cy), r)
                }
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun updateResultDisplay(result: CalculationResult?) {
        if (result == null) {
            binding.resultCard.visibility = View.GONE
            binding.canvasView.setIntersectionPoints(emptyList())
            return
        }

        binding.resultCard.visibility = View.VISIBLE
        val decimalPlaces = viewModel.decimalPlaces.value ?: 2

        when (result) {
            is CalculationResult.Success -> {
                binding.canvasView.setIntersectionPoints(result.points)

                if (result.points.size == 1) {
                    val point = result.points[0]
                    val rounded = RoundingUtil.roundPoint(point.point, decimalPlaces)
                    val typeLabel = if (point.type == IntersectionType.TANGENT)
                        getString(R.string.tangent_point) else getString(R.string.intersection_point)
                    binding.resultText.text = "$typeLabel: ${rounded.format(decimalPlaces)}"
                } else {
                    binding.resultText.text = getString(R.string.select_point)
                }
            }
            is CalculationResult.NoIntersection -> {
                binding.canvasView.setIntersectionPoints(emptyList())
                binding.resultText.text = result.reason
            }
            is CalculationResult.Error -> {
                binding.canvasView.setIntersectionPoints(emptyList())
                binding.resultText.text = result.message
            }
        }
    }

    private fun updateSelectedPointDisplay(point: jp.dev.tanaka.coordinatecalculator.util.IntersectionResult?) {
        if (point == null) return

        val decimalPlaces = viewModel.decimalPlaces.value ?: 2
        val rounded = RoundingUtil.roundPoint(point.point, decimalPlaces)
        val typeLabel = if (point.type == IntersectionType.TANGENT)
            getString(R.string.tangent_point) else getString(R.string.intersection_point)
        binding.resultText.text = "$typeLabel: ${rounded.format(decimalPlaces)}"
    }

    private fun clearAll() {
        viewModel.clearResult()

        // Clear input fields
        listOf(shapeAInputBinding, shapeBInputBinding).forEach { inputBinding ->
            inputBinding.inputP1X.text?.clear()
            inputBinding.inputP1Y.text?.clear()
            inputBinding.inputP2X.text?.clear()
            inputBinding.inputP2Y.text?.clear()
            inputBinding.inputAnglePX.text?.clear()
            inputBinding.inputAnglePY.text?.clear()
            inputBinding.inputAngle.text?.clear()
            inputBinding.inputCenterX.text?.clear()
            inputBinding.inputCenterY.text?.clear()
            inputBinding.inputRadius.text?.clear()
        }

        binding.canvasView.resetView()
    }

    private fun showSaveDialog() {
        val result = viewModel.calculationResult.value
        if (result !is CalculationResult.Success) {
            Toast.makeText(requireContext(), R.string.no_intersection, Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogSaveHistoryBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.save)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val title = dialogBinding.inputTitle.text.toString()
                val memo = dialogBinding.inputMemo.text.toString()
                viewModel.saveToHistory(title, memo)
                Toast.makeText(requireContext(), R.string.save, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
