package com.example.compiladorespractica1

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.compiladorespractica1.adapter.ErrorAdapter
import com.example.compiladorespractica1.analyzer.LexerAnalyzer
import com.example.compiladorespractica1.analyzer.ParserAnalyzer
import com.example.compiladorespractica1.analyzer.models.TokenInfo
import com.example.compiladorespractica1.analyzer.models.ErrorInfo
import com.example.compiladorespractica1.processor.DiagramGenerator
import com.example.compiladorespractica1.processor.ConfigProcessor
import com.example.compiladorespractica1.views.DiagramCanvas

class MainActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var analyzeButton: Button
    private lateinit var clearButton: Button
    private lateinit var resultsTextView: TextView
    private lateinit var errorRecyclerView: RecyclerView
    private lateinit var diagramCanvas: DiagramCanvas
    private lateinit var diagramScrollView: ScrollView
    private lateinit var resultsScrollView: ScrollView
    private lateinit var errorAdapter: ErrorAdapter

    private val lexerAnalyzer = LexerAnalyzer()
    private val parserAnalyzer = ParserAnalyzer()
    private val diagramGenerator = DiagramGenerator()
    private val configProcessor = ConfigProcessor()

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
        diagramScrollView = findViewById(R.id.diagramScrollView)
        diagramCanvas = findViewById(R.id.diagramCanvas)
        resultsScrollView = findViewById(R.id.resultsScrollView)

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
        diagramScrollView.visibility = View.GONE  // <-- CAMBIO
        resultsScrollView.visibility = View.VISIBLE
    }

    private fun analyzeCode() {
        val code = inputEditText.text.toString()

        if (code.isEmpty()) {
            Toast.makeText(this, "Ingresa código para analizar", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpiar UI
        resultsTextView.text = ""
        errorAdapter.clearErrors()
        errorRecyclerView.visibility = View.GONE
        diagramScrollView.visibility = View.GONE  // <-- CAMBIO
        resultsScrollView.visibility = View.VISIBLE

        // Análisis léxico
        val lexerResult = lexerAnalyzer.analyze(code)
        Log.d("DEBUG", "Lexer: tokens=${lexerResult.tokens.size}, errores=${lexerResult.errores.size}")

        val parserResult = if (lexerResult.success) {
            parserAnalyzer.analyze(code)
        } else {
            ParserAnalyzer.ParserResult(emptyList(), false)
        }
        Log.d("DEBUG", "Parser: errores=${parserResult.errores.size}")

        val todosLosErrores = lexerResult.errores + parserResult.errores
        Log.d("DEBUG", "Total errores=${todosLosErrores.size}")

        if (todosLosErrores.isEmpty()) {
            showDiagram(lexerResult.tokens)
        } else {
            showErrorResult(todosLosErrores)
        }
    }

    private fun showDiagram(tokens: List<TokenInfo>) {
        try {
            configProcessor.procesarTokens(tokens)

            val elementos = diagramGenerator.generarElementos(tokens)

            diagramCanvas.setDiagrama(elementos) { id ->
                configProcessor.getConfigParaElemento(id)
            }

            diagramScrollView.visibility = View.VISIBLE
            resultsScrollView.visibility = View.GONE
            resultsTextView.text = "ANÁLISIS EXITOSO - Mostrando diagrama"

        } catch (e: Exception) {
            Log.e("DEBUG", "Error al generar diagrama: ${e.message}")
            resultsTextView.text = "Error al generar diagrama: ${e.message}"
            resultsScrollView.visibility = View.VISIBLE
        }
    }

    private fun showErrorResult(errores: List<ErrorInfo>) {
        errorAdapter.submitList(errores)
        errorRecyclerView.visibility = View.VISIBLE
        diagramScrollView.visibility = View.GONE
        resultsScrollView.visibility = View.GONE
        resultsTextView.text = "❌ SE ENCONTRARON ${errores.size} ERROR(ES)"
    }
}