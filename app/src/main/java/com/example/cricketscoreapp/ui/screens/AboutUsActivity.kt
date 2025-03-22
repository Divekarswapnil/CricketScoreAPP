package com.example.cricketscoreapp.ui.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cricketscoreapp.R

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        supportActionBar?.title = "About Us"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Back button
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
