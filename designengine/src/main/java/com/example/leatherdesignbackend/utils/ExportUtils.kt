package com.example.leatherdesignbackend.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import com.caverock.androidsvg.SVG
import com.example.leatherdesignbackend.data.DesignProject
import com.example.leatherdesignbackend.models.DesignElement
import com.example.leatherdesignbackend.models.LeatherTool
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utility class for exporting designs in various formats
 */
class ExportUtils(private val context: Context) {
    
    private val TAG = "ExportUtils"
    
    /**
     * Export design as SVG file
     * @param project The design project
     * @return Uri to the exported file or null if export failed
     */
    fun exportSvg(project: DesignProject, designElements: List<DesignElement>): Uri? {
        try {
            // Create a new SVG document
            val svgBuilder = StringBuilder()
            svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
            svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
            svgBuilder.append("width=\"800\" height=\"600\" viewBox=\"0 0 800 600\">\n")
            
            // Add metadata
            svgBuilder.append("  <metadata>\n")
            svgBuilder.append("    <leathercraft:metadata xmlns:leathercraft=\"http://leathercraft.example.com/ns\">\n")
            svgBuilder.append("      <leathercraft:project-name>${project.name}</leathercraft:project-name>\n")
            svgBuilder.append("      <leathercraft:project-type>${project.type}</leathercraft:project-type}\n")
            svgBuilder.append("      <leathercraft:creation-date>${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(project.creationDate)}</leathercraft:creation-date>\n")
            svgBuilder.append("    </leathercraft:metadata>\n")
            svgBuilder.append("  </metadata>\n")
            
            // Add design elements
            designElements.forEach { element ->
                // Convert path to SVG path data
                val pathData = convertPathToSvgData(element)
                val strokeColor = String.format("#%06X", 0xFFFFFF and element.paint.color)
                val strokeWidth = element.paint.strokeWidth
                
                svgBuilder.append("  <path d=\"$pathData\" ")
                svgBuilder.append("stroke=\"$strokeColor\" ")
                svgBuilder.append("stroke-width=\"$strokeWidth\" ")
                svgBuilder.append("fill=\"none\" />\n")
            }
            
            svgBuilder.append("</svg>")
            
            // Write SVG to file
            val fileName = "${project.name.replace(" ", "_")}_${System.currentTimeMillis()}.svg"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(svgBuilder.toString())
                }
            }
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting SVG", e)
            return null
        }
    }
    
    /**
     * Export design as DXF file for CNC/laser cutter
     * @param project The design project
     * @return Uri to the exported file or null if export failed
     */
    fun exportDxf(project: DesignProject, designElements: List<DesignElement>): Uri? {
        try {
            // Create a new DXF document
            val dxfBuilder = StringBuilder()
            
            // DXF header
            dxfBuilder.append("0\nSECTION\n")
            dxfBuilder.append("2\nHEADER\n")
            dxfBuilder.append("9\n\$ACADVER\n1\nAC1009\n")  // AutoCAD R12 format
            dxfBuilder.append("0\nENDSEC\n")
            
            // Entities section start
            dxfBuilder.append("0\nSECTION\n")
            dxfBuilder.append("2\nENTITIES\n")
            
            // Add design elements
            designElements.forEach { element ->
                // Convert paths to polylines
                val points = extractPointsFromPath(element)
                
                if (points.size >= 2) {
                    // Create a polyline entity
                    dxfBuilder.append("0\nPOLYLINE\n")
                    dxfBuilder.append("8\nLeatherPattern\n")  // Layer name
                    dxfBuilder.append("66\n1\n")  // Polyline flag
                    dxfBuilder.append("70\n0\n")  // Open polyline
                    
                    // Add vertices
                    for (i in points.indices) {
                        val point = points[i]
                        dxfBuilder.append("0\nVERTEX\n")
                        dxfBuilder.append("8\nLeatherPattern\n")
                        dxfBuilder.append("10\n${point.x}\n")  // X coordinate
                        dxfBuilder.append("20\n${point.y}\n")  // Y coordinate
                        dxfBuilder.append("30\n0.0\n")        // Z coordinate
                    }
                    
                    // End polyline
                    dxfBuilder.append("0\nSEQEND\n")
                }
            }
            
            // End entities section
            dxfBuilder.append("0\nENDSEC\n")
            
            // End of file
            dxfBuilder.append("0\nEOF\n")
            
            // Write DXF to file
            val fileName = "${project.name.replace(" ", "_")}_${System.currentTimeMillis()}.dxf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(dxfBuilder.toString())
                }
            }
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting DXF", e)
            return null
        }
    }
    
    /**
     * Export design as print-ready PDF with cutting and fold lines
     * @param project The design project
     * @return Uri to the exported file or null if export failed
     */
    fun exportPdf(project: DesignProject, designElements: List<DesignElement>): Uri? {
        try {
            // Create document
            val document = Document(PageSize.A4)
            val fileName = "${project.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            
            // Get canvas for direct drawing
            val cb = writer.directContent
            
            // Convert design elements to PDF paths
            designElementsToPdf(cb, designElements)
            
            // Add project information
            document.add(com.itextpdf.text.Paragraph("Project: ${project.name}"))
            document.add(com.itextpdf.text.Paragraph("Type: ${project.type}"))
            document.add(com.itextpdf.text.Paragraph("Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(project.creationDate)}"))
            
            document.close()
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting PDF", e)
            return null
        }
    }
    
    /**
     * Export design as PNG image
     * @param project The design project
     * @return Uri to the exported file or null if export failed
     */
    fun exportPng(project: DesignProject, designView: android.view.View): Uri? {
        try {
            // Create bitmap from view
            val bitmap = Bitmap.createBitmap(
                designView.width, 
                designView.height, 
                Bitmap.Config.ARGB_8888
            )
            
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // White background
            designView.draw(canvas)
            
            // Save bitmap to file
            val fileName = "${project.name.replace(" ", "_")}_${System.currentTimeMillis()}.png"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting PNG", e)
            return null
        }
    }
    
    /**
     * Generate a cut list summarizing all pieces in the design
     * @param project The design project
     * @param tools List of tools required for the project
     * @return Uri to the exported file or null if export failed
     */
    fun generateCutList(project: DesignProject, tools: List<LeatherTool>, pieces: List<String>): Uri? {
        try {
            // Create text file with cut list
            val content = buildString {
                appendLine("CUT LIST FOR: ${project.name}")
                appendLine("Type: ${project.type}")
                appendLine("Created: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(project.creationDate)}")
                appendLine()
                
                appendLine("PIECES:")
                pieces.forEachIndexed { index, piece ->
                    appendLine("${index + 1}. $piece")
                }
                appendLine()
                
                appendLine("TOOLS REQUIRED:")
                tools.forEach { tool ->
                    appendLine("- ${tool.name}: ${tool.description}")
                }
            }
            
            val fileName = "${project.name.replace(" ", "_")}_CutList.txt"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(content)
                }
            }
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating cut list", e)
            return null
        }
    }
    
    /**
     * Helper function to convert Android Path to SVG path data
     */
    private fun convertPathToSvgData(element: DesignElement): String {
        // This is a simplified conversion for demonstration
        // A real implementation would parse the Android Path and convert to SVG path data
        return "M100,100 L200,100 L200,200 L100,200 Z"
    }
    
    /**
     * Helper function to extract points from a Path for DXF export
     */
    private fun extractPointsFromPath(element: DesignElement): List<Point> {
        // This would extract points from the Android Path
        // For now returning dummy points
        return listOf(
            Point(100f, 100f),
            Point(200f, 100f),
            Point(200f, 200f),
            Point(100f, 200f)
        )
    }
    
    /**
     * Helper function to convert design elements to PDF drawing commands
     */
    private fun designElementsToPdf(cb: PdfContentByte, elements: List<DesignElement>) {
        // This would convert Android paths to PDF drawing commands
        // Simplified implementation for demonstration
        cb.setLineWidth(1f)
        cb.setColorStroke(com.itextpdf.text.BaseColor.BLACK)
        
        // Draw a sample rectangle
        cb.rectangle(100f, 100f, 400f, 300f)
        cb.stroke()
    }
    
    /**
     * Simple point class for DXF export
     */
    data class Point(val x: Float, val y: Float)
}