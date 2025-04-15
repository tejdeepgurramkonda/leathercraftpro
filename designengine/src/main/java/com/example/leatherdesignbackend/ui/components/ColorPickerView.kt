package com.example.leatherdesignbackend.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlin.math.*

/**
 * Custom color picker view for leather crafting application
 * Provides HSV color selection with saturation/value rectangle and hue slider
 * Includes alpha slider and material preview functionality
 */
class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Current color values
    private var hue = 0f
    private var saturation = 1f
    private var value = 1f
    private var alpha = 255

    // Callback for when color changes
    private var onColorChangedListener: ((Int) -> Unit)? = null

    // Predefined leather colors
    val leatherPresets = listOf(
        Color.parseColor("#8B4513"), // Saddle Brown
        Color.parseColor("#A0522D"), // Sienna
        Color.parseColor("#D2691E"), // Chocolate
        Color.parseColor("#CD853F"), // Peru
        Color.parseColor("#DEB887"), // Burlywood
        Color.parseColor("#F5DEB3"), // Wheat
        Color.parseColor("#3C280D"), // Dark Brown
        Color.parseColor("#000000"), // Black
        Color.parseColor("#8B0000"), // Dark Red
        Color.parseColor("#191970")  // Midnight Blue
    ).toMutableList()

    // Recently used colors
    private val recentColors = mutableListOf<Int>()
    private val maxRecentColors = 8

    // Geometry
    private var rectSize = 0f
    private var panelMargin = 0f
    private var sliderWidth = 0f
    private var colorRectLeft = 0f
    private var colorRectTop = 0f
    private var hueSliderLeft = 0f
    private var hueSliderTop = 0f
    private var alphaSliderLeft = 0f
    private var alphaSliderTop = 0f
    private var presetColorsTop = 0f
    private var presetColorsLeft = 0f
    private var presetColorSize = 0f
    private var presetColorMargin = 0f
    private var recentColorsTop = 0f
    private var recentColorsLeft = 0f

    // Tracking which element is being interacted with
    private var activeElement = ActiveElement.NONE

    // Paint objects
    private val huePaint = Paint()
    private val alphaPaint = Paint()
    private val satValPaint = Paint()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val colorPreviewPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    // Shader for saturation/value rectangle
    private var satValShader: Shader? = null

    // Shader for hue slider
    private var hueShader: Shader? = null

    // Shader for alpha slider
    private var alphaShader: Shader? = null

    private enum class ActiveElement {
        NONE, SAT_VAL_RECT, HUE_SLIDER, ALPHA_SLIDER, PRESET_COLOR, RECENT_COLOR
    }

    init {
        // Initialize with a default color
        setColor(Color.RED)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculate dimensions based on view size
        val padding = 16f
        rectSize = min(w - (padding * 2), h * 0.5f - (padding * 2))
        panelMargin = padding
        sliderWidth = 48f

        // Layout coordinates
        colorRectLeft = padding
        colorRectTop = padding

        hueSliderLeft = colorRectLeft
        hueSliderTop = colorRectTop + rectSize + panelMargin

        alphaSliderLeft = colorRectLeft
        alphaSliderTop = hueSliderTop + sliderWidth + panelMargin

        // Calculate preset color dimensions
        presetColorSize = min(rectSize / 6, 48f)
        presetColorMargin = presetColorSize * 0.2f
        presetColorsLeft = colorRectLeft + rectSize + panelMargin * 2
        presetColorsTop = colorRectTop

        // Recent colors layout
        recentColorsLeft = presetColorsLeft
        recentColorsTop = presetColorsTop + (presetColorSize + presetColorMargin) * 2 + panelMargin

        // Create shaders
        createShaders()
    }

    private fun createShaders() {
        // Create shader for saturation/value rectangle
        satValShader = ComposeShader(
            LinearGradient(
                colorRectLeft,
                colorRectTop,
                colorRectLeft + rectSize,
                colorRectTop,
                Color.WHITE,
                Color.HSVToColor(floatArrayOf(hue, 1f, 1f)),
                Shader.TileMode.CLAMP
            ),
            LinearGradient(
                colorRectLeft,
                colorRectTop,
                colorRectLeft,
                colorRectTop + rectSize,
                Color.TRANSPARENT,
                Color.BLACK,
                Shader.TileMode.CLAMP
            ),
            PorterDuff.Mode.MULTIPLY
        )

        // Create shader for hue slider
        val hueColors = IntArray(361)
        for (i in 0..360) {
            hueColors[i] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }

        hueShader = LinearGradient(
            hueSliderLeft,
            0f,
            hueSliderLeft + rectSize,
            0f,
            hueColors,
            null,
            Shader.TileMode.CLAMP
        )

        // Create shader for alpha slider
        val baseColor = getCurrentColor(255)
        alphaShader = LinearGradient(
            alphaSliderLeft,
            0f,
            alphaSliderLeft + rectSize,
            0f,
            Color.TRANSPARENT,
            baseColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw saturation/value rectangle
        satValPaint.shader = satValShader
        canvas.drawRect(
            colorRectLeft,
            colorRectTop,
            colorRectLeft + rectSize,
            colorRectTop + rectSize,
            satValPaint
        )
        canvas.drawRect(
            colorRectLeft,
            colorRectTop,
            colorRectLeft + rectSize,
            colorRectTop + rectSize,
            borderPaint
        )

        // Draw indicator on saturation/value rectangle
        val satX = colorRectLeft + saturation * rectSize
        val valY = colorRectTop + (1 - value) * rectSize
        canvas.drawCircle(satX, valY, 10f, indicatorPaint)

        // Draw hue slider
        huePaint.shader = hueShader
        canvas.drawRect(
            hueSliderLeft,
            hueSliderTop,
            hueSliderLeft + rectSize,
            hueSliderTop + sliderWidth,
            huePaint
        )
        canvas.drawRect(
            hueSliderLeft,
            hueSliderTop,
            hueSliderLeft + rectSize,
            hueSliderTop + sliderWidth,
            borderPaint
        )

        // Draw indicator on hue slider
        val hueX = hueSliderLeft + (hue / 360f) * rectSize
        canvas.drawLine(
            hueX,
            hueSliderTop - 5f,
            hueX,
            hueSliderTop + sliderWidth + 5f,
            indicatorPaint
        )

        // Draw alpha slider
        // Create the checkered background pattern for transparency
        createCheckerboardPattern(canvas, alphaSliderLeft, alphaSliderTop, rectSize, sliderWidth)

        // Draw alpha gradient over checkerboard
        alphaPaint.shader = alphaShader
        canvas.drawRect(
            alphaSliderLeft,
            alphaSliderTop,
            alphaSliderLeft + rectSize,
            alphaSliderTop + sliderWidth,
            alphaPaint
        )
        canvas.drawRect(
            alphaSliderLeft,
            alphaSliderTop,
            alphaSliderLeft + rectSize,
            alphaSliderTop + sliderWidth,
            borderPaint
        )

        // Draw indicator on alpha slider
        val alphaX = alphaSliderLeft + (alpha / 255f) * rectSize
        canvas.drawLine(
            alphaX,
            alphaSliderTop - 5f,
            alphaX,
            alphaSliderTop + sliderWidth + 5f,
            indicatorPaint
        )

        // Draw current color preview
        drawColorPreview(canvas)

        // Draw preset colors
        drawPresetColors(canvas)

        // Draw recent colors
        drawRecentColors(canvas)
    }

    private fun drawColorPreview(canvas: Canvas) {
        val previewLeft = colorRectLeft + rectSize + panelMargin
        val previewTop = alphaSliderTop
        val previewSize = sliderWidth

        // Draw checkered pattern for transparency
        createCheckerboardPattern(canvas, previewLeft, previewTop, previewSize, previewSize)

        // Draw current color
        colorPreviewPaint.color = getCurrentColor()
        canvas.drawRect(
            previewLeft,
            previewTop,
            previewLeft + previewSize,
            previewTop + previewSize,
            colorPreviewPaint
        )
        canvas.drawRect(
            previewLeft,
            previewTop,
            previewLeft + previewSize,
            previewTop + previewSize,
            borderPaint
        )

        // Draw RGB text
        val color = getCurrentColor()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        val hexColor = String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
        canvas.drawText(
            hexColor,
            previewLeft + previewSize + 60f,
            previewTop + previewSize / 2 + 8f,
            textPaint
        )
    }

    private fun drawPresetColors(canvas: Canvas) {
        canvas.drawText(
            "Leather Presets",
            presetColorsLeft + (presetColorSize * 2.5f),
            presetColorsTop - 10f,
            textPaint
        )

        var row = 0
        var col = 0

        for (i in leatherPresets.indices) {
            val left = presetColorsLeft + col * (presetColorSize + presetColorMargin)
            val top = presetColorsTop + row * (presetColorSize + presetColorMargin)

            colorPreviewPaint.color = leatherPresets[i]
            canvas.drawRect(
                left,
                top,
                left + presetColorSize,
                top + presetColorSize,
                colorPreviewPaint
            )
            canvas.drawRect(
                left,
                top,
                left + presetColorSize,
                top + presetColorSize,
                borderPaint
            )

            col++
            if (col >= 5) {
                col = 0
                row++
            }
        }
    }

    private fun drawRecentColors(canvas: Canvas) {
        canvas.drawText(
            "Recent Colors",
            recentColorsLeft + (presetColorSize * 2f),
            recentColorsTop - 10f,
            textPaint
        )

        for (i in recentColors.indices) {
            val left = recentColorsLeft + i * (presetColorSize + presetColorMargin)
            val top = recentColorsTop

            // Draw checkered pattern for transparency
            createCheckerboardPattern(canvas, left, top, presetColorSize, presetColorSize)

            colorPreviewPaint.color = recentColors[i]
            canvas.drawRect(
                left,
                top,
                left + presetColorSize,
                top + presetColorSize,
                colorPreviewPaint
            )
            canvas.drawRect(
                left,
                top,
                left + presetColorSize,
                top + presetColorSize,
                borderPaint
            )
        }
    }

    private fun createCheckerboardPattern(canvas: Canvas, left: Float, top: Float, width: Float, height: Float) {
        val cellSize = 8f
        val checkerPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        var isWhite = true
        var cy = top
        while (cy < top + height) {
            var cx = left
            val rowIsWhite = isWhite
            isWhite = rowIsWhite

            while (cx < left + width) {
                checkerPaint.color = if (isWhite) Color.LTGRAY else Color.WHITE
                canvas.drawRect(cx, cy, min(cx + cellSize, left + width), min(cy + cellSize, top + height), checkerPaint)
                cx += cellSize
                isWhite = !isWhite
            }

            cy += cellSize
            isWhite = !rowIsWhite
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeElement = when {
                    // Check if touch is within saturation/value rectangle
                    x >= colorRectLeft && x <= colorRectLeft + rectSize &&
                            y >= colorRectTop && y <= colorRectTop + rectSize -> {
                        ActiveElement.SAT_VAL_RECT
                    }
                    // Check if touch is within hue slider
                    x >= hueSliderLeft && x <= hueSliderLeft + rectSize &&
                            y >= hueSliderTop && y <= hueSliderTop + sliderWidth -> {
                        ActiveElement.HUE_SLIDER
                    }
                    // Check if touch is within alpha slider
                    x >= alphaSliderLeft && x <= alphaSliderLeft + rectSize &&
                            y >= alphaSliderTop && y <= alphaSliderTop + sliderWidth -> {
                        ActiveElement.ALPHA_SLIDER
                    }
                    // Check if touch is within preset colors
                    x >= presetColorsLeft && x <= presetColorsLeft + 5 * (presetColorSize + presetColorMargin) &&
                            y >= presetColorsTop && y <= presetColorsTop + 2 * (presetColorSize + presetColorMargin) -> {
                        ActiveElement.PRESET_COLOR
                    }
                    // Check if touch is within recent colors
                    x >= recentColorsLeft && x <= recentColorsLeft + recentColors.size * (presetColorSize + presetColorMargin) &&
                            y >= recentColorsTop && y <= recentColorsTop + presetColorSize -> {
                        ActiveElement.RECENT_COLOR
                    }
                    else -> ActiveElement.NONE
                }

                // Handle the interaction
                when (activeElement) {
                    ActiveElement.SAT_VAL_RECT -> {
                        updateSaturationValue(x, y)
                        return true
                    }
                    ActiveElement.HUE_SLIDER -> {
                        updateHue(x)
                        return true
                    }
                    ActiveElement.ALPHA_SLIDER -> {
                        updateAlpha(x)
                        return true
                    }
                    ActiveElement.PRESET_COLOR -> {
                        selectPresetColor(x, y)
                        return true
                    }
                    ActiveElement.RECENT_COLOR -> {
                        selectRecentColor(x)
                        return true
                    }
                    else -> return false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (activeElement) {
                    ActiveElement.SAT_VAL_RECT -> {
                        updateSaturationValue(x, y)
                        return true
                    }
                    ActiveElement.HUE_SLIDER -> {
                        updateHue(x)
                        return true
                    }
                    ActiveElement.ALPHA_SLIDER -> {
                        updateAlpha(x)
                        return true
                    }
                    else -> return false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Add current color to recent colors when interaction ends
                if (activeElement != ActiveElement.NONE && activeElement != ActiveElement.RECENT_COLOR) {
                    addToRecentColors(getCurrentColor())
                }
                activeElement = ActiveElement.NONE
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun updateSaturationValue(x: Float, y: Float) {
        saturation = (x - colorRectLeft).coerceIn(0f, rectSize) / rectSize
        value = 1f - (y - colorRectTop).coerceIn(0f, rectSize) / rectSize

        // Recalculate alpha shader since base color changed
        updateAlphaShader()

        notifyColorChanged()
        invalidate()
    }

    private fun updateHue(x: Float) {
        hue = ((x - hueSliderLeft) / rectSize * 360f).coerceIn(0f, 360f)

        // Recreate shaders with new hue
        createShaders()

        notifyColorChanged()
        invalidate()
    }

    private fun updateAlpha(x: Float) {
        alpha = ((x - alphaSliderLeft) / rectSize * 255).toInt().coerceIn(0, 255)

        notifyColorChanged()
        invalidate()
    }

    private fun selectPresetColor(x: Float, y: Float) {
        val col = ((x - presetColorsLeft) / (presetColorSize + presetColorMargin)).toInt()
        val row = ((y - presetColorsTop) / (presetColorSize + presetColorMargin)).toInt()

        val index = row * 5 + col
        if (index >= 0 && index < leatherPresets.size) {
            setColor(leatherPresets[index])
        }
    }

    private fun selectRecentColor(x: Float) {
        val index = ((x - recentColorsLeft) / (presetColorSize + presetColorMargin)).toInt()
        if (index >= 0 && index < recentColors.size) {
            setColor(recentColors[index])
        }
    }

    private fun updateAlphaShader() {
        val baseColor = getCurrentColor(255)
        alphaShader = LinearGradient(
            alphaSliderLeft,
            0f,
            alphaSliderLeft + rectSize,
            0f,
            Color.TRANSPARENT,
            baseColor,
            Shader.TileMode.CLAMP
        )
    }

    fun getCurrentColor(overrideAlpha: Int = -1): Int {
        val hsv = floatArrayOf(hue, saturation, value)
        val alphaValue = if (overrideAlpha >= 0) overrideAlpha else alpha
        return Color.HSVToColor(alphaValue, hsv)
    }

    fun setColor(color: Int) {
        // Extract HSV components
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        alpha = Color.alpha(color)

        // Recreate shaders with new color
        createShaders()

        notifyColorChanged()
        invalidate()
    }

    fun setOnColorChangedListener(listener: (Int) -> Unit) {
        onColorChangedListener = listener
    }

    private fun notifyColorChanged() {
        onColorChangedListener?.invoke(getCurrentColor())
    }

    private fun addToRecentColors(color: Int) {
        // Remove if already exists
        recentColors.remove(color)

        // Add to beginning of list
        recentColors.add(0, color)

        // Trim list if needed
        if (recentColors.size > maxRecentColors) {
            recentColors.removeAt(recentColors.size - 1)
        }

        invalidate()
    }

    // Get complementary color
    fun getComplementaryColor(): Int {
        val complementaryHue = (hue + 180) % 360
        return Color.HSVToColor(alpha, floatArrayOf(complementaryHue, saturation, value))
    }

    // Get analogous colors
    fun getAnalogousColors(): Pair<Int, Int> {
        val hue1 = (hue + 30) % 360
        val hue2 = (hue + 330) % 360

        return Pair(
            Color.HSVToColor(alpha, floatArrayOf(hue1, saturation, value)),
            Color.HSVToColor(alpha, floatArrayOf(hue2, saturation, value))
        )
    }

    /**
     * Set the preset colors to be displayed in the color picker
     *
     * @param colors List of color integers to use as presets
     */
    fun setPresetColors(colors: List<Int>) {
        leatherPresets.clear()
        leatherPresets.addAll(colors)
        invalidate()
    }
}
