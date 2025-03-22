package com.example.cricketscoreapp.model

data class MatchDetails(
    val id: String? = null,
    val matchId: String,
    val scoreHistory: String,
    val team1: String,
    val team2: String,
    val overs: Int,
    val tossWinner: String,
    val tossChoice: String,
    val createdAt: String = "" // API will handle timestamp
)

