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
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates document line spacing according to FDV technical requirements.
 *
 * Requirements:
 * - Main text must use 1.5 line spacing
 * - Footnotes may use single spacing (1.0)
 * - Tables may use different spacing if needed for layout
 * - Headings should maintain consistent spacing
 *
 * Line spacing in Word documents:
 * - Single spacing = 240 twips = 1.0
 * - 1.5 spacing = 360 twips = 1.5
 * - Double spacing = 480 twips = 2.0
 *
 * @author TechCheck System
 * @version 1.0
 */
public final class LineSpacingValidator extends AbstractDocumentValidator {

    // FDV requirements - line spacing values in twips (1/20 of a point)
    private static final int REQUIRED_LINE_SPACING_TWIPS = 360;  // 1.5 spacing
    private static final double REQUIRED_LINE_SPACING_FACTOR = 1.5;
    private static final int ALLOWED_FOOTNOTE_SPACING_TWIPS = 240; // 1.0 spacing
    private static final double ALLOWED_FOOTNOTE_SPACING_FACTOR = 1.0;

    // Tolerance for line spacing variations (in twips)
    private static final int SPACING_TOLERANCE_TWIPS = 10;

    // Analysis thresholds
    private static final double MAJOR_VIOLATION_THRESHOLD = 0.1;  // 10% of paragraphs
    private static final double MINOR_VIOLATION_THRESHOLD = 0.05; // 5% of paragraphs

    // Magic number constants
    private static final double TWIPS_PER_LINE = 240.0;
    private static final double PERCENTAGE_MULTIPLIER = 100.0;
    private static final double SPACING_TOLERANCE = 0.1;
    private static final double MAJOR_DEVIATION_THRESHOLD = 0.5;
    private static final double MINOR_DEVIATION_THRESHOLD = 0.2;
    private static final int MAX_UNIQUE_SPACINGS_THRESHOLD = 3;
    private static final int SHORT_TEXT_LENGTH_THRESHOLD = 100;

    private static final String VALIDATOR_NAME = "Line Spacing Validator";
    private static final String VALIDATOR_DESCRIPTION =
            "Validates document line spacing according to FDV requirements (1.5 spacing)";

    public LineSpacingValidator() {
        super(VALIDATOR_NAME, ValidationSeverity.CRITICAL, VALIDATOR_DESCRIPTION);
    }

    @Override
    public ValidationResult performValidation(final ThesisDocument document) throws ValidationException {
        final long startTime = System.currentTimeMillis();

        try {
            logger.info("Starting line spacing validation for document: {}",
                       document.getMetadata().getTitle());

            final List<ValidationDetail> details = new ArrayList<>();
            final XWPFDocument xwpfDoc = document.getXwpfDocument();

            // Analyze line spacing throughout the document
            final SpacingAnalysis analysis = analyzeLineSpacing(xwpfDoc);

            // Validate main text line spacing
            validateMainTextSpacing(analysis, details);

            // Check for consistency
            validateSpacingConsistency(analysis, details);

            // Check for common issues
            checkCommonSpacingIssues(analysis, details);

            // Determine overall status
            final ValidationStatus status = determineStatus(details);

            final long duration = System.currentTimeMillis() - startTime;
            logger.info("Line spacing validation completed in {} ms with status: {}", duration, status);

            if (status == ValidationStatus.PASS) {
                return ValidationResult.pass(getValidatorName());
            } else if (status == ValidationStatus.WARNING) {
                return ValidationResult.warning(getValidatorName(), details);
            } else {
                return ValidationResult.fail(getValidatorName(), details);
            }

        } catch (final Exception e) {
            final String errorMsg = "Failed to validate document line spacing: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new ValidationException(getValidatorName(), errorMsg, e, ValidationSeverity.CRITICAL);
        }
    }

    /**
     * Analyzes line spacing used throughout the document.
     */
    private SpacingAnalysis analyzeLineSpacing(final XWPFDocument document) {
        final SpacingAnalysis analysis = new SpacingAnalysis();

        // Analyze main document paragraphs
        for (final XWPFParagraph paragraph : document.getParagraphs()) {
            analyzeParagraphSpacing(paragraph, analysis, false);
        }

        // Analyze table content
        for (final XWPFTable table : document.getTables()) {
            analyzeTableSpacing(table, analysis);
        }

        // Calculate statistics
        analysis.calculateStatistics();

        logger.debug("Line spacing analysis completed: {} paragraphs analyzed", analysis.getTotalParagraphs());
        return analysis;
    }

