package com.example.compiladorespractica1.views

import android.content.Context
import android.graphics.*
import android.util.Log
import android.util.AttributeSet
import android.view.View
import com.example.compiladorespractica1.analyzer.models.ElementoDiagrama
import com.example.compiladorespractica1.analyzer.models.DiagramConfig

class DiagramCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var elementos: List<ElementoDiagrama> = emptyList()
    private var configGetter: (Int) -> DiagramConfig = { DiagramConfig() }

    private var elementosPorEstructura: Map<Int?, List<ElementoDiagrama>> = emptyMap()
    private var diagramHeight = 0f

    fun setDiagrama(elementos: List<ElementoDiagrama>, configGetter: (Int) -> DiagramConfig) {
        this.elementos = elementos
        this.configGetter = configGetter
        this.elementosPorEstructura = elementos.groupBy { it.estructuraPadre }

        calcularAlturaTotal()
        requestLayout()
        invalidate()
    }

    private fun calcularAlturaTotal() {
        var altura = 100f
        val raices = elementosPorEstructura[null] ?: emptyList()
        altura = calcularAlturaNivel(raices, altura)
        diagramHeight = altura + 100f
    }

    private fun calcularAlturaNivel(
        elementosNivel: List<ElementoDiagrama>,
        yInicial: Float
    ): Float {
        var yActual = yInicial

        for (elemento in elementosNivel) {
            yActual += 80f

            val hijos = elementosPorEstructura[elemento.id] ?: emptyList()
            if (hijos.isNotEmpty()) {
                if (elemento.figura == "DECISION") {
                    yActual = calcularAlturaNivel(hijos, yActual + 50f)
                    yActual += 100f
                } else {
                    yActual = calcularAlturaNivel(hijos, yActual)
                }
            }

            yActual += 50f
        }

        return yActual
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = diagramHeight.toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (elementos.isEmpty()) return

        val startX = width / 2f
        val raices = elementosPorEstructura[null] ?: emptyList()
        dibujarNivel(canvas, raices, startX, 100f)
    }

    private fun dibujarNivel(
        canvas: Canvas,
        elementosNivel: List<ElementoDiagrama>,
        x: Float,
        yInicial: Float
    ): Float {
        var yActual = yInicial
        var indice = 0

        while (indice < elementosNivel.size) {
            val elemento = elementosNivel[indice]
            val config = configGetter(elemento.id)

            paint.color = safeParseColor(config.colorFondo)
            paint.textSize = config.tamanoLetra
            paint.typeface = when (config.letra.uppercase()) {
                "ARIAL" -> Typeface.create("arial", Typeface.NORMAL)
                else -> Typeface.SANS_SERIF
            }

            // Dibujar según el tipo de figura
            when (elemento.figura.uppercase()) {
                "TERMINAL", "CIRCULO", "ELIPSE" -> drawTerminal(canvas, x, yActual, elemento.texto, config)
                "DECISION", "ROMBO" -> drawDecision(canvas, x, yActual, elemento.texto, config)
                "ENTRADA_SALIDA", "PARALELOGRAMO" -> drawEntradaSalida(canvas, x, yActual, elemento.texto, config)
                "RECTANGULO_REDONDEADO" -> drawRectanguloRedondeado(canvas, x, yActual, elemento.texto, config)
                else -> drawProceso(canvas, x, yActual, elemento.texto, config) // RECTANGULO, PROCESO, etc.
            }

            val hijos = elementosPorEstructura[elemento.id] ?: emptyList()
            if (hijos.isNotEmpty()) {
                if (elemento.figura == "DECISION") {
                    val hijosY = dibujarNivel(canvas, hijos, x - 150f, yActual + 150f)

                    paint.color = Color.WHITE
                    paint.strokeWidth = 3f
                    canvas.drawLine(x, yActual + 50f, x - 150f, yActual + 100f, paint)
                    drawArrowhead(canvas, x - 150f, yActual + 100f, 270f)

                    paint.textSize = 24f
                    paint.textAlign = Paint.Align.CENTER
                    paint.style = Paint.Style.FILL
                    canvas.drawText("V", x - 100f, yActual - 20f, paint)
                    canvas.drawText("F", x + 100f, yActual - 20f, paint)

                    if (indice < elementosNivel.size - 1) {
                        canvas.drawLine(x + 150f, yActual + 50f, x + 150f, hijosY, paint)
                        canvas.drawLine(x + 150f, hijosY, x, hijosY + 20f, paint)
                        drawArrowhead(canvas, x, hijosY + 20f, 270f)
                        yActual = hijosY + 50f
                    } else {
                        canvas.drawLine(x + 150f, yActual + 50f, x + 150f, hijosY, paint)
                        yActual = hijosY
                    }
                } else {
                    val hijosY = dibujarNivel(canvas, hijos, x, yActual + 100f)

                    paint.color = Color.WHITE
                    paint.strokeWidth = 3f
                    canvas.drawLine(x, yActual + 50f, x, yActual + 100f, paint)
                    drawArrowhead(canvas, x, yActual + 100f, 270f)

                    yActual = hijosY
                }
            } else {
                if (indice < elementosNivel.size - 1) {
                    paint.color = Color.WHITE
                    paint.strokeWidth = 3f
                    canvas.drawLine(x, yActual + 50f, x, yActual + 100f, paint)
                    drawArrowhead(canvas, x, yActual + 100f, 270f)
                    yActual += 100f
                } else {
                    yActual += 50f
                }
            }

            indice++
        }

        return yActual
    }

    private fun drawTerminal(canvas: Canvas, x: Float, y: Float, texto: String, config: DiagramConfig) {
        val ancho = 200f
        val alto = 60f
        val rect = RectF(x - ancho/2, y - alto/2, x + ancho/2, y + alto/2)

        paint.style = Paint.Style.FILL
        canvas.drawOval(rect, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawOval(rect, paint)

        drawCenteredText(canvas, texto, x, y, config.colorTexto)
    }

    private fun drawProceso(canvas: Canvas, x: Float, y: Float, texto: String, config: DiagramConfig) {
        val ancho = 200f
        val alto = 60f

        paint.style = Paint.Style.FILL
        canvas.drawRect(x - ancho/2, y - alto/2, x + ancho/2, y + alto/2, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawRect(x - ancho/2, y - alto/2, x + ancho/2, y + alto/2, paint)

        drawCenteredText(canvas, texto, x, y, config.colorTexto)
    }

    private fun drawRectanguloRedondeado(canvas: Canvas, x: Float, y: Float, texto: String, config: DiagramConfig) {
        val ancho = 200f
        val alto = 60f
        val radio = 20f
        val rect = RectF(x - ancho/2, y - alto/2, x + ancho/2, y + alto/2)

        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, radio, radio, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawRoundRect(rect, radio, radio, paint)

        drawCenteredText(canvas, texto, x, y, config.colorTexto)
    }

    private fun drawDecision(canvas: Canvas, x: Float, y: Float, texto: String, config: DiagramConfig) {
        val ancho = 200f
        val alto = 100f

        val path = Path()
        path.moveTo(x, y - alto/2)
        path.lineTo(x + ancho/2, y)
        path.lineTo(x, y + alto/2)
        path.lineTo(x - ancho/2, y)
        path.close()

        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawPath(path, paint)

        drawCenteredText(canvas, texto, x, y, config.colorTexto)
    }

    private fun drawEntradaSalida(canvas: Canvas, x: Float, y: Float, texto: String, config: DiagramConfig) {
        val ancho = 220f
        val alto = 60f
        val sesgo = 30f

        val path = Path()
        path.moveTo(x - ancho/2 + sesgo, y - alto/2)
        path.lineTo(x + ancho/2 + sesgo, y - alto/2)
        path.lineTo(x + ancho/2 - sesgo, y + alto/2)
        path.lineTo(x - ancho/2 - sesgo, y + alto/2)
        path.close()

        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawPath(path, paint)

        drawCenteredText(canvas, texto, x, y, config.colorTexto)
    }

    private fun drawCenteredText(canvas: Canvas, texto: String, x: Float, y: Float, colorTexto: String) {
        paint.style = Paint.Style.FILL
        paint.color = safeParseColor(colorTexto)
        paint.textAlign = Paint.Align.CENTER

        val palabras = texto.split(" ")
        val lineas = mutableListOf<String>()
        var lineaActual = ""

        for (palabra in palabras) {
            val textoPrueba = if (lineaActual.isEmpty()) palabra else "$lineaActual $palabra"
            if (paint.measureText(textoPrueba) < 180) {
                lineaActual = textoPrueba
            } else {
                if (lineaActual.isNotEmpty()) lineas.add(lineaActual)
                lineaActual = palabra
            }
        }
        if (lineaActual.isNotEmpty()) lineas.add(lineaActual)

        val inicioY = y - ((lineas.size - 1) * paint.textSize / 2)
        lineas.forEachIndexed { index, linea ->
            canvas.drawText(linea, x, inicioY + index * paint.textSize, paint)
        }
    }

    private fun drawArrowhead(canvas: Canvas, x: Float, y: Float, direction: Float) {
        val arrowSize = 15f
        val path = Path()

        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        when (direction.toInt()) {
            270 -> {
                path.moveTo(x - arrowSize/2, y - arrowSize)
                path.lineTo(x + arrowSize/2, y - arrowSize)
                path.lineTo(x, y)
                path.close()
            }
            90 -> {
                path.moveTo(x - arrowSize/2, y + arrowSize)
                path.lineTo(x + arrowSize/2, y + arrowSize)
                path.lineTo(x, y)
                path.close()
            }
        }

        canvas.drawPath(path, paint)
    }

    private fun safeParseColor(colorStr: String): Int {
        return try {
            var color = colorStr
            if (!color.startsWith("#")) {
                color = "#$color"
            }
            android.graphics.Color.parseColor(color)
        } catch (e: IllegalArgumentException) {
            Log.e("COLOR", "Color inválido: $colorStr, usando gris")
            android.graphics.Color.GRAY
        }
    }
}