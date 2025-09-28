package com.fdv.techcheck.core.validation;

import com.fdv.techcheck.core.document.ThesisDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Abstract base class for all document validators.
 * Provides common functionality for validation timing, error handling, and logging.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public abstract class AbstractDocumentValidator implements IValidator<ThesisDocument> {
    
    protected final Logger logger;
    private final String validatorName;
    private final ValidationSeverity defaultSeverity;
    private final String description;
    private boolean enabled = true;
    
    /**
     * Constructor for abstract validator.
     * 
     * @param validatorName Human-readable name for this validator
     * @param defaultSeverity Default severity level for issues found
     * @param description Brief description of what this validator checks
     */
    protected AbstractDocumentValidator(String validatorName, 
                                      ValidationSeverity defaultSeverity, 
                                      String description) {
        this.validatorName = Objects.requireNonNull(validatorName, "Validator name cannot be null");
        this.defaultSeverity = Objects.requireNonNull(defaultSeverity, "Default severity cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.logger = LoggerFactory.getLogger(this.getClass());
    }
    
    /**
     * Final implementation of validate method that handles timing and error catching.
     * Delegates actual validation logic to performValidation method.
     */
    @Override
    public final ValidationResult validate(ThesisDocument document) throws ValidationException {
        Objects.requireNonNull(document, "Document cannot be null");
        
        if (!isEnabled()) {
            logger.debug("Validator {} is disabled, skipping validation", validatorName);
            return ValidationResult.skip(validatorName, "Validator disabled");
        }
        
        logger.debug("Starting validation: {}", validatorName);
        Instant startTime = Instant.now();
        
        try {
            // Perform pre-validation checks
            preValidationCheck(document);
            
            // Execute the actual validation logic
            ValidationResult result = performValidation(document);
            
            // Perform post-validation processing
            result = postValidationProcess(result);
            
            // Add timing information
            Duration processingTime = Duration.between(startTime, Instant.now());
            result = result.withProcessingTime(processingTime);
            
            logger.debug("Completed validation: {} in {}ms with status: {}", 
                        validatorName, processingTime.toMillis(), result.getStatus());
            
            return result;
            
        } catch (ValidationException e) {
            Duration processingTime = Duration.between(startTime, Instant.now());
            logger.error("Validation failed: {} after {}ms", validatorName, processingTime.toMillis(), e);
            throw e;
            
        } catch (Exception e) {
            Duration processingTime = Duration.between(startTime, Instant.now());
            logger.error("Unexpected error during validation: {} after {}ms", 
                        validatorName, processingTime.toMillis(), e);
            
            ValidationException validationException = new ValidationException(
                validatorName, 
                "Unexpected error during validation: " + e.getMessage(), 
                e, 
                ValidationSeverity.CRITICAL
            );
            throw validationException;
        }
    }
    
    /**
     * Performs the actual validation logic for this validator.
     * Subclasses must implement this method with their specific validation rules.
     * 
     * @param document The document to validate
     * @return ValidationResult containing the outcome
     * @throws ValidationException if validation cannot be completed
     */
    protected abstract ValidationResult performValidation(ThesisDocument document) throws ValidationException;
    
    /**
     * Pre-validation check hook that can be overridden by subclasses.
     * Called before the main validation logic.
     * 
     * @param document The document to validate
     * @throws ValidationException if pre-validation check fails
     */
    protected void preValidationCheck(ThesisDocument document) throws ValidationException {
        // Default implementation: check for basic document validity
        if (document.isEmpty()) {
            throw new ValidationException(validatorName, 
                "Document appears to be empty or has insufficient content");
        }
    }
    
    /**
     * Post-validation processing hook that can be overridden by subclasses.
     * Called after the main validation logic to modify or enhance results.
     * 
     * @param result The validation result from performValidation
     * @return Modified validation result
     */
    protected ValidationResult postValidationProcess(ValidationResult result) {
        // Default implementation: no post-processing
        return result;
    }
    
    /**
     * Helper method to check if a numeric value is within an acceptable tolerance.
     * 
     * @param actual The actual value found
     * @param expected The expected value
     * @param tolerance The acceptable deviation
     * @return true if actual is within tolerance of expected
     */
    protected boolean isWithinTolerance(double actual, double expected, double tolerance) {
        return Math.abs(actual - expected) <= tolerance;
    }
    
    /**
     * Helper method to format a value in centimeters for display.
     * 
     * @param value Value in centimeters
     * @return Formatted string (e.g., "2.5 cm")
     */
    protected String formatCentimeters(double value) {
        return String.format("%.1f cm", value);
    }
    
    /**
     * Helper method to format a value in points for display.
     * 
     * @param value Value in points
     * @return Formatted string (e.g., "12 pt")
     */
    protected String formatPoints(double value) {
        return String.format("%.0f pt", value);
    }
    
    /**
     * Helper method to format a percentage for display.
     * 
     * @param value Value as decimal (e.g., 1.5 for 150%)
     * @return Formatted string (e.g., "150%")
     */
    protected String formatPercentage(double value) {
        return String.format("%.0f%%", value * 100);
    }
    
    /**
     * Creates a ValidationDetail with this validator's default settings.
     * 
     * @param location Where the issue was found
     * @param expected What was expected
     * @param actual What was found
     * @param recommendation How to fix the issue
     * @return ValidationDetail instance
     */
    protected ValidationDetail createDetail(String location, String expected, 
                                          String actual, String recommendation) {
        return ValidationDetail.builder()
                .location(location)
                .expected(expected)
                .actual(actual)
                .severity(defaultSeverity)
                .recommendation(recommendation)
                .build();
    }
    
    /**
     * Creates a ValidationDetail with custom severity.
     * 
     * @param location Where the issue was found
     * @param expected What was expected
     * @param actual What was found
     * @param recommendation How to fix the issue
     * @param severity Custom severity level
     * @return ValidationDetail instance
     */
    protected ValidationDetail createDetail(String location, String expected, 
                                          String actual, String recommendation, 
                                          ValidationSeverity severity) {
        return ValidationDetail.builder()
                .location(location)
                .expected(expected)
                .actual(actual)
                .severity(severity)
                .recommendation(recommendation)
                .build();
    }
    
    // IValidator interface implementations
    
    @Override
    public String getValidatorName() {
        return validatorName;
    }
    
    @Override
    public ValidationSeverity getDefaultSeverity() {
        return defaultSeverity;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets whether this validator is enabled.
     * 
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logger.debug("Validator {} {}", validatorName, enabled ? "enabled" : "disabled");
    }
    
    @Override
    public String toString() {
        return String.format("%s{enabled=%s, severity=%s}", 
                           validatorName, enabled, defaultSeverity);
    }
}