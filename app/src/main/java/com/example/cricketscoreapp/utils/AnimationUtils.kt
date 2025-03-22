package com.example.cricketscoreapp.utils

import android.animation.Animator
import android.view.View
import com.airbnb.lottie.LottieAnimationView

object AnimationUtils {

    fun playAnimation(animationView: LottieAnimationView) {
        animationView.visibility = View.VISIBLE
        animationView.playAnimation()

        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                animationView.visibility = View.GONE
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun showWideAnimation(wideAnimation: LottieAnimationView) {
        playAnimation(wideAnimation)
    }

    fun showWicketAnimation(wicketAnimation: LottieAnimationView) {
        playAnimation(wicketAnimation)
    }

    fun showFourAnimation(fourAnimation: LottieAnimationView) {
        playAnimation(fourAnimation)
    }

    fun showSixAnimation(sixAnimation: LottieAnimationView) {
        playAnimation(sixAnimation)
    }

    fun showRunCelebrationAnimation(runCelebrationAnimation: LottieAnimationView) {
        playAnimation(runCelebrationAnimation)
    }
}
