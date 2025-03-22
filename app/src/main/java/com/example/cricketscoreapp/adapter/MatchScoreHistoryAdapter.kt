package com.example.cricketscoreapp.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.model.CombinedScoreData
import com.example.cricketscoreapp.model.ScoreData
import com.example.cricketscoreapp.network.RetrofitClient
import com.example.cricketscoreapp.utils.ViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchScoreHistoryAdapter(
    private val matchScoreList: MutableList<CombinedScoreData>,
    private val onItemClick: (CombinedScoreData) -> Unit
) : RecyclerView.Adapter<MatchScoreHistoryAdapter.MatchScoreViewHolder>() {

    class MatchScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMatchTitle: TextView = itemView.findViewById(R.id.tv_match_title)
        val tvTotalScore: TextView = itemView.findViewById(R.id.tv_match_score)
        val tvTotalSecondInningScore: TextView = itemView.findViewById(R.id.tv_match_second_inning_score)
        val tvTotalOvers: TextView = itemView.findViewById(R.id.tv_match_overs)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_match)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_score_history, parent, false)
        return MatchScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchScoreViewHolder, position: Int) {
        val match = matchScoreList[holder.adapterPosition]  // Use holder.adapterPosition dynamically

        // Determine which team batted first
        val battingFirstTeam = if (match.matchDetails!!.tossChoice == "Batting") {
            match.matchDetails!!.tossWinner // Toss-winning team batted first
        } else {
            // Toss-winning team chose to bowl, so the other team batted first
            if (match.matchDetails!!.tossWinner == match.matchDetails?.team1) {
                match.matchDetails?.team2 // Other team batted first
            } else {
                match.matchDetails?.team1 // Other team batted first
            }
        }

        holder.tvMatchTitle.text = "${match.matchDetails.team1} VS ${match.matchDetails.team2}"
        //ViewUtils.applyBounceAnimation(holder.tvMatchTitle)
       // holder.tvMatchTitle.text = "Match ID: ${match.matchDetails?.team1}"
        holder.tvTotalScore.text = "${match.scoreData.firstInningRuns}/${match.scoreData.firstInningWickets}"
        holder.tvTotalSecondInningScore.text = "${match.scoreData.secondInningRuns}/${match.scoreData.secondInningWickets}"
        holder.tvTotalOvers.text = "${match.scoreData.overs}.${match.scoreData.balls}"

        // Determine the winning team and apply bounce animation
        when {
            match.scoreData.firstInningRuns > match.scoreData.secondInningRuns -> {
                ViewUtils.applyBounceAnimation(holder.tvTotalScore) // Apply bounce to first inning score
            }
            match.scoreData.secondInningRuns > match.scoreData.firstInningRuns -> {
                ViewUtils.applyBounceAnimation(holder.tvTotalSecondInningScore) // Apply bounce to second inning score
            }
            else -> {
               ViewUtils.applyTieAnimation(holder.tvTotalScore)
               ViewUtils.applyTieAnimation(holder.tvTotalSecondInningScore)
            }
        }

        holder.itemView.setOnClickListener {
            it.animate().apply {
                scaleX(0.95f)
                scaleY(0.95f)
                duration = 100
                withEndAction {
                    scaleX(1f)
                    scaleY(1f)
                    onItemClick(match)  // Trigger click event
                }
            }
        }

        holder.btnDelete.setOnClickListener {
            val actualPosition = holder.adapterPosition
            if (actualPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            Log.d("DELETE", "Attempting to delete match with ID: $match.matchDetails?.id")

            RetrofitClient.apiService.deleteScore(match.matchDetails?.id ?: "").enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        matchScoreList.removeAt(actualPosition)

                        if (matchScoreList.isEmpty()) {
                            notifyDataSetChanged() // Force full refresh when list is empty
                        } else {
                            notifyItemRemoved(actualPosition)
                            notifyItemRangeChanged(actualPosition, matchScoreList.size)
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(holder.itemView.context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    override fun getItemCount(): Int = matchScoreList.size
}
