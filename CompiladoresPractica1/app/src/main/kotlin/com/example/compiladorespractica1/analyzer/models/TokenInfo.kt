package com.example.compiladorespractica1.analyzer.models

import java_cup.runtime.Symbol

data class TokenInfo(
    val symbol: Symbol,
    val linea: Int,
    val columna: Int,
    val nombre: String,
    val valor: String
)