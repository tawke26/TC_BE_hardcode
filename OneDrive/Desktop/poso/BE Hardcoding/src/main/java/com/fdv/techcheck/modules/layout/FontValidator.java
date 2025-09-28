package com.fdv.techcheck.modules.layout;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.AbstractDocumentValidator;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import com.fdv.techcheck.core.validation.ValidationStatus;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates document fonts according to FDV technical requirements.
 * 
 * Requirements:
 * - Main text must use Times New Roman font family
 * - Main text must be 12 point size
 * - Headings may use different sizes but must be Times New Roman
 * - Footnotes may be 10 point but must be Times New Roman
 * 
 * @author TechCheck System
 * @version 1.0
 */
public final class FontValidator extends AbstractDocumentValidator {
    
    // FDV requirements
    private static final String REQUIRED_FONT_FAMILY = "Times New Roman";
    private static final int REQUIRED_MAIN_TEXT_SIZE = 12; // 12pt for main text
    private static final int ALLOWED_FOOTNOTE_SIZE = 10;   // 10pt for footnotes
    
    // Font analysis thresholds
    private static final double MAJOR_VIOLATION_THRESHOLD = 0.1;  // 10% of text with wrong font
    private static final double MINOR_VIOLATION_THRESHOLD = 0.05; // 5% of text with wrong font
    
    private static final String VALIDATOR_NAME = "Font Validator";
    private static final String VALIDATOR_DESCRIPTION = "Validates document fonts according to FDV requirements (Times New Roman 12pt)";
    
    public FontValidator() {
        super(VALIDATOR_NAME, ValidationSeverity.CRITICAL, VALIDATOR_DESCRIPTION);
    }
    
    @Override
    public ValidationResult performValidation(final ThesisDocument document) throws ValidationException {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Starting font validation for document: {}", 
                       document.getMetadata().getTitle());
            
            List<ValidationDetail> details = new ArrayList<>();
            XWPFDocument xwpfDoc = document.getXwpfDocument();
            
            // Analyze fonts throughout the document
            FontAnalysis analysis = analyzeFonts(xwpfDoc);
            
            // Validate main text font family
            validateFontFamily(analysis, details);
            
            // Validate main text font size
            validateFontSize(analysis, details);
            
            // Check for consistency
            validateFontConsistency(analysis, details);
            
            // Determine overall status
            ValidationStatus status = determineStatus(details);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Font validation completed in {} ms with status: {}", duration, status);
            
