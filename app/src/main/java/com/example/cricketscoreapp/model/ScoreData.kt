package com.example.cricketscoreapp.model

data class ScoreData(
    val id: String? = null,
    val firstInningRuns: Int,
    val firstInningWickets: Int,
    val secondInningRuns: Int,
    val secondInningWickets: Int,
    val overs: Int,
    val balls: Int,
    val scoreHistory: String, // Comma-separated values
    val secondScoreHistory: String, // Comma-separated values
    val userId: String,
    val matchId: String
)
