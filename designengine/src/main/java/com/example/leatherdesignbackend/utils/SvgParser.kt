package com.example.leatherdesignbackend.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.Path
import android.net.Uri
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Utility class for parsing SVG files and converting them to Path objects
 */
class SvgParser(private val context: Context) {

    /**
     * Parse an SVG file from a URI and return a list of Path objects
     */
    fun parseSvgFromUri(uri: Uri): List<Path> {
        val inputStream = context.contentResolver.openInputStream(uri)
        return parseSvgFromInputStream(inputStream)
    }

    /**
     * Parse SVG content from a string and return a list of Path objects
     */
    fun parseSvgFromString(svgContent: String): List<Path> {
        val inputStream = svgContent.byteInputStream()
        return parseSvgFromInputStream(inputStream)
    }

    /**
     * Parse SVG content from an input stream and return a list of Path objects
     */
    private fun parseSvgFromInputStream(inputStream: InputStream?): List<Path> {
        val paths = mutableListOf<Path>()

        try {
            inputStream?.use { stream ->
                val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = docBuilder.parse(stream)

                // Get all path elements
                val pathElements = doc.getElementsByTagName("path")

                // Process each path element
                for (i in 0 until pathElements.length) {
                    val pathElement = pathElements.item(i)

                    if (pathElement.nodeType == Node.ELEMENT_NODE) {
                        val element = pathElement as Element

                        // Get the path data
                        val pathData = element.getAttribute("d")

                        // Convert SVG path data to Android Path
                        val path = pathDataToPath(pathData)
                        paths.add(path)
                    }
                }

                // Also process other SVG elements like rect, circle, etc.
                processRectElements(doc.getElementsByTagName("rect"), paths)
                processCircleElements(doc.getElementsByTagName("circle"), paths)
                processEllipseElements(doc.getElementsByTagName("ellipse"), paths)
                processLineElements(doc.getElementsByTagName("line"), paths)
                processPolylineElements(doc.getElementsByTagName("polyline"), paths)
                processPolygonElements(doc.getElementsByTagName("polygon"), paths)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return paths
    }

    /**
     * Convert SVG path data to Android Path
     */
    private fun pathDataToPath(pathData: String): Path {
        val path = Path()

        // This is a simplified parser that handles basic path commands
        // A full implementation would handle all SVG path commands

        var index = 0
        var lastX = 0f
        var lastY = 0f

        while (index < pathData.length) {
            // Skip whitespace
            while (index < pathData.length && pathData[index].isWhitespace()) {
                index++
            }

            if (index >= pathData.length) break

            val cmd = pathData[index]
            index++

            // Skip whitespace after command
            while (index < pathData.length && pathData[index].isWhitespace()) {
                index++
            }

            when (cmd) {
                'M', 'm' -> {
                    // Move to
                    val coords = parseCoordinatePair(pathData, index)
                    index = coords.third

                    if (cmd == 'm') {
                        // Relative coordinates
                        path.moveTo(lastX + coords.first, lastY + coords.second)
                        lastX += coords.first
                        lastY += coords.second
                    } else {
                        // Absolute coordinates
                        path.moveTo(coords.first, coords.second)
                        lastX = coords.first
                        lastY = coords.second
                    }
                }

                'L', 'l' -> {
                    // Line to
                    val coords = parseCoordinatePair(pathData, index)
                    index = coords.third

                    if (cmd == 'l') {
                        // Relative coordinates
                        path.lineTo(lastX + coords.first, lastY + coords.second)
                        lastX += coords.first
                        lastY += coords.second
                    } else {
                        // Absolute coordinates
                        path.lineTo(coords.first, coords.second)
                        lastX = coords.first
                        lastY = coords.second
                    }
                }

                'H', 'h' -> {
                    // Horizontal line
                    val value = parseFloat(pathData, index)
                    index = value.second

                    if (cmd == 'h') {
                        // Relative coordinates
                        path.lineTo(lastX + value.first, lastY)
                        lastX += value.first
                    } else {
                        // Absolute coordinates
                        path.lineTo(value.first, lastY)
                        lastX = value.first
                    }
                }

                'V', 'v' -> {
                    // Vertical line
                    val value = parseFloat(pathData, index)
                    index = value.second

                    if (cmd == 'v') {
                        // Relative coordinates
                        path.lineTo(lastX, lastY + value.first)
                        lastY += value.first
                    } else {
                        // Absolute coordinates
                        path.lineTo(lastX, value.first)
                        lastY = value.first
                    }
                }

                'Z', 'z' -> {
                    // Close path
                    path.close()
                }

                // Add more command handlers for C, S, Q, T, A, etc.
                // These are more complex curve commands
            }
        }

        return path
    }

    /**
     * Parse a coordinate pair (x,y) from the string
     * Returns Triple(x, y, newIndex)
     */
    private fun parseCoordinatePair(data: String, startIndex: Int): Triple<Float, Float, Int> {
        val x = parseFloat(data, startIndex)
        val y = parseFloat(data, x.second)
        return Triple(x.first, y.first, y.second)
    }

    /**
     * Parse a float value from the string
     * Returns Pair(value, newIndex)
     */
    private fun parseFloat(data: String, startIndex: Int): Pair<Float, Int> {
        var index = startIndex

        // Skip whitespace and commas
        while (index < data.length && (data[index].isWhitespace() || data[index] == ',')) {
            index++
        }

        if (index >= data.length) {
            return Pair(0f, index)
        }

        // Find the end of the number
        val startNumber = index
        var hasDecimal = false
        var hasExponent = false

        if (data[index] == '-' || data[index] == '+') {
            index++
        }

        while (index < data.length) {
            val c = data[index]

            if (c.isDigit()) {
                index++
            } else if (c == '.' && !hasDecimal) {
                hasDecimal = true
                index++
            } else if ((c == 'e' || c == 'E') && !hasExponent) {
                hasExponent = true
                index++

                if (index < data.length && (data[index] == '-' || data[index] == '+')) {
                    index++
                }
            } else {
                break
            }
        }

        val numberStr = data.substring(startNumber, index)
        return try {
            Pair(numberStr.toFloat(), index)
        } catch (e: Exception) {
            Pair(0f, index)
        }
    }

    /**
     * Process rectangle elements
     */
    private fun processRectElements(elements: NodeList, paths: MutableList<Path>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)

            if (element.nodeType == Node.ELEMENT_NODE) {
                val rectElement = element as Element

                val x = rectElement.getAttribute("x").toFloatOrNull() ?: 0f
                val y = rectElement.getAttribute("y").toFloatOrNull() ?: 0f
                val width = rectElement.getAttribute("width").toFloatOrNull() ?: 0f
                val height = rectElement.getAttribute("height").toFloatOrNull() ?: 0f

                val path = Path()
                path.moveTo(x, y)
                path.lineTo(x + width, y)
                path.lineTo(x + width, y + height)
                path.lineTo(x, y + height)
                path.close()

                paths.add(path)
            }
        }
    }

