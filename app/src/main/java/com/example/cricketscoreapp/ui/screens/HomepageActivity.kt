package com.example.cricketscoreapp.ui.screens

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieAnimationView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.model.MatchDetails
import com.example.cricketscoreapp.model.User
import com.example.cricketscoreapp.network.AuthRetrofitClient
import com.example.cricketscoreapp.network.RetrofitClient
import com.example.cricketscoreapp.ui.fragment.ScoreHistoryFragment
import com.example.cricketscoreapp.ui.fragment.ScoringFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random

class HomepageActivity : AppCompatActivity() {

    private lateinit var networkReceiver: BroadcastReceiver
    private lateinit var semiTransparentView: View
    private lateinit var loadingAnimationView: LottieAnimationView
    private lateinit var btnScoreHistory: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        Log.d("NetworkCheck", "Internet Available: ${isInternetAvailable(this)}")

        networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.let {
                    if (!isInternetAvailable(it)) {
                        startActivity(Intent(this@HomepageActivity, OfflineActivity::class.java))
                        finish()
                    }
                }
            }
        }
        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        val userId = getUserIdFromSession()
        if (userId != null) {
            fetchUserByUserId(userId)
            Log.d("UserSession", "Retrieved User ID: $userId")
            // You can use this userId in API calls or UI updates
        } else {
            Log.e("UserSession", "No User ID found in session")
        }

        val btnCreateMatch: Button = findViewById(R.id.btn_create_match)
        btnScoreHistory = findViewById(R.id.btn_score_history)
        semiTransparentView = findViewById(R.id.semiTransparentView)
        loadingAnimationView = findViewById(R.id.loadingAnimationView)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        menuButton = findViewById(R.id.fab_menu)

        menuButton.setOnClickListener{
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Retrieve the header view from NavigationView
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tv_username)

        // Set username dynamically (Replace "YourUsernameHere" with actual username)
        val username = "Swapnil Divekar" // Replace this with dynamic data from SharedPreferences, API, or Intent
        usernameTextView.text = userId

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> logoutUser()
                R.id.nav_rule -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.lords.org/mcc/the-laws-of-cricket"))
                    startActivity(intent) // Open the URL in the default browser
                }
                R.id.nav_about -> {
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent) // Navigate to About Us screen
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        btnCreateMatch.setOnClickListener { showMatchDialog() }

        btnScoreHistory.setOnClickListener {
            findViewById<View>(R.id.homeLayout).visibility = View.GONE
            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
            replaceFragment(ScoreHistoryFragment(), "ScoreHistoryFragment")
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    // Clear all fragments when returning to homepage
                    supportFragmentManager.popBackStackImmediate(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    findViewById<View>(R.id.homeLayout).visibility = View.VISIBLE
                    findViewById<View>(R.id.fragment_container).visibility = View.GONE
                    menuButton.visibility = View.VISIBLE
                } else {
                    finish()
                }
            }
        })

        checkUserSession()
    }


    private fun replaceFragment(fragment: Fragment, uniqueTag: String) {
        Log.d("FragmentTransaction", "Replacing with $uniqueTag")

        // Hide the FAB before replacing the fragment
        menuButton.visibility = View.GONE

        // Clear all existing fragments in the container to prevent stacking
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment, uniqueTag)
            .addToBackStack(uniqueTag)
            .commit()
        Log.d("FragmentTransaction", "Fragment transaction committed for $uniqueTag")
    }


    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showMatchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_match_setup, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etTeam1: EditText = dialogView.findViewById(R.id.et_team1)
        val etTeam2: EditText = dialogView.findViewById(R.id.et_team2)
        val tvSelectedOvers: TextView = dialogView.findViewById(R.id.tv_overs_label) // Fix here
        val btnSubmit: Button = dialogView.findViewById(R.id.btn_submit)
        val btnCancel: Button = dialogView.findViewById(R.id.btn_cancel)
        val btnFlipCoin: Button = dialogView.findViewById(R.id.btn_flip_coin)
        val ivCoin: ImageView = dialogView.findViewById(R.id.iv_coin)
        val tvTossResult: TextView = dialogView.findViewById(R.id.tv_toss_result)
        val numberPicker: NumberPicker =
            dialogView.findViewById(R.id.numberPicker_overs) // Fix here

        numberPicker.minValue = 1
        numberPicker.maxValue = 50
        numberPicker.wrapSelectorWheel = true

        var selectedOvers = numberPicker.value // Initialize overs with default value

        numberPicker.setOnValueChangedListener { _, _, newVal ->
            tvSelectedOvers.text = "Overs: $newVal"
            selectedOvers = newVal
        }

        var tossWinner: String? = null
        var tossChoice: String? = null

        // Initially hide buttons and result text
        btnFlipCoin.visibility = View.GONE
        ivCoin.visibility = View.GONE
        tvTossResult.visibility = View.GONE

        // Function to check if both team names are entered
        fun checkTeamNames() {
            val team1 = etTeam1.text.toString().trim()
            val team2 = etTeam2.text.toString().trim()

            val shouldShow = team1.isNotEmpty() && team2.isNotEmpty()
            btnFlipCoin.visibility = if (shouldShow) View.VISIBLE else View.GONE
            ivCoin.visibility = if (shouldShow) View.VISIBLE else View.GONE
            tvTossResult.visibility = if (shouldShow) View.VISIBLE else View.GONE
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkTeamNames()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etTeam1.addTextChangedListener(textWatcher)
        etTeam2.addTextChangedListener(textWatcher)

        btnFlipCoin.setOnClickListener {
            val team1 = etTeam1.text.toString().trim()
            val team2 = etTeam2.text.toString().trim()
            btnFlipCoin.isEnabled = false

            val coinFront = createCoinDrawable(team1)
            val coinBack = createCoinDrawable(team2)

            val flipAnimation = ObjectAnimator.ofFloat(ivCoin, "rotationY", 0f, 1800f)
            flipAnimation.duration = 1500
            flipAnimation.interpolator = AccelerateDecelerateInterpolator()

            flipAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val isTeam1 = Random.nextBoolean()
                    ivCoin.setImageDrawable(if (isTeam1) coinFront else coinBack)
                    tossWinner = if (isTeam1) team1 else team2

                    tvTossResult.text = "$tossWinner wins the toss"
                    tvTossResult.setTextColor(Color.GREEN)

                    // Zoom In and Out Animation
                    val scaleX = ObjectAnimator.ofFloat(tvTossResult, "scaleX", 1f, 1.9f, 1f)
                    val scaleY = ObjectAnimator.ofFloat(tvTossResult, "scaleY", 1f, 1.5f, 1f)
                    scaleX.duration = 600
                    scaleY.duration = 600
                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(scaleX, scaleY)
                    animatorSet.start()

                    btnFlipCoin.isEnabled = true

                    // Show batting/bowling choice dialog
                    showBatBallChoiceDialog(tossWinner!!) { choice ->
                        tossChoice = choice
                        Log.e("coinResult", "$tossWinner, $tossChoice")
                    }
                }
            })

            flipAnimation.start()
        }

        btnSubmit.setOnClickListener {
            val team1 = etTeam1.text.toString().trim()
            val team2 = etTeam2.text.toString().trim()

            if (team1.isNotEmpty() && team2.isNotEmpty() && selectedOvers > 0 && tossWinner != null && tossChoice != null) {
                semiTransparentView.visibility = View.VISIBLE
                loadingAnimationView.visibility = View.VISIBLE
                loadingAnimationView.playAnimation()

                val (matchId, createdAt) = generateUniqueMatchId(team1, team2) // Get both values

                createNewMatch(
                    team1,
                    team2,
                    selectedOvers,
                    matchId,
                    createdAt.toString(),
                    tossWinner!!,
                    tossChoice!!
                )
                dialog.dismiss()
                Log.d("generateUniqueMatchId", matchId)
            } else {
                Toast.makeText(
                    this,
                    "Please enter valid details and complete the toss process!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showBatBallChoiceDialog(tossWinner: String, onChoiceSelected: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_toss_choice, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val tvTossWinner: TextView = dialogView.findViewById(R.id.tv_toss_winner)
        val btnBatting: ImageButton = dialogView.findViewById(R.id.btn_batting)
        val btnBowling: ImageButton = dialogView.findViewById(R.id.btn_bowling)
        val btnConfirmChoice: Button = dialogView.findViewById(R.id.btn_confirm_choice)
        val cardBatting: CardView = dialogView.findViewById(R.id.card_batting)
        val cardBowling: CardView = dialogView.findViewById(R.id.card_bowling)

        tvTossWinner.text = "$tossWinner wins the toss"

        var selectedChoice: String? = null

        btnBatting.setOnClickListener {
            selectedChoice = "Batting"

            cardBatting.cardElevation = 8f
            cardBatting.setBackgroundResource(R.drawable.button_shadow) // Add shadow
            cardBowling.cardElevation = 0f
            cardBowling.setBackgroundResource(0) // Remove shadow
        }

        btnBowling.setOnClickListener {
            selectedChoice = "Bowling"

            cardBowling.cardElevation = 8f
            cardBowling.setBackgroundResource(R.drawable.button_shadow) // Add shadow
            cardBatting.cardElevation = 0f
            cardBatting.setBackgroundResource(0) // Remove shadow
        }



        btnConfirmChoice.setOnClickListener {
            if (selectedChoice != null) {
                onChoiceSelected(selectedChoice!!)
                dialog.dismiss()
            } else {
                Toast.makeText(
                    dialogView.context,
                    "Please select Batting or Bowling",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun createCoinDrawable(teamName: String): BitmapDrawable {
        val size = 300  // Coin size in pixels
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Create a radial gradient for a metallic effect
        val gradient = RadialGradient(
            size / 2f, size / 2f, size / 2f,
            intArrayOf(Color.LTGRAY, Color.DKGRAY),  // Light center, dark edges
            floatArrayOf(0.3f, 1f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // Add an inner shadow for depth
        val shadowPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 8f
            alpha = 80  // Make it semi-transparent
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, shadowPaint)

        // Draw Team Name with Engraved Effect
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(8f, 2f, 2f, Color.DKGRAY)  // Engraved effect
        }
        canvas.drawText(teamName, size / 2f, size / 2f + 15, textPaint)

        // Add a highlight effect at the top-left
        val highlightPaint = Paint().apply {
            color = Color.WHITE
            alpha = 100
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawArc(
            size * 0.2f, size * 0.2f, size * 0.8f, size * 0.8f,
            -45f, 90f, false, highlightPaint
        )

        return BitmapDrawable(resources, bitmap)
    }

    private fun generateUniqueMatchId(team1: String, team2: String): Pair<String, Long> {
        val createdAt = System.currentTimeMillis()
        val matchId = "${team1}_${team2}_$createdAt"
        return Pair(matchId, createdAt)
    }

    private fun createNewMatch(
        team1: String,
        team2: String,
        overs: Int,
        matchId: String,
        createdAt: String,
        tossWinner: String,
        tossChoice: String
    ) {
        val matchDetails = MatchDetails(
            matchId = matchId,
            scoreHistory = "",
            team1 = team1,
            team2 = team2,
            overs = overs,
            createdAt = createdAt,
            tossWinner = tossWinner,
            tossChoice = tossChoice
        )

        Log.d("API_REQUEST", "Match Details: $matchDetails")

        RetrofitClient.apiService.createMatch(matchDetails)
            .enqueue(object : Callback<MatchDetails> {
                override fun onResponse(
                    call: Call<MatchDetails>,
                    response: Response<MatchDetails>
                ) {

                    loadingAnimationView.cancelAnimation()
                    loadingAnimationView.visibility = View.GONE
                    semiTransparentView.visibility = View.GONE
                    /*  dialog.findViewById<Button>(R.id.btn_submit)?.isEnabled = true
                      dialog.findViewById<Button>(R.id.btn_cancel)?.isEnabled = true*/
                    if (response.isSuccessful) {
                        val createdMatch = response.body()
                        createdMatch?.let {
                            // Ensure fragment container is visible
                            findViewById<View>(R.id.homeLayout).visibility = View.GONE
                            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
                            // Navigate to ScoringFragment
                            val fragment = ScoringFragment.newInstance(it.matchId)
                            val fragmentTag = "SCORING_${it.matchId}" // Unique tag per match
                            replaceFragment(fragment, fragmentTag)
                        }
                    } else {
                        Log.e("API", "Failed to create match: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<MatchDetails>, t: Throwable) {
                    loadingAnimationView.cancelAnimation()
                    loadingAnimationView.visibility = View.GONE
                    semiTransparentView.visibility = View.GONE
                    /* dialog.findViewById<Button>(R.id.btn_submit)?.isEnabled = true
                     dialog.findViewById<Button>(R.id.btn_cancel)?.isEnabled = true*/
                    Log.e("API", "API call failed: ${t.message}")
                }
            })
    }


    private fun fetchUserByUserId(userId: String) {
        val apiService = AuthRetrofitClient.apiService

        // Step 1: Fetch all users to get the matching 'id'
        apiService.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && response.body() != null) {
                    val userList = response.body()
                    val matchedUser = userList?.find { it.userId == userId }

                    if (matchedUser != null) {
                        Log.d("API_RESPONSE", "Found ID: ${matchedUser.id} for userId: $userId")

                        // Step 2: Now fetch user details using the 'id'
                        fetchUserById(matchedUser.id.toString())
                    } else {
                        Log.e("UserFragment", "User not found for userId: $userId")
                        Toast.makeText(this@HomepageActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("UserFragment", "Failed to fetch users: ${response.errorBody()?.string()}")
                    Toast.makeText(this@HomepageActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("UserFragment", "API call failed: ${t.message}")
                Toast.makeText(this@HomepageActivity, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to fetch user details using the 'id'
    private fun fetchUserById(id: String) {
        val apiService = AuthRetrofitClient.apiService

        apiService.getUserById(id).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()
                    Log.d("UserFragment", "User Details: $user")
                    updateUI(user)
                } else {
                    Log.e("UserFragment", "Failed to fetch user details: ${response.errorBody()?.string()}")
                    Toast.makeText(this@HomepageActivity, "User details not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("UserFragment", "API call failed: ${t.message}")
                Toast.makeText(this@HomepageActivity, "API Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(user: User?) {
        // Retrieve the header view from NavigationView
        val headerView = navigationView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tv_username)

        // Set username dynamically (Replace "YourUsernameHere" with actual username)
        val username = user?.username
        usernameTextView.text = username
    }


    private fun getUserIdFromSession(): String? {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)
        Log.d("UserSession", "Retrieved User ID from session: $userId") // Debug log
        return userId
    }

    private fun checkUserSession() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)

        if (userId == null) {
            // Redirect to login page if no session exists
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finish()
        } else {
            Log.d("UserSession", "User is already logged in with ID: $userId")
        }
    }

    private fun logoutUser() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect to login screen
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        finish()
    }




}
