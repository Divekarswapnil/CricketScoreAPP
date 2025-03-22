package com.example.cricketscoreapp.ui.screens


import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.cricketscoreapp.R

private lateinit var imageLogo : ImageView
private lateinit var  loadingAnimation : LottieAnimationView

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        imageLogo = findViewById(R.id.iv_logo)
        loadingAnimation = findViewById(R.id.noBallAnimation)

        // Fade-in animation for logo
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 2000 // 2 seconds
        showloadingAnimationn()

        imageLogo.startAnimation(fadeIn)

        // Delay before checking session
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000) // 2-second delay
    }

    private fun checkUserSession() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)

        if (userId == null) {
            // No session found, go to LoginRegisterActivity
            startActivity(Intent(this, LoginRegisterActivity::class.java))
        } else {
            // User session exists, go to HomePageActivity
            startActivity(Intent(this, HomepageActivity::class.java))
        }

        finish() // Close SplashActivity
    }


    fun showloadingAnimationn(){
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()

        // Hide animation after it finishes
        loadingAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                loadingAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}