    /**
     * Analyzes line spacing in a paragraph.
     */
    private void analyzeParagraphSpacing(final XWPFParagraph paragraph, final SpacingAnalysis analysis,
                                       final boolean isTable) {
        if (paragraph == null) {
            return;
        }

        // Get spacing information
        final CTSpacing spacing = paragraph.getCTP().getPPr() != null
                           ? paragraph.getCTP().getPPr().getSpacing() : null;

        final double lineSpacing = extractLineSpacing(spacing);
        final int lineSpacingTwips = extractLineSpacingTwips(spacing);

        // Determine paragraph type based on style
        final ParagraphType type = determineParagraphType(paragraph);

        analysis.addParagraph(lineSpacing, lineSpacingTwips, type, isTable, paragraph.getText());
    }

    /**
     * Analyzes line spacing in table content.
     */
    private void analyzeTableSpacing(final XWPFTable table, final SpacingAnalysis analysis) {
        for (final XWPFTableRow row : table.getRows()) {
            for (final XWPFTableCell cell : row.getTableCells()) {
                for (final XWPFParagraph paragraph : cell.getParagraphs()) {
                    analyzeParagraphSpacing(paragraph, analysis, true);
                }
            }
        }
    }

    /**
     * Extracts line spacing as a factor (1.0, 1.5, 2.0, etc.).
     */
    private double extractLineSpacing(final CTSpacing spacing) {
        if (spacing == null) {
            return REQUIRED_LINE_SPACING_FACTOR; // Default to required spacing
        }

        // Check for line rule and line spacing
        if (spacing.getLineRule() != null) {
            final String rule = spacing.getLineRule().toString();
            if ("auto".equals(rule) && spacing.getLine() != null) {
                // Line spacing in 240ths of a line
                return ((Number) spacing.getLine()).doubleValue() / TWIPS_PER_LINE;
            }
        }

        // Check for multiple line spacing
        if (spacing.getLine() != null) {
            return ((Number) spacing.getLine()).doubleValue() / TWIPS_PER_LINE;
        }

        return REQUIRED_LINE_SPACING_FACTOR; // Default
    }

    /**
     * Extracts line spacing in twips.
     */
    private int extractLineSpacingTwips(final CTSpacing spacing) {
        if (spacing == null) {
            return REQUIRED_LINE_SPACING_TWIPS; // Default
        }

        if (spacing.getLine() != null) {
            return ((Number) spacing.getLine()).intValue();
        }

        return REQUIRED_LINE_SPACING_TWIPS; // Default
    }

    /**
     * Determines the type of paragraph based on style and content.
     */
    private ParagraphType determineParagraphType(final XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        final String text = paragraph.getText();

        if (style != null) {
            style = style.toLowerCase();
            if (style.contains("heading") || style.contains("title")) {
                return ParagraphType.HEADING;
            }
            if (style.contains("footnote")) {
                return ParagraphType.FOOTNOTE;
            }
            if (style.contains("caption")) {
                return ParagraphType.CAPTION;
            }
        }

        // Check content patterns
        if (text != null && !text.trim().isEmpty()) {
            if (text.trim().matches("^\\d+\\.\\d+.*") || text.trim().matches("^[A-Z].*")) {
                // Likely a heading based on numbering or capitalization
                if (text.length() < SHORT_TEXT_LENGTH_THRESHOLD) { // Short lines are likely headings
                    return ParagraphType.HEADING;
                }
            }
        }

        return ParagraphType.BODY_TEXT;
    }

