package jp.dev.tanaka.coordinatecalculator.ui.scenario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.dev.tanaka.coordinatecalculator.CoordinateCalculatorApp
import jp.dev.tanaka.coordinatecalculator.R
import jp.dev.tanaka.coordinatecalculator.ui.ToolPathViewModel

class ScenarioSelectFragment : Fragment() {

    private val viewModel: ToolPathViewModel by activityViewModels {
        ToolPathViewModel.Factory((requireActivity().application as CoordinateCalculatorApp).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scenario_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.scenarioRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val adapter = ScenarioAdapter { scenario ->
            if (scenario.enabled) {
                viewModel.selectScenario(scenario)
                findNavController().navigate(R.id.action_scenario_to_direction)
            }
        }
        recyclerView.adapter = adapter
        adapter.submitList(Scenario.all)
    }
}
