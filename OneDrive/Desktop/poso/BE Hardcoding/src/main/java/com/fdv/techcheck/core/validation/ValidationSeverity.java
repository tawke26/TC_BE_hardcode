package com.fdv.techcheck.core.validation;

/**
 * Enumeration of validation issue severity levels.
 * Used to categorize the importance and impact of validation findings.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public enum ValidationSeverity {
    
    /**
     * Critical issues that must be fixed before document can be accepted.
     * These represent fundamental formatting problems.
     */
    CRITICAL("Critical", "Must be fixed", "#FF4444"),
    
    /**
     * Major issues that should be fixed for compliance.
     * These represent significant formatting deviations.
     */
    MAJOR("Major", "Should be fixed", "#FF8800"),
    
    /**
     * Minor issues that are recommended to fix.
     * These represent minor formatting inconsistencies.
     */
    MINOR("Minor", "Recommended to fix", "#FFAA00"),
    
    /**
     * Informational findings that don't require action.
     * These provide helpful feedback or suggestions.
     */
    INFO("Info", "For information only", "#0088FF");
    
    private final String displayName;
    private final String actionText;
    private final String colorCode;
    
    ValidationSeverity(String displayName, String actionText, String colorCode) {
        this.displayName = displayName;
        this.actionText = actionText;
        this.colorCode = colorCode;
    }
    
    /**
     * Gets the human-readable display name for this severity.
     * 
     * @return Display name suitable for user interfaces
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the recommended action text for this severity level.
     * 
     * @return Action text describing what should be done
     */
    public String getActionText() {
        return actionText;
    }
    
    /**
     * Gets the color code associated with this severity level.
     * 
     * @return Hex color code for UI display
     */
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * Checks if this severity level requires immediate action.
     * 
     * @return true for CRITICAL and MAJOR, false for others
     */
    public boolean requiresAction() {
        return this == CRITICAL || this == MAJOR;
    }
    
    /**
     * Checks if this severity level blocks document acceptance.
     * 
     * @return true for CRITICAL only
     */
    public boolean isBlocking() {
        return this == CRITICAL;
    }
    
    /**
     * Gets the priority order for sorting severities (most severe first).
     * 
     * @return Integer representing sort priority
     */
    public int getPriority() {
        return switch (this) {
            case CRITICAL -> 1;
            case MAJOR -> 2;
            case MINOR -> 3;
            case INFO -> 4;
        };
    }
    
    
    @Override
    public String toString() {
        return displayName;
    }
}