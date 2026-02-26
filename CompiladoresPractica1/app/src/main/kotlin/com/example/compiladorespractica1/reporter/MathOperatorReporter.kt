package com.example.compiladorespractica1.reporter

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import com.example.compiladorespractica1.analyzer.models.TokenInfo

class MathOperatorReporter {

    data class OperatorOccurrence(
        val operador: String,
        val simbolo: String,
        val linea: Int,
        val columna: Int,
        val contexto: String
    )

    private fun findOperators(tokens: List<TokenInfo>): List<OperatorOccurrence> {
        val operadores = mutableListOf<OperatorOccurrence>()
        var i = 0

        while (i < tokens.size) {
            val token = tokens[i]
            var operador: String? = null
            var simbolo = ""

            when (token.nombre) {
                "MAS" -> {
                    operador = "MAS"
                    simbolo = "+"
                }
                "MENOS" -> {
                    operador = "MENOS"
                    simbolo = "-"
                }
                "POR" -> {
                    operador = "POR"
                    simbolo = "*"
                }
                "DIV" -> {
                    operador = "DIV"
                    simbolo = "/"
                }
            }

            if (operador != null) {
                val contexto = obtenerContexto(tokens, i)
                operadores.add(OperatorOccurrence(
                    operador = operador,
                    simbolo = simbolo,
                    linea = token.linea,
                    columna = token.columna,
                    contexto = contexto
                ))
            }
            i++
        }

        return operadores
    }

    private fun obtenerContexto(tokens: List<TokenInfo>, pos: Int): String {
        val inicio = maxOf(0, pos - 2)
        val fin = minOf(tokens.size - 1, pos + 2)
        val contexto = StringBuilder()

        for (j in inicio..fin) {
            if (j == pos) {
                contexto.append(" [${tokens[j].valor}] ")
            } else {
                contexto.append(" ${tokens[j].valor} ")
            }
        }

        return contexto.toString().trim()
    }
}