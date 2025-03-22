package com.example.cricketscoreapp.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.model.MatchDetails
import com.example.cricketscoreapp.model.ScoreData
import com.example.cricketscoreapp.network.RetrofitClient
import com.example.cricketscoreapp.ui.adapters.ScoreHistoryAdapter
import com.example.cricketscoreapp.utils.AnimationUtils
import com.example.cricketscoreapp.utils.SoundUtils
import com.example.cricketscoreapp.utils.ViewUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Stack

class SecondInningFragment : Fragment() {

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
    private lateinit var btnWideBall: Button
    private lateinit var btnReset: Button
    private lateinit var rvScoreHistory: RecyclerView
    private lateinit var btnUndo: Button
    private lateinit var loadingAnimationView: LottieAnimationView
    private lateinit var wideAnimation: LottieAnimationView
    private lateinit var wicketAnimation: LottieAnimationView
    private lateinit var fourAnimation: LottieAnimationView
    private lateinit var sixAnimation: LottieAnimationView
    private lateinit var runCelebrationAnimation: LottieAnimationView
    private lateinit var noBallAnimation: LottieAnimationView
    private lateinit var btnUpload: Button
    private lateinit var semiTransparentView: View
    private lateinit var tvOvers: TextView
    private var validBallsCount = 0
    private lateinit var team1Name: TextView
    private lateinit var team2Name: TextView
    private lateinit var matchOvers: TextView
    private lateinit var tvtarget: TextView
    private lateinit var tieAnimation: ImageView

    private var runs = 0
    private var wickets = 0
    private var balls = 0
    private var overs = 0
    private var tossWinner = ""
    private var tossChoice = ""
    private var totalOvers = 0
    private var target = 0
    private val secondScoreHistory = mutableListOf<String>()
    private lateinit var historyAdapter: ScoreHistoryAdapter
    private val actionStack = Stack<Pair<Int, String>>() // Stack to store actions


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_second_inning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val matchId = arguments?.getString("matchId")
        if (matchId != null) {
            fetchMatchByMatchId(matchId)
        } else {
            Toast.makeText(context, "Match ID not found", Toast.LENGTH_SHORT).show()
        }

        tvScore = view.findViewById(R.id.tv_score)
        tvOvers = view.findViewById(R.id.tv_overs)
        btnZero = view.findViewById(R.id.btn_dot_ball)
        btnOne = view.findViewById(R.id.btn_one)
        btnTwo = view.findViewById(R.id.btn_two)
        btnThree = view.findViewById(R.id.btn_three)
        btnFour = view.findViewById(R.id.btn_four)
        btnFive = view.findViewById(R.id.btn_five)
        btnSix = view.findViewById(R.id.btn_six)
        btnWicket = view.findViewById(R.id.btn_wicket)
        btnLegBye = view.findViewById(R.id.btn_leg_bye)
        btnNoBall = view.findViewById(R.id.btn_no_ball)
        btnUndo = view.findViewById(R.id.btn_undo)
        btnReset = view.findViewById(R.id.btn_reset)
        btnUpload = view.findViewById(R.id.btn_upload)
        btnWideBall = view.findViewById(R.id.btn_wide)
        semiTransparentView = view.findViewById(R.id.semiTransparentView)
        team1Name = view.findViewById(R.id.tv_team1Name)
        team2Name = view.findViewById(R.id.tv_team2Name)
        matchOvers = view.findViewById(R.id.tv_total_overs)
        tvtarget = view.findViewById(R.id.tv_target)
        target = arguments?.getInt("target", 0) ?: 0
        tvtarget.text = "Target : ${target}"

        rvScoreHistory = view.findViewById(R.id.rv_score_history)
        loadingAnimationView = view.findViewById(R.id.loadingAnimationView)
        wideAnimation = view.findViewById(R.id.wideAnimation)
        wicketAnimation = view.findViewById(R.id.wicketAnimation)
        fourAnimation = view.findViewById(R.id.fourAnimation)
        sixAnimation = view.findViewById(R.id.sixAnimation)
        runCelebrationAnimation = view.findViewById(R.id.runCelebrationAnimation)
        noBallAnimation = view.findViewById(R.id.noBallAnimation)
        tieAnimation = view.findViewById(R.id.tieAnimation)

        // Set up RecyclerView
        historyAdapter = ScoreHistoryAdapter(secondScoreHistory)
        rvScoreHistory.layoutManager = LinearLayoutManager(requireContext())
        rvScoreHistory.adapter = historyAdapter

