package com.example.compiladorespractica1.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.compiladorespractica1.R
import com.example.compiladorespractica1.analyzer.models.ErrorInfo

class ErrorAdapter : RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder>() {

    private var errores = listOf<ErrorInfo>()

    class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tipoText: TextView = itemView.findViewById(R.id.tipoText)
        private val mensajeText: TextView = itemView.findViewById(R.id.mensajeText)
        private val lineaText: TextView = itemView.findViewById(R.id.lineaText)
        private val columnaText: TextView = itemView.findViewById(R.id.columnaText)
        private val tokenText: TextView = itemView.findViewById(R.id.tokenText)

        fun bind(error: ErrorInfo) {
            tipoText.text = error.tipo
            mensajeText.text = error.mensaje
            lineaText.text = error.linea.toString()
            columnaText.text = error.columna.toString()
            tokenText.text = error.token

            tipoText.setBackgroundColor(android.graphics.Color.parseColor("#FF6B6B"))
            mensajeText.setBackgroundColor(android.graphics.Color.parseColor("#4ECDC4"))
            lineaText.setBackgroundColor(android.graphics.Color.parseColor("#95A5A6"))
            columnaText.setBackgroundColor(android.graphics.Color.parseColor("#95A5A6"))
            tokenText.setBackgroundColor(android.graphics.Color.parseColor("#F39C12"))

            tipoText.setTextColor(android.graphics.Color.BLACK)
            mensajeText.setTextColor(android.graphics.Color.BLACK)
            lineaText.setTextColor(android.graphics.Color.BLACK)
            columnaText.setTextColor(android.graphics.Color.BLACK)
            tokenText.setTextColor(android.graphics.Color.BLACK)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_error, parent, false)
        return ErrorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
        holder.bind(errores[position])
    }

    override fun getItemCount() = errores.size

    fun submitList(list: List<ErrorInfo>) {
        errores = list
        notifyDataSetChanged()
    }

    fun clearErrors() {
        errores = emptyList()
        notifyDataSetChanged()
    }
}