package com.example.compiladorespractica1.analyzer.models

data class DiagramaFlujo(
    val elementos: List<ElementoDiagrama>,
    val configPorDefecto: DiagramConfig,
    val configuraciones: Map<Int, DiagramConfig>
)