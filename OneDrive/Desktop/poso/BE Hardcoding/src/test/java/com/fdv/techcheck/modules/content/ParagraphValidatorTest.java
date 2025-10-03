package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationStatus;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParagraphValidator.
 * Tests paragraph length, line spacing, and alignment validation.
 */
class ParagraphValidatorTest {

    private ParagraphValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ParagraphValidator();
    }

    @Test
    void testValidParagraphs() throws IOException, ValidationException {
        // Create document with valid paragraphs
        XWPFDocument doc = new XWPFDocument();
        
        // Add valid paragraph (justified, 1.5 spacing, proper length)
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(para, 1.5);
        XWPFRun run = para.createRun();
        run.setText("This is a valid paragraph with sufficient content to meet the minimum length requirements. " +
                   "It has proper formatting including justified alignment and 1.5 line spacing as required by FDV standards.");

        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.PASS, result.getStatus(), "Valid paragraphs should pass validation");
        assertTrue(result.getDetails().isEmpty(), "No validation issues should be found");
        doc.close();
    }

    @Test
    void testParagraphTooShort() throws IOException, ValidationException {
        XWPFDocument doc = new XWPFDocument();
        
        // Add short paragraph
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(para, 1.5);
        XWPFRun run = para.createRun();
        run.setText("Short text."); // Only 11 characters, below minimum 50

        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Short paragraphs should fail validation");
        assertTrue(result.getDetails().size() >= 1, "Should have at least one validation issue");
        assertTrue(result.getDetails().stream()
            .anyMatch(detail -> detail.getExpected().contains("50") || detail.getActual().contains("too short")));
        doc.close();
    }

    @Test
    void testParagraphTooLong() throws IOException, ValidationException {
        XWPFDocument doc = new XWPFDocument();
        
        // Add very long paragraph (over 2000 characters)
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(para, 1.5);
        XWPFRun run = para.createRun();
        
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longText.append("This is a very long paragraph that exceeds the maximum recommended length. ");
        }
        run.setText(longText.toString()); // Over 2000 characters

        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Long paragraphs should fail validation");
        assertTrue(result.getDetails().size() >= 1, "Should have at least one validation issue");
        assertTrue(result.getDetails().stream()
            .anyMatch(detail -> detail.getExpected().contains("2000") || detail.getActual().contains("too long")));
        doc.close();
    }

    @Test
    void testIncorrectLineSpacing() throws IOException, ValidationException {
        XWPFDocument doc = new XWPFDocument();
        
        // Add paragraph with single spacing instead of 1.5
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(para, 1.0); // Single spacing instead of 1.5
        XWPFRun run = para.createRun();
        run.setText("This paragraph has incorrect line spacing. It should use 1.5 line spacing but currently uses single spacing.");

        // Add adequate filler content for isEmpty() check while preserving test paragraph issues
        addFillerParagraphsWithAdequateContent(doc, 2);
        
        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("test-document.docx"))
                .xwpfDocument(doc)
                .build();
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Incorrect line spacing should fail validation");
        assertTrue(result.getDetails().size() >= 1, "Should have at least one validation issue");
        
        // More flexible check for line spacing validation details
        boolean hasLineSpacingIssue = result.getDetails().stream()
            .anyMatch(detail -> (detail.getExpected().contains("1.5") || detail.getExpected().contains("spacing"))
                             && (detail.getActual().contains("1.0") || detail.getActual().contains("spacing")));
        assertTrue(hasLineSpacingIssue, "Should find line spacing validation issue");
        doc.close();
    }

    @Test
    void testIncorrectAlignment() throws IOException, ValidationException {
        XWPFDocument doc = new XWPFDocument();
        
        // Add paragraph with left alignment instead of justified
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        setLineSpacing(para, 1.5);
        XWPFRun run = para.createRun();
        run.setText("This paragraph has incorrect alignment. It should be justified but is currently left-aligned according to FDV standards.");

        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Incorrect alignment should fail validation");
        assertTrue(result.getDetails().size() >= 1, "Should have at least one validation issue");
        assertTrue(result.getDetails().stream()
            .anyMatch(detail -> detail.getExpected().contains("JUSTIFIED") && detail.getActual().contains("LEFT")));
        doc.close();
    }

    @Test
    void testMultipleIssues() throws IOException, ValidationException {
        XWPFDocument doc = new XWPFDocument();
        
        // Add paragraph with multiple issues
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.CENTER); // Wrong alignment
        setLineSpacing(para, 2.0); // Wrong line spacing
        XWPFRun run = para.createRun();
        run.setText("Short."); // Too short

        // Add adequate filler content for isEmpty() check while preserving test paragraph issues
        addFillerParagraphsWithAdequateContent(doc, 2);
        
        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("test-document.docx"))
                .xwpfDocument(doc)
                .build();
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Multiple issues should fail validation");
        assertTrue(result.getDetails().size() >= 2, "Should have multiple validation issues");
        
        // Verify issues are present (flexible check since exact messages may vary)
        assertTrue(result.getDetails().stream()
            .anyMatch(detail -> detail.getActual().contains("too short") || detail.getExpected().contains("50")),
            "Should find paragraph length issue");
        assertTrue(result.getDetails().stream()
            .anyMatch(detail -> detail.getExpected().contains("1.5") || detail.getActual().contains("2.0")),
            "Should find line spacing issue");
        doc.close();
    }

    @Test
    void testEmptyDocument() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        // Don't add any paragraphs - create ThesisDocument directly without filler
        
        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("test-document.docx"))
                .xwpfDocument(doc)
                .build();
        
        // Empty document should throw ValidationException in preValidationCheck
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            validator.validate(thesisDoc);
        });
        
        assertTrue(exception.getMessage().contains("empty") || exception.getMessage().contains("insufficient"),
                "Exception should mention empty or insufficient content");
        doc.close();
    }

    @Test
    void testValidatorName() {
        assertEquals("Paragraph Validator", validator.getValidatorName());
    }

    /**
     * Helper method to set line spacing on a paragraph.
     */
    private void setLineSpacing(XWPFParagraph paragraph, double spacing) {
        CTSpacing ctSpacing = paragraph.getCTP().getPPr().isSetSpacing() 
            ? paragraph.getCTP().getPPr().getSpacing() 
            : paragraph.getCTP().getPPr().addNewSpacing();
        
        // Set line spacing in 240ths (240 = single spacing, 360 = 1.5 spacing)
        ctSpacing.setLine(java.math.BigInteger.valueOf((long)(spacing * 240)));
        ctSpacing.setLineRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule.AUTO);
    }

    /**
     * Helper method to create ThesisDocument from XWPFDocument.
     * Automatically adds filler paragraphs to ensure document passes isEmpty() check.
     */
    private ThesisDocument createThesisDocument(XWPFDocument doc) throws IOException {
        // Add filler paragraphs to ensure we have at least 3 paragraphs and 50+ words
        addFillerParagraphs(doc, 3);
        
        return ThesisDocument.builder()
                .filePath(Paths.get("test-document.docx"))
                .xwpfDocument(doc)
                .build();
    }
    
    /**
     * Helper method to add filler paragraphs to ensure document passes isEmpty() check.
     * Adds paragraphs with sufficient word count and proper formatting.
     */
    private void addFillerParagraphs(XWPFDocument doc, int count) {
        for (int i = 0; i < count; i++) {
            XWPFParagraph filler = doc.createParagraph();
            filler.setSpacingBetween(1.5);
            filler.setAlignment(ParagraphAlignment.BOTH);
            
            XWPFRun fillerRun = filler.createRun();
            fillerRun.setText("This is a filler paragraph added to ensure the document has sufficient content " +
                           "to pass the isEmpty() validation check. It contains proper formatting and adequate length " +
                           "to meet the minimum requirements for document validation testing purposes. " +
                           "Each filler paragraph provides substantial word count to reach the fifty word minimum.");
            fillerRun.setFontFamily("Times New Roman");
            fillerRun.setFontSize(12);
        }
    }
    
    /**
     * Helper method to add filler paragraphs with substantial content that passes isEmpty() check
     * while preserving validation issues in the test paragraphs.
     */
    private void addFillerParagraphsWithAdequateContent(XWPFDocument doc, int count) {
        for (int i = 0; i < count; i++) {
            XWPFParagraph filler = doc.createParagraph();
            // Don't set formatting to avoid interfering with test cases
            XWPFRun fillerRun = filler.createRun();
            fillerRun.setText("This is a substantial filler paragraph that provides adequate word count to meet " +
                           "the isEmpty validation requirements without interfering with the specific test validation logic. " +
                           "Each filler paragraph contains sufficient content to contribute meaningfully to the total word count " +
                           "needed for the document to pass the preValidationCheck while allowing the actual test scenarios " +
                           "to demonstrate their specific validation failures as intended.");
        }
    }

    /**
     * Helper method to create ThesisDocument without adding correct filler paragraphs.
     * Adds minimal filler paragraphs with intentionally neutral formatting to pass isEmpty() check
     * while preserving validation issues in the test paragraphs.
     */
    private ThesisDocument createThesisDocumentWithoutCorrectingIssues(XWPFDocument doc) throws IOException {
        // Add minimal filler paragraphs with neutral content to pass isEmpty() check
        // but don't override the test paragraph formatting
        int existingParagraphs = doc.getParagraphs().size();
        int needed = Math.max(0, 3 - existingParagraphs);
        
        for (int i = 0; i < needed; i++) {
            XWPFParagraph filler = doc.createParagraph();
            // Add minimal content without setting formatting that would interfere with tests
            XWPFRun fillerRun = filler.createRun();
            fillerRun.setText("Minimal filler paragraph content to meet isEmpty validation requirements. " +
                           "This paragraph provides adequate word count without interfering with test validation.");
        }
        
        return ThesisDocument.builder()
                .filePath(Paths.get("test-document.docx"))
                .xwpfDocument(doc)
                .build();
    }
}