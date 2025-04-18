package com.example.bubu.CSVRow

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CSVRowAdapter(private val data: List<CSVRow>) :
    RecyclerView.Adapter<CSVRowAdapter.CSVRowViewHolder>() {

    inner class CSVRowViewHolder(val rowLayout: LinearLayout) : RecyclerView.ViewHolder(rowLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CSVRowViewHolder {
        val rowLayout = LinearLayout(parent.context)
        rowLayout.orientation = LinearLayout.HORIZONTAL
        return CSVRowViewHolder(rowLayout)
    }

    override fun onBindViewHolder(holder: CSVRowViewHolder, position: Int) {
        val row = data[position]
        holder.rowLayout.removeAllViews()
        row.values.forEach {
            val cell = TextView(holder.rowLayout.context).apply {
                text = it
                setPadding(16, 8, 16, 8)
                setTextColor(Color.BLACK)
            }
            holder.rowLayout.addView(cell)
        }
    }

    override fun getItemCount(): Int = data.size
}
