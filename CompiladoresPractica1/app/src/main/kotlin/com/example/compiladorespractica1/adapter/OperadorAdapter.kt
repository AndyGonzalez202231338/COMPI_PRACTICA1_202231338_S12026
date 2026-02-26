package com.example.compiladorespractica1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.compiladorespractica1.R

data class OperadorInfo(
    val operador: String,
    val linea: Int,
    val columna: Int,
    val contexto: String
)

class OperadorAdapter : RecyclerView.Adapter<OperadorAdapter.OperadorViewHolder>() {

    private var operadores = listOf<OperadorInfo>()

    class OperadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val operadorText: TextView = itemView.findViewById(R.id.operadorText)
        private val lineaText: TextView = itemView.findViewById(R.id.lineaOperadorText)
        private val columnaText: TextView = itemView.findViewById(R.id.columnaOperadorText)
        private val contextoText: TextView = itemView.findViewById(R.id.contextoText)

        fun bind(operador: OperadorInfo) {
            operadorText.text = operador.operador
            lineaText.text = operador.linea.toString()
            columnaText.text = operador.columna.toString()
            contextoText.text = operador.contexto
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_operador, parent, false)
        return OperadorViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperadorViewHolder, position: Int) {
        holder.bind(operadores[position])
    }

    override fun getItemCount(): Int = operadores.size

    fun submitList(list: List<OperadorInfo>) {
        operadores = list
        notifyDataSetChanged()
    }

    fun clearOperators() {
        operadores = emptyList()
        notifyDataSetChanged()
    }
}