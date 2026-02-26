package com.example.compiladorespractica1.analyzer.models

data class ElementoDiagrama(
    val id: Int,
    val tipo: String,
    val texto: String,
    val linea: Int,
    val figura: String,
    val estructuraPadre: Int? = null
)