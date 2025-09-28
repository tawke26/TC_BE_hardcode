package com.fdv.techcheck.core.document;

import java.util.Objects;

/**
 * Contains page formatting settings extracted from a thesis document.
 * Includes page size, orientation, margins, and other layout properties.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class PageSettings {
    
    private final double pageWidth;
    private final double pageHeight;
    private final String orientation;
    private final double topMargin;
    private final double bottomMargin;
    private final double leftMargin;
    private final double rightMargin;
    private final double headerMargin;
    private final double footerMargin;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private PageSettings(Builder builder) {
        this.pageWidth = builder.pageWidth;
        this.pageHeight = builder.pageHeight;
        this.orientation = builder.orientation;
        this.topMargin = builder.topMargin;
        this.bottomMargin = builder.bottomMargin;
        this.leftMargin = builder.leftMargin;
        this.rightMargin = builder.rightMargin;
        this.headerMargin = builder.headerMargin;
        this.footerMargin = builder.footerMargin;
    }
    
    /**
     * Creates a new builder for constructing PageSettings instances.
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates PageSettings with standard A4 dimensions and default margins.
     * 
     * @return PageSettings for A4 portrait with 2.5cm margins
     */
    public static PageSettings a4Portrait() {
        return builder()
                .pageWidth(21.0)
                .pageHeight(29.7)
                .orientation("portrait")
                .topMargin(2.5)
                .bottomMargin(2.5)
                .leftMargin(2.5)
                .rightMargin(2.5)
                .headerMargin(1.25)
                .footerMargin(1.25)
                .build();
    }
    
    // Getters
    
    public double getPageWidth() {
        return pageWidth;
    }
    
    public double getPageHeight() {
        return pageHeight;
    }
    
    public String getOrientation() {
        return orientation;
    }
    
    public double getTopMargin() {
        return topMargin;
    }
    
    public double getBottomMargin() {
        return bottomMargin;
    }
    
    public double getLeftMargin() {
        return leftMargin;
    }
    
    public double getRightMargin() {
        return rightMargin;
    }
    
    public double getHeaderMargin() {
        return headerMargin;
    }
    
    public double getFooterMargin() {
        return footerMargin;
    }
    
    // Analysis methods
    
    /**
     * Gets the effective text width (page width minus left and right margins).
     * 
     * @return Available text width in cm
     */
    public double getTextWidth() {
        return pageWidth - leftMargin - rightMargin;
    }
    
    /**
     * Gets the effective text height (page height minus top and bottom margins).
     * 
     * @return Available text height in cm
     */
    public double getTextHeight() {
        return pageHeight - topMargin - bottomMargin;
    }
    
    /**
     * Checks if this is a standard A4 page size.
     * 
     * @return true if dimensions match A4 (within tolerance)
     */
    public boolean isA4Size() {
        double tolerance = 0.1; // 1mm tolerance
        return Math.abs(pageWidth - 21.0) <= tolerance && 
               Math.abs(pageHeight - 29.7) <= tolerance;
    }
    
    /**
     * Checks if the page is in portrait orientation.
     * 
     * @return true if height > width
     */
    public boolean isPortrait() {
        return pageHeight > pageWidth;
    }
    
    /**
     * Checks if the page is in landscape orientation.
     * 
     * @return true if width > height
     */
    public boolean isLandscape() {
        return pageWidth > pageHeight;
    }
    
    /**
     * Gets the page size name if it matches a standard size.
     * 
     * @return Page size name (e.g., "A4", "Letter") or "Custom"
     */
    public String getPageSizeName() {
        double tolerance = 0.1;
        
        // A4: 21.0 x 29.7 cm
        if (Math.abs(pageWidth - 21.0) <= tolerance && Math.abs(pageHeight - 29.7) <= tolerance) {
            return "A4";
        }
        
        // Letter: 21.59 x 27.94 cm
        if (Math.abs(pageWidth - 21.59) <= tolerance && Math.abs(pageHeight - 27.94) <= tolerance) {
            return "Letter";
        }
        
        // A3: 29.7 x 42.0 cm
        if (Math.abs(pageWidth - 29.7) <= tolerance && Math.abs(pageHeight - 42.0) <= tolerance) {
            return "A3";
        }
        
        return "Custom";
    }
    
    /**
     * Checks if all margins are equal.
     * 
     * @return true if all margins are the same value
     */
    public boolean hasEqualMargins() {
        double tolerance = 0.01; // 0.1mm tolerance
        return Math.abs(topMargin - bottomMargin) <= tolerance &&
               Math.abs(topMargin - leftMargin) <= tolerance &&
               Math.abs(topMargin - rightMargin) <= tolerance;
    }
    
    /**
     * Gets a summary of the page layout.
     * 
     * @return Formatted summary string
     */
    public String getLayoutSummary() {
        return String.format("%s %s (%.1f × %.1f cm), margins: %.1f cm", 
                           getPageSizeName(), 
                           orientation,
                           pageWidth, 
                           pageHeight,
                           hasEqualMargins() ? topMargin : -1);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageSettings that = (PageSettings) o;
        return Double.compare(that.pageWidth, pageWidth) == 0 &&
               Double.compare(that.pageHeight, pageHeight) == 0 &&
               Double.compare(that.topMargin, topMargin) == 0 &&
               Double.compare(that.bottomMargin, bottomMargin) == 0 &&
               Double.compare(that.leftMargin, leftMargin) == 0 &&
               Double.compare(that.rightMargin, rightMargin) == 0 &&
               Objects.equals(orientation, that.orientation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pageWidth, pageHeight, orientation, topMargin, 
                          bottomMargin, leftMargin, rightMargin);
    }
    
    @Override
    public String toString() {
        return String.format("PageSettings{%s}", getLayoutSummary());
    }
    
    /**
     * Builder class for constructing PageSettings instances.
     */
    public static class Builder {
        private double pageWidth = 21.0; // A4 default
        private double pageHeight = 29.7; // A4 default
        private String orientation = "portrait";
        private double topMargin = 2.5;
        private double bottomMargin = 2.5;
        private double leftMargin = 2.5;
        private double rightMargin = 2.5;
        private double headerMargin = 1.25;
        private double footerMargin = 1.25;
        
        private Builder() {}
        
        public Builder pageWidth(double pageWidth) {
            this.pageWidth = pageWidth;
            return this;
        }
        
        public Builder pageHeight(double pageHeight) {
            this.pageHeight = pageHeight;
            return this;
        }
        
        public Builder orientation(String orientation) {
            this.orientation = orientation;
            return this;
        }
        
        public Builder topMargin(double topMargin) {
            this.topMargin = topMargin;
            return this;
        }
        
        public Builder bottomMargin(double bottomMargin) {
            this.bottomMargin = bottomMargin;
            return this;
        }
        
        public Builder leftMargin(double leftMargin) {
            this.leftMargin = leftMargin;
            return this;
        }
        
        public Builder rightMargin(double rightMargin) {
            this.rightMargin = rightMargin;
            return this;
        }
        
        public Builder headerMargin(double headerMargin) {
            this.headerMargin = headerMargin;
            return this;
        }
        
        public Builder footerMargin(double footerMargin) {
            this.footerMargin = footerMargin;
            return this;
        }
        
        /**
         * Sets all margins to the same value.
         * 
         * @param margin Margin value for all sides
         * @return This builder
         */
        public Builder allMargins(double margin) {
            this.topMargin = margin;
            this.bottomMargin = margin;
            this.leftMargin = margin;
            this.rightMargin = margin;
            return this;
        }
        
        public PageSettings build() {
            return new PageSettings(this);
        }
    }
}