package com.example.compiladorespractica1

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var analyzeButton: Button
    private lateinit var clearButton: Button
    private lateinit var resultsTextView: TextView
    private lateinit var errorRecyclerView: RecyclerView
    private lateinit var errorAdapter: ErrorAdapter

    private val lexerAnalyzer = LexerAnalyzer()
    private val parserAnalyzer = ParserAnalyzer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        loadExample()
    }

    private fun initializeViews() {
        inputEditText = findViewById(R.id.inputEditText)
        analyzeButton = findViewById(R.id.analyzeButton)
        clearButton = findViewById(R.id.clearButton)
        resultsTextView = findViewById(R.id.resultsTextView)
        errorRecyclerView = findViewById(R.id.errorRecyclerView)

        errorAdapter = ErrorAdapter()
        errorRecyclerView.layoutManager = LinearLayoutManager(this)
        errorRecyclerView.adapter = errorAdapter
    }

    private fun setupListeners() {
        analyzeButton.setOnClickListener { analyzeCode() }
        clearButton.setOnClickListener { clearInput() }
    }

    private fun loadExample() {
        inputEditText.setText(getString(R.string.example_code))
    }

    private fun clearInput() {
        inputEditText.text.clear()
        resultsTextView.text = "Aquí se mostrarán los resultados..."
        errorAdapter.clearErrors()
        errorRecyclerView.visibility = View.GONE
    }

    private fun analyzeCode() {
        val code = inputEditText.text.toString()

        if (code.isEmpty()) {
            Toast.makeText(this, "Ingresa código para analizar", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpiar resultados anteriores
        resultsTextView.text = ""
        errorAdapter.clearErrors()
        errorRecyclerView.visibility = View.GONE

        // ============================================
        // ANÁLISIS LÉXICO
        // ============================================
        val lexerResult = lexerAnalyzer.analyze(code)

        // ============================================
        // ANÁLISIS SINTÁCTICO (solo si no hay errores léxicos)
        // ============================================
        val parserResult = if (lexerResult.success) {
            parserAnalyzer.analyze(code)
        } else {
            parserAnalyzer.ParserResult(emptyList(), false)
        }

        // ============================================
        // COMBINAR ERRORES
        // ============================================
        val todosLosErrores = lexerResult.errores + parserResult.errores

        // ============================================
        // MOSTRAR RESULTADOS
        // ============================================
        if (todosLosErrores.isEmpty()) {
            showSuccessResult(lexerResult.tokens)
        } else {
            showErrorResult(todosLosErrores)
        }
    }

    private fun showSuccessResult(tokens: List<LexerAnalyzer.TokenInfo>) {
        val tokenList = tokens.joinToString("\n") { token ->
            "  ${token.linea}:${token.columna} - ${token.nombre} (${token.valor})"
        }

        resultsTextView.text = """
            ✅ ANÁLISIS EXITOSO
            
            Tokens encontrados: ${tokens.size}
            
            Lista de tokens:
            $tokenList
            
            No se encontraron errores.
            
            El código es válido.
        """.trimIndent()
    }

    private fun showErrorResult(errores: List<ErrorInfo>) {
        errorAdapter.submitList(errores)
        errorRecyclerView.visibility = View.VISIBLE
        resultsTextView.text = "❌ SE ENCONTRARON ${errores.size} ERROR(ES)"
    }
}