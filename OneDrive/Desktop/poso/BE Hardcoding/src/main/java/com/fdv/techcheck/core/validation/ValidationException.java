package com.fdv.techcheck.core.validation;

/**
 * Exception thrown when validation operations cannot be completed.
 * This is a checked exception that forces callers to handle validation failures explicitly.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class ValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String validatorName;
    private final ValidationSeverity severity;
    
    /**
     * Constructs a new validation exception with the specified detail message.
     * 
     * @param message The detail message
     */
    public ValidationException(String message) {
        super(message);
        this.validatorName = null;
        this.severity = ValidationSeverity.CRITICAL;
    }
    
    /**
     * Constructs a new validation exception with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The cause of this exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validatorName = null;
        this.severity = ValidationSeverity.CRITICAL;
    }
    
    /**
     * Constructs a new validation exception with validator context.
     * 
     * @param validatorName Name of the validator that failed
     * @param message The detail message
     */
    public ValidationException(String validatorName, String message) {
        super(message);
        this.validatorName = validatorName;
        this.severity = ValidationSeverity.CRITICAL;
    }
    
    /**
     * Constructs a new validation exception with full context.
     * 
     * @param validatorName Name of the validator that failed
     * @param message The detail message
     * @param cause The cause of this exception
     * @param severity The severity level of this validation failure
     */
    public ValidationException(String validatorName, String message, Throwable cause, ValidationSeverity severity) {
        super(message, cause);
        this.validatorName = validatorName;
        this.severity = severity != null ? severity : ValidationSeverity.CRITICAL;
    }
    
    /**
     * Gets the name of the validator that threw this exception.
     * 
     * @return Validator name, or null if not specified
     */
    public String getValidatorName() {
        return validatorName;
    }
    
    /**
     * Gets the severity level of this validation failure.
     * 
     * @return Severity level (defaults to CRITICAL)
     */
    public ValidationSeverity getSeverity() {
        return severity;
    }
    
    /**
     * Creates a ValidationResult representing this exception.
     * 
     * @return ValidationResult with ERROR status
     */
    public ValidationResult toValidationResult() {
        String validator = validatorName != null ? validatorName : "Unknown Validator";
        return ValidationResult.error(validator, getMessage());
    }
    
    @Override
    public String toString() {
        if (validatorName != null) {
            return String.format("ValidationException[%s]: %s", validatorName, getMessage());
        }
        return super.toString();
    }
}