package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.AbstractDocumentValidator;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Validator for paragraph formatting requirements in thesis documents.
 * 
 * Validates:
 * - Paragraph length (50-2000 characters)
 * - Line spacing (should be 1.5)
 * - Text alignment (should be justified)
 * - Paragraph spacing consistency
 * 
 * @author TechCheck System
 * @version 1.0
 */
public class ParagraphValidator extends AbstractDocumentValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ParagraphValidator.class);
    
    // Validation thresholds
    private static final int MIN_PARAGRAPH_LENGTH = 50;
    private static final int MAX_PARAGRAPH_LENGTH = 2000;
    private static final double EXPECTED_LINE_SPACING = 1.5;
    private static final double LINE_SPACING_TOLERANCE = 0.1;
    
    /**
     * Constructor for ParagraphValidator.
     */
    public ParagraphValidator() {
        super("Paragraph Validator",
              ValidationSeverity.MINOR,
              "Validates paragraph length, line spacing, and alignment according to FDV Ljubljana standards");
    }
    
    @Override
    public String getValidatorName() {
        return "Paragraph Validator";
    }
    
    @Override
    protected ValidationResult performValidation(ThesisDocument document) {
        String fileName = document.getMetadata() != null ? document.getMetadata().getFileName() : "unknown";
        logger.debug("Starting paragraph validation for document: {}", fileName);
        
        List<ValidationDetail> details = new ArrayList<>();
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        
        logger.debug("Found {} paragraphs to validate", paragraphs.size());
        
        int validParagraphs = 0;
        int totalTextParagraphs = 0;
        
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            String text = paragraph.getText().trim();
            
            // Skip empty paragraphs and headings
            if (text.isEmpty() || isHeading(paragraph)) {
                continue;
            }
            
            totalTextParagraphs++;
            
            // Validate paragraph length
            ValidationDetail lengthValidation = validateParagraphLength(paragraph, text, i + 1);
            if (lengthValidation != null) {
                details.add(lengthValidation);
            } else {
                validParagraphs++;
            }
            
            // Validate line spacing
            ValidationDetail spacingValidation = validateLineSpacing(paragraph, i + 1);
            if (spacingValidation != null) {
                details.add(spacingValidation);
            }
            
            // Validate text alignment
            ValidationDetail alignmentValidation = validateTextAlignment(paragraph, i + 1);
            if (alignmentValidation != null) {
                details.add(alignmentValidation);
            }
        }
        
        // Add summary information
        if (totalTextParagraphs == 0) {
            details.add(ValidationDetail.builder()
                .location("Document")
                .expected("Body text paragraphs")
                .actual("No text paragraphs found")
                .severity(ValidationSeverity.MAJOR)
                .recommendation("Ensure document contains body text paragraphs")
                .build());
        } else {
            logger.debug("Validated {} text paragraphs, {} passed length requirements",
                totalTextParagraphs, validParagraphs);
        }
        
        logger.debug("Paragraph validation completed with {} issues", details.size());
        
        return details.isEmpty()
            ? ValidationResult.pass(getValidatorName())
            : ValidationResult.fail(getValidatorName(), details);
    }
    
    /**
     * Validates paragraph length requirements
     */
    private ValidationDetail validateParagraphLength(XWPFParagraph paragraph, String text, int paragraphNumber) {
        int length = text.length();
        
        if (length < MIN_PARAGRAPH_LENGTH) {
            return ValidationDetail.builder()
                .location(String.format("Paragraph %d", paragraphNumber))
                .expected(String.format("Minimum %d characters", MIN_PARAGRAPH_LENGTH))
                .actual(String.format("%d characters", length))
                .severity(ValidationSeverity.MINOR)
                .recommendation(String.format("Expand paragraph content to at least %d characters", MIN_PARAGRAPH_LENGTH))
                .build();
        }
        
        if (length > MAX_PARAGRAPH_LENGTH) {
            return ValidationDetail.builder()
                .location(String.format("Paragraph %d", paragraphNumber))
                .expected(String.format("Maximum %d characters", MAX_PARAGRAPH_LENGTH))
                .actual(String.format("%d characters", length))
                .severity(ValidationSeverity.MINOR)
                .recommendation(String.format("Consider splitting paragraph into smaller sections (max %d characters)", MAX_PARAGRAPH_LENGTH))
                .build();
        }
        
        return null; // Valid length
    }
    
    /**
     * Validates line spacing requirements
     */
    private ValidationDetail validateLineSpacing(XWPFParagraph paragraph, int paragraphNumber) {
        // Get line spacing from paragraph formatting
        double lineSpacing = getLineSpacing(paragraph);
        
        if (Math.abs(lineSpacing - EXPECTED_LINE_SPACING) > LINE_SPACING_TOLERANCE) {
            return ValidationDetail.builder()
                .location(String.format("Paragraph %d", paragraphNumber))
                .expected(String.format(Locale.US, "Line spacing: %.1f", EXPECTED_LINE_SPACING))
                .actual(String.format(Locale.US, "Line spacing: %.1f", lineSpacing))
                .severity(ValidationSeverity.MINOR)
                .recommendation(String.format(Locale.US, "Set line spacing to %.1f", EXPECTED_LINE_SPACING))
                .build();
        }
        
        return null; // Valid line spacing
    }
    
    /**
     * Validates text alignment requirements
     */
    private ValidationDetail validateTextAlignment(XWPFParagraph paragraph, int paragraphNumber) {
        ParagraphAlignment alignment = paragraph.getAlignment();
        
        // Check if alignment is justified
        if (alignment != ParagraphAlignment.BOTH) {
            String alignmentName = alignment != null ? alignment.toString() : "undefined";
            return ValidationDetail.builder()
                .location(String.format("Paragraph %d", paragraphNumber))
                .expected("Alignment: JUSTIFIED")
                .actual(String.format("Alignment: %s", alignmentName))
                .severity(ValidationSeverity.MINOR)
                .recommendation("Set paragraph alignment to justified")
                .build();
        }
        
        return null; // Valid alignment
    }
    
    /**
     * Checks if paragraph is a heading based on styling
     */
    private boolean isHeading(XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        if (style != null && style.toLowerCase().contains("heading")) {
            return true;
        }
        
        // Check if paragraph has heading-like formatting (large font, bold)
        if (!paragraph.getRuns().isEmpty()) {
            var firstRun = paragraph.getRuns().get(0);
            if (firstRun.isBold() && firstRun.getFontSize() > 12) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets line spacing value from paragraph
     */
    private double getLineSpacing(XWPFParagraph paragraph) {
        // Try to get line spacing from paragraph formatting
        try {
            if (paragraph.getCTP().getPPr() != null && 
                paragraph.getCTP().getPPr().getSpacing() != null) {
                
                var spacing = paragraph.getCTP().getPPr().getSpacing();
                
                // Check if line rule is set to multiple (for 1.5 spacing)
                if (spacing.getLineRule() != null) {
                    // Line spacing is typically stored as 240 * multiplier for multiple spacing
                    if (spacing.getLine() != null) {
                        return ((Number) spacing.getLine()).intValue() / 240.0;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Unable to read line spacing for paragraph: {}", e.getMessage());
        }
        
        // Default assumption if we can't read the spacing
        return 1.0; // Assume single spacing if we can't determine
    }
    
    /**
     * Generates validation summary
     */
    private String generateSummary(int totalParagraphs, int issueCount) {
        if (issueCount == 0) {
            return String.format("All %d paragraphs meet formatting requirements", totalParagraphs);
        } else {
            return String.format("Found %d formatting issues in %d paragraphs", issueCount, totalParagraphs);
        }
    }
}