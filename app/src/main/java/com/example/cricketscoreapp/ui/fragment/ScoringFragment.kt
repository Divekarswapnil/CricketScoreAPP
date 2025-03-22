package com.example.cricketscoreapp.ui.fragment

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Stack

class ScoringFragment : Fragment() {

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
    private lateinit var btnSecondInning: Button
    private lateinit var semiTransparentView: View
    private lateinit var tvOvers: TextView
    private var validBallsCount = 0
    private lateinit var team1Name: TextView
    private lateinit var team2Name: TextView
    private lateinit var matchOvers: TextView


    private var runs = 0
    private var wickets = 0
    private var balls = 0
    private var overs = 0
    private var tossWinner = ""
    private var tossChoice = ""
    private var totalOvers = 0
    private val scoreHistory = mutableListOf<String>()
    private val secondScoreHistory = mutableListOf<String>()
    private lateinit var historyAdapter: ScoreHistoryAdapter
    private val actionStack = Stack<Pair<Int, String>>() // Stack to store actions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_scoring, container, false)
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
        btnSecondInning = view.findViewById(R.id.btn_second_inning)
        btnWideBall = view.findViewById(R.id.btn_wide)
        semiTransparentView = view.findViewById(R.id.semiTransparentView)
        team1Name = view.findViewById(R.id.tv_team1Name)
        team2Name = view.findViewById(R.id.tv_team2Name)
        matchOvers = view.findViewById(R.id.tv_total_overs)

        rvScoreHistory = view.findViewById(R.id.rv_score_history)
        loadingAnimationView = view.findViewById(R.id.loadingAnimationView)
        wideAnimation = view.findViewById(R.id.wideAnimation)
        wicketAnimation = view.findViewById(R.id.wicketAnimation)
        fourAnimation = view.findViewById(R.id.fourAnimation)
        sixAnimation = view.findViewById(R.id.sixAnimation)
        runCelebrationAnimation = view.findViewById(R.id.runCelebrationAnimation)
        noBallAnimation = view.findViewById(R.id.noBallAnimation)

        // Set up RecyclerView
        historyAdapter = ScoreHistoryAdapter(scoreHistory)
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
        btnSecondInning.setOnClickListener() { postScoreToApi() }

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

