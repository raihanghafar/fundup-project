package com.example.fundup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StartupAdapter(private val startupList: List<StartupData>) : RecyclerView.Adapter<StartupAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_startup, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val startup = startupList[position]
        holder.namaLengkapTextView.text = startup.namaLengkap
        holder.nikStartupTextView.text = startup.nikStartup
        holder.emailStartupTextView.text = startup.emailStartup
        holder.industriStartupTextView.text = startup.industriStartup
        holder.tingkatPerkembanganPerusahaanTextView.text = startup.tingkatPerkembanganPerusahaan
        holder.namaPerusahaanTextView.text = startup.namaPerusahaan
        holder.targetPerusahaanTextView.text = startup.targetPerusahaan
    }

    override fun getItemCount(): Int {
        return startupList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaLengkapTextView: TextView = itemView.findViewById(R.id.namaLengkapTextView)
        val nikStartupTextView: TextView = itemView.findViewById(R.id.nikStartupTextView)
        val emailStartupTextView: TextView = itemView.findViewById(R.id.emailStartupTextView)
        val industriStartupTextView: TextView = itemView.findViewById(R.id.industriStartupTextView)
        val tingkatPerkembanganPerusahaanTextView: TextView = itemView.findViewById(R.id.tingkatPerkembanganPerusahaanTextView)
        val namaPerusahaanTextView: TextView = itemView.findViewById(R.id.namaPerusahaanTextView)
        val targetPerusahaanTextView: TextView = itemView.findViewById(R.id.targetPerusahaanTextView)
    }
}
