package com.example.cricketscoreapp.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.cricketscoreapp.R

class HomepageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val btnStart: Button = findViewById(R.id.btn_start_scoring)

        btnStart.setOnClickListener {
            // Open Scoring Activity (Change to your scoring activity)
            val intent = Intent(this, ScoringActivity::class.java)
            startActivity(intent)
        }
    }
}
