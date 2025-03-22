package com.example.cricketscoreapp.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import com.example.cricketscoreapp.R

object ViewUtils {

    /**
     * Applies a continuous bounce animation (zoom in & out) to any View.
     * @param view The target View to animate
     */
    fun applyBounceAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.5f, 1f)

        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE

        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatMode = ValueAnimator.REVERSE

        scaleX.duration = 1500 // Speed of bounce
        scaleY.duration = 1500

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.start()
    }

    /**
     * Applies a bounce animation using XML resource (Alternative method).
     * @param view The target View to animate
     * @param context The context to fetch the animation resource
     */
    fun applyBounceAnimationXML(view: View, context: Context) {
        val bounceAnimation: Animation = AnimationUtils.loadAnimation(context, R.anim.bounce)
        view.startAnimation(bounceAnimation)
    }

    /**
     * Applies a glowing fade animation for tied scores.
     * @param view The target View to animate
     */
    fun applyTieAnimation(view: View) {
        val fadeInOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.5f, 1f)
        fadeInOut.duration = 2000
        fadeInOut.repeatCount = ValueAnimator.INFINITE
        fadeInOut.repeatMode = ValueAnimator.REVERSE
        fadeInOut.start()
    }
}
