package com.example.waterdelivery

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.waterdelivery.R.id
import com.example.waterdelivery.R.layout


class Animation_tubigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_animation_tubig)
        supportActionBar?.hide()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)


        val textView = findViewById<TextView>(id.TubigSplashText)
        textView.animate().translationX(1000F).setDuration(1000).setStartDelay(2500)

        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(4777)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val intent = Intent(this@Animation_tubigActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        thread.start()
    }
}