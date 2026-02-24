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
    private lateinit var resultsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        inputEditText = findViewById(R.id.inputEditText)
        analyzeButton = findViewById(R.id.analyzeButton)
        resultsTextView = findViewById(R.id.resultsTextView)

        // Configurar texto de ejemplo
        inputEditText.setText(getString(R.string.example_code))

        // Configurar botón de análisis
        analyzeButton.setOnClickListener {
            analyzeCode()
        }
    }

    private fun analyzeCode() {
        val code = inputEditText.text.toString().trim()

        if (code.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa código para analizar", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar mensaje de procesamiento
        resultsTextView.text = "Procesando análisis...\n"

        try {
            // Aquí integraremos el lexer y parser
            // Por ahora solo mostramos el texto ingresado
            val result = """
                === ANÁLISIS RECIBIDO ===
                
                Longitud: ${code.length} caracteres
                
                Primeros 100 caracteres:
                ${code.take(100)}
                
                ${if (code.length > 100) "..." else ""}
                
                Próximamente: Integración con JFlex y CUP
            """.trimIndent()

            resultsTextView.text = result

        } catch (e: Exception) {
            resultsTextView.text = "Error durante el análisis:\n${e.message}"
            e.printStackTrace()
        }
    }
}