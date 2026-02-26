package com.example.compiladorespractica1.processor

import android.util.Log
import com.example.compiladorespractica1.analyzer.models.TokenInfo
import com.example.compiladorespractica1.analyzer.models.DiagramConfig
import java.util.Stack

class ConfigProcessor {
    private val configMap = mutableMapOf<Int, DiagramConfig>()

    fun procesarTokens(tokens: List<TokenInfo>) {
        configMap.clear()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            when (token.nombre) {
                "SEP_SECCIONES" -> {
                    i++
                    continue
                }
                else -> {
                    if (token.nombre.startsWith("CONF_") || token.nombre.startsWith("COLOR_") ||
                        token.nombre.startsWith("FIGURA_") || token.nombre.startsWith("LETRA_")) {
                        i = procesarInstruccion(tokens, i)
                    } else {
                        i++
                    }
                }
            }
        }
    }

    private fun procesarInstruccion(tokens: List<TokenInfo>, startIdx: Int): Int {
        var idx = startIdx
        val instrToken = tokens[idx]
        val instrName = instrToken.nombre

        if (idx + 1 >= tokens.size || tokens[idx + 1].nombre != "ASIGNACION") {
            return idx + 1
        }
        idx += 2

        // Recolectar tokens hasta PIPE
        val valorTokens = mutableListOf<TokenInfo>()
        while (idx < tokens.size && tokens[idx].nombre != "PIPE") {
            valorTokens.add(tokens[idx])
            idx++
        }
        if (idx >= tokens.size || tokens[idx].nombre != "PIPE") {
            return idx
        }
        idx++

        if (idx >= tokens.size || tokens[idx].nombre != "ENTERO") {
            return idx
        }
        val indice = tokens[idx].valor.toString().toInt()
        idx++

        val config = configMap.getOrPut(indice) { DiagramConfig() }

        when (instrName) {
            "CONF_DEFAULT" -> {
            }
            "COLOR_TEXTO_SI", "COLOR_TEXTO_MIENTRAS", "COLOR_TEXTO_BLOQUE",
            "COLOR_SI", "COLOR_MIENTRAS", "COLOR_BLOQUE" -> {
                val color = parseColor(valorTokens)
                if (color != null) {
                    when (instrName) {
                        "COLOR_TEXTO_SI", "COLOR_TEXTO_MIENTRAS", "COLOR_TEXTO_BLOQUE" ->
                            config.colorTexto = color
                        else -> config.colorFondo = color
                    }
                }
            }
            "FIGURA_SI", "FIGURA_MIENTRAS", "FIGURA_BLOQUE" -> {
                if (valorTokens.isNotEmpty() && valorTokens[0].nombre == "FIGURA") {
                    config.figura = valorTokens[0].valor.toString()
                }
            }
            "LETRA_SI", "LETRA_MIENTRAS", "LETRA_BLOQUE" -> {
                if (valorTokens.isNotEmpty() && valorTokens[0].nombre == "IDENTIFICADOR") {
                    config.letra = valorTokens[0].valor.toString()
                }
            }
            "LETRA_SIZE_SI", "LETRA_SIZE_MIENTRAS", "LETRA_SIZE_BLOQUE" -> {
                val tamano = evaluarExpresionNumerica(valorTokens)
                if (tamano != null) {
                    config.tamanoLetra = tamano
                }
            }
        }
        return idx
    }

    private fun parseColor(tokens: List<TokenInfo>): String? {
        if (tokens.isEmpty()) return null
        if (tokens.size == 1 && tokens[0].nombre == "COLOR_HEX") {
            var hex = tokens[0].valor.toString()
            // Eliminar la H inicial si existe
            if (hex.startsWith("H")) {
                hex = "#" + hex.substring(1)
            }
            // Asegurar que tenga 7 caracteres (# + 6 dígitos)
            if (hex.length == 7) {
                try {
                    android.graphics.Color.parseColor(hex)
                    return hex
                } catch (e: IllegalArgumentException) {
                    Log.e("CONFIG", "Color hexadecimal inválido: $hex")
                }
            }
            return "#FF00FF"
        }
        val componentes = mutableListOf<Int>()
        var i = 0
        while (i < tokens.size) {
            val exprTokens = mutableListOf<TokenInfo>()
            while (i < tokens.size && tokens[i].nombre != "COMA") {
                exprTokens.add(tokens[i])
                i++
            }
            if (exprTokens.isNotEmpty()) {
                val valor = evaluarExpresionNumerica(exprTokens)?.toInt()
                if (valor != null) {
                    componentes.add(valor)
                } else {
                    return null
                }
            }
            if (i < tokens.size && tokens[i].nombre == "COMA") {
                i++
            }
        }
        if (componentes.size == 3) {
            return String.format("#%02X%02X%02X", componentes[0], componentes[1], componentes[2])
        }
        return null
    }

    private fun evaluarExpresionNumerica(tokens: List<TokenInfo>): Float? {
        if (tokens.isEmpty()) return null
        if (tokens.size == 1 && tokens[0].nombre == "ENTERO") {
            return tokens[0].valor.toString().toFloat()
        }
        if (tokens.size == 1 && tokens[0].nombre == "DECIMAL") {
            return tokens[0].valor.toString().toFloat()
        }
        val valores = mutableListOf<Float>()
        val operadores = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            when (val token = tokens[i]) {
                is TokenInfo -> when (token.nombre) {
                    "ENTERO" -> valores.add(token.valor.toString().toFloat())
                    "DECIMAL" -> valores.add(token.valor.toString().toFloat())
                    "MAS" -> operadores.add("+")
                    "MENOS" -> operadores.add("-")
                    "POR" -> operadores.add("*")
                    "DIV" -> operadores.add("/")
                    "PAREN_ABRE" -> {
                        // Buscar el paréntesis que cierra
                        val subTokens = mutableListOf<TokenInfo>()
                        var nivel = 1
                        i++
                        while (i < tokens.size && nivel > 0) {
                            when (tokens[i].nombre) {
                                "PAREN_ABRE" -> nivel++
                                "PAREN_CIERRA" -> nivel--
                            }
                            if (nivel > 0) subTokens.add(tokens[i])
                            i++
                        }
                        val subResult = evaluarExpresionNumerica(subTokens)
                        if (subResult == null) return null
                        valores.add(subResult)
                        continue
                    }
                    else -> {
                        return null
                    }
                }
            }
            i++
        }
        var j = 0
        while (j < operadores.size) {
            if (operadores[j] == "*" || operadores[j] == "/") {
                val op = operadores.removeAt(j)
                val a = valores.removeAt(j)
                val b = valores.removeAt(j)
                val res = if (op == "*") a * b else a / b
                valores.add(j, res)
            } else {
                j++
            }
        }
        var resultado = valores[0]
        for (k in operadores.indices) {
            when (operadores[k]) {
                "+" -> resultado += valores[k + 1]
                "-" -> resultado -= valores[k + 1]
            }
        }
        return resultado
    }

    fun getConfigParaElemento(id: Int): DiagramConfig {
        return configMap[id] ?: DiagramConfig()
    }
}