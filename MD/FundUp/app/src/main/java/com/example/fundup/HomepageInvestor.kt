package com.example.fundup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomepageInvestor : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StartupAdapter
    private val startupDataList: MutableList<StartupData> = mutableListOf()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_investor)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StartupAdapter(startupDataList)
        recyclerView.adapter = adapter

        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userID = auth.currentUser?.uid
        val inputId = userID.toString()

        db.collection("startup_matches").document(inputId).get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                val startupMatches = data?.get("startup_matches") as? List<String> ?: emptyList()
                val startupIdsMatches = startupMatches.toList()

                var fetchCount = 0
                for (startupId in startupIdsMatches) {
                    db.collection("startup").document(startupId).get()
                        .addOnSuccessListener { startupDocument ->
                            fetchCount++
                            if (startupDocument.exists()) {
                                val startupData = startupDocument.data
                                val namaLengkap = startupData?.get("nama_lengkap") as? String ?: ""
                                val nikStartup = startupData?.get("nik_startup")?.toString() ?: ""
                                val emailStartup = startupData?.get("email_startup") as? String ?: ""
                                val industriStartup = startupData?.get("industri_startup") as? String ?: ""
                                val tingkatPerkembanganPerusahaan = startupData?.get("tingkat_perkembangan_perusahaan") as? String ?: ""
                                val namaPerusahaan = startupData?.get("nama_perusahaan") as? String ?: ""
                                val targetPerusahaan = startupData?.get("target_perusahaan") as? String ?: ""

                                val startup = StartupData(
                                    namaLengkap,
                                    nikStartup,
                                    emailStartup,
                                    industriStartup,
                                    tingkatPerkembanganPerusahaan,
                                    namaPerusahaan,
                                    targetPerusahaan
                                )
                                startupDataList.add(startup)
                                adapter.notifyDataSetChanged()
                            } else {
                                println("No data found for Startup ID: $startupId")
                            }

                            if (fetchCount == startupIdsMatches.size) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Failed to fetch startup data: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("Failed to fetch startup matches data: ${exception.message}")
            }
    }
}