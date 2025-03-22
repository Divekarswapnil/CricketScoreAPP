package com.example.cricketscoreapp.network

import com.example.cricketscoreapp.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("users")
    fun registerUser(@Body user: User): Call<User>

    @GET("users")
    fun loginUser(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<List<User>> // MockAPI returns a list

    @GET("users/{id}")
    fun getUserById(@retrofit2.http.Path("id") id: String): Call<User>

    @GET("users")
    fun getAllUsers(): Call<List<User>>  // Required to map userId → id

}
