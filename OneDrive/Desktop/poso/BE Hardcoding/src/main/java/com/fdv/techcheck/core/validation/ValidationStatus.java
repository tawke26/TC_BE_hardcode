package com.fdv.techcheck.core.validation;

/**
 * Enumeration of possible validation statuses.
 * Represents the overall outcome of a validation operation.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public enum ValidationStatus {
    
    /**
     * Validation completed successfully with no issues found.
     */
    PASS("Passed", "All validation rules satisfied"),
    
    /**
     * Validation found issues that prevent document acceptance.
     */
    FAIL("Failed", "Validation rules violations found"),
    
    /**
     * Validation found minor issues that should be reviewed but don't prevent acceptance.
     */
    WARNING("Warning", "Minor issues found for review"),
    
    /**
     * Validation could not be completed due to an error.
     */
    ERROR("Error", "Validation process encountered an error"),
    
    /**
     * Validation was skipped (disabled or not applicable).
     */
    SKIP("Skipped", "Validation was not performed");
    
    private final String displayName;
    private final String description;
    
    ValidationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name for this status.
     * 
     * @return Display name suitable for user interfaces
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets a description of what this status means.
     * 
     * @return Descriptive text explaining the status
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status indicates a successful validation.
     * 
     * @return true for PASS and WARNING, false for others
     */
    public boolean isSuccessful() {
        return this == PASS || this == WARNING;
    }
    
    /**
     * Checks if this status indicates a critical failure.
     * 
     * @return true for FAIL and ERROR, false for others
     */
    public boolean isFailure() {
        return this == FAIL || this == ERROR;
    }
    
    /**
     * Gets the priority order for sorting statuses (most severe first).
     * 
     * @return Integer representing sort priority
     */
    public int getPriority() {
        return switch (this) {
            case ERROR -> 1;
            case FAIL -> 2;
            case WARNING -> 3;
            case PASS -> 4;
            case SKIP -> 5;
        };
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}