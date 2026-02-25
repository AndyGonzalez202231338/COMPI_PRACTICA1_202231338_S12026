package com.example.compiladorespractica1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var analyzeButton: Button
    private lateinit var clearButton: Button
    private lateinit var resultsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        inputEditText = findViewById(R.id.inputEditText)
        analyzeButton = findViewById(R.id.analyzeButton)
        clearButton = findViewById(R.id.clearButton)
        resultsTextView = findViewById(R.id.resultsTextView)

        // Configurar texto de ejemplo
        inputEditText.setText(getString(R.string.example_code))

        // Configurar botón de análisis (pendiente de implementar)
        analyzeButton.setOnClickListener {
            analyzeCode()
        }

        // Configurar botón de limpiar
        clearButton.setOnClickListener {
            inputEditText.text.clear()
            resultsTextView.text = "Aquí se mostrarán los resultados..."
            Toast.makeText(this, "Texto limpiado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeCode() {
        val code = inputEditText.text.toString().trim()

        if (code.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa código para analizar", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar mensaje de procesamiento (temporal)
        resultsTextView.text = """
            === ANÁLISIS RECIBIDO ===
            
            Longitud: ${code.length} caracteres
            
            Primeros 100 caracteres:
            ${code.take(100)}
            
            ${if (code.length > 100) "..." else ""}
            
            Próximamente: Integración con JFlex y CUP
        """.trimIndent()
    }
}