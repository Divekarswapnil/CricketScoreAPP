package com.example.cricketscoreapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.model.CombinedScoreData
import com.example.cricketscoreapp.model.MatchDetails
import com.example.cricketscoreapp.model.ScoreData
import com.example.cricketscoreapp.network.RetrofitClient
import com.example.cricketscoreapp.ui.adapters.MatchScoreHistoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScoreHistoryFragment : Fragment() {

    private lateinit var rvScoreHistory: RecyclerView
    private lateinit var matchScoreHistoryAdapter: MatchScoreHistoryAdapter
        private val matchScoreList = mutableListOf<CombinedScoreData>()
    private lateinit var semiTransparentView: View
    private lateinit var loadingAnimationView: LottieAnimationView
    private lateinit var imageviewEmpty : ImageView
    private lateinit var emptyMessage : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_score_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        semiTransparentView = view.findViewById(R.id.semiTransparentView)
        loadingAnimationView = view.findViewById(R.id.loadingAnimation)
        imageviewEmpty = view.findViewById(R.id.iv_empty)
        emptyMessage = view.findViewById(R.id.tv_empty_message)

        rvScoreHistory = view.findViewById(R.id.rv_score_history)
        rvScoreHistory.layoutManager = LinearLayoutManager(requireContext())

        matchScoreHistoryAdapter = MatchScoreHistoryAdapter(matchScoreList) { selectedMatch ->
            val matchDetailFragment = MatchDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("MATCH_ID", selectedMatch.matchDetails?.id)
                    putString("TOTAL_SCORE", "${selectedMatch.scoreData.firstInningRuns}/${selectedMatch.scoreData.firstInningWickets}")
                    putString("TOTAL_OVERS", "${selectedMatch.scoreData.overs}.${selectedMatch.scoreData.balls}")
                    putString("TOTAL_SECOND_INNING_SCORE", "${selectedMatch.scoreData.secondInningRuns}/${selectedMatch.scoreData.firstInningWickets}")
                    putStringArrayList("SCORE_HISTORY", ArrayList(selectedMatch.scoreData.scoreHistory.split(",")))
                    putStringArrayList("SECOND_INNING_SCORE_HISTORY", ArrayList(selectedMatch.scoreData.secondScoreHistory.split(",")))
                    putString("TEAM1", selectedMatch.matchDetails?.team1 ?: "N/A")
                    putString("TEAM2", selectedMatch.matchDetails?.team2 ?: "N/A")
                    putString("DATE", selectedMatch.matchDetails?.createdAt ?: "N/A")

                }
            }

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, matchDetailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvScoreHistory.adapter = matchScoreHistoryAdapter
        fetchScoresFromApi()
    }

    private fun fetchScoresFromApi() {
        val userId = getUserIdFromSession()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading()

        RetrofitClient.apiService.getScores().enqueue(object : Callback<List<ScoreData>> {
            override fun onResponse(
                call: Call<List<ScoreData>>,
                response: Response<List<ScoreData>>
            ) {
                if (response.isSuccessful) {
                    val scores = response.body()?.filter { it.userId == userId } ?: emptyList()
                    val matchIds = scores.map { it.matchId }.distinct()
                    fetchMatchDetails(matchIds) { matchDetailsList ->
                        combineData(scores, matchDetailsList)
                        hideLoading()
                    }
                } else {
                    hideLoading()
                    Toast.makeText(context, "Failed to fetch scores", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ScoreData>>, t: Throwable) {
                hideLoading()
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchMatchDetails(matchIds: List<String>, onComplete: (List<MatchDetails>) -> Unit) {
        if (matchIds.isEmpty()) {
            onComplete(emptyList())
            return
        }

        RetrofitClient.apiService.getMatchDetails().enqueue(object : Callback<List<MatchDetails>> {
            override fun onResponse(call: Call<List<MatchDetails>>, response: Response<List<MatchDetails>>) {
                if (response.isSuccessful) {
                    val allMatches = response.body() ?: emptyList()
                    val relevantMatches = allMatches.filter { matchIds.contains(it.matchId) }
                    onComplete(relevantMatches)
                } else {
                    onComplete(emptyList())
                }
            }

            override fun onFailure(call: Call<List<MatchDetails>>, t: Throwable) {
                onComplete(emptyList())
            }
        })
    }

    private fun combineData(scores: List<ScoreData>, matches: List<MatchDetails>) {
        val combinedList = scores.map { score ->
            CombinedScoreData(
                scoreData = score,
                matchDetails = matches.find { it.matchId == score.matchId }
            )
        }

        matchScoreList.clear()
        matchScoreList.addAll(combinedList)
        matchScoreHistoryAdapter.notifyDataSetChanged()
        toggleEmptyState()
    }

    private fun toggleEmptyState() {
        if (matchScoreList.isEmpty()) {
            imageviewEmpty.visibility = View.VISIBLE
            emptyMessage.visibility = View.VISIBLE
            rvScoreHistory.visibility = View.GONE
        } else {
            imageviewEmpty.visibility = View.GONE
            emptyMessage.visibility = View.GONE
            rvScoreHistory.visibility = View.VISIBLE
        }
    }


    private fun showLoading() {
        semiTransparentView.visibility = View.VISIBLE
        loadingAnimationView.visibility = View.VISIBLE
        loadingAnimationView.playAnimation()
    }

    private fun hideLoading() {
        loadingAnimationView.cancelAnimation()
        semiTransparentView.visibility = View.GONE
        loadingAnimationView.visibility = View.GONE
    }


    private fun getUserIdFromSession(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }

}
