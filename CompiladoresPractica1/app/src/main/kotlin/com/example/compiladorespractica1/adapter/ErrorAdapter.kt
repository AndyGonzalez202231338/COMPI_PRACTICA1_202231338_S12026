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

            // Dar colores de fondo para ver si se renderizan
            tipoText.setBackgroundColor(android.graphics.Color.RED)
            mensajeText.setBackgroundColor(android.graphics.Color.YELLOW)
            lineaText.setBackgroundColor(android.graphics.Color.LTGRAY)
            columnaText.setBackgroundColor(android.graphics.Color.LTGRAY)
            tokenText.setBackgroundColor(android.graphics.Color.CYAN)

            Log.d("DEBUG", "Binding error: ${error.tipo} en línea ${error.linea}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_error, parent, false)
        return ErrorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
        Log.d("DEBUG", "onBindViewHolder posición $position")
        holder.bind(errores[position])
    }

    override fun getItemCount() = errores.size

    fun submitList(list: List<ErrorInfo>) {
        Log.d("DEBUG", "ErrorAdapter.submitList recibió ${list.size} errores")
        errores = list
        notifyDataSetChanged()
        Log.d("DEBUG", "notifyDataSetChanged llamado")
    }

    fun clearErrors() {
        Log.d("DEBUG", "clearErrors")
        errores = emptyList()
        notifyDataSetChanged()
    }
}