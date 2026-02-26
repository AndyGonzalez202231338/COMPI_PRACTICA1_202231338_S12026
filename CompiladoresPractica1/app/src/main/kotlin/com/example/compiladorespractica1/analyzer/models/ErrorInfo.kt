package com.example.compiladorespractica1.analyzer.models

data class ErrorInfo(
    val tipo: String,
    val mensaje: String,
    val linea: Int,
    val columna: Int,
    val token: String
)