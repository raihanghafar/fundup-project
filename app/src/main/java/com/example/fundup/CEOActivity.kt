package com.example.fundup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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

class CEOActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val questionPages = listOf(
        R.layout.activity_ceo_question1,
        R.layout.activity_ceo_question2,
        R.layout.activity_ceo_question3,
        R.layout.activity_ceo_question4,
        R.layout.activity_ceo_question5
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

        sharedPreferences = getSharedPreferences("CEOPreferences", Context.MODE_PRIVATE)

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
                previousButton.visibility = View.GONE
            } else {
                previousButton.visibility = View.VISIBLE
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
                    val ceoNameValue = sharedPreferences.getString("nama_lengkap$i", null)
                    val ceoNikValue = sharedPreferences.getString("nik_startup$i", null)
                    val ceoEmailValue = sharedPreferences.getString("email_startup$i", null)
                    val startupNameValue = sharedPreferences.getString("nama_perusahaan$i", null)
                    val startupWebsiteValue = sharedPreferences.getString("website_perusahaan$i", null)
                    val targetValue = sharedPreferences.getString("target_perusahaan$i", null)
                    val developmentalLevelValue = sharedPreferences.getString("tingkat_perkembangan_perusahaan$i", null)
                    val industryValue = sharedPreferences.getString("industri_startup$i", null)

                    // Add non-null values to the data HashMap
                    ceoNameValue?.let { data["nama_lengkap"] = it }
                    ceoNikValue?.let { data["nik_startup"] = it }
                    ceoEmailValue?.let { data["email_startup"] = it }
                    startupNameValue?.let { data["nama_perusahaan"] = it }
                    startupWebsiteValue?.let { data["website_perusahaan"] = it }
                    targetValue?.let { data["target_perusahaan"] = it }
                    developmentalLevelValue?.let { data["tingkat_perkembangan_perusahaan"] = it }
                    industryValue?.let { data["industri_startup"] = it }

                    // Print the input data in the logcat
                    Log.d("InputData", "CEO Name: $ceoNameValue")
                    Log.d("InputData", "CEO NIK: $ceoNikValue")
                    Log.d("InputData", "CEO Email: $ceoEmailValue")
                    Log.d("InputData", "Startup Name: $startupNameValue")
                    Log.d("InputData", "Startup Website: $startupWebsiteValue")
                    Log.d("InputData", "Target: $targetValue")
                    Log.d("InputData", "Developmental Level: $developmentalLevelValue")
                    Log.d("InputData", "Industry: $industryValue")
                }

                // Save the entered values to Firestore with the user ID as the document ID
                db.collection("startup")
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

                val intent = Intent(this, LoadingStartup::class.java)
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
        val ceoNameEditText = findViewById<EditText>(R.id.ceoNameEditText)
        val ceoNikEditText = findViewById<EditText>(R.id.ceoNikEditText)
        val ceoEmailEditText = findViewById<EditText>(R.id.ceoEmailEditText)
        val startupNameEditText = findViewById<EditText>(R.id.startupNameEditText)
        val startupWebsiteEditText = findViewById<EditText>(R.id.startupWebsiteEditText)
        val targetSpinner = findViewById<Spinner>(R.id.spinnerTarget)
        val developmentalSpinner = findViewById<Spinner>(R.id.spinnerDevelopmental)
        val industrySpinner = findViewById<Spinner>(R.id.spinnerIndustry)

        // Get the entered values from the views
        val ceoNameValue = ceoNameEditText?.text?.toString()
        val ceoNikValue = ceoNikEditText?.text?.toString()
        val ceoEmailValue = ceoEmailEditText?.text?.toString()
        val startupNameValue = startupNameEditText?.text?.toString()
        val startupWebsiteValue = startupWebsiteEditText?.text?.toString()
        val targetValue = targetSpinner?.selectedItem?.toString()
        val developmentalLevelValue = developmentalSpinner?.selectedItem?.toString()
        val industryValue = industrySpinner?.selectedItem?.toString()

        // Save the entered values to SharedPreferences with the correct keys using dynamic index
        val editor = sharedPreferences.edit()
        editor.putString("nama_lengkap$currentQuestionIndex", ceoNameValue)
        editor.putString("nik_startup$currentQuestionIndex", ceoNikValue)
        editor.putString("email_startup$currentQuestionIndex", ceoEmailValue)
        editor.putString("nama_perusahaan$currentQuestionIndex", startupNameValue)
        editor.putString("website_perusahaan$currentQuestionIndex", startupWebsiteValue)
        editor.putString("target_perusahaan$currentQuestionIndex", targetValue)
        editor.putString("tingkat_perkembangan_perusahaan$currentQuestionIndex", developmentalLevelValue)
        editor.putString("industri_startup$currentQuestionIndex", industryValue)
        editor.apply()
    }

    private fun loadEnteredValues() {
        // Get the references to the views
        val ceoNameEditText = findViewById<EditText>(R.id.ceoNameEditText)
        val ceoNikEditText = findViewById<EditText>(R.id.ceoNikEditText)
        val ceoEmailEditText = findViewById<EditText>(R.id.ceoEmailEditText)
        val startupNameEditText = findViewById<EditText>(R.id.startupNameEditText)
        val startupWebsiteEditText = findViewById<EditText>(R.id.startupWebsiteEditText)
        val targetSpinner = findViewById<Spinner>(R.id.spinnerTarget)
        val developmentalSpinner = findViewById<Spinner>(R.id.spinnerDevelopmental)
        val industrySpinner = findViewById<Spinner>(R.id.spinnerIndustry)

        // Load the entered values from SharedPreferences
        val ceoNameValue = sharedPreferences.getString("nama_lengkap$currentQuestionIndex", "")
        val ceoNikValue = sharedPreferences.getString("nik_startup$currentQuestionIndex", "")
        val ceoEmailValue = sharedPreferences.getString("email_startup$currentQuestionIndex", "")
        val startupNameValue = sharedPreferences.getString("nama_perusahaan$currentQuestionIndex", "")
        val startupWebsiteValue =
            sharedPreferences.getString("website_perusahaan$currentQuestionIndex", "")
        val targetValue = sharedPreferences.getString("target_perusahaan$currentQuestionIndex", "")
        val developmentalLevelValue =
            sharedPreferences.getString("tingkat_perkembangan_perusahaan$currentQuestionIndex", "")
        val industryValue = sharedPreferences.getString("industri_startup$currentQuestionIndex", "")

        // Set the entered values to the views
        ceoNameEditText?.setText(ceoNameValue)
        ceoNikEditText?.setText(ceoNikValue)
        ceoEmailEditText?.setText(ceoEmailValue)
        startupNameEditText?.setText(startupNameValue)
        startupWebsiteEditText?.setText(startupWebsiteValue)
        targetSpinner?.setSelection(getSpinnerIndex(targetSpinner, targetValue))
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
        val url = "https://fundup-6pay5onqfa-et.a.run.app/startup"

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