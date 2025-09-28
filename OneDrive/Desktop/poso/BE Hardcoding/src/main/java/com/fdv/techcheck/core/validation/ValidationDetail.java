package com.fdv.techcheck.core.validation;

import java.util.Objects;

/**
 * Represents a specific validation issue found during document analysis.
 * Contains detailed information about what was expected, what was found, 
 * and recommendations for fixing the issue.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class ValidationDetail {
    
    private final String location;
    private final String expected;
    private final String actual;
    private final ValidationSeverity severity;
    private final String recommendation;
    private final String ruleReference;
    private final String pageNumber;
    private final String lineNumber;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private ValidationDetail(Builder builder) {
        this.location = Objects.requireNonNull(builder.location, "Location cannot be null");
        this.expected = Objects.requireNonNull(builder.expected, "Expected value cannot be null");
        this.actual = Objects.requireNonNull(builder.actual, "Actual value cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.recommendation = builder.recommendation;
        this.ruleReference = builder.ruleReference;
        this.pageNumber = builder.pageNumber;
        this.lineNumber = builder.lineNumber;
    }
    
    /**
     * Creates a new builder for constructing ValidationDetail instances.
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a ValidationDetail with required fields.
     * 
     * @param location Where the issue was found
     * @param expected What was expected
     * @param actual What was actually found
     * @param severity The severity level of the issue
     * @return A new ValidationDetail instance
     */
    public static ValidationDetail of(String location, String expected, String actual, ValidationSeverity severity) {
        return builder()
                .location(location)
                .expected(expected)
                .actual(actual)
                .severity(severity)
                .build();
    }
    
    // Getters
    
    public String getLocation() {
        return location;
    }
    
    public String getExpected() {
        return expected;
    }
    
    public String getActual() {
        return actual;
    }
    
    public ValidationSeverity getSeverity() {
        return severity;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public String getRuleReference() {
        return ruleReference;
    }
    
    public String getPageNumber() {
        return pageNumber;
    }
    
    public String getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Checks if this validation detail has location information (page/line).
     * 
     * @return true if page or line number is available
     */
    public boolean hasLocationInfo() {
        return pageNumber != null || lineNumber != null;
    }
    
    /**
     * Gets a formatted location string for display.
     * 
     * @return Formatted location string
     */
    public String getFormattedLocation() {
        StringBuilder sb = new StringBuilder(location);
        
        if (pageNumber != null) {
            sb.append(" (Page ").append(pageNumber);
            if (lineNumber != null) {
                sb.append(", Line ").append(lineNumber);
            }
            sb.append(")");
        } else if (lineNumber != null) {
            sb.append(" (Line ").append(lineNumber).append(")");
        }
        
        return sb.toString();
    }
    
    /**
     * Gets a summary message describing this validation issue.
     * 
     * @return Summary message for display
     */
    public String getSummaryMessage() {
        return String.format("%s: Expected '%s', found '%s'", 
                           getFormattedLocation(), expected, actual);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationDetail that = (ValidationDetail) o;
        return Objects.equals(location, that.location) &&
               Objects.equals(expected, that.expected) &&
               Objects.equals(actual, that.actual) &&
               severity == that.severity;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(location, expected, actual, severity);
    }
    
    @Override
    public String toString() {
        return String.format("ValidationDetail{location='%s', expected='%s', actual='%s', severity=%s}", 
                           location, expected, actual, severity);
    }
    
    /**
     * Builder class for constructing ValidationDetail instances.
     */
    public static class Builder {
        private String location;
        private String expected;
        private String actual;
        private ValidationSeverity severity;
        private String recommendation;
        private String ruleReference;
        private String pageNumber;
        private String lineNumber;
        
        private Builder() {}
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder expected(String expected) {
            this.expected = expected;
            return this;
        }
        
        public Builder actual(String actual) {
            this.actual = actual;
            return this;
        }
        
        public Builder severity(ValidationSeverity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }
        
        public Builder ruleReference(String ruleReference) {
            this.ruleReference = ruleReference;
            return this;
        }
        
        public Builder pageNumber(String pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }
        
        public Builder pageNumber(int pageNumber) {
            this.pageNumber = String.valueOf(pageNumber);
            return this;
        }
        
        public Builder lineNumber(String lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }
        
        public Builder lineNumber(int lineNumber) {
            this.lineNumber = String.valueOf(lineNumber);
            return this;
        }
        
        public ValidationDetail build() {
            return new ValidationDetail(this);
        }
    }
}