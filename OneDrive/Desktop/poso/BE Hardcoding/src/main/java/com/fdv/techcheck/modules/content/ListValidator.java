package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.AbstractDocumentValidator;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Validator for list formatting requirements in thesis documents.
 * 
 * Validates:
 * - Maximum 3 levels of nesting for lists
 * - Consistent numbering/bullet styles within each list level
 * - Proper indentation for nested lists
 * - No mixing of numbered and bulleted lists at the same level
 * 
 * @author TechCheck System
 * @version 1.0
 */
public class ListValidator extends AbstractDocumentValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ListValidator.class);
    
    // Validation thresholds
    private static final int MAX_NESTING_LEVELS = 3;
    
    /**
     * Constructor for ListValidator.
     */
    public ListValidator() {
        super("List Validator",
              ValidationSeverity.MINOR,
              "Validates list structure, nesting levels, and formatting consistency according to FDV Ljubljana standards");
    }
    
    @Override
    public String getValidatorName() {
        return "List Validator";
    }
    
    @Override
    protected ValidationResult performValidation(ThesisDocument document) {
        String fileName = document.getMetadata() != null ? document.getMetadata().getFileName() : "unknown";
        logger.debug("Starting list validation for document: {}", fileName);
        
        List<ValidationDetail> details = new ArrayList<>();
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        
        logger.debug("Found {} paragraphs to validate for lists", paragraphs.size());
        
        int listCount = 0;
        int nestingLevel = 0;
        ListType currentListType = null;
        List<Integer> levelStyles = new ArrayList<>();
        
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            
            // Check if this paragraph is part of a list
            if (isListParagraph(paragraph)) {
                listCount++;
                int currentLevel = getNestingLevel(paragraph);
                
                // Validate nesting level
                if (currentLevel > MAX_NESTING_LEVELS) {
                    details.add(createNestingLevelDetail(paragraph, i + 1, currentLevel));
                }
                
                // Validate consistent styling at each level
                ListType paragraphType = getListType(paragraph);
                if (paragraphType != null) {
                    if (currentListType == null || currentLevel == 0) {
                        // Starting new list
                        currentListType = paragraphType;
                        levelStyles.clear();
                        for (int j = 0; j <= MAX_NESTING_LEVELS; j++) {
                            levelStyles.add(null);
                        }
                    }
                    
                    // Check consistency at this level
                    if (levelStyles.get(currentLevel) == null) {
                        levelStyles.set(currentLevel, getStyleId(paragraph));
                    } else if (!levelStyles.get(currentLevel).equals(getStyleId(paragraph))) {
                        details.add(createStyleConsistencyDetail(paragraph, i + 1, currentLevel, levelStyles.get(currentLevel)));
                    }
                    
                    // Check type consistency at this level
                    if (!paragraphType.equals(currentListType)) {
                        details.add(createListTypeDetail(paragraph, i + 1, currentListType, paragraphType));
                    }
                    
                    // Validate indentation
                    double indentation = getIndentation(paragraph);
                    double expectedIndentation = calculateExpectedIndentation(currentLevel);
                    if (Math.abs(indentation - expectedIndentation) > 0.5) { // 0.5 cm tolerance
                        details.add(createIndentationDetail(paragraph, i + 1, currentLevel, expectedIndentation, indentation));
                    }
                    
                    nestingLevel = currentLevel;
                }
            } else if (nestingLevel > 0) {
                // End of list - reset
                nestingLevel = 0;
                currentListType = null;
                levelStyles.clear();
            }
        }
        
        // Summary validation
        if (listCount == 0) {
            logger.debug("No lists found in document");
        } else {
            logger.debug("Validated {} list items across the document", listCount);
        }
        
        logger.debug("List validation completed with {} issues", details.size());
        
        return details.isEmpty()
            ? ValidationResult.pass(getValidatorName())
            : ValidationResult.fail(getValidatorName(), details);
    }
    
    /**
     * Checks if a paragraph is part of a list.
     */
    private boolean isListParagraph(XWPFParagraph paragraph) {
        // Check for numbering properties
        if (paragraph.getCTP().getPPr() != null && 
            paragraph.getCTP().getPPr().getNumPr() != null) {
            return true;
        }
        
        // Check for bullet characters in runs
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null && (text.startsWith("•") || text.startsWith("◦") || text.startsWith("-") ||
                                text.matches("^\\d+\\."))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the nesting level of a list paragraph.
     */
    private int getNestingLevel(XWPFParagraph paragraph) {
        if (paragraph.getCTP().getPPr() != null && 
            paragraph.getCTP().getPPr().getNumPr() != null &&
            paragraph.getCTP().getPPr().getNumPr().getIlvl() != null) {
            
            CTDecimalNumber ilvl = paragraph.getCTP().getPPr().getNumPr().getIlvl();
            if (ilvl != null && ilvl.getVal() != null) {
                return ilvl.getVal().intValue();
            }
        }
        
        // Fallback: check indentation level
        double indentation = getIndentation(paragraph);
        if (indentation < 1.0) return 0;
        else if (indentation < 2.0) return 1;
        else if (indentation < 3.0) return 2;
        else return 3;
    }
    
    /**
     * Determines the type of list (numbered or bulleted).
     */
    private ListType getListType(XWPFParagraph paragraph) {
        if (paragraph.getCTP().getPPr() != null && 
            paragraph.getCTP().getPPr().getNumPr() != null) {
            
            // Check numbering instance
            // This is a simplified check - in practice would need to parse numbering.xml
            return ListType.NUMBERED;
        }
        
        // Check for bullet characters
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null && (text.startsWith("•") || text.startsWith("◦") || text.startsWith("-"))) {
                return ListType.BULLETED;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the style ID for the list level.
     */
    private Integer getStyleId(XWPFParagraph paragraph) {
        if (paragraph.getCTP().getPPr() != null && 
            paragraph.getCTP().getPPr().getNumPr() != null &&
            paragraph.getCTP().getPPr().getNumPr().getNumId() != null) {
            
            return paragraph.getCTP().getPPr().getNumPr().getNumId().getVal().intValue();
        }
        
        // Fallback based on content
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                if (text.startsWith("•")) return 1;
                if (text.startsWith("◦")) return 2;
                if (text.matches("^\\d+\\.")) return 10;
                if (text.matches("^[a-z]\\)")) return 20;
            }
        }
        
        return 0;
    }
    
    /**
     * Gets the left indentation of the paragraph in cm.
     */
    private double getIndentation(XWPFParagraph paragraph) {
        if (paragraph.getCTP().getPPr() != null &&
            paragraph.getCTP().getPPr().getInd() != null) {
            
            var ind = paragraph.getCTP().getPPr().getInd();
            if (ind.getLeft() != null) {
                // Convert twips to cm (1 cm = 567 twips)
                return ((java.math.BigInteger) ind.getLeft()).doubleValue() / 567.0;
            }
        }
        
        return 0.0;
    }
    
    /**
     * Calculates expected indentation for a given nesting level (in cm).
     */
    private double calculateExpectedIndentation(int level) {
        return level * 1.25; // 1.25 cm per level
    }
    
    /**
     * Creates validation detail for excessive nesting levels.
     */
    private ValidationDetail createNestingLevelDetail(XWPFParagraph paragraph, int paragraphNumber, int currentLevel) {
        return ValidationDetail.builder()
            .location(String.format(Locale.US, "Paragraph %d (List level %d)", paragraphNumber, currentLevel))
            .expected(String.format(Locale.US, "Maximum %d nesting levels", MAX_NESTING_LEVELS))
            .actual(String.format(Locale.US, "Nesting level %d", currentLevel))
            .severity(ValidationSeverity.MAJOR)
            .recommendation(String.format(Locale.US, "Reduce nesting to maximum %d levels", MAX_NESTING_LEVELS))
            .build();
    }
    
    /**
     * Creates validation detail for inconsistent list styles.
     */
    private ValidationDetail createStyleConsistencyDetail(XWPFParagraph paragraph, int paragraphNumber, int level, Integer expectedStyle) {
        return ValidationDetail.builder()
            .location(String.format(Locale.US, "Paragraph %d (Level %d)", paragraphNumber, level))
            .expected(String.format(Locale.US, "Consistent style ID %d for level %d", expectedStyle, level))
            .actual("Inconsistent list style at this level")
            .severity(ValidationSeverity.MINOR)
            .recommendation("Use consistent numbering/bullet style within each list level")
            .build();
    }
    
    /**
     * Creates validation detail for mixed list types.
     */
    private ValidationDetail createListTypeDetail(XWPFParagraph paragraph, int paragraphNumber, ListType expectedType, ListType actualType) {
        return ValidationDetail.builder()
            .location(String.format(Locale.US, "Paragraph %d", paragraphNumber))
            .expected(String.format(Locale.US, "%s list type", expectedType.name()))
            .actual(String.format(Locale.US, "%s list type", actualType.name()))
            .severity(ValidationSeverity.MINOR)
            .recommendation("Maintain consistent list type (numbered or bulleted) within the same list")
            .build();
    }
    
    /**
     * Creates validation detail for incorrect indentation.
     */
    private ValidationDetail createIndentationDetail(XWPFParagraph paragraph, int paragraphNumber, int level, double expected, double actual) {
        return ValidationDetail.builder()
            .location(String.format(Locale.US, "Paragraph %d (Level %d)", paragraphNumber, level))
            .expected(String.format(Locale.US, "%.2f cm indentation", expected))
            .actual(String.format(Locale.US, "%.2f cm indentation", actual))
            .severity(ValidationSeverity.MINOR)
            .recommendation(String.format(Locale.US, "Set indentation to %.2f cm for level %d", expected, level))
            .build();
    }
    
    /**
     * Enum for list types.
     */
    private enum ListType {
        NUMBERED,
        BULLETED
    }
}