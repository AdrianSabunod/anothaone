package com.example.waterdelivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class SignUpAct : AppCompatActivity() {
    private lateinit var editTextUsername: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        editTextUsername = findViewById(R.id.editTextText)
        editTextName = findViewById(R.id.editTextText2)
        editTextPassword = findViewById(R.id.editTextTextPassword3)
        editTextConfirmPassword = findViewById(R.id.editTextTextPassword4)
        editTextEmail = findViewById(R.id.editTextTextEmailAddress2)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        progressBar = findViewById(R.id.progressBar)

        val signUpButton: Button = findViewById(R.id.button4)
        signUpButton.setOnClickListener { onSignUpButtonClicked() }
    }

    private fun onSignUpButtonClicked() {
        val username: String = editTextUsername.text.toString()
        val name: String = editTextName.text.toString()
        val password: String = editTextPassword.text.toString()
        val confirmPassword: String = editTextConfirmPassword.text.toString()
        val email: String = editTextEmail.text.toString()

        // Check if any required field is empty
        if (username.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if password and confirm password match
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Show the progress bar
        progressBar.visibility = ProgressBar.VISIBLE

        // Perform the sign-up process using the retrieved information
        registerUser(email, password, username)
    }

    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    setUserDisplayName(user, username)
                    storeUserDataInFirebase(user, username)
                    showSignUpSuccessMessage()
                    navigateToHomePage()
                } else {
                    Toast.makeText(
                        this,
                        "Sign-up failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Hide the progress bar
                progressBar.visibility = ProgressBar.GONE
            }
    }

    private fun setUserDisplayName(user: FirebaseUser?, username: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Username is set successfully
                } else {
                    // Username update failed
                }
            }
    }
    private fun storeUserDataInFirebase(user: FirebaseUser?, username: String) {
        val userId = user?.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        val userData = HashMap<String, Any>()
        userData["username"] = username
        userData["name"] = editTextName.text.toString()
        userData["email"] = editTextEmail.text.toString()
        userData["password"] = editTextPassword.text.toString()

        if (userId != null) {
            databaseRef.child(userId).setValue(userData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User data is stored successfully
                    } else {
                        // User data storage failed
                    }
                }
        }
    }

    private fun showSignUpSuccessMessage() {
        Toast.makeText(this, "You have successfully registered", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHomePage() {
        val intent = Intent(this, Hompage::class.java)
        startActivity(intent)
    }
}

