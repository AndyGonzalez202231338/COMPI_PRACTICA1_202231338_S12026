package com.example.compiladorespractica1.analyzer

import com.example.compiladorespractica1.analyzer.models.ErrorInfo
import com.example.compiladorespractica1.analyzer.models.TokenInfo
import com.example.compiladorespractica1.utils.TokenHelper
import java.io.StringReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import lexer.Lexer
import lexer.sym
import java_cup.runtime.ComplexSymbolFactory
import java_cup.runtime.Symbol

class LexerAnalyzer {

    data class LexerResult(
        val tokens: List<TokenInfo>,
        val errores: List<ErrorInfo>,
        val success: Boolean
    )

    fun analyze(input: String): LexerResult {
        val sf = ComplexSymbolFactory()
        val lexer = Lexer(StringReader(input), sf)

        val tokens = mutableListOf<TokenInfo>()
        val errores = mutableListOf<ErrorInfo>()

        val originalOut = System.out
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)
        System.setOut(ps)

        try {
            var token: Symbol? = lexer.next_token()
            while (token != null && token.sym != sym.EOF) {
                val tokenName = TokenHelper.getTokenName(token.sym)
                val linea = lexer.getLine()
                val columna = lexer.getColumn()
                val valor = token.value?.toString() ?: "-"

                tokens.add(
                    TokenInfo(
                        symbol = token,
                        linea = linea,
                        columna = columna,
                        nombre = tokenName,
                        valor = valor
                    )
                )
                token = lexer.next_token()
            }
        } catch (e: Exception) {
            errores.add(
                ErrorInfo(
                    tipo = "LÉXICO",
                    mensaje = e.message ?: "Error léxico desconocido",
                    linea = lexer.getLine(),
                    columna = lexer.getColumn(),
                    token = ""
                )
            )
        } finally {
            System.setOut(originalOut)
        }

        val output = baos.toString()
        if (output.isNotEmpty()) {
            val lineas = output.split("\n")
            for (linea in lineas) {
                if (linea.contains("Error léxico:")) {
                    // Extraer información del error
                    val match = Regex("Error léxico: (.) en línea (\\d+), columna (\\d+)").find(linea)
                    if (match != null) {
                        val caracter = match.groupValues[1]
                        val lineaNum = match.groupValues[2].toInt()
                        val columnaNum = match.groupValues[3].toInt()

                        errores.add(
                            ErrorInfo(
                                tipo = "LÉXICO",
                                mensaje = "Caracter no válido: '$caracter'",
                                linea = lineaNum,
                                columna = columnaNum,
                                token = caracter
                            )
                        )
                    } else {
                        errores.add(
                            ErrorInfo(
                                tipo = "LÉXICO",
                                mensaje = linea,
                                linea = 0,
                                columna = 0,
                                token = ""
                            )
                        )
                    }
                }
            }
        }

        return LexerResult(
            tokens = tokens,
            errores = errores,
            success = errores.isEmpty()
        )
    }
}