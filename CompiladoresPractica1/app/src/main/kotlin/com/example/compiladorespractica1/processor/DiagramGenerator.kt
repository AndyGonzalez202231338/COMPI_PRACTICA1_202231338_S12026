package com.example.compiladorespractica1.processor

import android.util.Log
import com.example.compiladorespractica1.analyzer.models.TokenInfo
import com.example.compiladorespractica1.analyzer.models.ElementoDiagrama

class DiagramGenerator {

    private var idCounter = 1
    private val pilaEstructuras = mutableListOf<Int>()
    private var enSeccionAlgoritmo = true

    fun generarElementos(tokens: List<TokenInfo>): List<ElementoDiagrama> {
        Log.d("DEBUG_TOKENS", "=== TODOS LOS TOKENS ===")
        tokens.forEachIndexed { index, token ->
            Log.d("DEBUG_TOKENS", "[$index] ${token.nombre} : ${token.valor} (línea ${token.linea})")
        }
        val elementos = mutableListOf<ElementoDiagrama>()
        idCounter = 1
        pilaEstructuras.clear()
        enSeccionAlgoritmo = true

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            // Detectar separador de secciones
            if (token.nombre == "SEP_SECCIONES") {
                enSeccionAlgoritmo = false
                i++
                continue
            }

            if (enSeccionAlgoritmo) {
                when (token.nombre) {
                    "INICIO" -> {
                        elementos.add(ElementoDiagrama(
                            id = idCounter++,
                            tipo = "INICIO",
                            texto = "INICIO",
                            linea = token.linea,
                            figura = "TERMINAL",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))
                        i++
                    }
                    "FIN" -> {
                        elementos.add(ElementoDiagrama(
                            id = idCounter++,
                            tipo = "FIN",
                            texto = "FIN",
                            linea = token.linea,
                            figura = "TERMINAL",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))
                        i++
                    }
                    "VAR" -> {
                        val identToken = tokens.getOrNull(i + 1)?.valor ?: ""
                        val igualToken = tokens.getOrNull(i + 2)

                        val texto = if (igualToken?.nombre == "ASIGNACION") {
                            val valorToken = tokens.getOrNull(i + 3)?.valor ?: ""
                            "VAR $identToken = $valorToken"
                        } else {
                            "VAR $identToken"
                        }

                        elementos.add(ElementoDiagrama(
                            id = idCounter++,
                            tipo = "VAR",
                            texto = texto,
                            linea = token.linea,
                            figura = "PROCESO",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))

                        i = if (igualToken?.nombre == "ASIGNACION") i + 4 else i + 2
                    }
                    "SI" -> {
                        var condicion = ""
                        var j = i + 2

                        while (j < tokens.size && tokens[j].nombre != "PAREN_CIERRA") {
                            condicion += tokens[j].valor + " "
                            j++
                        }

                        val id = idCounter++
                        elementos.add(ElementoDiagrama(
                            id = id,
                            tipo = "SI",
                            texto = condicion.trim(),
                            linea = token.linea,
                            figura = "DECISION",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))

                        pilaEstructuras.add(id)
                        i = j + 2
                    }
                    "FINSI" -> {
                        if (pilaEstructuras.isNotEmpty()) {
                            pilaEstructuras.removeAt(pilaEstructuras.size - 1)
                        }
                        i++
                    }
                    "MIENTRAS" -> {
                        var condicion = ""
                        var j = i + 2

                        while (j < tokens.size && tokens[j].nombre != "PAREN_CIERRA") {
                            condicion += tokens[j].valor + " "
                            j++
                        }

                        val id = idCounter++
                        elementos.add(ElementoDiagrama(
                            id = id,
                            tipo = "MIENTRAS",
                            texto = condicion.trim(),
                            linea = token.linea,
                            figura = "DECISION",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))

                        pilaEstructuras.add(id)
                        i = j + 2
                    }
                    "FINMIENTRAS" -> {
                        if (pilaEstructuras.isNotEmpty()) {
                            pilaEstructuras.removeAt(pilaEstructuras.size - 1)
                        }
                        i++
                    }
                    "MOSTRAR" -> {
                        val mensajeToken = tokens.getOrNull(i + 1)?.valor?.replace("\"", "") ?: ""
                        elementos.add(ElementoDiagrama(
                            id = idCounter++,
                            tipo = "MOSTRAR",
                            texto = mensajeToken,
                            linea = token.linea,
                            figura = "ENTRADA_SALIDA",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))
                        i += 2
                    }
                    "LEER" -> {
                        val varToken = tokens.getOrNull(i + 1)?.valor ?: ""
                        elementos.add(ElementoDiagrama(
                            id = idCounter++,
                            tipo = "LEER",
                            texto = "LEER $varToken",
                            linea = token.linea,
                            figura = "ENTRADA_SALIDA",
                            estructuraPadre = pilaEstructuras.lastOrNull()
                        ))
                        i += 2
                    }
                    "IDENTIFICADOR" -> {
                        if (i + 1 < tokens.size && tokens[i+1].nombre == "ASIGNACION") {
                            Log.d("DEBUG_ASIG", "¡Es una asignación!")
                            var expr = token.valor
                            expr += " = "
                            var j = i + 2
                            while (j < tokens.size &&
                                tokens[j].nombre != "SEP_SECCIONES" &&
                                tokens[j].nombre != "INICIO" &&
                                tokens[j].nombre != "FIN" &&
                                tokens[j].nombre != "SI" &&
                                tokens[j].nombre != "FINSI" &&
                                tokens[j].nombre != "MIENTRAS" &&
                                tokens[j].nombre != "FINMIENTRAS" &&
                                tokens[j].nombre != "MOSTRAR" &&
                                tokens[j].nombre != "LEER" &&
                                tokens[j].nombre != "VAR") {
                                expr += tokens[j].valor
                                j++
                            }

                            elementos.add(ElementoDiagrama(
                                id = idCounter++,
                                tipo = "ASIGNACION",
                                texto = expr,
                                linea = token.linea,
                                figura = "PROCESO",
                                estructuraPadre = pilaEstructuras.lastOrNull()
                            ))
                            i = j
                        } else {
                            i++
                        }
                    }
                    else -> i++
                }
            } else {
                i++
            }
        }
        return elementos
    }
}