package com.example.fundup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomepageStartup : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InvestorAdapter
    private val investorDataList: MutableList<InvestorData> = mutableListOf()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_startup)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = InvestorAdapter(investorDataList)
        recyclerView.adapter = adapter

        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userID = auth.currentUser?.uid
        val inputId = userID.toString()

        db.collection("investor_matches").document(inputId).get()
            .addOnSuccessListener { documentSnapshot ->
                val data = documentSnapshot.data
                val investorMatches = data?.get("investor_matches") as? List<String> ?: emptyList()
                val investorIdsMatches = investorMatches.toList()

                var fetchCount = 0
                for (investorId in investorIdsMatches) {
                    db.collection("investor_loker").document(investorId).get()
                        .addOnSuccessListener { investorDocument ->
                            fetchCount++
                            if (investorDocument.exists()) {
                                val investorData = investorDocument.data
                                val namaLengkap = investorData?.get("nama_lengkap") as? String ?: ""
                                val nikInvestor = investorData?.get("nik_investor")?.toString() ?: ""
                                val emailInvestor = investorData?.get("email_investor") as? String ?: ""
                                val targetIndustri = investorData?.get("target_industri") as? String ?: ""
                                val targetPerkembangan = investorData?.get("target_perkembangan") as? String ?: ""
                                val tipeInvestor = investorData?.get("tipe_investor") as? String ?: ""
                                val pengalamanInvestasi = investorData?.get("pengalaman_investasi") as? String ?: ""

                                val investor = InvestorData(
                                    namaLengkap,
                                    nikInvestor,
                                    emailInvestor,
                                    targetIndustri,
                                    targetPerkembangan,
                                    tipeInvestor,
                                    pengalamanInvestasi
                                )
                                investorDataList.add(investor)
                                adapter.notifyDataSetChanged()
                            } else {
                                println("No data found for Investor ID: $investorId")
                            }

                            if (fetchCount == investorIdsMatches.size) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Failed to fetch investor data: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("Failed to fetch investor matches data: ${exception.message}")
            }
    }
}