        // Set Button Click Listeners
        btnZero.setOnClickListener { updateScore(0, "Dot Ball") }
        btnOne.setOnClickListener { updateScore(1) }
        btnTwo.setOnClickListener { updateScore(2) }
        btnThree.setOnClickListener { updateScore(3) }
        btnFour.setOnClickListener { updateScore(4) }
        btnFive.setOnClickListener { updateScore(5) }
        btnSix.setOnClickListener { updateScore(6) }
        btnWicket.setOnClickListener { updateWicket() }
        btnLegBye.setOnClickListener { showLegByeDialog() }
        btnNoBall.setOnClickListener {
            updateScore(1, "NB")
            showExtraRunsDialog()
        }
        btnWideBall.setOnClickListener { updateScore(1, "WB") }
        btnReset.setOnClickListener { resetScore() }
        btnUndo.setOnClickListener { undoLastAction() }
        btnUpload.setOnClickListener() {
            updateSecondInningScore(
                runs,
                wickets,
                secondScoreHistory.joinToString(",")
            )
        }

    }

    private fun checkMatchStatus(target: Int) {
        val totalDeliveries = totalOvers * 6
        val deliveriesRemaining = totalDeliveries - validBallsCount

        // Immediate win check (regardless of deliveries remaining)
        if (runs >= target) {
            showWinDialog()
            hideAllButtons()
        }
        // Win when exactly reaching target with deliveries remaining
        else if (runs == target && deliveriesRemaining > 0) {
            hideAllButtons()
        }
        // Final ball scenarios
        else if (deliveriesRemaining == 0) {
            when {
                runs == target - 1 -> showTieDialog()
                runs < target -> showLossDialog()
                runs >= target ->  showWinDialog() // Handle late NB/WB extras
            }
            hideAllButtons()
        }
    }

    private fun showWinDialog() {
        showCustomDialog(R.layout.dialog_win, "You Won 🎉")
        SoundUtils.handleMatchResult(requireContext(), "win") // Play win sound
    }

    private fun showTieDialog() {
        showCustomDialog(R.layout.dialog_tie, "Match Tied! 🤝")
        SoundUtils.handleMatchResult(requireContext(), "tie") // Play win sound
    }

    private fun showLossDialog() {
        showCustomDialog(R.layout.dialog_loss, "You Lost 😞")
        SoundUtils.handleMatchResult(requireContext(), "lose") // Play win sound
    }

    private fun showCustomDialog(layout: Int, message: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layout)
        dialog.setCancelable(false)

        val btnOk = dialog.findViewById<Button>(R.id.btn_ok)
        val tvCountdown = dialog.findViewById<TextView>(R.id.tv_countdown)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)

        tvMessage.text = message

        // Start a 5-second countdown
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                tvCountdown.text = "Closing in $secondsRemaining seconds..."
            }

            override fun onFinish() {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        }.start()

        // Button to dismiss manually
        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



    private fun updateScore(run: Int, type: String = "") {
        val totalBallsAllowed = totalOvers * 6
        // 🛑 Hide buttons ONLY IF the last delivery is a valid one (not WB/NB)
        if (validBallsCount == totalBallsAllowed - 1 && type != "NB" && type != "WB") {
            hideAllButtons()
        }
        // 🛑 Prevent adding runs if innings are complete
        if (validBallsCount >= totalBallsAllowed) {
            Toast.makeText(context, "Innings Over! No more runs allowed.", Toast.LENGTH_SHORT)
                .show()

            return
        }
        runs += run
        val displayText = when (type) {
            "LB" -> if (run > 0) "$run LB" else "0 LB (Dot Ball)"
            else -> if (type.isEmpty()) "$run Runs" else "$run $type"
        }
        secondScoreHistory.add(displayText)
        actionStack.push(Pair(run, type))
        historyAdapter.notifyDataSetChanged()
        rvScoreHistory.smoothScrollToPosition(secondScoreHistory.size - 1)
        updateScoreDisplay()

        if (type != "WB" && type != "NB") {
            validBallsCount++
            updateBallsAndOvers()
        }

        when {
            run == 4 -> AnimationUtils.showFourAnimation(fourAnimation)
            run == 6 -> AnimationUtils.showSixAnimation(sixAnimation)
            run in listOf(
                1,
                2,
                3,
                5
            ) && type.isEmpty() -> AnimationUtils.showRunCelebrationAnimation(
                runCelebrationAnimation
            )

            type in listOf("NB", "LB") -> AnimationUtils.showRunCelebrationAnimation(
                runCelebrationAnimation
            )

            type in listOf("WB") -> AnimationUtils.showWideAnimation(wideAnimation)
        }
        checkMatchStatus(target)
    }

    private fun updateBallsAndOvers() {
        overs = validBallsCount / 6  // Full overs
        balls = validBallsCount % 6  // Remaining balls
        tvOvers.text = "Overs: $overs.$balls"
    }

    // Updated updateWicket() function
    private fun updateWicket() {
        val totalBallsAllowed = totalOvers * 6
        if (validBallsCount >= totalBallsAllowed) {
            Toast.makeText(
                context,
                "Innings completed! No more wickets can be added.",
                Toast.LENGTH_SHORT
            ).show()
            return // Stop execution
        }
        wickets += 1
        validBallsCount++ // Increment validBallsCount for the valid delivery
        secondScoreHistory.add("Wicket!")
        actionStack.push(Pair(0, "W")) // Correct runDelta to 0 as wickets don't affect runs
        checkMatchStatus(target)
        historyAdapter.notifyDataSetChanged()
        rvScoreHistory.smoothScrollToPosition(secondScoreHistory.size - 1)
        updateScoreDisplay()
        updateBallsAndOvers()
        AnimationUtils.showWicketAnimation(wicketAnimation)
        // 🛑 Hide all buttons if this was the last ball of the innings
        if (validBallsCount == totalBallsAllowed) {
            hideAllButtons()
        }
    }

    private fun updateScoreDisplay() {
        tvScore.text = "Score: $runs/$wickets"
    }

    private fun fetchMatchByMatchId(matchId: String) {
        val apiService = RetrofitClient.apiService

        apiService.getMatchDetails().enqueue(object : Callback<List<MatchDetails>> {
            override fun onResponse(
                call: Call<List<MatchDetails>>,
                response: Response<List<MatchDetails>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("API_RESPONSE", "Fetched Matches: $matchId")
                    val matchList = response.body()
                    val matchDetails = matchList?.find { it.matchId == matchId } // Find by matchId

                    if (matchDetails != null) {
                        Log.d("ScoringFragment", "Match Details: $matchDetails")
                        updateUI(matchDetails)
                    } else {
                        Log.e("ScoringFragment", "Match not found for matchId: $matchId")
                        Toast.makeText(context, "Match not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(
                        "ScoringFragment",
                        "Failed to fetch match details: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(context, "Failed to load match details", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<MatchDetails>>, t: Throwable) {
                Log.e("ScoringFragment", "API call failed: ${t.message}")
                Toast.makeText(context, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(matchDetails: MatchDetails?) {
        matchDetails?.let {
            // Set team names with uppercase for consistency
            team1Name.text = it.team1?.uppercase()?.take(5) ?: "N/A"
            team2Name.text = it.team2?.uppercase()?.take(5) ?: "N/A"
            matchOvers.text = "Total Overs: ${it.overs ?: 0}"

            // Get toss information
            tossWinner = it.tossWinner ?: "N/A"
            tossChoice = it.tossChoice ?: "N/A"
            totalOvers = (it.overs ?: "N/A") as Int
            Log.d("TossInfo", "Winner: $tossWinner, Choice: $tossChoice")

            // Determine batting team
            val battingTeam = if (!tossChoice.equals("Batting", ignoreCase = true)) tossWinner
            else if (tossWinner == it.team1) it.team2
            else it.team1

            // Get styled resources
            val batIcon = ContextCompat.getDrawable(requireContext(), R.drawable.bat)?.let {
                getScaledDrawable(it, 28.dpToPx(), 28.dpToPx()) // Slightly bigger icon
            }
            val highlightColor = ContextCompat.getColor(requireContext(), R.color.blue)
            val normalColor = ContextCompat.getColor(requireContext(), R.color.red)

            // Function to style team views
            fun styleTeamView(view: TextView, isBatting: Boolean) {
                view.setTextColor(if (isBatting) highlightColor else normalColor)
                view.setCompoundDrawablesWithIntrinsicBounds(
                    if (isBatting) batIcon else null,
                    null,
                    null,
                    null
                )
                view.textSize = if (isBatting) 24f else 12f
                view.compoundDrawablePadding = 8.dpToPx() // More spacing
                view.paint.isFakeBoldText = isBatting

                // Smooth transition animation
                view.animate().alpha(0f).setDuration(200).withEndAction {
                    view.alpha = 1f
                }.start()
            }

            // Apply styles to both teams
            styleTeamView(team1Name, it.team1 == battingTeam)
            styleTeamView(team2Name, it.team2 == battingTeam)
            ViewUtils.applyBounceAnimationXML(tvtarget, requireContext())
        }
    }

    private fun getScaledDrawable(drawable: Drawable, widthPx: Int, heightPx: Int): Drawable {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, widthPx, heightPx)
        drawable.draw(canvas)
        return BitmapDrawable(resources, bitmap)
    }

    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun showLegByeDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_extra_runs, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set title
        dialogView.findViewById<TextView>(R.id.tv_title).text = "Leg Bye Runs"

        // Configure button visibility
        dialogView.findViewById<Button>(R.id.btn_three).visibility = View.VISIBLE // Show 3 runs
        dialogView.findViewById<Button>(R.id.btn_six).visibility = View.GONE     // Hide 6 runs

        // Button click listeners
        dialogView.findViewById<Button>(R.id.btn_zero).setOnClickListener {
            updateScore(0, "LB")
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_one).setOnClickListener {
            updateScore(1, "LB")
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_two).setOnClickListener {
            updateScore(2, "LB")
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_three).setOnClickListener {
            updateScore(3, "LB")
            dialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btn_four).setOnClickListener {
            updateScore(4, "LB")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun undoLastAction() {
        if (actionStack.isNotEmpty()) {
            val (runDelta, type) = actionStack.pop()

            // Subtract the runDelta from runs
            runs -= runDelta
            runs = maxOf(runs, 0) // Ensure runs don't go negative

            // Adjust wickets if the action was a wicket
            if (type == "W") {
                wickets = maxOf(wickets - 1, 0)
            }

            // Decrease validBallsCount if the action was a valid ball (not WB or NB)
            if (type != "WB" && type != "NB") {
                validBallsCount = maxOf(validBallsCount - 1, 0)
            }

            // ✅ Enable No Ball button if innings are NOT complete after undo
            if (validBallsCount < totalOvers * 6) {
                showAllButtons()
            }

            // Remove the last entry from secondScoreHistory
            if (secondScoreHistory.isNotEmpty()) {
                secondScoreHistory.removeAt(secondScoreHistory.size - 1)
            }

            // Update UI components
            updateScoreDisplay()
            updateBallsAndOvers()
            historyAdapter.notifyDataSetChanged()
            // rvScoreHistory.smoothScrollToPosition(secondScoreHistory.size - 1)
            // Update scroll position only if there are items left
            if (secondScoreHistory.isNotEmpty()) {
                rvScoreHistory.smoothScrollToPosition(secondScoreHistory.size - 1)
            } else {
                // Clear RecyclerView explicitly when empty
                historyAdapter.notifyDataSetChanged()
            }

            Log.d("UndoAction", "Undid: $runDelta, type: $type. New score: $runs/$wickets")
        } else {
            Toast.makeText(requireContext(), "No actions to undo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetScore() {
        runs = 0
        wickets = 0
        balls = 0
        overs = 0
        validBallsCount = 0  // Make sure validBallsCount resets
        secondScoreHistory.clear()
        actionStack.clear()  // Also clear undo stack
        historyAdapter.notifyDataSetChanged()
        updateScoreDisplay()
        tvOvers.text = "Overs: $overs.$balls"
    }

    private fun updateSecondInningScore(
        secondInningRuns: Int,
        secondInningWickets: Int,
        secondScoreHistory: String
    ) {
        val matchId = arguments?.getString("matchId") ?: return

        // Show loading animation
        showAnimationLoad()

        RetrofitClient.apiService.getScores().enqueue(object : Callback<List<ScoreData>> {
            override fun onResponse(
                call: Call<List<ScoreData>>,
                response: Response<List<ScoreData>>
            ) {
                if (response.isSuccessful) {
                    val scores = response.body()
                    val scoreData = scores?.find { it.matchId == matchId }

                    if (scoreData != null) {
                        val scoreId = scoreData.id ?: run {
                            handleApiCompletion()
                            return
                        }

                        val updatedScoreData = mapOf(
                            "secondInningRuns" to secondInningRuns,
                            "secondInningWickets" to secondInningWickets,
                            "secondScoreHistory" to secondScoreHistory
                        )

                        RetrofitClient.apiService.updateSecondInningScore(scoreId, updatedScoreData)
                            .enqueue(object : Callback<ScoreData> {
                                override fun onResponse(
                                    call: Call<ScoreData>,
                                    response: Response<ScoreData>
                                ) {
                                    handleApiCompletion()
                                    if (response.isSuccessful) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Score updated!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Update failed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: Call<ScoreData>, t: Throwable) {
                                    handleApiCompletion()
                                    Toast.makeText(
                                        requireContext(),
                                        "Error: ${t.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } else {
                        handleApiCompletion()
                        Toast.makeText(requireContext(), "Score data not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    handleApiCompletion()
                    Toast.makeText(requireContext(), "Failed to fetch scores", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<ScoreData>>, t: Throwable) {
                handleApiCompletion()
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }

            private fun handleApiCompletion() {
                // Hide loading animation
                semiTransparentView.visibility = View.GONE
                loadingAnimationView.visibility = View.GONE
                loadingAnimationView.cancelAnimation()

                // Navigate back after 1 seconds
                view?.postDelayed({
                    requireActivity().onBackPressed()
                }, 1000)
            }
        })
    }

    private fun showExtraRunsDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_extra_runs, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_zero)
            .setOnClickListener { dialog.dismiss() } // No changes
        dialogView.findViewById<Button>(R.id.btn_one).setOnClickListener { addExtraRuns(1, dialog) }
        dialogView.findViewById<Button>(R.id.btn_two).setOnClickListener { addExtraRuns(2, dialog) }
        dialogView.findViewById<Button>(R.id.btn_four)
            .setOnClickListener { addExtraRuns(4, dialog) }
        dialogView.findViewById<Button>(R.id.btn_six).setOnClickListener { addExtraRuns(6, dialog) }
        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun addExtraRuns(extraRuns: Int, dialog: AlertDialog) {
        if (extraRuns > 0) {
            runs += extraRuns

            if (secondScoreHistory.isNotEmpty() && secondScoreHistory.last().contains("NB")) {
                // Extract previous NB runs
                val previousEntry = secondScoreHistory.last()
                val match = """(\d+)(?: \+ (\d+))? => (\d+) NB""".toRegex().find(previousEntry)

                val previousRuns = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val totalRuns = previousRuns + extraRuns

                // Update last entry with the new extra runs
                secondScoreHistory[secondScoreHistory.size - 1] =
                    "$previousRuns + $extraRuns => $totalRuns NB"

                // Update the action stack correctly for undo
                actionStack.pop() // Remove the previous "1 NB"
                actionStack.push(Pair(totalRuns, "NB"))
            } else {
                // If there's no previous NB entry, add a new NB with extra runs
                secondScoreHistory.add("1 + $extraRuns => ${1 + extraRuns} NB")
                actionStack.push(Pair(1 + extraRuns, "NB"))
            }

            historyAdapter.notifyDataSetChanged()
            rvScoreHistory.smoothScrollToPosition(secondScoreHistory.size - 1)
            updateScoreDisplay()
            checkMatchStatus(target)
        }

        dialog.dismiss()
    }

    private fun hideAllButtons() {
        btnZero.visibility = View.GONE
        btnOne.visibility = View.GONE
        btnTwo.visibility = View.GONE
        btnThree.visibility = View.GONE
        btnFour.visibility = View.GONE
        btnFive.visibility = View.GONE
        btnSix.visibility = View.GONE
        btnNoBall.visibility = View.GONE
        btnWideBall.visibility = View.GONE
        btnLegBye.visibility = View.GONE
        btnWicket.visibility = View.GONE
        btnUpload.visibility = View.VISIBLE
    }

    private fun showAllButtons() {
        btnZero.visibility = View.VISIBLE
        btnOne.visibility = View.VISIBLE
        btnTwo.visibility = View.VISIBLE
        btnThree.visibility = View.VISIBLE
        btnFour.visibility = View.VISIBLE
        btnFive.visibility = View.VISIBLE
        btnSix.visibility = View.VISIBLE
        btnNoBall.visibility = View.VISIBLE
        btnWideBall.visibility = View.VISIBLE
        btnLegBye.visibility = View.VISIBLE
        btnWicket.visibility = View.VISIBLE
        btnUpload.visibility = View.GONE
    }

    private fun showAnimationLoad() {
        semiTransparentView.visibility = View.VISIBLE
        loadingAnimationView.visibility = View.VISIBLE
        loadingAnimationView.playAnimation()
    }

    override fun onResume() {
        super.onResume()
        val matchId = arguments?.getString("matchId")
        matchId?.let {
            fetchMatchByMatchId(it) // Fetch data again when Fragment is resumed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear any fragment-specific references
        parentFragmentManager.beginTransaction().remove(this).commit()
        SoundUtils.release()
    }

    companion object {
        fun newInstance(matchId: String, target: Int): SecondInningFragment {
            val fragment = SecondInningFragment()
            val args = Bundle()
            args.putString("matchId", matchId)
            args.putInt("target", target)
            fragment.arguments = args
            return fragment
        }
    }
}