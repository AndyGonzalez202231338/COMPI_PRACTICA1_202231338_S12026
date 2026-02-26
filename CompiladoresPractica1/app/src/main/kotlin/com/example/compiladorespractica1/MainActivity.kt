package com.example.compiladorespractica1

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.compiladorespractica1.adapter.ErrorAdapter
import com.example.compiladorespractica1.adapter.OperadorAdapter
import com.example.compiladorespractica1.adapter.OperadorInfo
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
    private lateinit var errorRecyclerView: RecyclerView
    private lateinit var errorAdapter: ErrorAdapter
    private lateinit var operadoresAdapter: OperadorAdapter
    private lateinit var operadoresRecyclerView: RecyclerView
    private lateinit var codeVisualization: TextView
    private lateinit var erroresTitle: TextView
    private lateinit var reporteTitle: TextView
    private lateinit var operadoresHeader: LinearLayout
    private lateinit var diagramScrollView: ScrollView
    private lateinit var diagramCanvas: DiagramCanvas

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
        errorRecyclerView = findViewById(R.id.errorRecyclerView)
        operadoresRecyclerView = findViewById(R.id.operadoresRecyclerView)
        codeVisualization = findViewById(R.id.codeVisualization)
        erroresTitle = findViewById(R.id.erroresTitle)
        reporteTitle = findViewById(R.id.reporteTitle)
        operadoresHeader = findViewById(R.id.operadoresHeader)
        diagramScrollView = findViewById(R.id.diagramScrollView)
        diagramCanvas = findViewById(R.id.diagramCanvas)

        errorAdapter = ErrorAdapter()
        errorRecyclerView.layoutManager = LinearLayoutManager(this)
        errorRecyclerView.adapter = errorAdapter

        operadoresAdapter = OperadorAdapter()
        operadoresRecyclerView.layoutManager = LinearLayoutManager(this)
        operadoresRecyclerView.adapter = operadoresAdapter
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
        codeVisualization.text = "Aquí se mostrarán los resultados..."
        codeVisualization.visibility = View.VISIBLE
        errorAdapter.clearErrors()
        operadoresAdapter.submitList(emptyList())
        errorRecyclerView.visibility = View.GONE
        erroresTitle.visibility = View.GONE
        reporteTitle.visibility = View.VISIBLE
        operadoresHeader.visibility = View.VISIBLE
        diagramScrollView.visibility = View.GONE
    }

    private fun analyzeCode() {
        val code = inputEditText.text.toString()

        if (code.isEmpty()) {
            Toast.makeText(this, "Ingresa código para analizar", Toast.LENGTH_SHORT).show()
            return
        }
        codeVisualization.text = ""
        errorAdapter.clearErrors()
        operadoresAdapter.submitList(emptyList())
        errorRecyclerView.visibility = View.GONE
        erroresTitle.visibility = View.GONE
        diagramScrollView.visibility = View.GONE

        // Análisis léxico
        val lexerResult = lexerAnalyzer.analyze(code)

        // Análisis sintáctico
        val parserResult = parserAnalyzer.analyze(code)

        // Análisis de operadores matemáticos
        val operadores = analyzeOperators(code)
        operadoresAdapter.submitList(operadores)

        val codeFormatted = formatCodeForDisplay(code)
        codeVisualization.text = codeFormatted

        // Jugntando lso errores lexicos y sintacticos
        val todosLosErrores = lexerResult.errores + parserResult.errores

        if (todosLosErrores.isEmpty()) {
            showSuccessResult(lexerResult.tokens)
        } else {
            showErrorResult(todosLosErrores)
        }
    }

    private fun analyzeOperators(code: String): List<OperadorInfo> {
        val operadores = mutableListOf<OperadorInfo>()
        val lineas = code.split("\n")
        lineas.forEachIndexed { index, linea ->
            var pos = 0
            while (pos < linea.length) {
                when {
                    linea.startsWith("+", pos) -> {
                        operadores.add(OperadorInfo("+", index + 1, pos + 1, linea.trim()))
                        pos++
                    }
                    linea.startsWith("-", pos) && (pos == 0 || !linea[pos - 1].isDigit()) -> {
                        operadores.add(OperadorInfo("-", index + 1, pos + 1, linea.trim()))
                        pos++
                    }
                    linea.startsWith("*", pos) -> {
                        operadores.add(OperadorInfo("*", index + 1, pos + 1, linea.trim()))
                        pos++
                    }
                    linea.startsWith("/", pos) -> {
                        operadores.add(OperadorInfo("/", index + 1, pos + 1, linea.trim()))
                        pos++
                    }
                    else -> pos++
                }
            }
        }
        return operadores
    }

    private fun formatCodeForDisplay(code: String): String {
        return code.split("\n").joinToString("\n") { linea ->
            when {
                linea.contains("INICIO") -> "• $linea"
                linea.contains("FIN") -> "• $linea"
                linea.contains("VAR") -> "  • $linea"
                linea.contains("SI") -> "  • $linea"
                linea.contains("MIENTRAS") -> "  • $linea"
                linea.contains("MOSTRAR") -> "    • $linea"
                linea.contains("%") -> "\n$linea"
                else -> linea
            }
        }
    }

    private fun showSuccessResult(tokens: List<TokenInfo>) {
        try {
            configProcessor.procesarTokens(tokens)
            val elementos = diagramGenerator.generarElementos(tokens)
            if (elementos.isNotEmpty()) {
                diagramCanvas.setDiagrama(elementos) { id ->
                    configProcessor.getConfigParaElemento(id)
                }
                diagramCanvas.invalidate()
                diagramScrollView.visibility = View.VISIBLE
                codeVisualization.visibility = View.GONE
            } else {
                codeVisualization.text = "No se pudo generar el diagrama (estructura vacía)"
                codeVisualization.visibility = View.VISIBLE
                diagramScrollView.visibility = View.GONE
            }
            errorRecyclerView.visibility = View.GONE
            erroresTitle.visibility = View.GONE
            reporteTitle.visibility = View.VISIBLE
            operadoresHeader.visibility = View.VISIBLE
        } catch (e: Exception) {
            codeVisualization.text = "Error al generar diagrama: ${e.message}"
            codeVisualization.visibility = View.VISIBLE
            diagramScrollView.visibility = View.GONE
        }
    }

    private fun showErrorResult(errores: List<ErrorInfo>) {
        errorAdapter.submitList(errores)
        errorRecyclerView.visibility = View.VISIBLE
        erroresTitle.visibility = View.VISIBLE
        diagramScrollView.visibility = View.GONE
        codeVisualization.visibility = View.VISIBLE
        reporteTitle.visibility = View.VISIBLE
        operadoresHeader.visibility = View.VISIBLE
    }
}