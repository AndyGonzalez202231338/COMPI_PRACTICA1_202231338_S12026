package com.example.compiladorespractica1.analyzer

import com.example.compiladorespractica1.analyzer.models.ErrorInfo
import java.io.StringReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import android.util.Log
import lexer.Lexer
import lexer.Parser
import java_cup.runtime.ComplexSymbolFactory

class ParserAnalyzer {

    data class ParserResult(
        val errores: List<ErrorInfo>,
        val success: Boolean
    )

    fun analyze(input: String): ParserResult {
        Log.d("PARSER", "Iniciando análisis sintáctico")
        val sf = ComplexSymbolFactory()
        val lexer = Lexer(StringReader(input), sf)
        val parser = Parser(lexer, sf)

        val errores = mutableListOf<ErrorInfo>()

        val originalErr = System.err
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        System.setErr(ps)

        try {
            Log.d("PARSER", "Llamando a parser.parse()")
            parser.parse()
            Log.d("PARSER", "parser.parse() terminó normalmente")
        } catch (e: Exception) {
            // Ignorar errores de casteo
            if (!e.message?.contains("cannot be cast to", ignoreCase = true)!!) {
                Log.d("PARSER", "Excepción en parser.parse(): ${e.message}")
                errores.add(
                    ErrorInfo(
                        tipo = "SINTÁCTICO",
                        mensaje = e.message ?: "Error fatal",
                        linea = 0,
                        columna = 0,
                        token = ""
                    )
                )
            }
        } finally {
            System.setErr(originalErr)
            Log.d("PARSER", "Restaurado System.err")
        }

        // Procesar la salida capturada
        val output = baos.toString()
        val lineas = output.split("\n")
        var i = 0
        while (i < lineas.size) {
            val linea = lineas[i]

            // Ignorar líneas con errores de casteo
            if (linea.contains("cannot be cast to", ignoreCase = true) ||
                linea.contains("ClassCastException", ignoreCase = true)) {
                i++
                continue
            }

            if (linea.contains("=== ERROR SINTÁCTICO DETECTADO ===")) {
                val lineaLínea = lineas.getOrNull(i + 1) ?: ""
                val lineaToken = lineas.getOrNull(i + 2) ?: ""

                val lineaMatch = Regex("Línea: (\\d+), Columna: (\\d+)").find(lineaLínea)
                val tokenMatch = Regex("Token encontrado: (\\w+)").find(lineaToken)

                val lineaNum = lineaMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val columnaNum = lineaMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0
                val tokenStr = tokenMatch?.groupValues?.get(1) ?: ""

                errores.add(
                    ErrorInfo(
                        tipo = "SINTÁCTICO",
                        mensaje = "Error de sintaxis",
                        linea = lineaNum,
                        columna = columnaNum,
                        token = tokenStr
                    )
                )

                i += 3
            } else {
                i++
            }
        }

        return ParserResult(
            errores = errores,
            success = errores.isEmpty()
        )
    }
}