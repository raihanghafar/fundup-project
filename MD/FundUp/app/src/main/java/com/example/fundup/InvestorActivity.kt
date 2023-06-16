package com.example.fundup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class InvestorActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val questionPages = listOf(
        R.layout.activity_investor_question1,
        R.layout.activity_investor_question2,
        R.layout.activity_investor_question3,
        R.layout.activity_investor_question4,
        R.layout.activity_investor_question5
    )

    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: Button

    private var currentQuestionIndex = 0

    private lateinit var sharedPreferences: SharedPreferences

    // Initialize Firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(questionPages[currentQuestionIndex]) // Set the initial layout file

        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)

        previousButton.setOnClickListener {
            handlePreviousButtonClick()
        }

        nextButton.setOnClickListener {
            handleNextButtonClick()
        }

        sharedPreferences = getSharedPreferences("InvestorPreferences", Context.MODE_PRIVATE)

        currentQuestionIndex = savedInstanceState?.getInt("currentQuestionIndex") ?: 0
        loadQuestionPage()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentQuestionIndex", currentQuestionIndex)
    }

    override fun onBackPressed() {
        handlePreviousButtonClick()
    }

    private fun loadQuestionPage() {
        if (currentQuestionIndex < questionPages.size) {
            setContentView(questionPages[currentQuestionIndex]) // Change the layout file

            previousButton = findViewById(R.id.previousButton)
            nextButton = findViewById(R.id.nextButton)

            previousButton.setOnClickListener {
                handlePreviousButtonClick()
            }

            nextButton.setOnClickListener {
                handleNextButtonClick()
            }

            if (currentQuestionIndex == 0) {
                previousButton.visibility = View.GONE // Hide the previous button on the first page
            } else {
                previousButton.visibility = View.VISIBLE // Show the previous button on subsequent pages
            }

            if (currentQuestionIndex == questionPages.size - 1) {
                nextButton.text = "Finish"
            } else {
                nextButton.text = "Next"
            }

            loadEnteredValues() // Load entered values into the fields
        }
    }

    private fun handlePreviousButtonClick() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            loadQuestionPage()
        } else {
            val intent = Intent(this, RoleSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun handleNextButtonClick() {
        // Save entered values to SharedPreferences
        saveEnteredValues()

        if (currentQuestionIndex < questionPages.size - 1) {
            // If there are more question pages remaining, increment the index and load the next page
            currentQuestionIndex++
            loadQuestionPage()
        } else {
            // Retrieve the user ID from FirebaseAuth
            val userID = auth.currentUser?.uid

            if (userID != null) {
                // Create a HashMap with the field names and their corresponding values
                val data = hashMapOf<String, Any?>()

                // Retrieve the values from SharedPreferences using the correct keys
                for (i in 0 until questionPages.size) {
                    val investorNameValue = sharedPreferences.getString("nama_lengkap$i", null)
                    val investorNikValue = sharedPreferences.getString("nik_investor$i", null)
                    val investorEmailValue = sharedPreferences.getString("email_investor$i", null)
                    val investorTypeValue = sharedPreferences.getString("tipe_investor$i", null)
                    val investorExperienceValue =
                        sharedPreferences.getString("pengalaman_investasi$i", null)
                    val investorTargetValue = sharedPreferences.getString("target_investasi$i", null)
                    val startupTypeValue = sharedPreferences.getString("tipe_startup$i", null)
                    val developmentalLevelValue =
                        sharedPreferences.getString("target_perkembangan$i", null)
                    val industryValue = sharedPreferences.getString("target_industri$i", null)

                    // Add non-null values to the data HashMap
                    investorNameValue?.let { data["nama_lengkap"] = it }
                    investorNikValue?.let { data["nik_investor"] = it }
                    investorEmailValue?.let { data["email_investor"] = it }
                    investorTypeValue?.let { data["tipe_investor"] = it }
                    investorExperienceValue?.let { data["pengalaman_investasi"] = it }
                    investorTargetValue?.let { data["target_investasi"] = it }
                    startupTypeValue?.let { data["tipe_startup"] = it }
                    developmentalLevelValue?.let { data["target_perkembangan"] = it }
                    industryValue?.let { data["target_industri"] = it }
                }

                // Save the entered values to Firestore with the user ID as the document ID
                db.collection("investor_loker")
                    .document(userID)
                    .set(data)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Data saved successfully! Document ID: $userID",
                            Toast.LENGTH_SHORT
                        ).show()

                        callFlaskAPI()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error saving data. Please try again. Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                val intent = Intent(this, LoadingInvestor::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                // User is not logged in or user ID is null
                // Handle the case accordingly
            }
        }
    }

    private fun saveEnteredValues() {
        // Get the references to the views
        val investorNameEditText = findViewById<EditText>(R.id.investorNameEditText)
        val investorNikEditText = findViewById<EditText>(R.id.investorNikEditText)
        val investorEmailEditText = findViewById<EditText>(R.id.investorEmailEditText)
        val investorTypeSpinner = findViewById<Spinner>(R.id.spinnerInvestorType)
        val investorExperienceSpinner = findViewById<Spinner>(R.id.spinnerInvestorExperience)
        val investorTargetSpinner = findViewById<Spinner>(R.id.spinnerInvestorTarget)
        val startupTypeSpinner = findViewById<Spinner>(R.id.spinnerStartupType)
        val developmentalSpinner = findViewById<Spinner>(R.id.spinnerDevelopmental)
        val industrySpinner = findViewById<Spinner>(R.id.spinnerIndustry)

        // Get the entered values from the views
        val investorNameValue = investorNameEditText?.text?.toString()
        val investorNikValue = investorNikEditText?.text?.toString()
        val investorEmailValue = investorEmailEditText?.text?.toString()
        val investorTypeValue = investorTypeSpinner?.selectedItem?.toString()
        val investorExperienceValue = investorExperienceSpinner?.selectedItem?.toString()
        val investorTargetValue = investorTargetSpinner?.selectedItem?.toString()
        val startupTypeValue = startupTypeSpinner?.selectedItem?.toString()
        val developmentalLevelValue = developmentalSpinner?.selectedItem?.toString()
        val industryValue = industrySpinner?.selectedItem?.toString()

        // Save the entered values to SharedPreferences with the correct keys using dynamic index
        val editor = sharedPreferences.edit()
        editor.putString("nama_lengkap$currentQuestionIndex", investorNameValue)
        editor.putString("nik_investor$currentQuestionIndex", investorNikValue)
        editor.putString("email_investor$currentQuestionIndex", investorEmailValue)
        editor.putString("tipe_investor$currentQuestionIndex", investorTypeValue)
        editor.putString("pengalaman_investasi$currentQuestionIndex", investorExperienceValue)
        editor.putString("target_investasi$currentQuestionIndex", investorTargetValue)
        editor.putString("tipe_startup$currentQuestionIndex", startupTypeValue)
        editor.putString("target_perkembangan$currentQuestionIndex", developmentalLevelValue)
        editor.putString("target_industri$currentQuestionIndex", industryValue)
        editor.apply()
    }

    private fun loadEnteredValues() {
        // Get the references to the views
        val investorNameEditText = findViewById<EditText>(R.id.investorNameEditText)
        val investorNikEditText = findViewById<EditText>(R.id.investorNikEditText)
        val investorEmailEditText = findViewById<EditText>(R.id.investorEmailEditText)
        val investorTypeSpinner = findViewById<Spinner>(R.id.spinnerInvestorType)
        val investorExperienceSpinner = findViewById<Spinner>(R.id.spinnerInvestorExperience)
        val investorTargetSpinner = findViewById<Spinner>(R.id.spinnerInvestorTarget)
        val startupTypeSpinner = findViewById<Spinner>(R.id.spinnerStartupType)
        val developmentalSpinner = findViewById<Spinner>(R.id.spinnerDevelopmental)
        val industrySpinner = findViewById<Spinner>(R.id.spinnerIndustry)

        // Load the entered values from SharedPreferences
        val investorNameValue = sharedPreferences.getString("nama_lengkap$currentQuestionIndex", "")
        val investorNikValue = sharedPreferences.getString("nik_investor$currentQuestionIndex", "")
        val investorEmailValue = sharedPreferences.getString("email_investor$currentQuestionIndex", "")
        val investorTypeValue = sharedPreferences.getString("tipe_investor$currentQuestionIndex", "")
        val investorExperienceValue = sharedPreferences.getString("pengalaman_investasi$currentQuestionIndex", "")
        val investorTargetValue = sharedPreferences.getString("target_investasi$currentQuestionIndex", "")
        val startupTypeValue = sharedPreferences.getString("tipe_startup$currentQuestionIndex", "")
        val developmentalLevelValue = sharedPreferences.getString("target_perkembangan$currentQuestionIndex", "")
        val industryValue = sharedPreferences.getString("target_industri$currentQuestionIndex", "")

        // Set the entered values to the views
        investorNameEditText?.setText(investorNameValue)
        investorNikEditText?.setText(investorNikValue)
        investorEmailEditText?.setText(investorEmailValue)
        investorTypeSpinner?.setSelection(getSpinnerIndex(investorTypeSpinner, investorTypeValue))
        investorExperienceSpinner?.setSelection(getSpinnerIndex(investorExperienceSpinner, investorExperienceValue))
        investorTargetSpinner?.setSelection(getSpinnerIndex(investorTargetSpinner, investorTargetValue))
        startupTypeSpinner?.setSelection(getSpinnerIndex(startupTypeSpinner, startupTypeValue))
        developmentalSpinner?.setSelection(
            getSpinnerIndex(
                developmentalSpinner,
                developmentalLevelValue
            )
        )
        industrySpinner?.setSelection(getSpinnerIndex(industrySpinner, industryValue))
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String?): Int {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    private fun callFlaskAPI() {
        val url = "https://fundup-6pay5onqfa-et.a.run.app/investor"

        val client = OkHttpClient()

        // Build the POST request without a request body
        val requestBody = ByteArray(0).toRequestBody()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure to call API
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to call Flask API: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle API response
                val responseBody = response.body?.string()

                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Flask API response: $responseBody",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}