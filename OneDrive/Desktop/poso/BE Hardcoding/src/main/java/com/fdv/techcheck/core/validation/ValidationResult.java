package com.fdv.techcheck.core.validation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a validation operation.
 * Contains the status, any issues found, and metadata about the validation process.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class ValidationResult {
    
    private final ValidationStatus status;
    private final String validatorName;
    private final List<ValidationDetail> details;
    private final Instant timestamp;
    private final Duration processingTime;
    private final String errorMessage;
    
    /**
     * Private constructor - use factory methods to create instances.
     */
    private ValidationResult(ValidationStatus status, String validatorName, 
                           List<ValidationDetail> details, Instant timestamp, 
                           Duration processingTime, String errorMessage) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.validatorName = Objects.requireNonNull(validatorName, "Validator name cannot be null");
        this.details = details != null ? new ArrayList<>(details) : new ArrayList<>();
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.processingTime = processingTime != null ? processingTime : Duration.ZERO;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Creates a successful validation result with no issues.
     * 
     * @param validatorName The name of the validator that produced this result
     * @return A PASS validation result
     */
    public static ValidationResult pass(String validatorName) {
        return new ValidationResult(ValidationStatus.PASS, validatorName, 
                                  Collections.emptyList(), Instant.now(), Duration.ZERO, null);
    }
    
    /**
     * Creates a failed validation result with specific issues.
     * 
     * @param validatorName The name of the validator that produced this result
     * @param details List of validation issues found
     * @return A FAIL validation result
     */
    public static ValidationResult fail(String validatorName, List<ValidationDetail> details) {
        return new ValidationResult(ValidationStatus.FAIL, validatorName, 
                                  details, Instant.now(), Duration.ZERO, null);
    }
    
    /**
     * Creates a warning validation result with minor issues.
     * 
     * @param validatorName The name of the validator that produced this result
     * @param details List of validation warnings found
     * @return A WARNING validation result
     */
    public static ValidationResult warning(String validatorName, List<ValidationDetail> details) {
        return new ValidationResult(ValidationStatus.WARNING, validatorName, 
                                  details, Instant.now(), Duration.ZERO, null);
    }
    
    /**
     * Creates an error validation result when validation cannot complete.
     * 
     * @param validatorName The name of the validator that produced this result
     * @param errorMessage Description of the error that occurred
     * @return An ERROR validation result
     */
    public static ValidationResult error(String validatorName, String errorMessage) {
        return new ValidationResult(ValidationStatus.ERROR, validatorName, 
                                  Collections.emptyList(), Instant.now(), Duration.ZERO, errorMessage);
    }
    
    /**
     * Creates a skipped validation result when validation is bypassed.
     * 
     * @param validatorName The name of the validator that produced this result
     * @param reason Reason why validation was skipped
     * @return A SKIP validation result
     */
    public static ValidationResult skip(String validatorName, String reason) {
        return new ValidationResult(ValidationStatus.SKIP, validatorName, 
                                  Collections.emptyList(), Instant.now(), Duration.ZERO, reason);
    }
    
    /**
     * Creates a new ValidationResult with updated processing time.
     * 
     * @param processingTime Time taken to complete validation
     * @return New ValidationResult with timing information
     */
    public ValidationResult withProcessingTime(Duration processingTime) {
        return new ValidationResult(this.status, this.validatorName, this.details, 
                                  this.timestamp, processingTime, this.errorMessage);
    }
    
    // Getters
    
    public ValidationStatus getStatus() {
        return status;
    }
    
    public String getValidatorName() {
        return validatorName;
    }
    
    public List<ValidationDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Duration getProcessingTime() {
        return processingTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Gets a descriptive message about the validation result.
     *
     * @return A human-readable message describing the validation outcome
     */
    public String getMessage() {
        switch (status) {
            case PASS:
                return "Validation passed successfully - no issues found";
            case WARNING:
                return "Validation completed with " + details.size() + " warning(s)";
            case FAIL:
                return "Validation failed with " + details.size() + " issue(s)";
            case ERROR:
                return errorMessage != null ? errorMessage : "Validation error occurred";
            case SKIP:
                return errorMessage != null ? "Skipped: " + errorMessage : "Validation was skipped";
            default:
                return "Unknown validation status";
        }
    }
    
    // Analysis methods
    
    /**
     * Checks if this validation result indicates any issues.
     * 
     * @return true if status is FAIL, WARNING, or ERROR
     */
    public boolean hasIssues() {
        return status == ValidationStatus.FAIL || 
               status == ValidationStatus.WARNING || 
               status == ValidationStatus.ERROR;
    }
    
    /**
     * Counts the number of critical issues found.
     * 
     * @return Number of critical severity issues
     */
    public long getCriticalIssueCount() {
        return details.stream()
                .filter(detail -> detail.getSeverity() == ValidationSeverity.CRITICAL)
                .count();
    }
    
    /**
     * Counts the number of major issues found.
     * 
     * @return Number of major severity issues
     */
    public long getMajorIssueCount() {
        return details.stream()
                .filter(detail -> detail.getSeverity() == ValidationSeverity.MAJOR)
                .count();
    }
    
    /**
     * Counts the number of minor issues found.
     * 
     * @return Number of minor severity issues
     */
    public long getMinorIssueCount() {
        return details.stream()
                .filter(detail -> detail.getSeverity() == ValidationSeverity.MINOR)
                .count();
    }
    
    /**
     * Gets the highest severity level among all issues.
     * 
     * @return The most severe ValidationSeverity, or null if no issues
     */
    public ValidationSeverity getHighestSeverity() {
        return details.stream()
                .map(ValidationDetail::getSeverity)
                .max(ValidationSeverity::compareTo)
                .orElse(null);
    }
    
    /**
     * Checks if validation was successful (no failures or errors).
     * 
     * @return true if status is PASS or WARNING
     */
    public boolean isSuccessful() {
        return status == ValidationStatus.PASS || status == ValidationStatus.WARNING;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return status == that.status &&
               Objects.equals(validatorName, that.validatorName) &&
               Objects.equals(details, that.details) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status, validatorName, details, timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("ValidationResult{status=%s, validator='%s', issues=%d, time=%dms}", 
                           status, validatorName, details.size(), processingTime.toMillis());
    }
}