            if (status == ValidationStatus.PASS) {
                return ValidationResult.pass(getValidatorName());
            } else if (status == ValidationStatus.WARNING) {
                return ValidationResult.warning(getValidatorName(), details);
            } else {
                return ValidationResult.fail(getValidatorName(), details);
            }
                    
        } catch (Exception e) {
            String errorMsg = "Failed to validate document fonts: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new ValidationException(getValidatorName(), errorMsg, e, ValidationSeverity.CRITICAL);
        }
    }
    
    /**
     * Analyzes fonts used throughout the document.
     */
    private FontAnalysis analyzeFonts(final XWPFDocument document) {
        FontAnalysis analysis = new FontAnalysis();
        
        // Analyze paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            analyzeParagraphFonts(paragraph, analysis);
        }
        
        // Analyze tables
        for (XWPFTable table : document.getTables()) {
            analyzeTableFonts(table, analysis);
        }
        
        // Calculate statistics
        analysis.calculateStatistics();
        
        logger.debug("Font analysis completed: {} text runs analyzed", analysis.getTotalRuns());
        return analysis;
    }
    
    /**
     * Analyzes fonts in a paragraph.
     */
    private void analyzeParagraphFonts(final XWPFParagraph paragraph, final FontAnalysis analysis) {
        if (paragraph.getRuns() == null) return;
        
        for (XWPFRun run : paragraph.getRuns()) {
            analyzeRunFont(run, analysis, false);
        }
    }
    
    /**
     * Analyzes fonts in a table.
     */
    private void analyzeTableFonts(final XWPFTable table, final FontAnalysis analysis) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    if (paragraph.getRuns() != null) {
                        for (XWPFRun run : paragraph.getRuns()) {
                            analyzeRunFont(run, analysis, true);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Analyzes font properties of a text run.
     */
    private void analyzeRunFont(final XWPFRun run, final FontAnalysis analysis, final boolean isTable) {
        if (run == null || run.text() == null || run.text().trim().isEmpty()) {
            return;
        }
        
        String fontFamily = run.getFontFamily();
        Integer fontSize = run.getFontSize();
        
        // Handle default values
        if (fontFamily == null) {
            fontFamily = "Times New Roman"; // Default font in Word documents
        }
        if (fontSize == null || fontSize == -1) {
            fontSize = 12; // Default font size
        }
        
        analysis.addRun(fontFamily, fontSize, run.text().length(), isTable);
    }
    
    /**
     * Validates font family usage.
     */
    private void validateFontFamily(final FontAnalysis analysis, final List<ValidationDetail> details) {
        Map<String, Integer> fontFamilyUsage = analysis.getFontFamilyUsage();
        int totalCharacters = analysis.getTotalCharacters();
        
        for (Map.Entry<String, Integer> entry : fontFamilyUsage.entrySet()) {
            String fontFamily = entry.getKey();
            int characterCount = entry.getValue();
            
            if (!REQUIRED_FONT_FAMILY.equalsIgnoreCase(fontFamily)) {
                double percentage = (double) characterCount / totalCharacters;
                ValidationSeverity severity = determineFontFamilySeverity(percentage);
                
                ValidationDetail detail = ValidationDetail.builder()
                        .location("Document text")
                        .expected(REQUIRED_FONT_FAMILY)
                        .actual(fontFamily)
                        .severity(severity)
                        .build();
                        
                details.add(detail);
                
                logger.warn("Font family violation: {} used in {:.1f}% of text", 
                           fontFamily, percentage * 100);
            }
        }
    }
    
    /**
     * Validates font size usage.
     */
    private void validateFontSize(final FontAnalysis analysis, final List<ValidationDetail> details) {
        Map<Integer, Integer> fontSizeUsage = analysis.getFontSizeUsage();
        int totalCharacters = analysis.getTotalCharacters();
        
        for (Map.Entry<Integer, Integer> entry : fontSizeUsage.entrySet()) {
            Integer fontSize = entry.getKey();
            int characterCount = entry.getValue();
            
            if (!isValidFontSize(fontSize)) {
                double percentage = (double) characterCount / totalCharacters;
                ValidationSeverity severity = determineFontSizeSeverity(fontSize, percentage);
                
                ValidationDetail detail = ValidationDetail.builder()
                        .location("Document text")
                        .expected(String.format("%dpt (main text) or %dpt (footnotes)",
                                                REQUIRED_MAIN_TEXT_SIZE, ALLOWED_FOOTNOTE_SIZE))
                        .actual(fontSize + "pt")
                        .severity(severity)
                        .build();
                        
                details.add(detail);
                
                logger.warn("Font size violation: {}pt used in {:.1f}% of text", 
                           fontSize, percentage * 100);
            }
        }
    }
    
    /**
     * Validates font consistency throughout the document.
     */
    private void validateFontConsistency(final FontAnalysis analysis, final List<ValidationDetail> details) {
        // Check if Times New Roman is the predominant font
        Map<String, Integer> fontFamilyUsage = analysis.getFontFamilyUsage();
        String mostUsedFont = findMostUsedFont(fontFamilyUsage);
        
        if (!REQUIRED_FONT_FAMILY.equalsIgnoreCase(mostUsedFont)) {
            ValidationDetail detail = ValidationDetail.builder()
                    .location("Document formatting")
                    .expected(REQUIRED_FONT_FAMILY + " as primary font")
                    .actual(mostUsedFont + " as primary font")
                    .severity(ValidationSeverity.MAJOR)
                    .build();
                    
            details.add(detail);
        }
        
        // Check for excessive font variety
        int uniqueFonts = fontFamilyUsage.size();
        if (uniqueFonts > 3) {
            ValidationDetail detail = ValidationDetail.builder()
                    .location("Document formatting")
                    .expected("Consistent use of Times New Roman")
                    .actual(String.format("%d different fonts: %s", uniqueFonts, String.join(", ", fontFamilyUsage.keySet())))
                    .severity(ValidationSeverity.MINOR)
                    .build();
                    
            details.add(detail);
        }
    }
    
    /**
     * Checks if a font size is valid according to FDV requirements.
     */
    private boolean isValidFontSize(final int fontSize) {
        return fontSize == REQUIRED_MAIN_TEXT_SIZE || fontSize == ALLOWED_FOOTNOTE_SIZE;
    }
    
    /**
     * Determines severity for font family violations.
     */
    private ValidationSeverity determineFontFamilySeverity(final double percentage) {
        if (percentage >= MAJOR_VIOLATION_THRESHOLD) {
            return ValidationSeverity.MAJOR;
        } else if (percentage >= MINOR_VIOLATION_THRESHOLD) {
            return ValidationSeverity.MINOR;
        } else {
            return ValidationSeverity.INFO;
        }
    }
    
    /**
     * Determines severity for font size violations.
     */
    private ValidationSeverity determineFontSizeSeverity(final int fontSize, final double percentage) {
        // Larger deviations from 12pt are more severe
        int deviation = Math.abs(fontSize - REQUIRED_MAIN_TEXT_SIZE);
        
        if (deviation >= 4 || percentage >= MAJOR_VIOLATION_THRESHOLD) {
            return ValidationSeverity.MAJOR;
        } else if (deviation >= 2 || percentage >= MINOR_VIOLATION_THRESHOLD) {
            return ValidationSeverity.MINOR;
        } else {
            return ValidationSeverity.INFO;
        }
    }
    
    /**
     * Generates recommendation for font size issues.
     */
    private String generateFontSizeRecommendation(final int fontSize) {
        if (fontSize < ALLOWED_FOOTNOTE_SIZE) {
            return String.format("Increase font size from %dpt to %dpt for main text", fontSize, REQUIRED_MAIN_TEXT_SIZE);
        } else if (fontSize > REQUIRED_MAIN_TEXT_SIZE) {
            return String.format("Decrease font size from %dpt to %dpt for main text", fontSize, REQUIRED_MAIN_TEXT_SIZE);
        } else {
            return String.format("Use %dpt for main text and %dpt for footnotes only", REQUIRED_MAIN_TEXT_SIZE, ALLOWED_FOOTNOTE_SIZE);
        }
    }
    
    /**
     * Finds the most frequently used font family.
     */
    private String findMostUsedFont(final Map<String, Integer> fontFamilyUsage) {
        return fontFamilyUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
    
    /**
     * Determines overall validation status based on details.
     */
    private ValidationStatus determineStatus(final List<ValidationDetail> details) {
        if (details.isEmpty()) {
            return ValidationStatus.PASS;
        }
        
        boolean hasCritical = details.stream().anyMatch(d -> d.getSeverity() == ValidationSeverity.CRITICAL);
        boolean hasMajor = details.stream().anyMatch(d -> d.getSeverity() == ValidationSeverity.MAJOR);
        
        if (hasCritical || hasMajor) {
            return ValidationStatus.FAIL;
        } else {
            return ValidationStatus.WARNING;
        }
    }
    
    @Override
    public boolean isEnabled() {
        return true; // Font validation is always enabled for FDV requirements
    }
    
    @Override
    public String toString() {
        return String.format("FontValidator{name='%s', enabled=%s, requiredFont='%s %dpt'}", 
                           getValidatorName(), isEnabled(), REQUIRED_FONT_FAMILY, REQUIRED_MAIN_TEXT_SIZE);
    }
    
    /**
     * Internal class to track font analysis results.
     */
    private static class FontAnalysis {
        private final Map<String, Integer> fontFamilyUsage = new HashMap<>();
        private final Map<Integer, Integer> fontSizeUsage = new HashMap<>();
        private int totalRuns = 0;
        private int totalCharacters = 0;
        private int tableCharacters = 0;
        
        public void addRun(final String fontFamily, final int fontSize, final int characterCount, final boolean isTable) {
            fontFamilyUsage.merge(fontFamily, characterCount, Integer::sum);
            fontSizeUsage.merge(fontSize, characterCount, Integer::sum);
            totalRuns++;
            totalCharacters += characterCount;
            if (isTable) {
                tableCharacters += characterCount;
            }
        }
        
        public void calculateStatistics() {
            // Additional statistics can be calculated here if needed
        }
        
        public Map<String, Integer> getFontFamilyUsage() { return fontFamilyUsage; }
        public Map<Integer, Integer> getFontSizeUsage() { return fontSizeUsage; }
        public int getTotalRuns() { return totalRuns; }
        public int getTotalCharacters() { return totalCharacters; }
        public int getTableCharacters() { return tableCharacters; }
    }
}