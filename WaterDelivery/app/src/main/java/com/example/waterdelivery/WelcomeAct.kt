package com.example.waterdelivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Button


class WelcomeAct : AppCompatActivity() {

    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")

        myRef.setValue("Water is Tubig")

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                Log.d("WATER_DELIVERY_APP", "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("WATER_DELIVERY_APP", "Failed to read value.", error.toException())
            }
        })

        val googleSignUpButton = findViewById<Button>(R.id.googleSignUpButton)
        val appSignUpButton = findViewById<Button>(R.id.appSignUpButton)
        val appSignInButton = findViewById<Button>(R.id.button2)

        googleSignUpButton.setOnClickListener {
            signUpWithGoogle()
        }

        appSignUpButton.setOnClickListener {
            signUpWithApp()
        }

        appSignInButton.setOnClickListener {
            signInWithApp()
        }
    }

    private fun signUpWithGoogle() {
        // Create a GoogleSignInOptions object with the desired configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Build the GoogleApiClient with the options specified
        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        // Start the Google sign-in intent
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Handle the result of the Google sign-in process
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            result?.let { handleGoogleSignInResult(it) }
        }
    }

    private fun handleGoogleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            // Google sign-in successful, handle the authenticated user
            val account = result.signInAccount
            // You can access account information like account.id, account.email, etc.
        } else {
            // Google sign-in failed, handle the error
            val error = result.status
            // You can display an error message or take appropriate action
        }
    }

    private fun signUpWithApp() {
        val intent = Intent(this, SignUpAct::class.java)
        startActivity(intent)
    }

    private fun signInWithApp() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}