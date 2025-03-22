package com.example.cricketscoreapp.network

import com.example.cricketscoreapp.model.MatchDetails
import com.example.cricketscoreapp.model.ScoreData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ScoreApiService {
    @POST("scores") // Change "scores" to your MockAPI endpoint
    fun postScore(@Body scoreData: ScoreData): Call<ScoreData>

   /* @PATCH("scores/{id}") // Update score by ID
    fun updateSecondInningScore(
        @Path("id") id: String,
        @Body updatedData: Map<String, Any>
    ): Call<ScoreData>*/

    @PATCH("scores/{id}")
    fun updateSecondInningScore(
        @Path("id") id: String,
        @Body updatedData: @JvmSuppressWildcards Map<String, Any> // 🔥 Fix applied here
    ): Call<ScoreData>

    @GET("scores")
    fun getScores(): Call<List<ScoreData>>

    @DELETE("scores/{id}") // Ensure your backend supports this
    fun deleteScore(@Path("id") id: String): Call<Void>

    //match details api endpoints
    @GET("matchdetails")
    fun getMatchDetails(): Call<List<MatchDetails>>

    @GET("matchdetails/{id}")
    fun getMatchDetailsById(@Path("id") matchId: String): Call<MatchDetails>

    @POST("matchdetails")
    fun createMatch(@Body match: MatchDetails): Call<MatchDetails>

    @PUT("matchdetails/{id}")
    fun updateMatchDetails(
        @Path("id") id: String,
        @Body match: MatchDetails
    ): Call<MatchDetails>
}
