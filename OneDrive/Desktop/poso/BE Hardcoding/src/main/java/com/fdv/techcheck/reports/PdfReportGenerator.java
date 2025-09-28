package com.fdv.techcheck.reports;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationStatus;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates PDF reports for thesis validation results.
 * Creates professional reports suitable for technical service staff review.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class PdfReportGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);
    
    // Colors for different validation statuses
    private static final Color COLOR_PASS = new DeviceRgb(34, 139, 34);      // Forest Green
    private static final Color COLOR_WARNING = new DeviceRgb(255, 140, 0);   // Dark Orange
    private static final Color COLOR_FAIL = new DeviceRgb(220, 20, 60);      // Crimson
    private static final Color COLOR_ERROR = new DeviceRgb(139, 0, 0);       // Dark Red
    private static final Color COLOR_HEADER = new DeviceRgb(25, 25, 112);    // Midnight Blue
    
    /**
     * Generates a PDF report for the given validation results.
     * 
     * @param outputFile Output file path for the PDF report
     * @param document The thesis document that was validated
     * @param validationResults Map of validator names to their results
     * @throws IOException If there's an error writing the PDF file
     */
    public void generateReport(File outputFile, ThesisDocument document, 
                             Map<String, ValidationResult> validationResults) throws IOException {
        
        logger.info("Generating PDF report: {}", outputFile.getAbsolutePath());
        
        try (PdfWriter writer = new PdfWriter(outputFile.getAbsolutePath());
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc)) {
            
            // Create fonts
            PdfFont boldFont = PdfFontFactory.createFont();
            PdfFont regularFont = PdfFontFactory.createFont();
            
            // Add header
            addReportHeader(doc, document, boldFont);
            
            // Add summary
            addValidationSummary(doc, validationResults, boldFont, regularFont);
            
            // Add detailed results
            addDetailedResults(doc, validationResults, boldFont, regularFont);
            
            // Add footer
            addReportFooter(doc, regularFont);
            
            logger.info("PDF report generated successfully: {}", outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            logger.error("Failed to generate PDF report", e);
            throw new IOException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds the report header with document information.
     */
    private void addReportHeader(Document doc, ThesisDocument document, PdfFont boldFont) {
        // Title
        Paragraph title = new Paragraph("THESIS TECHNICAL VALIDATION REPORT")
            .setFont(boldFont)
            .setFontSize(20)
            .setFontColor(COLOR_HEADER)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        doc.add(title);
        
        // Institution
        Paragraph institution = new Paragraph("Faculty of Social Sciences, University of Ljubljana")
            .setFont(boldFont)
            .setFontSize(14)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(30);
        doc.add(institution);
        
        // Document information table
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(20);
        
        addInfoRow(infoTable, "Document Name:", document.getMetadata().getFileName(), boldFont);
        addInfoRow(infoTable, "File Size:", formatFileSize(document.getMetadata().getFileSize()), boldFont);
        addInfoRow(infoTable, "Pages:", String.valueOf(document.getMetadata().getPageCount()), boldFont);
        addInfoRow(infoTable, "Words:", String.valueOf(document.getMetadata().getWordCount()), boldFont);
        addInfoRow(infoTable, "Validation Date:", 
                  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), boldFont);
        
        doc.add(infoTable);
    }
    
    /**
     * Adds a row to the information table.
     */
    private void addInfoRow(Table table, String label, String value, PdfFont boldFont) {
        table.addCell(new Cell().add(new Paragraph(label).setFont(boldFont)));
        table.addCell(new Cell().add(new Paragraph(value)));
    }
    
    /**
     * Adds the validation summary section.
     */
    private void addValidationSummary(Document doc, Map<String, ValidationResult> validationResults, 
                                    PdfFont boldFont, PdfFont regularFont) {
        
        // Summary title
        Paragraph summaryTitle = new Paragraph("VALIDATION SUMMARY")
            .setFont(boldFont)
            .setFontSize(16)
            .setFontColor(COLOR_HEADER)
            .setMarginTop(20)
            .setMarginBottom(15);
        doc.add(summaryTitle);
        
        // Summary table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(30);
        
        // Header row
        summaryTable.addHeaderCell(createHeaderCell("Validation Module", boldFont));
        summaryTable.addHeaderCell(createHeaderCell("Status", boldFont));
        summaryTable.addHeaderCell(createHeaderCell("Issues", boldFont));
        summaryTable.addHeaderCell(createHeaderCell("Severity", boldFont));
        
        // Add rows for each validation result
        for (Map.Entry<String, ValidationResult> entry : validationResults.entrySet()) {
            String validatorName = entry.getKey();
            ValidationResult result = entry.getValue();
            
            summaryTable.addCell(new Cell().add(new Paragraph(validatorName).setFont(regularFont)));
            summaryTable.addCell(createStatusCell(result.getStatus(), boldFont));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(result.getDetails().size())).setFont(regularFont)));
            summaryTable.addCell(createSeverityCell(result.getStatus(), regularFont));
        }
        
        doc.add(summaryTable);
        
        // Overall status
        ValidationStatus overallStatus = calculateOverallStatus(validationResults);
        Paragraph overallStatusPara = new Paragraph("OVERALL STATUS: " + getStatusText(overallStatus))
            .setFont(boldFont)
            .setFontSize(14)
            .setFontColor(getStatusColor(overallStatus))
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);
        doc.add(overallStatusPara);
    }
    
    /**
     * Adds the detailed validation results section.
     */
    private void addDetailedResults(Document doc, Map<String, ValidationResult> validationResults,
                                  PdfFont boldFont, PdfFont regularFont) {
        
        // Detailed results title
        Paragraph detailsTitle = new Paragraph("DETAILED VALIDATION RESULTS")
            .setFont(boldFont)
            .setFontSize(16)
            .setFontColor(COLOR_HEADER)
            .setMarginTop(20)
            .setMarginBottom(15);
        doc.add(detailsTitle);
        
        for (Map.Entry<String, ValidationResult> entry : validationResults.entrySet()) {
            String validatorName = entry.getKey();
            ValidationResult result = entry.getValue();
            
            // Validator section title
            Paragraph validatorTitle = new Paragraph(validatorName + " Validation")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(10);
            doc.add(validatorTitle);
            
            // Status and message
            Paragraph statusPara = new Paragraph("Status: " + getStatusText(result.getStatus()) + " - " + result.getMessage())
                .setFont(regularFont)
                .setFontColor(getStatusColor(result.getStatus()))
                .setMarginBottom(10);
            doc.add(statusPara);
            
            // Details if any issues found
            if (result.hasIssues() && !result.getDetails().isEmpty()) {
                Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);
                
                // Header row
                detailsTable.addHeaderCell(createHeaderCell("Location", boldFont));
                detailsTable.addHeaderCell(createHeaderCell("Expected", boldFont));
                detailsTable.addHeaderCell(createHeaderCell("Found", boldFont));
                detailsTable.addHeaderCell(createHeaderCell("Severity", boldFont));
                
                // Add detail rows
                for (ValidationDetail detail : result.getDetails()) {
                    detailsTable.addCell(new Cell().add(new Paragraph(detail.getLocation()).setFont(regularFont)));
                    detailsTable.addCell(new Cell().add(new Paragraph(detail.getExpected()).setFont(regularFont)));
                    detailsTable.addCell(new Cell().add(new Paragraph(detail.getActual()).setFont(regularFont)));
                    detailsTable.addCell(new Cell().add(new Paragraph(detail.getSeverity().toString()).setFont(regularFont)));
                }
                
                doc.add(detailsTable);
            } else {
                Paragraph noIssues = new Paragraph("No issues found - validation passed successfully.")
                    .setFont(regularFont)
                    .setFontColor(COLOR_PASS)
                    .setMarginBottom(15);
                doc.add(noIssues);
            }
        }
    }
    
    /**
     * Adds the report footer.
     */
    private void addReportFooter(Document doc, PdfFont regularFont) {
        Paragraph footer = new Paragraph(
            "Generated by TechCheck - Automated Thesis Technical Review System\n" +
            "Faculty of Social Sciences, University of Ljubljana\n" +
            "For technical support: technical@fdv.uni-lj.si"
        )
            .setFont(regularFont)
            .setFontSize(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30)
            .setFontColor(new DeviceRgb(128, 128, 128));
        doc.add(footer);
    }
    
    /**
     * Creates a header cell with consistent styling.
     */
    private Cell createHeaderCell(String text, PdfFont boldFont) {
        return new Cell()
            .add(new Paragraph(text).setFont(boldFont).setFontColor(new DeviceRgb(255, 255, 255)))
            .setBackgroundColor(COLOR_HEADER)
            .setTextAlignment(TextAlignment.CENTER);
    }
    
    /**
     * Creates a status cell with appropriate color coding.
     */
    private Cell createStatusCell(ValidationStatus status, PdfFont boldFont) {
        String statusText = getStatusText(status);
        Color statusColor = getStatusColor(status);
        
        return new Cell()
            .add(new Paragraph(statusText).setFont(boldFont).setFontColor(statusColor))
            .setTextAlignment(TextAlignment.CENTER);
    }
    
    /**
     * Creates a severity cell.
     */
    private Cell createSeverityCell(ValidationStatus status, PdfFont regularFont) {
        String severity;
        switch (status) {
            case PASS:
                severity = "None";
                break;
            case WARNING:
                severity = "Low";
                break;
            case FAIL:
                severity = "High";
                break;
            case ERROR:
                severity = "Critical";
                break;
            default:
                severity = "Unknown";
        }
        
        return new Cell()
            .add(new Paragraph(severity).setFont(regularFont))
            .setTextAlignment(TextAlignment.CENTER);
    }
    
    /**
     * Gets the display text for a validation status.
     */
    private String getStatusText(ValidationStatus status) {
        switch (status) {
            case PASS:
                return "PASSED";
            case WARNING:
                return "WARNING";
            case FAIL:
                return "FAILED";
            case ERROR:
                return "ERROR";
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Gets the color for a validation status.
     */
    private Color getStatusColor(ValidationStatus status) {
        switch (status) {
            case PASS:
                return COLOR_PASS;
            case WARNING:
                return COLOR_WARNING;
            case FAIL:
                return COLOR_FAIL;
            case ERROR:
                return COLOR_ERROR;
            default:
                return new DeviceRgb(128, 128, 128);
        }
    }
    
    /**
     * Calculates the overall validation status.
     */
    private ValidationStatus calculateOverallStatus(Map<String, ValidationResult> validationResults) {
        boolean hasError = false;
        boolean hasFail = false;
        boolean hasWarning = false;
        
        for (ValidationResult result : validationResults.values()) {
            switch (result.getStatus()) {
                case ERROR:
                    hasError = true;
                    break;
                case FAIL:
                    hasFail = true;
                    break;
                case WARNING:
                    hasWarning = true;
                    break;
            }
        }
        
        if (hasError) return ValidationStatus.ERROR;
        if (hasFail) return ValidationStatus.FAIL;
        if (hasWarning) return ValidationStatus.WARNING;
        return ValidationStatus.PASS;
    }
    
    /**
     * Formats file size in a human-readable format.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}