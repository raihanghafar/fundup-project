package com.example.fundup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val investorButton = findViewById<Button>(R.id.investorButton)
        val ceoButton = findViewById<Button>(R.id.ceoButton)

        investorButton.setOnClickListener {
            val intent = Intent(this, InvestorActivity::class.java)
            startActivity(intent)
        }

        ceoButton.setOnClickListener {
            val intent = Intent(this, CEOActivity::class.java)
            startActivity(intent)
        }
    }
}