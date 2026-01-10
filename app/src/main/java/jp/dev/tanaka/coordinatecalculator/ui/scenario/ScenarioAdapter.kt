package jp.dev.tanaka.coordinatecalculator.ui.scenario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.dev.tanaka.coordinatecalculator.R

class ScenarioAdapter(
    private val onScenarioClick: (Scenario) -> Unit
) : ListAdapter<Scenario, ScenarioAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scenario_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scenario = getItem(position)
        holder.bind(scenario)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.scenarioIcon)
        private val titleView: TextView = itemView.findViewById(R.id.scenarioTitle)
        private val descriptionView: TextView = itemView.findViewById(R.id.scenarioDescription)
        private val comingSoonLabel: TextView = itemView.findViewById(R.id.comingSoonLabel)

        fun bind(scenario: Scenario) {
            iconView.setImageResource(scenario.iconRes)
            titleView.setText(scenario.titleRes)
            descriptionView.setText(scenario.descriptionRes)

            if (scenario.enabled) {
                itemView.alpha = 1.0f
                comingSoonLabel.visibility = View.GONE
                itemView.setOnClickListener { onScenarioClick(scenario) }
            } else {
                itemView.alpha = 0.5f
                comingSoonLabel.visibility = View.VISIBLE
                itemView.setOnClickListener(null)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Scenario>() {
        override fun areItemsTheSame(oldItem: Scenario, newItem: Scenario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Scenario, newItem: Scenario): Boolean {
            return oldItem == newItem
        }
    }
}
