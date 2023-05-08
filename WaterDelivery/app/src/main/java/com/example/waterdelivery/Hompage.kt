package com.example.waterdelivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button

class Hompage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hompage)

        val pickUpButton = findViewById<Button>(R.id.button)
        val onlineOrderButton = findViewById<Button>(R.id.button3)

        pickUpButton.setOnClickListener {
            // Start the Pick_up.kt activity
            val intent = Intent(this, Pick_up::class.java)
            startActivity(intent)
        }

        onlineOrderButton.setOnClickListener {
            // Start the online_order.kt activity
            val intent = Intent(this, online_order::class.java)
            startActivity(intent)
        }
    }
}