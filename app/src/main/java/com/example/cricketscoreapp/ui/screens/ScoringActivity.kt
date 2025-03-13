package com.example.cricketscoreapp.ui.screens

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.ui.adapters.ScoreHistoryAdapter
import java.util.Stack

class ScoringActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var btnZero: Button
    private lateinit var btnOne: Button
    private lateinit var btnTwo: Button
    private lateinit var btnThree: Button
    private lateinit var btnFour: Button
    private lateinit var btnFive: Button
    private lateinit var btnSix: Button
    private lateinit var btnWicket: Button
    private lateinit var btnLegBye: Button
    private lateinit var btnNoBall: Button
    private lateinit var btnReset: Button
    private lateinit var rvScoreHistory: RecyclerView
    private lateinit var btnUndo: Button
    private val actionStack = Stack<Pair<Int, String>>() // Stack to store actions
    private lateinit var  wicketAnimation : LottieAnimationView
    private lateinit var  fourAnimation : LottieAnimationView
    private lateinit var  sixAnimation : LottieAnimationView
    private lateinit var  runCelebrationAnimation : LottieAnimationView
    private lateinit var  noBallAnimation : LottieAnimationView


    private var runs = 0
    private var wickets = 0
    private val scoreHistory = mutableListOf<String>()
    private lateinit var historyAdapter: ScoreHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoring)

        tvScore = findViewById(R.id.tv_score)
        btnZero = findViewById(R.id.btn_dot_ball)
        btnOne = findViewById(R.id.btn_one)
        btnTwo = findViewById(R.id.btn_two)
        btnThree = findViewById(R.id.btn_three)
        btnFour = findViewById(R.id.btn_four)
        btnFive = findViewById(R.id.btn_five)
        btnSix = findViewById(R.id.btn_six)
        btnWicket = findViewById(R.id.btn_wicket)
        btnLegBye = findViewById(R.id.btn_leg_bye)
        btnReset = findViewById(R.id.btn_reset)
        btnNoBall = findViewById(R.id.btn_no_ball)
        btnUndo = findViewById(R.id.btn_undo)
        rvScoreHistory = findViewById(R.id.rv_score_history)
        wicketAnimation = findViewById(R.id.wicketAnimation)
        fourAnimation = findViewById(R.id.fourAnimation)
        sixAnimation = findViewById(R.id.sixAnimation)
        runCelebrationAnimation = findViewById(R.id.runCelebrationAnimation)
        noBallAnimation = findViewById(R.id.noBallAnimation)

        // Set up RecyclerView
        historyAdapter = ScoreHistoryAdapter(scoreHistory)
        rvScoreHistory.layoutManager = LinearLayoutManager(this)
        rvScoreHistory.adapter = historyAdapter

        btnZero.setOnClickListener { updateScore(0, "Dot Ball") }
        btnOne.setOnClickListener { updateScore(1) }
        btnTwo.setOnClickListener { updateScore(2) }
        btnThree.setOnClickListener { updateScore(3) }
        btnFour.setOnClickListener { updateScore(4) }
        btnFive.setOnClickListener { updateScore(5) }
        btnSix.setOnClickListener { updateScore(6) }
        btnWicket.setOnClickListener { updateWicket() }
        btnLegBye.setOnClickListener { updateScore(1, "LB") }
        btnNoBall.setOnClickListener { updateScore(1, "NB") }
        btnReset.setOnClickListener { resetScore() }
        btnUndo.setOnClickListener { undoLastAction() }

        val btnHistory: TextView = findViewById(R.id.tv_history)
        btnHistory.setOnClickListener {
            val intent = Intent(this, ScoreHistoryActivity::class.java)
            startActivity(intent)
        }
    }

   /* private fun updateScore(run: Int, type: String = "") {
        runs += run
        val displayText = if (type.isEmpty()) "$run Runs" else "$run $type"
        scoreHistory.add(displayText)
        actionStack.push(Pair(run, type)) // Save action
        historyAdapter.notifyDataSetChanged()
        updateScoreDisplay()
        if(run == 4){
            showFourAnimation()
        }else if(run == 6){
            showSixAnimation()
        }
        else if(run == 1 && type.equals("")|| run == 2 || run ==3 || run == 5){
            showRunCelebrationAnimation()
        }
        else if(run == 1 && type.equals("NB")||run == 1 && type.equals("LB")){
            showOnballAnimation()
        }
    }*/

    private fun updateScore(run: Int, type: String = "") {
        runs += run
        val displayText = if (type.isEmpty()) "$run Runs" else "$run $type"
        scoreHistory.add(displayText)
        actionStack.push(Pair(run, type)) // Save action
        historyAdapter.notifyDataSetChanged()
        rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1) // Scroll to new item
        updateScoreDisplay()

        when {
            run == 4 -> showFourAnimation()
            run == 6 -> showSixAnimation()
            run in listOf(1, 2, 3, 5) && type.isEmpty() -> showRunCelebrationAnimation()
            type in listOf("NB", "LB") -> showOnballAnimation()
        }
    }


   /* private fun updateWicket() {
        wickets += 1
        scoreHistory.add("Wicket!")
        actionStack.push(Pair(-1, "W")) // -1 to indicate a wicket
        historyAdapter.notifyDataSetChanged()
        updateScoreDisplay()
        showWicketAnimation()
    }*/

    private fun updateWicket() {
        wickets += 1
        scoreHistory.add("Wicket!")
        actionStack.push(Pair(-1, "W")) // -1 to indicate a wicket
        historyAdapter.notifyDataSetChanged()
        rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1) // Scroll to new item
        updateScoreDisplay()
        showWicketAnimation()
    }


    private fun updateScoreDisplay() {
        tvScore.text = "Score: $runs/$wickets"
    }

    private fun undoLastAction() {
        if (actionStack.isNotEmpty()) {
            val lastAction = actionStack.pop()
            if (lastAction.second == "W") {
                wickets -= 1 // Undo wicket
            } else {
                runs -= lastAction.first // Undo runs
            }

            if (scoreHistory.isNotEmpty()) {
                scoreHistory.removeAt(scoreHistory.size - 1) // ✅ Works on API 27+
            }

            historyAdapter.notifyDataSetChanged()
            updateScoreDisplay()
        }
    }


    private fun resetScore() {
        runs = 0
        wickets = 0
        scoreHistory.clear()
        historyAdapter.notifyDataSetChanged()
        updateScoreDisplay()
    }

    fun showWicketAnimation(){
        wicketAnimation.visibility = View.VISIBLE
        wicketAnimation.playAnimation()

        // Hide animation after it finishes
        wicketAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                wicketAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun showFourAnimation(){
        fourAnimation.visibility = View.VISIBLE
        fourAnimation.playAnimation()

        // Hide animation after it finishes
        fourAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                fourAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun showSixAnimation(){
        sixAnimation.visibility = View.VISIBLE
        sixAnimation.playAnimation()

        // Hide animation after it finishes
        sixAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                sixAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun showRunCelebrationAnimation(){
        runCelebrationAnimation.visibility = View.VISIBLE
        runCelebrationAnimation.playAnimation()

        // Hide animation after it finishes
        runCelebrationAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                runCelebrationAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    fun showOnballAnimation(){
        noBallAnimation.visibility = View.VISIBLE
        noBallAnimation.playAnimation()

        // Hide animation after it finishes
        noBallAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                noBallAnimation.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}
