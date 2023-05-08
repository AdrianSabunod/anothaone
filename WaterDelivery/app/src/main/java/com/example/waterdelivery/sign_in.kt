package com.example.waterdelivery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        auth = FirebaseAuth.getInstance()

        val signInButton = findViewById<Button>(R.id.signInButton)
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        progressBar = findViewById(R.id.progressBar2)

        signInButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            signInWithCredentials(username, password)
        }
    }

    private fun signInWithCredentials(email: String, password: String) {
        // Show the progress bar
        progressBar.visibility = View.VISIBLE

        // Perform the sign-in process using the provided email and password
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, proceed to the next activity
                    val user: FirebaseUser? = auth.currentUser
                    val intent = Intent(this, Hompage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Sign-in failed, display an error message
                    Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
                }

                // Hide the progress bar
                progressBar.visibility = View.GONE
            }
    }

    private fun signInWithCustomToken() {
        val customToken: String? = null // Replace with your custom token logic

        customToken?.let {
            auth.signInWithCustomToken(it)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        // Update UI or perform necessary actions
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}









