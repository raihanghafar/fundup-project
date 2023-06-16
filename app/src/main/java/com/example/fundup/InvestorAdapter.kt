package com.example.fundup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InvestorAdapter(private val investorList: List<InvestorData>) : RecyclerView.Adapter<InvestorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_investor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val investor = investorList[position]
        holder.namaLengkapTextView.text = investor.namaLengkap
        holder.nikInvestorTextView.text = investor.nikInvestor
        holder.emailInvestorTextView.text = investor.emailInvestor
        holder.targetIndustriTextView.text = investor.targetIndustri
        holder.targetPerkembanganTextView.text = investor.targetPerkembangan
        holder.tipeInvestorTextView.text = investor.tipeInvestor
        holder.pengalamanInvestasiTextView.text = investor.pengalamanInvestasi
    }

    override fun getItemCount(): Int {
        return investorList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaLengkapTextView: TextView = itemView.findViewById(R.id.namaLengkapTextView)
        val nikInvestorTextView: TextView = itemView.findViewById(R.id.nikInvestorTextView)
        val emailInvestorTextView: TextView = itemView.findViewById(R.id.emailInvestorTextView)
        val targetIndustriTextView: TextView = itemView.findViewById(R.id.targetIndustriTextView)
        val targetPerkembanganTextView: TextView = itemView.findViewById(R.id.targetPerkembanganTextView)
        val tipeInvestorTextView: TextView = itemView.findViewById(R.id.tipeInvestorTextView)
        val pengalamanInvestasiTextView: TextView = itemView.findViewById(R.id.pengalamanInvestasiTextView)
    }
}
