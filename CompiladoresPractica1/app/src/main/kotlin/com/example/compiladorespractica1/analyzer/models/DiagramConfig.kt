package com.example.compiladorespractica1.analyzer.models

data class DiagramConfig(
    var colorTexto: String = "#FFFFFF",
    var colorFondo: String = "#333333",
    var figura: String = "PROCESO",
    var letra: String = "SANS_SERIF",
    var tamanoLetra: Float = 24f,
    var default: Boolean = false
)