package com.example.compiladorespractica1

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.compiladorespractica1.adapter.ErrorAdapter
import com.example.compiladorespractica1.analyzer.LexerAnalyzer
import com.example.compiladorespractica1.analyzer.ParserAnalyzer
import com.example.compiladorespractica1.analyzer.models.TokenInfo
import com.example.compiladorespractica1.analyzer.models.ErrorInfo

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

        resultsTextView.text = ""
        errorAdapter.clearErrors()
        errorRecyclerView.visibility = View.GONE

        val lexerResult = lexerAnalyzer.analyze(code)
        Log.d("DEBUG", "Lexer: tokens=${lexerResult.tokens.size}, errores=${lexerResult.errores.size}, success=${lexerResult.success}")

        val parserResult = if (lexerResult.success) {
            parserAnalyzer.analyze(code)
        } else {
            ParserAnalyzer.ParserResult(emptyList(), false)
        }
        Log.d("DEBUG", "Parser: errores=${parserResult.errores.size}, success=${parserResult.success}")

        val todosLosErrores = lexerResult.errores + parserResult.errores
        Log.d("DEBUG", "Total errores=${todosLosErrores.size}")

        if (todosLosErrores.isEmpty()) {
            showSuccessResult(lexerResult.tokens)
        } else {
            showErrorResult(todosLosErrores)
        }
    }

    private fun showSuccessResult(tokens: List<TokenInfo>) {
        val tokenList = tokens.joinToString("\n") { token ->
            "  ${token.linea}:${token.columna} - ${token.nombre} (${token.valor})"
        }

        resultsTextView.text = """
            ANÁLISIS EXITOSO
            
            Tokens encontrados: ${tokens.size}
            
            Lista de tokens:
            $tokenList
            
            No se encontraron errores.
            
            El código es válido.
        """.trimIndent()
    }

    private fun showErrorResult(errores: List<ErrorInfo>) {
        Log.d("DEBUG", "=== showErrorResult INICIADO ===")
        Log.d("DEBUG", "Número de errores recibidos: ${errores.size}")

        if (!::errorAdapter.isInitialized) {
            Log.e("DEBUG", "ERROR: errorAdapter no inicializado")
            return
        }
        if (!::errorRecyclerView.isInitialized) {
            Log.e("DEBUG", "ERROR: errorRecyclerView no inicializado")
            return
        }

        errores.forEachIndexed { index, error ->
            Log.d("DEBUG", "Error[$index]: tipo=${error.tipo}, línea=${error.linea}, col=${error.columna}, token=${error.token}, mensaje=${error.mensaje}")
        }

        try {
            errorAdapter.submitList(errores)
            Log.d("DEBUG", "submitList ejecutado correctamente")
        } catch (e: Exception) {
            Log.e("DEBUG", "Excepción en submitList: ${e.message}")
        }

        errorRecyclerView.visibility = View.VISIBLE
        Log.d("DEBUG", "RecyclerView visibility cambiado a VISIBLE")

        resultsTextView.text = "SE ENCONTRARON ${errores.size} ERROR(ES)"
        Log.d("DEBUG", "Texto de resultados actualizado")

        errorRecyclerView.post {
            errorRecyclerView.requestLayout()
            Log.d("DEBUG", "requestLayout ejecutado en RecyclerView")
        }

        Log.d("DEBUG", "=== showErrorResult FINALIZADO ===")
    }
}