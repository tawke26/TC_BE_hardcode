package com.fdv.techcheck.core.document;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Utility class for loading and processing DOCX thesis documents.
 * Handles document parsing, metadata extraction, and error handling.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class DocumentProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private DocumentProcessor() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Loads a DOCX document from the specified file path.
     * 
     * @param filePath Path to the DOCX file
     * @return ThesisDocument ready for validation
     * @throws DocumentProcessingException if the document cannot be loaded
     */
    public static ThesisDocument loadDocument(Path filePath) throws DocumentProcessingException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        
        logger.info("Loading document: {}", filePath);
        
        // Validate file exists and is readable
        validateFile(filePath);
        
        try {
            // Load the DOCX document using Apache POI
            XWPFDocument xwpfDocument = new XWPFDocument(Files.newInputStream(filePath));
            
            // Extract metadata
            DocumentMetadata metadata = extractMetadata(filePath, xwpfDocument);
            
            // Extract page settings
            PageSettings pageSettings = extractPageSettings(xwpfDocument);
            
            // Build and return the ThesisDocument
            ThesisDocument document = ThesisDocument.builder()
                    .filePath(filePath)
                    .xwpfDocument(xwpfDocument)
                    .metadata(metadata)
                    .pageSettings(pageSettings)
                    .build();
            
            logger.info("Successfully loaded document: {} ({} pages, {} words)", 
                       filePath.getFileName(), 
                       metadata.getPageCount(), 
                       metadata.getWordCount());
            
            return document;
            
        } catch (IOException e) {
            String message = String.format("Failed to load document %s: %s", 
                                         filePath.getFileName(), e.getMessage());
            logger.error(message, e);
            throw new DocumentProcessingException(message, e);
        } catch (Exception e) {
            String message = String.format("Unexpected error loading document %s: %s", 
                                         filePath.getFileName(), e.getMessage());
            logger.error(message, e);
            throw new DocumentProcessingException(message, e);
        }
    }
    
    /**
     * Validates that the file exists, is readable, and appears to be a valid DOCX file.
     * 
     * @param filePath Path to validate
     * @throws DocumentProcessingException if validation fails
     */
    private static void validateFile(Path filePath) throws DocumentProcessingException {
        if (!Files.exists(filePath)) {
            throw new DocumentProcessingException("File does not exist: " + filePath);
        }
        
        if (!Files.isReadable(filePath)) {
            throw new DocumentProcessingException("File is not readable: " + filePath);
        }
        
        if (!Files.isRegularFile(filePath)) {
            throw new DocumentProcessingException("Path is not a regular file: " + filePath);
        }
        
        // Check file extension
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (!fileName.endsWith(".docx")) {
            throw new DocumentProcessingException("File must have .docx extension: " + fileName);
        }
        
        // Check file size (basic sanity check)
        try {
            long fileSize = Files.size(filePath);
            if (fileSize == 0) {
                throw new DocumentProcessingException("File is empty: " + filePath);
            }
            if (fileSize > 100 * 1024 * 1024) { // 100MB limit
                logger.warn("Large file detected: {} ({} MB)", filePath, fileSize / (1024 * 1024));
            }
        } catch (IOException e) {
            throw new DocumentProcessingException("Cannot read file size: " + filePath, e);
        }
    }
    
    /**
     * Extracts metadata from the document and file system.
     * 
     * @param filePath Path to the document file
     * @param xwpfDocument Loaded DOCX document
     * @return DocumentMetadata with extracted information
     */
    private static DocumentMetadata extractMetadata(Path filePath, XWPFDocument xwpfDocument) {
        logger.debug("Extracting metadata from document: {}", filePath.getFileName());
        
        DocumentMetadata.Builder builder = DocumentMetadata.builder();
        
        try {
            // Extract file system metadata
            long fileSize = Files.size(filePath);
            builder.fileSizeBytes(fileSize);
            
            // Extract creation/modification times
            try {
                Instant createdTime = Files.readAttributes(filePath, "creationTime", java.nio.file.LinkOption.NOFOLLOW_LINKS)
                        .get("creationTime") != null ? 
                        ((java.nio.file.attribute.FileTime) Files.readAttributes(filePath, "creationTime", java.nio.file.LinkOption.NOFOLLOW_LINKS).get("creationTime")).toInstant() : 
                        null;
                Instant modifiedTime = Files.getLastModifiedTime(filePath).toInstant();
                
                builder.createdDate(createdTime).modifiedDate(modifiedTime);
            } catch (Exception e) {
                logger.debug("Could not extract file times: {}", e.getMessage());
            }
            
            // Extract document properties from DOCX
            try {
                var coreProps = xwpfDocument.getProperties().getCoreProperties();
                if (coreProps != null) {
                    if (coreProps.getTitle() != null) {
                        builder.title(coreProps.getTitle());
                    }
                    if (coreProps.getCreator() != null) {
                        builder.author(coreProps.getCreator());
                    }
                    if (coreProps.getSubject() != null) {
                        builder.subject(coreProps.getSubject());
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not extract document properties: {}", e.getMessage());
            }
            
            // Count content elements
            int paragraphCount = xwpfDocument.getParagraphs().size();
            int wordCount = countWords(xwpfDocument);
            int characterCount = countCharacters(xwpfDocument);
            int pageCount = estimatePageCount(xwpfDocument, wordCount);
            
            builder.paragraphCount(paragraphCount)
                   .wordCount(wordCount)
                   .characterCount(characterCount)
                   .pageCount(pageCount);
            
        } catch (IOException e) {
            logger.warn("Error extracting some metadata: {}", e.getMessage());
        }
        
        return builder.build();
    }
    
    /**
     * Extracts page settings from the document.
     * 
     * @param xwpfDocument Loaded DOCX document
     * @return PageSettings with extracted formatting information
     */
    private static PageSettings extractPageSettings(XWPFDocument xwpfDocument) {
        logger.debug("Extracting page settings from document");
        
        PageSettings.Builder builder = PageSettings.builder();
        
        try {
            var sectPr = xwpfDocument.getDocument().getBody().getSectPr();
            
            if (sectPr != null) {
                // Extract page size
                if (sectPr.getPgSz() != null) {
                    if (sectPr.getPgSz().getW() != null) {
                        double width = convertTwipsToCentimeters(((Number) sectPr.getPgSz().getW()).intValue());
                        builder.pageWidth(width);
                    }
                    if (sectPr.getPgSz().getH() != null) {
                        double height = convertTwipsToCentimeters(((Number) sectPr.getPgSz().getH()).intValue());
                        builder.pageHeight(height);
                    }
                    if (sectPr.getPgSz().getOrient() != null) {
                        builder.orientation(sectPr.getPgSz().getOrient().toString().toLowerCase());
                    }
                }
                
                // Extract margins
                if (sectPr.getPgMar() != null) {
                    if (sectPr.getPgMar().getTop() != null) {
                        double top = convertTwipsToCentimeters(((Number) sectPr.getPgMar().getTop()).intValue());
                        builder.topMargin(top);
                    }
                    if (sectPr.getPgMar().getBottom() != null) {
                        double bottom = convertTwipsToCentimeters(((Number) sectPr.getPgMar().getBottom()).intValue());
                        builder.bottomMargin(bottom);
                    }
                    if (sectPr.getPgMar().getLeft() != null) {
                        double left = convertTwipsToCentimeters(((Number) sectPr.getPgMar().getLeft()).intValue());
                        builder.leftMargin(left);
                    }
                    if (sectPr.getPgMar().getRight() != null) {
                        double right = convertTwipsToCentimeters(((Number) sectPr.getPgMar().getRight()).intValue());
                        builder.rightMargin(right);
                    }
                    if (sectPr.getPgMar().getHeader() != null) {
                        double header = convertTwipsToCentimeters(((Number) sectPr.getPgMar().getHeader()).intValue());
                        builder.headerMargin(header);
                    }
                    if (sectPr.getPgMar().getFooter() != null) {
                        double footer = convertTwipsToCentimeters(((Number) sectPr.getPgMar().getFooter()).intValue());
                        builder.footerMargin(footer);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error extracting page settings, using defaults: {}", e.getMessage());
            return PageSettings.a4Portrait(); // Return default A4 settings
        }
        
        return builder.build();
    }
    
    /**
     * Counts the total number of words in the document.
     * 
     * @param xwpfDocument Document to analyze
     * @return Total word count
     */
    private static int countWords(XWPFDocument xwpfDocument) {
        return xwpfDocument.getParagraphs().stream()
                .mapToInt(paragraph -> {
                    String text = paragraph.getText();
                    if (text == null || text.trim().isEmpty()) {
                        return 0;
                    }
                    return text.trim().split("\\s+").length;
                })
                .sum();
    }
    
    /**
     * Counts the total number of characters in the document.
     * 
     * @param xwpfDocument Document to analyze
     * @return Total character count
     */
    private static int countCharacters(XWPFDocument xwpfDocument) {
        return xwpfDocument.getParagraphs().stream()
                .mapToInt(paragraph -> {
                    String text = paragraph.getText();
                    return text != null ? text.length() : 0;
                })
                .sum();
    }
    
    /**
     * Estimates the number of pages based on content analysis.
     * 
     * @param xwpfDocument Document to analyze
     * @param wordCount Total word count
     * @return Estimated page count
     */
    private static int estimatePageCount(XWPFDocument xwpfDocument, int wordCount) {
        // Simple estimation: average of 250-300 words per page
        int pagesByWords = Math.max(1, wordCount / 275);
        
        // Also consider paragraph count (rough estimation)
        int paragraphCount = xwpfDocument.getParagraphs().size();
        int pagesByParagraphs = Math.max(1, paragraphCount / 25);
        
        // Use the average of both estimates
        return (pagesByWords + pagesByParagraphs) / 2;
    }
    
    /**
     * Converts twips (twentieths of a point) to centimeters.
     * 
     * @param twips Value in twips
     * @return Value in centimeters
     */
    private static double convertTwipsToCentimeters(int twips) {
        // 1 point = 20 twips, 1 inch = 72 points, 1 inch = 2.54 cm
        return (twips / 20.0) * (2.54 / 72.0);
    }
    
    /**
     * Closes a document and releases resources.
     * 
     * @param document Document to close
     */
    public static void closeDocument(ThesisDocument document) {
        if (document != null && document.getXwpfDocument() != null) {
            try {
                document.getXwpfDocument().close();
                logger.debug("Closed document: {}", document.getFilePath().getFileName());
            } catch (IOException e) {
                logger.warn("Error closing document: {}", e.getMessage());
            }
        }
    }
}