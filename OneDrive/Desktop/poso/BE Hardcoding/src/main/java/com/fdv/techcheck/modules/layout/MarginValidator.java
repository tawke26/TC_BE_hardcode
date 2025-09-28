package com.fdv.techcheck.modules.layout;

import com.fdv.techcheck.core.document.PageSettings;
import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.AbstractDocumentValidator;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import com.fdv.techcheck.core.validation.ValidationStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates document margins according to FDV technical requirements.
 *
 * Requirements:
 * - All margins must be exactly 2.5 cm (71 points)
 * - Left, right, top, and bottom margins are all checked
 * - Any deviation from 2.5 cm is flagged as a critical error
 *
 * @author TechCheck System
 * @version 1.0
 */
public final class MarginValidator extends AbstractDocumentValidator {
    
    // FDV requirement: 2.5 cm margins converted to points (1 cm = 28.35 points)
    private static final double REQUIRED_MARGIN_CM = 2.5;
    private static final double POINTS_PER_CM = 28.35;
    private static final double REQUIRED_MARGIN_POINTS = REQUIRED_MARGIN_CM * POINTS_PER_CM; // ≈ 70.87 points
    private static final double TOLERANCE_POINTS = 1.0; // Allow 1 point tolerance for rounding
    private static final double CRITICAL_DIFFERENCE_CM = 1.0; // 1+ cm difference
    private static final double MAJOR_DIFFERENCE_CM = 0.5; // 0.5-1 cm difference
    
    private static final String VALIDATOR_NAME = "Margin Validator";
    private static final String VALIDATOR_DESCRIPTION =
            "Validates document margins according to FDV requirements (2.5 cm)";
    public MarginValidator() {
        super(VALIDATOR_NAME, ValidationSeverity.CRITICAL, VALIDATOR_DESCRIPTION);
    }
    
    @Override
    protected ValidationResult performValidation(final ThesisDocument document) throws ValidationException {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting margin validation for document: {}",
                       document.getMetadata().getTitle());
            
            List<ValidationDetail> details = new ArrayList<>();
            PageSettings pageSettings = document.getPageSettings();
            
            // Validate each margin
            validateMargin("Left", pageSettings.getLeftMargin(), details);
            validateMargin("Right", pageSettings.getRightMargin(), details);
            validateMargin("Top", pageSettings.getTopMargin(), details);
            validateMargin("Bottom", pageSettings.getBottomMargin(), details);
            
            // Determine overall status
            ValidationStatus status = details.isEmpty() ? ValidationStatus.PASS : ValidationStatus.FAIL;
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Margin validation completed in {} ms with status: {}", duration, status);
            
            return details.isEmpty()
                ? ValidationResult.pass(getValidatorName())
                : ValidationResult.fail(getValidatorName(), details);
                    
        } catch (Exception e) {
            String errorMsg = "Failed to validate document margins: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new ValidationException(getValidatorName(), errorMsg, e, ValidationSeverity.CRITICAL);
        }
    }
    
    /**
     * Validates a specific margin against the FDV requirement.
     *
     * @param marginName The name of the margin (Left, Right, Top, Bottom)
     * @param actualMarginPoints The actual margin value in points
     * @param details List to add validation details to
     */
    private void validateMargin(final String marginName, final double actualMarginPoints,
                               final List<ValidationDetail> details) {
        double actualMarginCm = pointsToCentimeters(actualMarginPoints);
        double difference = Math.abs(actualMarginPoints - REQUIRED_MARGIN_POINTS);
        
        if (difference > TOLERANCE_POINTS) {
            ValidationSeverity severity = determineSeverity(difference);
            
            ValidationDetail detail = ValidationDetail.builder()
                    .location(marginName + " margin")
                    .expected(formatExpectedMargin())
                    .actual(formatActualMargin(actualMarginCm))
                    .severity(severity)
                    .build();
                    
            details.add(detail);
            
            logger.warn("Margin violation detected: {} margin is {:.2f} cm (expected: {:.1f} cm)", 
                       marginName.toLowerCase(), actualMarginCm, REQUIRED_MARGIN_CM);
        } else {
            logger.debug("{} margin validation passed: {:.2f} cm", marginName.toLowerCase(), actualMarginCm);
        }
    }
    
    /**
     * Determines the severity of a margin violation based on the difference from required value.
     */
    private ValidationSeverity determineSeverity(final double differencePoints) {
        double differenceCm = pointsToCentimeters(differencePoints);
        
        if (differenceCm >= CRITICAL_DIFFERENCE_CM) {
            return ValidationSeverity.CRITICAL; // 1+ cm difference
        } else if (differenceCm >= MAJOR_DIFFERENCE_CM) {
            return ValidationSeverity.MAJOR;    // 0.5-1 cm difference
        } else {
            return ValidationSeverity.MINOR;    // < 0.5 cm difference
        }
    }
    
    /**
     * Formats the margin issue description.
     */
    private String formatMarginIssue(final String marginName, final double actualCm) {
        return String.format("%s margin is %.2f cm instead of required %.1f cm", 
                           marginName, actualCm, REQUIRED_MARGIN_CM);
    }
    
    /**
     * Formats the expected margin value.
     */
    private String formatExpectedMargin() {
        return String.format("%.1f cm (%.0f points)", REQUIRED_MARGIN_CM, REQUIRED_MARGIN_POINTS);
    }
    
    /**
     * Formats the actual margin value.
     */
    private String formatActualMargin(final double actualCm) {
        return String.format("%.2f cm (%.0f points)", actualCm, centimetersToPoints(actualCm));
    }
    
    /**
     * Generates a recommendation for fixing the margin issue.
     */
    private String generateRecommendation(final String marginName, final double actualCm) {
        if (actualCm < REQUIRED_MARGIN_CM) {
            return String.format("Increase %s margin to %.1f cm. Current margin is too small by %.2f cm.",
                               marginName.toLowerCase(), REQUIRED_MARGIN_CM, REQUIRED_MARGIN_CM - actualCm);
        } else {
            return String.format("Decrease %s margin to %.1f cm. Current margin is too large by %.2f cm.",
                               marginName.toLowerCase(), REQUIRED_MARGIN_CM, actualCm - REQUIRED_MARGIN_CM);
        }
    }
    
    /**
     * Converts points to centimeters.
     * 1 point = 1/72 inch, 1 inch = 2.54 cm
     * Therefore: 1 point = 2.54/72 cm ≈ 0.0353 cm
     */
    private double pointsToCentimeters(final double points) {
        return points / POINTS_PER_CM; // 28.35 points per cm
    }
    
    /**
     * Converts centimeters to points.
     */
    private double centimetersToPoints(final double cm) {
        return cm * POINTS_PER_CM; // 28.35 points per cm
    }
    
    @Override
    public boolean isEnabled() {
        return true; // Margin validation is always enabled for FDV requirements
    }
    
    @Override
    public String toString() {
        return String.format("MarginValidator{name='%s', enabled=%s, requiredMargin=%.1f cm}", 
                           getValidatorName(), isEnabled(), REQUIRED_MARGIN_CM);
    }
}