package com.example.cricketscoreapp.ui.screens

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.cricketscoreapp.R
import com.example.cricketscoreapp.model.User
import com.example.cricketscoreapp.network.AuthRetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LoginRegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var fabBack: FloatingActionButton
    private lateinit var registerUsername: EditText
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var gotoRegister: TextView
    private lateinit var gotoLogin: TextView
    private lateinit var semiTransparentView: View
    private lateinit var loadingAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            navigateToHome()
        }

        viewFlipper = findViewById(R.id.viewFlipper)
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        registerUsername = findViewById(R.id.registerUsername)
        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerButton = findViewById(R.id.registerButton)
        gotoRegister = findViewById(R.id.gotoRegister)
        gotoLogin = findViewById(R.id.gotoLogin)
        semiTransparentView = findViewById(R.id.semiTransparentView)
        loadingAnimationView = findViewById(R.id.loadingAnimationView)
        fabBack = findViewById(R.id.fab_back)

        loginButton.setOnClickListener { loginUser() }
        registerButton.setOnClickListener { registerUser() }

        gotoRegister.setOnClickListener {
            viewFlipper.showNext()

        }
        gotoLogin.setOnClickListener { viewFlipper.showPrevious() }

        fabBack.setOnClickListener { finishAffinity() }
    }

    private fun generateUserId(email: String, password: String, createdAt: String): String {
        val input = "$email$password$createdAt"
        val digest = MessageDigest.getInstance("SHA-256") // Hashing for security
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) } // Convert to Hex
    }

    private fun registerUser() {
        val username = registerUsername.text.toString().trim()
        val email = registerEmail.text.toString().trim()
        val password = registerPassword.text.toString().trim()

        if (username.isEmpty()|| email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            hideAnim()
            return
        }
        showAnim()

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val createdAt = sdf.format(Date())

        val userId = generateUserId(email, password, createdAt) // Generate userId

        val user = User(userId = userId, email = email, password = password,username = username, createdAt = createdAt)
        val apiService = AuthRetrofitClient.apiService
        apiService.registerUser(user).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                hideAnim()
                if (response.isSuccessful) {
                    Toast.makeText(this@LoginRegisterActivity, "Registration Successful! Please Login.", Toast.LENGTH_SHORT).show()
                    viewFlipper.showPrevious() // Navigate to login page
                } else {
                    Toast.makeText(this@LoginRegisterActivity, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@LoginRegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun saveUserIdInSession(userId: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USER_ID", userId)
        editor.apply()
    }

    private fun loginUser() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()


        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            hideKeybaord()
            return
        }

        showAnim()
        val apiService = AuthRetrofitClient.apiService
        apiService.loginUser(email, password).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    hideAnim()
                    val users = response.body()
                    if (!users.isNullOrEmpty()) {
                        val user = users[0]  // Get first user from the list
                        saveUserIdInSession(user.userId) // Store userId in session
                        Log.d("UserSession", "User ID saved: $user.userId")
                        Toast.makeText(this@LoginRegisterActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginRegisterActivity, HomepageActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginRegisterActivity, "Invalid Credentials!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    hideAnim()
                    Toast.makeText(this@LoginRegisterActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(this@LoginRegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun hideAnim() {
        loadingAnimationView.cancelAnimation()
        loadingAnimationView.visibility = View.GONE
        semiTransparentView.visibility = View.GONE
        loginButton.isEnabled = true
        registerButton.isEnabled = true  // Ensure register button is also enabled
    }

    private fun showAnim(){
        semiTransparentView.visibility = View.VISIBLE
        loadingAnimationView.visibility = View.VISIBLE
        loadingAnimationView.playAnimation()
        loginButton.isEnabled = false
    }

    private fun hideKeybaord(){
        // Hide keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
    private fun navigateToHome() {
        startActivity(Intent(this, HomepageActivity::class.java))
        finish()
    }
}
