package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.AbstractDocumentValidator;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import com.fdv.techcheck.modules.content.models.HeadingInfo;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates heading structure, hierarchy, font sizes, and numbering in thesis documents.
 * 
 * Checks:
 * - Proper heading hierarchy (H1 → H2 → H3, no skipping)
 * - Font size requirements (H1=16pt, H2=14pt, H3-H4=12pt, H5-H6=10pt)
 * - Numbering pattern (1. 1.1 1.1.1)
 * - Font family consistency (Times New Roman)
 */
public class HeadingValidator extends AbstractDocumentValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(HeadingValidator.class);
    
    /**
     * Constructor for HeadingValidator.
     */
    public HeadingValidator() {
        super("Heading Validator",
              ValidationSeverity.MAJOR,
              "Validates heading hierarchy, numbering, font sizes, and formatting according to FDV Ljubljana standards");
    }
    
    // FDV Ljubljana Heading Requirements
    private static final Map<Integer, Integer> HEADING_FONT_SIZES = Map.of(
        1, 16,  // H1: 16pt
        2, 14,  // H2: 14pt  
        3, 12,  // H3: 12pt
        4, 12,  // H4: 12pt
        5, 10,  // H5: 10pt
        6, 10   // H6: 10pt
    );
    
    private static final String REQUIRED_FONT = "Times New Roman";
    private static final Pattern NUMBERING_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)*)\\.?\\s+(.*)");
    private static final Pattern HEADING_STYLE_PATTERN = Pattern.compile(".*[Hh]eading\\s*(\\d+).*");
    
    @Override
    protected void preValidationCheck(ThesisDocument document) throws ValidationException {
        // Override the default pre-validation check to be less strict for heading validation
        // We only need to ensure the document has paragraphs, not a minimum word count
        if (document.getParagraphs().isEmpty()) {
            throw new ValidationException(getValidatorName(),
                "Document contains no paragraphs to analyze");
        }
    }
    
    @Override
    protected ValidationResult performValidation(ThesisDocument document) {
        String fileName = document.getMetadata() != null ? document.getMetadata().getFileName() : "unknown";
        logger.debug("Starting heading validation for document: {}", fileName);
        
        List<ValidationDetail> issues = new ArrayList<>();
        List<HeadingInfo> headings = extractHeadings(document);
        
        logger.debug("Found {} headings in document", headings.size());
        
        if (headings.isEmpty()) {
            issues.add(ValidationDetail.builder()
                .location("Document structure")
                .expected("At least one heading")
                .actual("No headings found")
                .severity(ValidationSeverity.MAJOR)
                .recommendation("Add proper heading structure to organize document content")
                .build());
        } else {
            // Validate hierarchy
            issues.addAll(validateHierarchy(headings));
            
            // Validate font sizes
            issues.addAll(validateFontSizes(headings));
            
            // Validate numbering
            issues.addAll(validateNumbering(headings));
            
            // Validate font family
            issues.addAll(validateFontFamily(headings));
        }
        
        logger.debug("Heading validation completed with {} issues", issues.size());
        
        return issues.isEmpty() 
            ? ValidationResult.pass(getValidatorName())
            : ValidationResult.fail(getValidatorName(), issues);
    }
    
    @Override
    public String getValidatorName() {
        return "Heading Validator";
    }
    
    /**
     * Extracts heading information from the document.
     */
    private List<HeadingInfo> extractHeadings(ThesisDocument document) {
        List<HeadingInfo> headings = new ArrayList<>();
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            
            // Check if paragraph is a heading by style
            int headingLevel = getHeadingLevel(paragraph);
            if (headingLevel > 0) {
                String text = paragraph.getText().trim();
                int fontSize = getFontSize(paragraph);
                String fontFamily = getFontFamily(paragraph);
                boolean isBold = isBold(paragraph);
                String numberingText = extractNumbering(text);
                
                HeadingInfo heading = HeadingInfo.builder()
                    .level(headingLevel)
                    .text(text)
                    .fontSize(fontSize)
                    .fontFamily(fontFamily)
                    .isBold(isBold)
                    .paragraphIndex(i)
                    .numberingText(numberingText)
                    .build();
                
                headings.add(heading);
                logger.debug("Found heading: level={}, text='{}', fontSize={}", headingLevel, text, fontSize);
            }
        }
        
        return headings;
    }
    
    /**
     * Determines the heading level from paragraph style.
     */
    private int getHeadingLevel(XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        if (style != null) {
            Matcher matcher = HEADING_STYLE_PATTERN.matcher(style);
            if (matcher.matches()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid heading level in style: {}", style);
                }
            }
        }
        
        // Fallback: check if text looks like a heading (bold, larger font, etc.)
        if (isBold(paragraph) && getFontSize(paragraph) >= 12) {
            String text = paragraph.getText().trim();
            if (NUMBERING_PATTERN.matcher(text).matches()) {
                // Guess level from numbering depth
                String numbering = extractNumbering(text);
                return (int) numbering.chars().filter(ch -> ch == '.').count() + 1;
            }
        }
        
        return 0; // Not a heading
    }
    
    /**
     * Extracts font size from paragraph.
     */
    private int getFontSize(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            XWPFRun firstRun = runs.get(0);
            int fontSize = firstRun.getFontSize();
            return fontSize > 0 ? fontSize : 12; // Default to 12pt if not specified
        }
        return 12;
    }
    
    /**
     * Extracts font family from paragraph.
     */
    private String getFontFamily(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            XWPFRun firstRun = runs.get(0);
            String fontFamily = firstRun.getFontFamily();
            return fontFamily != null ? fontFamily : "";
        }
        return "";
    }
    
    /**
     * Checks if paragraph text is bold.
     */
    private boolean isBold(XWPFParagraph paragraph) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            XWPFRun firstRun = runs.get(0);
            return firstRun.isBold();
        }
        return false;
    }
    
    /**
     * Extracts numbering text from heading text.
     */
    private String extractNumbering(String text) {
        Matcher matcher = NUMBERING_PATTERN.matcher(text);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }
    
    /**
     * Validates heading hierarchy (proper nesting).
     */
    private List<ValidationDetail> validateHierarchy(List<HeadingInfo> headings) {
        List<ValidationDetail> issues = new ArrayList<>();
        
        for (int i = 0; i < headings.size(); i++) {
            HeadingInfo current = headings.get(i);
            
            if (i == 0) {
                // First heading should be H1
                if (current.getLevel() != 1) {
                    issues.add(ValidationDetail.builder()
                        .location("Paragraph " + (current.getParagraphIndex() + 1) + " (First heading)")
                        .expected("First heading should be H1")
                        .actual("H" + current.getLevel() + ": " + current.getText())
                        .severity(ValidationSeverity.MAJOR)
                        .recommendation("Start document with H1 heading")
                        .build());
                }
            } else {
                HeadingInfo previous = headings.get(i - 1);
                int levelDiff = current.getLevel() - previous.getLevel();
                
                // Check for skipped levels
                if (levelDiff > 1) {
                    issues.add(ValidationDetail.builder()
                        .location("Paragraph " + (current.getParagraphIndex() + 1))
                        .expected("H" + (previous.getLevel() + 1) + " after H" + previous.getLevel())
                        .actual("H" + current.getLevel() + " after H" + previous.getLevel())
                        .severity(ValidationSeverity.MAJOR)
                        .recommendation("Use sequential heading levels (don't skip levels)")
                        .build());
                }
            }
        }
        
        return issues;
    }
    
    /**
     * Validates heading font sizes.
     */
    private List<ValidationDetail> validateFontSizes(List<HeadingInfo> headings) {
        List<ValidationDetail> issues = new ArrayList<>();
        
        for (HeadingInfo heading : headings) {
            Integer expectedSize = HEADING_FONT_SIZES.get(heading.getLevel());
            if (expectedSize != null && heading.getFontSize() != expectedSize) {
                issues.add(ValidationDetail.builder()
                    .location("Paragraph " + (heading.getParagraphIndex() + 1) + " (H" + heading.getLevel() + ")")
                    .expected("Font size: " + expectedSize + "pt")
                    .actual("Font size: " + heading.getFontSize() + "pt")
                    .severity(ValidationSeverity.MINOR)
                    .recommendation("Set H" + heading.getLevel() + " font size to " + expectedSize + "pt")
                    .build());
            }
        }
        
        return issues;
    }
    
    /**
     * Validates heading numbering pattern.
     */
    private List<ValidationDetail> validateNumbering(List<HeadingInfo> headings) {
        List<ValidationDetail> issues = new ArrayList<>();
        
        // Track expected numbering for each level
        Map<Integer, Integer> levelCounters = new HashMap<>();
        
        for (HeadingInfo heading : headings) {
            String numberingText = heading.getNumberingText();
            
            if (numberingText.isEmpty()) {
                issues.add(ValidationDetail.builder()
                    .location("Paragraph " + (heading.getParagraphIndex() + 1) + " (H" + heading.getLevel() + ")")
                    .expected("Numbered heading (e.g., 1., 1.1, 1.1.1)")
                    .actual("Unnumbered heading: " + heading.getText())
                    .severity(ValidationSeverity.MAJOR)
                    .recommendation("Add numbering to heading")
                    .build());
                continue;
            }
            
            // Update counters
            int currentLevel = heading.getLevel();
            levelCounters.put(currentLevel, levelCounters.getOrDefault(currentLevel, 0) + 1);
            
            // Reset deeper level counters
            for (int level = currentLevel + 1; level <= 6; level++) {
                levelCounters.remove(level);
            }
            
            // Validate numbering format
            String[] parts = numberingText.split("\\.");
            if (parts.length != currentLevel) {
                issues.add(ValidationDetail.builder()
                    .location("Paragraph " + (heading.getParagraphIndex() + 1) + " (H" + heading.getLevel() + ")")
                    .expected("Numbering depth matching heading level")
                    .actual("Numbering: " + numberingText + " for H" + currentLevel)
                    .severity(ValidationSeverity.MINOR)
                    .recommendation("Adjust numbering depth to match heading level")
                    .build());
            }
            
            // Validate sequential numbering
            try {
                int actualNumber = Integer.parseInt(parts[parts.length - 1]);
                int expectedNumber = levelCounters.get(currentLevel);
                
                if (actualNumber != expectedNumber) {
                    issues.add(ValidationDetail.builder()
                        .location("Paragraph " + (heading.getParagraphIndex() + 1) + " (H" + heading.getLevel() + ")")
                        .expected("Sequential numbering: " + expectedNumber)
                        .actual("Found numbering: " + actualNumber)
                        .severity(ValidationSeverity.MINOR)
                        .recommendation("Use sequential numbering for headings")
                        .build());
                }
            } catch (NumberFormatException e) {
                issues.add(ValidationDetail.builder()
                    .location("Paragraph " + (heading.getParagraphIndex() + 1) + " (H" + heading.getLevel() + ")")
                    .expected("Numeric heading numbering")
                    .actual("Invalid numbering: " + numberingText)
                    .severity(ValidationSeverity.MAJOR)
                    .recommendation("Use numeric heading numbering (1, 2, 3, etc.)")
                    .build());
            }
        }
        
        return issues;
    }
    
    /**
     * Validates heading font family.
     */
    private List<ValidationDetail> validateFontFamily(List<HeadingInfo> headings) {
        List<ValidationDetail> issues = new ArrayList<>();
        
        for (HeadingInfo heading : headings) {
            String fontFamily = heading.getFontFamily();
            if (!fontFamily.isEmpty() && !REQUIRED_FONT.equalsIgnoreCase(fontFamily)) {
                issues.add(ValidationDetail.builder()
                    .location("Paragraph " + (heading.getParagraphIndex() + 1) + " (H" + heading.getLevel() + ")")
                    .expected("Font family: " + REQUIRED_FONT)
                    .actual("Font family: " + fontFamily)
                    .severity(ValidationSeverity.MINOR)
                    .recommendation("Use " + REQUIRED_FONT + " font for headings")
                    .build());
            }
        }
        
        return issues;
    }
}