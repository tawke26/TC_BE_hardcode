package com.fdv.techcheck.core.document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Represents a thesis document loaded for validation.
 * Provides access to document content, structure, and formatting information
 * extracted from the underlying DOCX file.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class ThesisDocument {
    
    private final Path filePath;
    private final XWPFDocument xwpfDocument;
    private final DocumentMetadata metadata;
    private final PageSettings pageSettings;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private ThesisDocument(Builder builder) {
        this.filePath = Objects.requireNonNull(builder.filePath, "File path cannot be null");
        this.xwpfDocument = Objects.requireNonNull(builder.xwpfDocument, "XWPF document cannot be null");
        this.metadata = builder.metadata;
        this.pageSettings = builder.pageSettings;
    }
    
    /**
     * Creates a new builder for constructing ThesisDocument instances.
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // Basic getters
    
    public Path getFilePath() {
        return filePath;
    }
    
    public XWPFDocument getXwpfDocument() {
        return xwpfDocument;
    }
    
    public DocumentMetadata getMetadata() {
        return metadata;
    }
    
    public PageSettings getPageSettings() {
        return pageSettings;
    }
    
    // Document content access methods
    
    /**
     * Gets all paragraphs in the document.
     * 
     * @return List of XWPFParagraph objects
     */
    public List<XWPFParagraph> getParagraphs() {
        return xwpfDocument.getParagraphs();
    }
    
    /**
     * Gets all tables in the document.
     * 
     * @return List of XWPFTable objects
     */
    public List<XWPFTable> getTables() {
        return xwpfDocument.getTables();
    }
    
    // Margin analysis methods
    
    /**
     * Gets the top margin in centimeters.
     * 
     * @return Top margin value in cm
     */
    public double getTopMargin() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgMar() != null && sectPr.getPgMar().getTop() != null) {
            return convertTwipsToCentimeters(((Number) sectPr.getPgMar().getTop()).intValue());
        }
        return 0.0;
    }
    
    /**
     * Gets the bottom margin in centimeters.
     * 
     * @return Bottom margin value in cm
     */
    public double getBottomMargin() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgMar() != null && sectPr.getPgMar().getBottom() != null) {
            return convertTwipsToCentimeters(((Number) sectPr.getPgMar().getBottom()).intValue());
        }
        return 0.0;
    }
    
    /**
     * Gets the left margin in centimeters.
     * 
     * @return Left margin value in cm
     */
    public double getLeftMargin() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgMar() != null && sectPr.getPgMar().getLeft() != null) {
            return convertTwipsToCentimeters(((Number) sectPr.getPgMar().getLeft()).intValue());
        }
        return 0.0;
    }
    
    /**
     * Gets the right margin in centimeters.
     * 
     * @return Right margin value in cm
     */
    public double getRightMargin() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgMar() != null && sectPr.getPgMar().getRight() != null) {
            return convertTwipsToCentimeters(((Number) sectPr.getPgMar().getRight()).intValue());
        }
        return 0.0;
    }
    
    // Page format methods
    
    /**
     * Gets the page width in centimeters.
     * 
     * @return Page width in cm
     */
    public double getPageWidth() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgSz() != null && sectPr.getPgSz().getW() != null) {
            return convertTwipsToCentimeters(((Number) sectPr.getPgSz().getW()).intValue());
        }
        return 21.0; // Default A4 width
    }
    
    /**
     * Gets the page height in centimeters.
     * 
     * @return Page height in cm
     */
    public double getPageHeight() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgSz() != null && sectPr.getPgSz().getH() != null) {
            return convertTwipsToCentimeters(((Number) sectPr.getPgSz().getH()).intValue());
        }
        return 29.7; // Default A4 height
    }
    
    /**
     * Gets the page orientation.
     * 
     * @return "portrait" or "landscape"
     */
    public String getPageOrientation() {
        CTSectPr sectPr = getDefaultSectionProperties();
        if (sectPr != null && sectPr.getPgSz() != null && sectPr.getPgSz().getOrient() != null) {
            return sectPr.getPgSz().getOrient().toString().toLowerCase();
        }
        return "portrait";
    }
    
    // Document analysis methods
    
    /**
     * Gets the total number of pages in the document.
     * Note: This is an approximation based on content and formatting.
     * 
     * @return Estimated page count
     */
    public int getEstimatedPageCount() {
        if (metadata != null && metadata.getPageCount() > 0) {
            return metadata.getPageCount();
        }
        
        // Rough estimation based on paragraphs
        int paragraphCount = getParagraphs().size();
        return Math.max(1, paragraphCount / 25); // Assume ~25 paragraphs per page
    }
    
    /**
     * Gets the total word count of the document.
     * 
     * @return Total word count
     */
    public int getWordCount() {
        if (metadata != null && metadata.getWordCount() > 0) {
            return metadata.getWordCount();
        }
        
        // Calculate from content
        return getParagraphs().stream()
                .mapToInt(p -> countWordsInText(p.getText()))
                .sum();
    }
    
    /**
     * Checks if the document appears to be empty or has minimal content.
     * 
     * @return true if document has very little content
     */
    public boolean isEmpty() {
        return getParagraphs().size() < 3 || getWordCount() < 50;
    }
    
    // Helper methods
    
    /**
     * Gets the default section properties from the document.
     * 
     * @return CTSectPr object or null if not available
     */
    private CTSectPr getDefaultSectionProperties() {
        try {
            return xwpfDocument.getDocument().getBody().getSectPr();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Converts twips (twentieths of a point) to centimeters.
     * 
     * @param twips Value in twips
     * @return Value in centimeters
     */
    private double convertTwipsToCentimeters(int twips) {
        // 1 point = 20 twips, 1 inch = 72 points, 1 inch = 2.54 cm
        return (twips / 20.0) * (2.54 / 72.0);
    }
    
    /**
     * Counts words in a text string.
     * 
     * @param text Text to count words in
     * @return Number of words
     */
    private int countWordsInText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
    
    @Override
    public String toString() {
        return String.format("ThesisDocument{file='%s', pages=%d, words=%d}", 
                           filePath.getFileName(), getEstimatedPageCount(), getWordCount());
    }
    
    /**
     * Builder class for constructing ThesisDocument instances.
     */
    public static class Builder {
        private Path filePath;
        private XWPFDocument xwpfDocument;
        private DocumentMetadata metadata;
        private PageSettings pageSettings;
        
        private Builder() {}
        
        public Builder filePath(Path filePath) {
            this.filePath = filePath;
            return this;
        }
        
        public Builder xwpfDocument(XWPFDocument xwpfDocument) {
            this.xwpfDocument = xwpfDocument;
            return this;
        }
        
        public Builder metadata(DocumentMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder pageSettings(PageSettings pageSettings) {
            this.pageSettings = pageSettings;
            return this;
        }
        
        public ThesisDocument build() {
            return new ThesisDocument(this);
        }
    }
}