package jp.dev.tanaka.coordinatecalculator.ui.direction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.ui.ToolPathViewModel

class DirectionSelectFragment : Fragment() {

    private val viewModel: ToolPathViewModel by activityViewModels {
        ToolPathViewModel.Factory((requireActivity().application as CoordinateCalculatorApp).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_direction_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDirectionCard(view, R.id.cardTopLeft, CornerDirection.TOP_LEFT)
        setupDirectionCard(view, R.id.cardTopRight, CornerDirection.TOP_RIGHT)
        setupDirectionCard(view, R.id.cardBottomLeft, CornerDirection.BOTTOM_LEFT)
        setupDirectionCard(view, R.id.cardBottomRight, CornerDirection.BOTTOM_RIGHT)
    }

    private fun setupDirectionCard(view: View, cardId: Int, direction: CornerDirection) {
        view.findViewById<MaterialCardView>(cardId).setOnClickListener {
            viewModel.selectDirection(direction)
            findNavController().navigate(R.id.action_direction_to_parameter)
        }
    }
}