    /**
     * Validates main text line spacing.
     */
    private void validateMainTextSpacing(final SpacingAnalysis analysis, final List<ValidationDetail> details) {
        final Map<Double, Integer> spacingUsage = analysis.getSpacingUsage();
        final int totalParagraphs = analysis.getTotalParagraphs();

        for (final Map.Entry<Double, Integer> entry : spacingUsage.entrySet()) {
            final Double spacing = entry.getKey();
            final int count = entry.getValue();

            if (!isValidLineSpacing(spacing)) {
                final double percentage = (double) count / totalParagraphs;
                final ValidationSeverity severity = determineSpacingSeverity(spacing, percentage);

                final ValidationDetail detail = ValidationDetail.builder()
                        .location("Document text")
                        .expected(String.format("%.1f line spacing", REQUIRED_LINE_SPACING_FACTOR))
                        .actual(String.format("%.1f line spacing", spacing))
                        .severity(severity)
                        .build();

                details.add(detail);

                logger.warn("Line spacing violation: {:.1f} used in {:.1f}% of paragraphs",
                           spacing, percentage * PERCENTAGE_MULTIPLIER);
            }
        }
    }

    /**
     * Validates line spacing consistency throughout the document.
     */
    private void validateSpacingConsistency(final SpacingAnalysis analysis, final List<ValidationDetail> details) {
        final Map<Double, Integer> spacingUsage = analysis.getSpacingUsage();

        // Check if 1.5 spacing is the predominant spacing
        final Double mostUsedSpacing = findMostUsedSpacing(spacingUsage);

        if (!isCloseToRequired(mostUsedSpacing, REQUIRED_LINE_SPACING_FACTOR)) {
            final ValidationDetail detail = ValidationDetail.builder()
                    .location("Document formatting")
                    .expected(String.format("%.1f line spacing as primary", REQUIRED_LINE_SPACING_FACTOR))
                    .actual(String.format("%.1f line spacing as primary", mostUsedSpacing))
                    .severity(ValidationSeverity.MAJOR)
                    .build();

            details.add(detail);
        }

        // Check for excessive spacing variety
        final int uniqueSpacings = spacingUsage.size();
        if (uniqueSpacings > MAX_UNIQUE_SPACINGS_THRESHOLD) {
            final List<String> spacingList = spacingUsage.keySet().stream()
                    .map(s -> String.format("%.1f", s))
                    .collect(java.util.stream.Collectors.toList());

            final ValidationDetail detail = ValidationDetail.builder()
                    .location("Document formatting")
                    .expected("Consistent 1.5 line spacing")
                    .actual(String.format("%d different spacings: %s", uniqueSpacings, String.join(", ", spacingList)))
                    .severity(ValidationSeverity.MINOR)
                    .build();

            details.add(detail);
        }
    }

    /**
     * Checks for common line spacing issues.
     */
    private void checkCommonSpacingIssues(final SpacingAnalysis analysis, final List<ValidationDetail> details) {
        // Check for single spacing (common mistake)
        final int singleSpacingCount = analysis.getSpacingCount(ALLOWED_FOOTNOTE_SPACING_FACTOR);
        if (singleSpacingCount > 0) {
            final double percentage = (double) singleSpacingCount / analysis.getTotalParagraphs();
            if (percentage > MAJOR_VIOLATION_THRESHOLD) { // More than 10%
                final ValidationDetail detail = ValidationDetail.builder()
                        .location("Document formatting")
                        .expected("1.5 line spacing")
                        .actual(String.format("Single spacing in %.1f%% of paragraphs", percentage * PERCENTAGE_MULTIPLIER))
                        .severity(ValidationSeverity.MAJOR)
                        .build();

                details.add(detail);
            }
        }

        // Check for double spacing (less common but worth noting)
        final int doubleSpacingCount = analysis.getSpacingCount(2.0);
        if (doubleSpacingCount > 0) {
            final ValidationDetail detail = ValidationDetail.builder()
                    .location("Document formatting")
                    .expected("1.5 line spacing")
                    .actual(String.format("Double spacing found in %d paragraphs", doubleSpacingCount))
                    .severity(ValidationSeverity.MINOR)
                    .build();

            details.add(detail);
        }
    }

    /**
     * Checks if a line spacing value is valid according to FDV requirements.
     */
    private boolean isValidLineSpacing(final double spacing) {
        return isCloseToRequired(spacing, REQUIRED_LINE_SPACING_FACTOR)
               || isCloseToRequired(spacing, ALLOWED_FOOTNOTE_SPACING_FACTOR);
    }

