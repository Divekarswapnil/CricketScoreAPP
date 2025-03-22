package com.example.cricketscoreapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketscoreapp.R

class ScoreDetailAdapter(
    private var scoreHistory: List<String>,
    private var secondInningScoreHistory: List<String>
) : RecyclerView.Adapter<ScoreDetailAdapter.ScoreDetailViewHolder>() {

    class ScoreDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llFirstInning: LinearLayout = itemView.findViewById(R.id.ll_first_inning)
        val llSecondInning: LinearLayout = itemView.findViewById(R.id.ll_second_inning)
        val tvScoreDetail: TextView = itemView.findViewById(R.id.tv_score_detail)
        val tvSecondInningScoreDetail: TextView = itemView.findViewById(R.id.tv_second_inning_score_detail)
        val imgScoreIcon: ImageView = itemView.findViewById(R.id.img_score_icon)
        val imgSecondInningScoreIcon: ImageView = itemView.findViewById(R.id.img_second_inning_score_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreDetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score_detail, parent, false)
        return ScoreDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreDetailViewHolder, position: Int) {
        if (position < scoreHistory.size) {
            val score = scoreHistory[position]
            holder.tvScoreDetail.text = score
            holder.imgScoreIcon.setImageResource(getScoreIcon(score))
            holder.tvScoreDetail.visibility = View.VISIBLE
            holder.imgScoreIcon.visibility = View.VISIBLE
            holder.llFirstInning.visibility = View.VISIBLE
        } else {
            holder.tvScoreDetail.visibility = View.GONE
            holder.imgScoreIcon.visibility = View.GONE
            holder.llFirstInning.visibility = View.GONE
        }

        if (position < secondInningScoreHistory.size) {
            val secondInningScore = secondInningScoreHistory[position]
            holder.tvSecondInningScoreDetail.text = secondInningScore
            holder.imgSecondInningScoreIcon.setImageResource(getScoreIcon(secondInningScore))
            holder.tvSecondInningScoreDetail.visibility = View.VISIBLE
            holder.imgSecondInningScoreIcon.visibility = View.VISIBLE
            holder.llSecondInning.visibility = View.VISIBLE
        } else {
            holder.tvSecondInningScoreDetail.visibility = View.GONE
            holder.imgSecondInningScoreIcon.visibility = View.GONE
            holder.llSecondInning.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = maxOf(scoreHistory.size, secondInningScoreHistory.size)

    private fun getScoreIcon(score: String): Int {
        return when {
            score.contains("WB", ignoreCase = true) -> R.drawable.ic_wide_action
            score.contains("NB", ignoreCase = true) -> R.drawable.ic_black_ball
            score.contains("LB", ignoreCase = true) -> R.drawable.ic_leg_bye
            score.contains("Dot Ball", ignoreCase = true) -> R.drawable.ic_white_ball
            score.contains("W", ignoreCase = true) -> R.drawable.ic_red_ball
            score.contains(Regex("[1-6]")) -> R.drawable.ic_cricket_ball
            else -> R.drawable.ic_cricket_ball // Default icon
        }
    }

    fun updateScores(newScoreHistory: List<String>, newSecondInningScoreHistory: List<String>) {
        scoreHistory = newScoreHistory
        secondInningScoreHistory = newSecondInningScoreHistory
        notifyDataSetChanged()
    }
}
