package com.example.cricketscoreapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketscoreapp.R

class ScoreHistoryAdapter(private val scoreHistoryList: List<String>) :
    RecyclerView.Adapter<ScoreHistoryAdapter.ScoreHistoryViewHolder>() {

    class ScoreHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHistoryItem: TextView = itemView.findViewById(R.id.tv_history_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score_history, parent, false)
        return ScoreHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreHistoryViewHolder, position: Int) {
        holder.tvHistoryItem.text = scoreHistoryList[position]
    }

    override fun getItemCount(): Int = scoreHistoryList.size
}


