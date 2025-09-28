package com.fdv.techcheck.modules.layout;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.AbstractDocumentValidator;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for page format and orientation requirements.
 * Validates A4 portrait orientation, page size, and layout settings.
 */
public class PageFormatValidator extends AbstractDocumentValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(PageFormatValidator.class);
    
    // A4 dimensions in points (1 inch = 72 points)
    private static final double A4_WIDTH_POINTS = 595.0;
    private static final double A4_HEIGHT_POINTS = 842.0;
    private static final double PAGE_SIZE_TOLERANCE = 10.0; // Allow 10 points tolerance
    
    private static final String VALIDATOR_NAME = "Page Format Validator";
    private static final String VALIDATOR_DESCRIPTION = "Validates A4 portrait page format requirements";

    public PageFormatValidator() {
        super(VALIDATOR_NAME, ValidationSeverity.CRITICAL, VALIDATOR_DESCRIPTION);
    }

    @Override
    protected ValidationResult performValidation(ThesisDocument document) {
        logger.info("Starting page format validation for document: {}", 
                   document.getMetadata().getTitle());
        
        List<ValidationDetail> details = new ArrayList<>();
        
        try {
            XWPFDocument docx = document.getXwpfDocument();
            
            // Validate page size and orientation
            validatePageSize(docx, details);
            validatePageOrientation(docx, details);
            
            logger.info("Page format validation completed. Found {} issues", details.size());
            
            if (details.stream().anyMatch(d -> d.getSeverity() == ValidationSeverity.CRITICAL || d.getSeverity() == ValidationSeverity.MAJOR)) {
                return ValidationResult.fail(getValidatorName(), details);
            } else if (details.stream().anyMatch(d -> d.getSeverity() == ValidationSeverity.MINOR)) {
                return ValidationResult.warning(getValidatorName(), details);
            } else {
                return ValidationResult.pass(getValidatorName());
            }
            
        } catch (Exception e) {
            logger.error("Error during page format validation", e);
            return ValidationResult.error(getValidatorName(), "Failed to validate page format: " + e.getMessage());
        }
    }

    /**
     * Validates that the document uses A4 page size
     */
    private void validatePageSize(XWPFDocument document, List<ValidationDetail> details) {
        try {
            // Get page settings from document
            if (document.getDocument().getBody().getSectPr() != null) {
                var sectPr = document.getDocument().getBody().getSectPr();
                
                if (sectPr.getPgSz() != null) {
                    var pageSize = sectPr.getPgSz();
                    
                    // Get width and height in twips (1/20 of a point)
                    double widthTwips = ((Number) pageSize.getW()).doubleValue();
                    double heightTwips = ((Number) pageSize.getH()).doubleValue();
                    
                    // Convert twips to points
                    double widthPoints = widthTwips / 20.0;
                    double heightPoints = heightTwips / 20.0;
                    
                    logger.debug("Page size: {}x{} points", widthPoints, heightPoints);
                    
                    // Check if it's A4 size (with tolerance)
                    boolean isA4Width = Math.abs(widthPoints - A4_WIDTH_POINTS) <= PAGE_SIZE_TOLERANCE;
                    boolean isA4Height = Math.abs(heightPoints - A4_HEIGHT_POINTS) <= PAGE_SIZE_TOLERANCE;
                    
                    if (!isA4Width || !isA4Height) {
                        details.add(ValidationDetail.builder()
                                .location("Document page settings")
                                .expected(String.format("A4 size (%.1fx%.1f points)", A4_WIDTH_POINTS, A4_HEIGHT_POINTS))
                                .actual(String.format("%.1fx%.1f points", widthPoints, heightPoints))
                                .severity(ValidationSeverity.CRITICAL)
                                .build());
                    } else {
                        details.add(ValidationDetail.builder()
                                .location("Document page settings")
                                .expected("A4 size")
                                .actual("A4 size")
                                .severity(ValidationSeverity.INFO)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not validate page size: {}", e.getMessage());
            details.add(ValidationDetail.builder()
                    .location("Document page settings")
                    .expected("Readable page size information")
                    .actual("Unreadable or missing page size")
                    .severity(ValidationSeverity.MAJOR)
                    .build());
        }
    }

    /**
     * Validates that the document is in portrait orientation
     */
    private void validatePageOrientation(XWPFDocument document, List<ValidationDetail> details) {
        try {
            if (document.getDocument().getBody().getSectPr() != null) {
                var sectPr = document.getDocument().getBody().getSectPr();
                
                if (sectPr.getPgSz() != null) {
                    var pageSize = sectPr.getPgSz();
                    
                    double widthTwips = ((Number) pageSize.getW()).doubleValue();
                    double heightTwips = ((Number) pageSize.getH()).doubleValue();
                    
                    // Portrait orientation means height > width
                    if (heightTwips <= widthTwips) {
                        details.add(ValidationDetail.builder()
                                .location("Document page settings")
                                .expected("Portrait orientation")
                                .actual("Landscape orientation")
                                .severity(ValidationSeverity.CRITICAL)
                                .build());
                    } else {
                        details.add(ValidationDetail.builder()
                                .location("Document page settings")
                                .expected("Portrait orientation")
                                .actual("Portrait orientation")
                                .severity(ValidationSeverity.INFO)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not validate page orientation: {}", e.getMessage());
            details.add(ValidationDetail.builder()
                    .location("Document page settings")
                    .expected("Readable page orientation information")
                    .actual("Unreadable or missing page orientation")
                    .severity(ValidationSeverity.MAJOR)
                    .build());
        }
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', enabled=%s, description='%s'}", 
                           getClass().getSimpleName(), getValidatorName(), isEnabled(), getDescription());
    }
}