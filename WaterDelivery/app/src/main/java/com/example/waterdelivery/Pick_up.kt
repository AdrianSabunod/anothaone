package com.example.waterdelivery
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.example.waterdelivery.Order
import android.widget.Button
import android.widget.TextView
import com.example.waterdelivery.check_out_page

class Pick_up : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var username: String
    private lateinit var name: String
    private lateinit var databaseRef: DatabaseReference
    private lateinit var ordersRef: DatabaseReference
    private lateinit var ordersListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_up)
        ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser

        val orderButton = findViewById<Button>(R.id.button5)
        val quantityText = findViewById<TextView>(R.id.quantityText)
        val minusButton = findViewById<Button>(R.id.minusButton)
        val plusButton = findViewById<Button>(R.id.plusButton)

        var quantity = 0 // Initialize quantity based on your logic

        val MAX_WATER_QUANTITY = 10

        minusButton.setOnClickListener {
            if (quantity > 0) {
                quantity--
            }
            quantityText.text = quantity.toString()
        }

        plusButton.setOnClickListener {
            if (quantity < MAX_WATER_QUANTITY) {
                quantity++
            }
            quantityText.text = quantity.toString()
        }

        orderButton.setOnClickListener {
            // Get the current user from Firebase Authentication
            currentUser = mAuth.currentUser

            // Check if the user is signed in
            currentUser?.let {
                val userId = it.uid
                username =
                    it.displayName ?: "Unknown user" // Use a default value if username is null
                name = it.email ?: "Unknown name" // Use a default value if name is null

                // Update the database with the user's information and water order
                val database = FirebaseDatabase.getInstance()
                ordersRef = database.getReference("orders")

                val order = Order(userId, username, name, "Water Gallon", quantity)
                val newOrderRef = ordersRef.push()
                newOrderRef.setValue(order)

                // Clear the data gathered from Pick_up.kt
                quantity = 0
                quantityText.text = quantity.toString()

                // Display success message
                Toast.makeText(this, "You have ordered successfully!", Toast.LENGTH_SHORT).show()

                // Proceed to the checkout page
                val intent = Intent(this, check_out_page::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Start listening for data changes in the orders node
        startOrdersListener()
    }

    override fun onStop() {
        super.onStop()
        // Stop listening for data changes in the orders node
        stopOrdersListener()
    }

    private fun startOrdersListener() {
        ordersListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Handle the data change
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        }

        ordersRef.addValueEventListener(ordersListener)
    }

    private fun stopOrdersListener() {
        ordersRef.removeEventListener(ordersListener)
    }
}
