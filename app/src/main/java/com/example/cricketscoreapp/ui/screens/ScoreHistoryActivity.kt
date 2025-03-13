package com.example.cricketscoreapp.ui.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.ui.adapters.ScoreHistoryAdapter

class ScoreHistoryActivity : AppCompatActivity() {

    private lateinit var scoreHistoryAdapter: ScoreHistoryAdapter
    private lateinit var rvScoreHistory: RecyclerView
    private val scoreHistoryList = mutableListOf<String>()  // Dummy list to hold scores

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_history)

        rvScoreHistory = findViewById(R.id.rv_score_history)

        // Dummy Data for Testing
        scoreHistoryList.add("Score: 120/5")
        scoreHistoryList.add("Score: 98/3")
        scoreHistoryList.add("Score: 150/7")

        // Setup RecyclerView
        scoreHistoryAdapter = ScoreHistoryAdapter(scoreHistoryList)
        rvScoreHistory.layoutManager = LinearLayoutManager(this)
        rvScoreHistory.adapter = scoreHistoryAdapter
    }
}
