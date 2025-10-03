package com.fdv.techcheck.modules.content.models;

/**
 * Data model for heading analysis information.
 * Contains all relevant data about a heading found in the document.
 */
public class HeadingInfo {
    
    private final int level;
    private final String text;
    private final int fontSize;
    private final String fontFamily;
    private final boolean isBold;
    private final int paragraphIndex;
    private final String numberingText;
    
    private HeadingInfo(Builder builder) {
        this.level = builder.level;
        this.text = builder.text;
        this.fontSize = builder.fontSize;
        this.fontFamily = builder.fontFamily;
        this.isBold = builder.isBold;
        this.paragraphIndex = builder.paragraphIndex;
        this.numberingText = builder.numberingText;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public int getLevel() { return level; }
    public String getText() { return text; }
    public int getFontSize() { return fontSize; }
    public String getFontFamily() { return fontFamily; }
    public boolean isBold() { return isBold; }
    public int getParagraphIndex() { return paragraphIndex; }
    public String getNumberingText() { return numberingText; }
    
    public static class Builder {
        private int level;
        private String text = "";
        private int fontSize;
        private String fontFamily = "";
        private boolean isBold;
        private int paragraphIndex;
        private String numberingText = "";
        
        public Builder level(int level) {
            this.level = level;
            return this;
        }
        
        public Builder text(String text) {
            this.text = text != null ? text : "";
            return this;
        }
        
        public Builder fontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }
        
        public Builder fontFamily(String fontFamily) {
            this.fontFamily = fontFamily != null ? fontFamily : "";
            return this;
        }
        
        public Builder isBold(boolean isBold) {
            this.isBold = isBold;
            return this;
        }
        
        public Builder paragraphIndex(int paragraphIndex) {
            this.paragraphIndex = paragraphIndex;
            return this;
        }
        
        public Builder numberingText(String numberingText) {
            this.numberingText = numberingText != null ? numberingText : "";
            return this;
        }
        
        public HeadingInfo build() {
            return new HeadingInfo(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("HeadingInfo{level=%d, text='%s', fontSize=%d, fontFamily='%s', isBold=%s, paragraphIndex=%d, numberingText='%s'}", 
                level, text, fontSize, fontFamily, isBold, paragraphIndex, numberingText);
    }
}