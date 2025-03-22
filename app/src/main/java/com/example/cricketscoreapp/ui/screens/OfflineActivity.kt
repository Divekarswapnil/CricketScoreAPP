package com.example.cricketscoreapp.ui.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.utils.NetworkUtil

class OfflineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline)

        val btnRetry: Button = findViewById(R.id.btnRetry)

        btnRetry.setOnClickListener {
            if (NetworkUtil.isConnected(this)) {
                Log.d("NetworkCheck", "Internet Restored - Restarting HomepageActivity")

                val intent = Intent(this, HomepageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)

                finish()  // Close OfflineActivity
            } else {
                Log.d("NetworkCheck", "Still no Internet")
            }
        }
    }
}