    /**
     * Process circle elements
     */
    private fun processCircleElements(elements: NodeList, paths: MutableList<Path>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)

            if (element.nodeType == Node.ELEMENT_NODE) {
                val circleElement = element as Element

                val cx = circleElement.getAttribute("cx").toFloatOrNull() ?: 0f
                val cy = circleElement.getAttribute("cy").toFloatOrNull() ?: 0f
                val r = circleElement.getAttribute("r").toFloatOrNull() ?: 0f

                val path = Path()
                path.addCircle(cx, cy, r, Path.Direction.CW)

                paths.add(path)
            }
        }
    }

    /**
     * Process ellipse elements
     */
    private fun processEllipseElements(elements: NodeList, paths: MutableList<Path>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)

            if (element.nodeType == Node.ELEMENT_NODE) {
                val ellipseElement = element as Element

                val cx = ellipseElement.getAttribute("cx").toFloatOrNull() ?: 0f
                val cy = ellipseElement.getAttribute("cy").toFloatOrNull() ?: 0f
                val rx = ellipseElement.getAttribute("rx").toFloatOrNull() ?: 0f
                val ry = ellipseElement.getAttribute("ry").toFloatOrNull() ?: 0f

                val path = Path()
                path.addOval(cx - rx, cy - ry, cx + rx, cy + ry, Path.Direction.CW)

                paths.add(path)
            }
        }
    }

    /**
     * Process line elements
     */
    private fun processLineElements(elements: NodeList, paths: MutableList<Path>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)

            if (element.nodeType == Node.ELEMENT_NODE) {
                val lineElement = element as Element

                val x1 = lineElement.getAttribute("x1").toFloatOrNull() ?: 0f
                val y1 = lineElement.getAttribute("y1").toFloatOrNull() ?: 0f
                val x2 = lineElement.getAttribute("x2").toFloatOrNull() ?: 0f
                val y2 = lineElement.getAttribute("y2").toFloatOrNull() ?: 0f

                val path = Path()
                path.moveTo(x1, y1)
                path.lineTo(x2, y2)

                paths.add(path)
            }
        }
    }

    /**
     * Process polyline elements
     */
    private fun processPolylineElements(elements: NodeList, paths: MutableList<Path>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)

            if (element.nodeType == Node.ELEMENT_NODE) {
                val polylineElement = element as Element

                val pointsStr = polylineElement.getAttribute("points")
                val path = parsePointsToPath(pointsStr, false)

                paths.add(path)
            }
        }
    }

    /**
     * Process polygon elements
     */
    private fun processPolygonElements(elements: NodeList, paths: MutableList<Path>) {
        for (i in 0 until elements.length) {
            val element = elements.item(i)

            if (element.nodeType == Node.ELEMENT_NODE) {
                val polygonElement = element as Element

                val pointsStr = polygonElement.getAttribute("points")
                val path = parsePointsToPath(pointsStr, true)

                paths.add(path)
            }
        }
    }

    /**
     * Parse a points string (used by polyline and polygon) to a Path
     */
    private fun parsePointsToPath(pointsStr: String, closePath: Boolean): Path {
        val path = Path()
        val pointPairs = parsePointsString(pointsStr)

        if (pointPairs.isNotEmpty()) {
            path.moveTo(pointPairs[0].first, pointPairs[0].second)

            for (i in 1 until pointPairs.size) {
                path.lineTo(pointPairs[i].first, pointPairs[i].second)
            }

            if (closePath) {
                path.close()
            }
        }

        return path
    }

    /**
     * Parse a string of points into a list of coordinate pairs
     */
    private fun parsePointsString(pointsStr: String): List<Pair<Float, Float>> {
        val pointPairs = mutableListOf<Pair<Float, Float>>()
        var index = 0

        while (index < pointsStr.length) {
            // Skip whitespace and commas
            while (index < pointsStr.length && (pointsStr[index].isWhitespace() || pointsStr[index] == ',')) {
                index++
            }

            if (index >= pointsStr.length) break

            // Parse x coordinate
            val x = parseFloat(pointsStr, index)
            index = x.second

            // Skip whitespace and commas
            while (index < pointsStr.length && (pointsStr[index].isWhitespace() || pointsStr[index] == ',')) {
                index++
            }

            if (index >= pointsStr.length) break

            // Parse y coordinate
            val y = parseFloat(pointsStr, index)
            index = y.second

            pointPairs.add(Pair(x.first, y.first))
        }

        return pointPairs
    }

    /**
     * Get the viewBox from an SVG document
     */
    fun getViewBox(svgContent: String): FloatArray? {
        try {
            val inputStream = svgContent.byteInputStream()

            inputStream.use { stream ->
                val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = docBuilder.parse(stream)

                val svgElement = doc.documentElement
                val viewBoxStr = svgElement.getAttribute("viewBox")

                if (viewBoxStr.isNotEmpty()) {
                    val parts = viewBoxStr.trim().split("\\s+".toRegex())

                    if (parts.size == 4) {
                        return floatArrayOf(
                            parts[0].toFloatOrNull() ?: 0f,
                            parts[1].toFloatOrNull() ?: 0f,
                            parts[2].toFloatOrNull() ?: 0f,
                            parts[3].toFloatOrNull() ?: 0f
                        )
                    }
                }

                // If no viewBox, try to get width and height attributes
                val width = svgElement.getAttribute("width").toFloatOrNull() ?: 0f
                val height = svgElement.getAttribute("height").toFloatOrNull() ?: 0f

                if (width > 0 && height > 0) {
                    return floatArrayOf(0f, 0f, width, height)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * Scale paths to fit within the given dimensions
     */
    fun scalePaths(paths: List<Path>, viewBox: FloatArray?, targetWidth: Float, targetHeight: Float): List<Path> {
        if (viewBox == null || paths.isEmpty()) {
            return paths
        }

        val sourceWidth = viewBox[2]
        val sourceHeight = viewBox[3]

        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return paths
        }

        val scaleX = targetWidth / sourceWidth
        val scaleY = targetHeight / sourceHeight

        // Use the smaller scale to maintain aspect ratio
        val scale = minOf(scaleX, scaleY)

        val scaledPaths = mutableListOf<Path>()

        for (path in paths) {
            val scaledPath = Path(path)
            val matrix = Matrix()
            matrix.setScale(scale, scale)
            scaledPath.transform(matrix)
            scaledPaths.add(scaledPath)
        }

        return scaledPaths
    }

    /**
     * Get style attributes from an SVG element
     */
    private fun getStyleAttributes(element: Element): Map<String, String> {
        val styleAttrs = mutableMapOf<String, String>()

        // Check for style attribute
        val styleAttr = element.getAttribute("style")

        if (styleAttr.isNotEmpty()) {
            // Parse style attribute (e.g., "fill:none;stroke:black;stroke-width:2")
            val styleParts = styleAttr.split(";")

            for (part in styleParts) {
                val keyValue = part.split(":")

                if (keyValue.size == 2) {
                    val key = keyValue[0].trim()
                    val value = keyValue[1].trim()
                    styleAttrs[key] = value
                }
            }
        }

        // Check for individual style attributes
        val attributes = listOf(
            "fill", "stroke", "stroke-width", "stroke-linecap",
            "stroke-linejoin", "stroke-dasharray", "opacity"
        )

        for (attr in attributes) {
            val value = element.getAttribute(attr)

            if (value.isNotEmpty()) {
                styleAttrs[attr] = value
            }
        }

        return styleAttrs
    }
}