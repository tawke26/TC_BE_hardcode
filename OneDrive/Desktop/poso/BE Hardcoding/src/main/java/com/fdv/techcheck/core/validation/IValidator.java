package com.fdv.techcheck.core.validation;

/**
 * Core interface for all validation components in TechCheck.
 * Provides a common contract for validating different aspects of thesis documents.
 * 
 * @param <T> The type of object to validate
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public interface IValidator<T> {
    
    /**
     * Validates the given target object according to specific rules.
     * 
     * @param target The object to validate
     * @return ValidationResult containing the outcome and any issues found
     * @throws ValidationException if validation cannot be completed
     */
    ValidationResult validate(T target) throws ValidationException;
    
    /**
     * Gets the human-readable name of this validator.
     * 
     * @return The validator name for reporting and logging
     */
    String getValidatorName();
    
    /**
     * Gets the default severity level for issues found by this validator.
     * 
     * @return The severity level (CRITICAL, MAJOR, MINOR, INFO)
     */
    ValidationSeverity getDefaultSeverity();
    
    /**
     * Determines if this validator is enabled in the current configuration.
     * 
     * @return true if the validator should run, false otherwise
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * Gets a brief description of what this validator checks.
     * 
     * @return Description text for user interface display
     */
    String getDescription();
}