        /*val displayText = if (type.isEmpty()) "$run Runs" else "$run $type"*/
        val displayText = when (type) {
            "LB" -> if (run > 0) "$run LB" else "0 LB (Dot Ball)"
            else -> if (type.isEmpty()) "$run Runs" else "$run $type"
        }
        scoreHistory.add(displayText)
        actionStack.push(Pair(run, type))
        historyAdapter.notifyDataSetChanged()
        rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1)
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
        scoreHistory.add("Wicket!")
        actionStack.push(Pair(0, "W")) // Correct runDelta to 0 as wickets don't affect runs
        historyAdapter.notifyDataSetChanged()
        rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1)
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

            // Remove the last entry from scoreHistory
            if (scoreHistory.isNotEmpty()) {
                scoreHistory.removeAt(scoreHistory.size - 1)
            }

            // Update UI components
            updateScoreDisplay()
            updateBallsAndOvers()
            historyAdapter.notifyDataSetChanged()
            // rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1)
            // Update scroll position only if there are items left
            if (scoreHistory.isNotEmpty()) {
                rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1)
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
        scoreHistory.clear()
        actionStack.clear()  // Also clear undo stack
        historyAdapter.notifyDataSetChanged()
        updateScoreDisplay()
        tvOvers.text = "Overs: $overs.$balls"
    }

    private fun postScoreToApi() {
        updateBallsAndOvers()
        val userId = getUserIdFromSession() ?: return // Fetch userId directly in Fragment
        val matchId = arguments?.getString("matchId") ?: return // Ensure matchId is retrieved

        val scoreData = ScoreData(
            firstInningRuns = runs,
            firstInningWickets = wickets,
            secondInningRuns = 0,
            secondInningWickets = 0,
            overs = overs,
            balls = balls,
            userId = userId,
            scoreHistory = scoreHistory.joinToString(","), // Convert list to comma-separated string
            secondScoreHistory = secondScoreHistory.joinToString(","), // Convert list to comma-separated string
            matchId = matchId
        )
        semiTransparentView.visibility = View.VISIBLE
        loadingAnimationView.visibility = View.VISIBLE
        loadingAnimationView.playAnimation()

        RetrofitClient.apiService.postScore(scoreData).enqueue(object : Callback<ScoreData> {
            override fun onResponse(call: Call<ScoreData>, response: Response<ScoreData>) {
                if (response.isSuccessful) {
                    loadingAnimationView.cancelAnimation()
                    loadingAnimationView.visibility = View.GONE
                    semiTransparentView.visibility = View.GONE
                    val target = runs + 1
                    showCompletionDialog(matchId, target)
                } else {
                    Toast.makeText(requireContext(), "Failed to update score!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<ScoreData>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCompletionDialog(matchId: String, target: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_first_inning_completed)
        dialog.setCancelable(false)

        val tvCounter = dialog.findViewById<TextView>(R.id.tv_counter)
        val btnStartInning = dialog.findViewById<Button>(R.id.btn_start_inning)

        tvCounter.text = "Redirecting in 5 sec..."
        var timeLeft = 5

        val handler = Handler(Looper.getMainLooper())
        val countdown = object : Runnable {
            override fun run() {
                if (timeLeft > 0) {
                    timeLeft--
                    tvCounter.text = "Redirecting in $timeLeft sec..."
                    handler.postDelayed(this, 1000)
                } else {
                    dialog.dismiss()
                    navigateToSecondInning(matchId, target)
                }
            }
        }

        handler.postDelayed(countdown, 1000)

        btnStartInning.setOnClickListener {
            handler.removeCallbacks(countdown)
            dialog.dismiss()
            navigateToSecondInning(matchId, target)
        }

        dialog.show()
    }

    private fun navigateToSecondInning(matchId: String, target: Int) {
        val secondInningFragment = SecondInningFragment.newInstance(matchId, target)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, secondInningFragment)
            .addToBackStack(null)
            .commit()
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
            val battingTeam = if (tossChoice.equals("Batting", ignoreCase = true)) tossWinner
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

            if (scoreHistory.isNotEmpty() && scoreHistory.last().contains("NB")) {
                // Extract previous NB runs
                val previousEntry = scoreHistory.last()
                val match = """(\d+)(?: \+ (\d+))? => (\d+) NB""".toRegex().find(previousEntry)

                val previousRuns = match?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val totalRuns = previousRuns + extraRuns

                // Update last entry with the new extra runs
                scoreHistory[scoreHistory.size - 1] = "$previousRuns + $extraRuns => $totalRuns NB"

                // Update the action stack correctly for undo
                actionStack.pop() // Remove the previous "1 NB"
                actionStack.push(Pair(totalRuns, "NB"))
            } else {
                // If there's no previous NB entry, add a new NB with extra runs
                scoreHistory.add("1 + $extraRuns => ${1 + extraRuns} NB")
                actionStack.push(Pair(1 + extraRuns, "NB"))
            }

            historyAdapter.notifyDataSetChanged()
            rvScoreHistory.smoothScrollToPosition(scoreHistory.size - 1)
            updateScoreDisplay()
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
        btnSecondInning.visibility = View.VISIBLE
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
        btnSecondInning.visibility = View.GONE
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
    }

    companion object {
        fun newInstance(matchId: String): ScoringFragment {
            val fragment = ScoringFragment()
            val args = Bundle()
            args.putString("matchId", matchId)
            fragment.arguments = args
            return fragment
        }
    }

    private fun getUserIdFromSession(): String? {
        val sharedPreferences =
            requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", null)
    }


}
