package com.example.waterdelivery

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.MultiAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase


class check_out_page : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var orderTextView: MultiAutoCompleteTextView
    private lateinit var checkBoxContainer: CheckBox
    private lateinit var checkBoxNoContainer: CheckBox
    private lateinit var cancelButton: Button
    private lateinit var checkBoxGcash: CheckBox
    private lateinit var checkOutButton: Button
    private lateinit var checkBoxPickup: CheckBox

    private var deliveryFee = 10
    private var waterPrice = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out_page)

        orderTextView = findViewById(R.id.multiAutoCompleteTextView)
        checkBoxContainer = findViewById(R.id.checkBox3)
        checkBoxNoContainer = findViewById(R.id.checkBox4)
        cancelButton = findViewById(R.id.button7)
        checkBoxGcash = findViewById(R.id.checkBox)
        checkOutButton = findViewById(R.id.button6)
        checkBoxPickup = findViewById(R.id.checkBox2)

        // Initialize Firebase database reference
        databaseRef = FirebaseDatabase.getInstance().reference

        // Set listeners for the checkboxes
        setCheckBoxListeners()

        // Disable editing of orderTextView
        orderTextView.isClickable = false
        orderTextView.isLongClickable = false
        orderTextView.isFocusable = false
        orderTextView.isFocusableInTouchMode = false

        checkOutButton.setOnClickListener {
            val selectedPaymentMethod = getSelectedPaymentMethod()
            val selectedContainerOption = getSelectedContainerOption()

            if (selectedPaymentMethod == null) {
                // Show error prompt for payment method selection
                showErrorPrompt("Must select payment method")
            } else if (selectedContainerOption == null) {
                // Show error prompt for container option selection
                showErrorPrompt("Must select if you have a container or you will be buying with the container")
            } else {
                val orderDetails = orderTextView.text?.toString() ?: ""

                if (selectedPaymentMethod == checkBoxGcash) {
                    // Redirect to gcash_screen.kt
                    val intent = Intent(this, gcash_screen::class.java)
                    intent.putExtra("orderDetails", orderDetails)
                    startActivity(intent)
                } else if (selectedPaymentMethod == checkBoxPickup) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        val userId = user.uid
                        val orderData = HashMap<String, Any>()
                        orderData["orderDetails"] = orderDetails
                        orderData["paymentMethod"] = "Pickup"
                        orderData["containerOption"] = if (selectedContainerOption == checkBoxContainer) {
                            "Has containers"
                        } else {
                            "Will be paying for the containers"
                        }
                        orderData["name"] = user.email ?: ""
                        orderData["username"] = user.displayName ?: ""
                        orderData["userId"] = userId

                        moveOrderDataToReadyForPickup(userId, orderData,)

                    }
                }
            }
        }
        cancelButton.setOnClickListener {
            showCancelConfirmationDialog()
        }

        // Retrieve order data for the current user from Firebase Realtime Database
        retrieveOrderDataForCurrentUser()
    }
    private fun moveOrderDataToReadyForPickup(userId: String, orderData: HashMap<String, Any>) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val ordersReadyRef = databaseRef.child("orders_ready_for_pick_up")
        val paidOrWaitingRef = databaseRef.child("Paid_or_WaitingforPickup")

        val containerOption = if (checkBoxNoContainer.isChecked) {
            "Will purchase water with container"
        } else {
            "Has container"
        }

        orderData["containerOption"] = containerOption

        val orderId = ordersReadyRef.push().key

        if (orderId != null) {
            ordersReadyRef.child(orderId).setValue(orderData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val ordersRef = databaseRef.child("orders")
                    ordersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val existingOrderData = dataSnapshot.value as HashMap<String, Any>

                                // Create a new order key under Paid_or_WaitingforPickup
                                val newOrderKey = paidOrWaitingRef.push().key

                                if (newOrderKey != null) {
                                    // Move the existing order data to Paid_or_WaitingforPickup with the new order key
                                    paidOrWaitingRef.child(newOrderKey).setValue(existingOrderData)
                                        .addOnCompleteListener { moveTask ->
                                            if (moveTask.isSuccessful) {
                                                // Remove the existing order from orders
                                                ordersRef.child(userId).removeValue()
                                                    .addOnCompleteListener { removeTask ->
                                                        if (removeTask.isSuccessful) {
                                                            // Clear the multiAutoCompleteTextView after successful order placement
                                                            orderTextView.text.clear()
                                                            showSuccessPrompt("Order placed successfully")
                                                        } else {
                                                            showErrorPrompt("Failed to place order")
                                                        }
                                                    }
                                            } else {
                                                showErrorPrompt("Failed to place order")
                                            }
                                        }
                                } else {
                                    showErrorPrompt("Failed to place order")
                                }
                            } else {
                                // No existing order data, directly move the new order to Paid_or_WaitingforPickup
                                paidOrWaitingRef.child(orderId).setValue(orderData)
                                    .addOnCompleteListener { moveTask ->
                                        if (moveTask.isSuccessful) {
                                            // Clear the multiAutoCompleteTextView after successful order placement
                                            orderTextView.text.clear()
                                            showSuccessPrompt("Order placed successfully")
                                        } else {
                                            showErrorPrompt("Failed to place order")
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            showErrorPrompt("Failed to place order")
                        }
                    })
                } else {
                    showErrorPrompt("Failed to place order")
                }
            }
        } else {
            showErrorPrompt("Failed to place order")
        }
    }
    private fun getSelectedPaymentMethod(): CheckBox? {
        if (checkBoxGcash.isChecked && !checkBoxPickup.isChecked) {
            return checkBoxGcash
        } else if (checkBoxPickup.isChecked && !checkBoxGcash.isChecked) {
            return checkBoxPickup
        }
        return null
    }

    private fun getSelectedContainerOption(): CheckBox? {
        if (checkBoxContainer.isChecked && !checkBoxNoContainer.isChecked) {
            return checkBoxContainer
        } else if (checkBoxNoContainer.isChecked && !checkBoxContainer.isChecked) {
            return checkBoxNoContainer
        }
        return null
    }

    private fun showErrorPrompt(errorMessage: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Error")
        alertDialogBuilder.setMessage(errorMessage)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showSuccessPrompt(message: String) {
        if (!isFinishing && !isDestroyed) {
            val alertDialog = AlertDialog.Builder(this)
                .setTitle(message)
                .setMessage("Ready for Pick-up")
                .setCancelable(false) // Prevent dismissing the dialog by clicking outside or pressing back
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss() // Dismiss the dialog when "OK" is pressed

                    // Redirect to Homepage.kt
                    val intent = Intent(this@check_out_page, Hompage::class.java)
                    startActivity(intent)
                    finish()
                }
                .create()

            alertDialog.show()
        }
    }

    private fun showCancelConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel your order?")
            .setPositiveButton("Yes") { _, _ ->  deleteOrderDataForCurrentUser()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun retrieveOrderDataForCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val ordersRef = databaseRef.child("orders")

            ordersRef.orderByChild("userId").equalTo(userId)
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val orderSnapshot = dataSnapshot.children.first()

                            val item = orderSnapshot.child("item").value.toString()
                            val quantity = orderSnapshot.child("quantity").value.toString().toInt()
                            val hasContainer =
                                orderSnapshot.child("hasContainer").value.toString().toBoolean()

                            val orderCost = if (hasContainer) {
                                quantity * waterPrice * 10
                            } else {
                                quantity * waterPrice
                            }

                            val orderDetails = StringBuilder()
                            orderDetails.append("$item - $quantity water(s) - PHP $orderCost\n")
                            orderDetails.append("Delivery Fee - PHP $deliveryFee\n")

                            val totalCost = orderCost + deliveryFee
                            orderDetails.append("Total - PHP $totalCost")

                            // Set the order details in the MultiAutoCompleteTextView
                            orderTextView.setText(orderDetails.toString())
                        } else {
                            // No order found for the current user
                            orderTextView.setText("No order received")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database error
                        orderTextView.setText("Failed to retrieve order")
                    }
                })
        }
    }

    private fun deleteOrderDataForCurrentUser() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val ordersRef = databaseRef.child("orders")

            ordersRef.orderByChild("userId").equalTo(userId)
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val orderSnapshot = dataSnapshot.children.first()
                            orderSnapshot.ref.removeValue()
                            orderTextView.setText("Order cancelled. No orders received.")

                            // Navigate to HomePage
                            val intent = Intent(this@check_out_page,Hompage::class.java)
                            startActivity(intent)
                            finish() // Optional: Close the current activity if needed
                        } else {
                            orderTextView.setText("No orders received")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        orderTextView.setText("Failed to delete order")
                    }
                })
        }
    }

    private fun setCheckBoxListeners() {
        checkBoxContainer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxNoContainer.isChecked = false
                waterPrice = 20
                retrieveOrderDataForCurrentUser()
            }
        }

        checkBoxNoContainer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxContainer.isChecked = false
                waterPrice = 200
                retrieveOrderDataForCurrentUser()
            } else {
                waterPrice = 20
                retrieveOrderDataForCurrentUser()
            }
        }
        checkBoxGcash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxPickup.isChecked = false
            }
        }

        checkBoxPickup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkBoxGcash.isChecked = false
            }
        }
    }
}