    /**
     * Checks if a spacing value is close to a required value within tolerance.
     */
    private boolean isCloseToRequired(final double actual, final double required) {
        return Math.abs(actual - required) <= SPACING_TOLERANCE; // 0.1 spacing tolerance
    }

    /**
     * Determines severity for line spacing violations.
     */
    private ValidationSeverity determineSpacingSeverity(final double spacing, final double percentage) {
        final double deviation = Math.abs(spacing - REQUIRED_LINE_SPACING_FACTOR);

        if (deviation >= MAJOR_DEVIATION_THRESHOLD || percentage >= MAJOR_VIOLATION_THRESHOLD) {
            return ValidationSeverity.MAJOR;
        } else if (deviation >= MINOR_DEVIATION_THRESHOLD || percentage >= MINOR_VIOLATION_THRESHOLD) {
            return ValidationSeverity.MINOR;
        } else {
            return ValidationSeverity.INFO;
        }
    }

    /**
     * Generates recommendation for line spacing issues.
     */
    private String generateSpacingRecommendation(final double spacing) {
        if (spacing < REQUIRED_LINE_SPACING_FACTOR) {
            return String.format("Increase line spacing from %.1f to %.1f", spacing, REQUIRED_LINE_SPACING_FACTOR);
        } else if (spacing > REQUIRED_LINE_SPACING_FACTOR) {
            return String.format("Decrease line spacing from %.1f to %.1f", spacing, REQUIRED_LINE_SPACING_FACTOR);
        } else {
            return String.format("Use %.1f line spacing consistently", REQUIRED_LINE_SPACING_FACTOR);
        }
    }

    /**
     * Finds the most frequently used line spacing.
     */
    private Double findMostUsedSpacing(final Map<Double, Integer> spacingUsage) {
        return spacingUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ALLOWED_FOOTNOTE_SPACING_FACTOR);
    }

    /**
     * Determines overall validation status based on details.
     */
    private ValidationStatus determineStatus(final List<ValidationDetail> details) {
        if (details.isEmpty()) {
            return ValidationStatus.PASS;
        }

        final boolean hasCritical = details.stream().anyMatch(d -> d.getSeverity() == ValidationSeverity.CRITICAL);
        final boolean hasMajor = details.stream().anyMatch(d -> d.getSeverity() == ValidationSeverity.MAJOR);

        if (hasCritical || hasMajor) {
            return ValidationStatus.FAIL;
        } else {
            return ValidationStatus.WARNING;
        }
    }

    @Override
    public boolean isEnabled() {
        return true; // Line spacing validation is always enabled for FDV requirements
    }

    @Override
    public String toString() {
        return String.format("LineSpacingValidator{name='%s', enabled=%s, requiredSpacing=%.1f}",
                           getValidatorName(), isEnabled(), REQUIRED_LINE_SPACING_FACTOR);
    }

    /**
     * Enumeration of paragraph types for analysis.
     */
    private enum ParagraphType {
        BODY_TEXT, HEADING, FOOTNOTE, CAPTION, TABLE_CONTENT
    }

    /**
     * Internal class to track line spacing analysis results.
     */
    private static class SpacingAnalysis {
        private final Map<Double, Integer> spacingUsage = new HashMap<>();
        private final Map<ParagraphType, Integer> typeDistribution = new HashMap<>();
        private int totalParagraphs = 0;
        private int tableParagraphs = 0;

        public void addParagraph(final double spacing, final int spacingTwips, final ParagraphType type,
                                final boolean isTable, final String text) {
            spacingUsage.merge(spacing, 1, Integer::sum);
            typeDistribution.merge(type, 1, Integer::sum);
            totalParagraphs++;
            if (isTable) {
                tableParagraphs++;
            }
        }

        public void calculateStatistics() {
            // Additional statistics can be calculated here if needed
        }

        public Map<Double, Integer> getSpacingUsage() {
            return spacingUsage;
        }

        public Map<ParagraphType, Integer> getTypeDistribution() {
            return typeDistribution;
        }

        public int getTotalParagraphs() {
            return totalParagraphs;
        }

        public int getTableParagraphs() {
            return tableParagraphs;
        }

        public int getSpacingCount(final double spacing) {
            return spacingUsage.getOrDefault(spacing, 0);
        }
    }
}
