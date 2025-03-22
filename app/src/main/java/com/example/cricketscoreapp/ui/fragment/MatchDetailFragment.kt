package com.example.cricketscoreapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.adapter.ScoreDetailAdapter
import com.example.cricketscoreapp.utils.SoundUtils
import com.example.cricketscoreapp.utils.ViewUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MatchDetailFragment : Fragment() {

    private lateinit var fab_sound: FloatingActionButton
    private var isSoundEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_match_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_sound = view.findViewById(R.id.fab_sound)
        val matchId = arguments?.getString("MATCH_ID") ?: "N/A"
        val totalScore = arguments?.getString("TOTAL_SCORE") ?: "0/0"
        val totalSecondInningScore = arguments?.getString("TOTAL_SECOND_INNING_SCORE") ?: "0/0"
        val team1 = arguments?.getString("TEAM1") ?: "0/0"
        val team2 = arguments?.getString("TEAM2") ?: "0/0"
        val totalOvers = arguments?.getString("TOTAL_OVERS") ?: "0.0"
        val scoreHistory = arguments?.getStringArrayList("SCORE_HISTORY") ?: arrayListOf()
        val secondInningScoreHistory = arguments?.getStringArrayList("SECOND_INNING_SCORE_HISTORY") ?: arrayListOf()


        view.findViewById<TextView>(R.id.tv_total_score_detail).text = "Total Score: $totalScore"
        view.findViewById<TextView>(R.id.tv_total_second_inning_score_detail).text = "Total Score: $totalSecondInningScore"
        view.findViewById<TextView>(R.id.tv_total_overs_detail).text = "Total Overs: $totalOvers"
        view.findViewById<TextView>(R.id.tv_team_a).text = team1
        view.findViewById<TextView>(R.id.tv_team_b).text = team2

        val rvScoreDetails: RecyclerView = view.findViewById(R.id.rv_score_details)
        rvScoreDetails.layoutManager = LinearLayoutManager(requireActivity())
        rvScoreDetails.adapter = ScoreDetailAdapter(scoreHistory,secondInningScoreHistory)

        // Determine match result
        val matchResult = getMatchResult(team1, totalScore, team2, totalSecondInningScore)
        view.findViewById<TextView>(R.id.tv_match_winner_detail).text = matchResult
        ViewUtils.applyBounceAnimation(view.findViewById<TextView>(R.id.tv_match_winner_detail))
        ViewUtils.applyBounceAnimation(view.findViewById<TextView>(R.id.tv_team_a))
        ViewUtils.applyBounceAnimation(view.findViewById<TextView>(R.id.tv_team_b))

        // Play sound initially
        onPlayBackgroundMusic()

        // Toggle button click listener
        fab_sound.setOnClickListener {
            isSoundEnabled = !isSoundEnabled // Toggle state

            if (isSoundEnabled) {
                onPlayBackgroundMusic()
                fab_sound.setImageResource(R.drawable.ic_volume)
            } else {
                SoundUtils.release()  // Stop the sound
                fab_sound.setImageResource(R.drawable.ic_volume_off)
            }
        }
    }

    private fun getMatchResult(team1: String, totalScore: String, team2: String, totalSecondInningScore: String): String {
        val team1Runs = totalScore.substringBefore("/").toIntOrNull() ?: 0
        val team2Runs = totalSecondInningScore.substringBefore("/").toIntOrNull() ?: 0

        return when {
            team1Runs > team2Runs -> {
                "$team1 Wins 🎉"
            }
            team2Runs > team1Runs -> "$team2 Wins 🎉"
            else -> "Match Tied 🤝"
        }
    }

    companion object {
        fun newInstance(matchId: String, totalScore: String, scoreHistory: ArrayList<String>): MatchDetailFragment {
            return MatchDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("MATCH_ID", matchId)
                    putString("TOTAL_SCORE", totalScore)
                    putString("TOTAL_OVERS", totalScore)
                    putStringArrayList("SCORE_HISTORY", scoreHistory)
                }
            }
        }
    }

    private fun onPlayBackgroundMusic() {
        if (isSoundEnabled) {
            SoundUtils.handleMatchResult(requireContext(), "bgm")
        }
    }

    override fun onPause() {
        super.onPause()
        SoundUtils.release() // Stop sound when fragment pauses
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SoundUtils.release() // Ensure sound stops when fragment is destroyed
    }


}
