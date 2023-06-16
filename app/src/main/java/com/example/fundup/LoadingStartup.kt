package com.example.fundup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class LoadingStartup : AppCompatActivity() {

    private val delayMillis: Long = 7000 // Adjust the delay time as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_startup)

        // Simulate a delay to show the loading screen
        Handler().postDelayed({
            // Proceed to the next activity
            val intent = Intent(this, HomepageStartup::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }, delayMillis)
    }
}