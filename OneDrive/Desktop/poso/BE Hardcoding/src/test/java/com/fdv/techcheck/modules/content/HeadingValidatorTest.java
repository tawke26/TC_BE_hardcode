package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import com.fdv.techcheck.core.validation.ValidationStatus;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HeadingValidator.
 */
class HeadingValidatorTest {
    
    private HeadingValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new HeadingValidator();
    }
    
    @Test
    void testValidHeadingHierarchy() throws IOException, ValidationException {
        // Create document with proper heading hierarchy
        XWPFDocument doc = new XWPFDocument();
        
        // H1
        XWPFParagraph h1 = doc.createParagraph();
        h1.setStyle("Heading1");
        XWPFRun h1Run = h1.createRun();
        h1Run.setText("1. Introduction");
        h1Run.setFontSize(16);
        h1Run.setFontFamily("Times New Roman");
        h1Run.setBold(true);
        
        // H2
        XWPFParagraph h2 = doc.createParagraph();
        h2.setStyle("Heading2");
        XWPFRun h2Run = h2.createRun();
        h2Run.setText("1.1 Background");
        h2Run.setFontSize(14);
        h2Run.setFontFamily("Times New Roman");
        h2Run.setBold(true);
        
        // H3
        XWPFParagraph h3 = doc.createParagraph();
        h3.setStyle("Heading3");
        XWPFRun h3Run = h3.createRun();
        h3Run.setText("1.1.1 Context");
        h3Run.setFontSize(12);
        h3Run.setFontFamily("Times New Roman");
        h3Run.setBold(true);
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.PASS, result.getStatus(), "Valid heading hierarchy should pass validation");
        doc.close();
    }
    
    @Test
    void testInvalidHeadingHierarchy() throws IOException, ValidationException {
        // Create document with skipped heading levels (H1 → H3)
        XWPFDocument doc = new XWPFDocument();
        
        // H1
        XWPFParagraph h1 = doc.createParagraph();
        h1.setStyle("Heading1");
        XWPFRun h1Run = h1.createRun();
        h1Run.setText("1. Introduction");
        h1Run.setFontSize(16);
        h1Run.setBold(true);
        
        // H3 (skipping H2)
        XWPFParagraph h3 = doc.createParagraph();
        h3.setStyle("Heading3");
        XWPFRun h3Run = h3.createRun();
        h3Run.setText("1.1.1 Context");
        h3Run.setFontSize(12);
        h3Run.setBold(true);
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Invalid heading hierarchy should fail validation");
        assertTrue(result.getDetails().stream()
            .anyMatch(issue -> issue.getLocation().contains("Paragraph") &&
                      issue.getSeverity() == ValidationSeverity.MAJOR),
            "Should report major issue for skipped heading level");
        doc.close();
    }
    
    @Test
    void testIncorrectFontSizes() throws IOException, ValidationException {
        // Create document with incorrect font sizes
        XWPFDocument doc = new XWPFDocument();
        
        // H1 with wrong font size
        XWPFParagraph h1 = doc.createParagraph();
        h1.setStyle("Heading1");
        XWPFRun h1Run = h1.createRun();
        h1Run.setText("1. Introduction");
        h1Run.setFontSize(14); // Should be 16pt
        h1Run.setBold(true);
        
        // H2 with wrong font size
        XWPFParagraph h2 = doc.createParagraph();
        h2.setStyle("Heading2");
        XWPFRun h2Run = h2.createRun();
        h2Run.setText("1.1 Background");
        h2Run.setFontSize(16); // Should be 14pt
        h2Run.setBold(true);
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Incorrect font sizes should fail validation");
        assertEquals(2, result.getDetails().stream()
            .mapToInt(issue -> issue.getActual().contains("Font size") ? 1 : 0)
            .sum(), "Should report font size issues for both headings");
        doc.close();
    }
    
    @Test
    void testMissingNumbering() throws IOException, ValidationException {
        // Create document with unnumbered headings
        XWPFDocument doc = new XWPFDocument();
        
        // H1 without numbering
        XWPFParagraph h1 = doc.createParagraph();
        h1.setStyle("Heading1");
        XWPFRun h1Run = h1.createRun();
        h1Run.setText("Introduction"); // No numbering
        h1Run.setFontSize(16);
        h1Run.setBold(true);
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Missing numbering should fail validation");
        assertTrue(result.getDetails().stream()
            .anyMatch(issue -> issue.getActual().contains("Unnumbered heading") &&
                      issue.getSeverity() == ValidationSeverity.MAJOR),
            "Should report major issue for unnumbered heading");
        doc.close();
    }
    
    @Test
    void testIncorrectFontFamily() throws IOException, ValidationException {
        // Create document with wrong font family
        XWPFDocument doc = new XWPFDocument();
        
        // H1 with wrong font
        XWPFParagraph h1 = doc.createParagraph();
        h1.setStyle("Heading1");
        XWPFRun h1Run = h1.createRun();
        h1Run.setText("1. Introduction");
        h1Run.setFontSize(16);
        h1Run.setFontFamily("Arial"); // Should be Times New Roman
        h1Run.setBold(true);
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Incorrect font family should fail validation");
        assertTrue(result.getDetails().stream()
            .anyMatch(issue -> issue.getActual().contains("Arial") &&
                      issue.getExpected().contains("Times New Roman")),
            "Should report font family issue");
        doc.close();
    }
    
    @Test
    void testNoHeadings() throws IOException, ValidationException {
        // Create document without any headings
        XWPFDocument doc = new XWPFDocument();
        
        // Just regular paragraph
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText("This is just regular text without any headings.");
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Document without headings should fail validation");
        assertTrue(result.getDetails().stream()
            .anyMatch(issue -> issue.getActual().contains("No headings found") &&
                      issue.getSeverity() == ValidationSeverity.MAJOR),
            "Should report major issue for missing headings");
        doc.close();
    }
    
    @Test
    void testDocumentStartingWithNonH1() throws IOException, ValidationException {
        // Create document starting with H2
        XWPFDocument doc = new XWPFDocument();
        
        // Start with H2 instead of H1
        XWPFParagraph h2 = doc.createParagraph();
        h2.setStyle("Heading2");
        XWPFRun h2Run = h2.createRun();
        h2Run.setText("1.1 Background");
        h2Run.setFontSize(14);
        h2Run.setBold(true);
        
        ThesisDocument thesisDoc = createThesisDocument(doc);
        ValidationResult result = validator.validate(thesisDoc);
        
        assertEquals(ValidationStatus.FAIL, result.getStatus(), "Document starting with non-H1 should fail validation");
        assertTrue(result.getDetails().stream()
            .anyMatch(issue -> issue.getLocation().contains("First heading") &&
                      issue.getSeverity() == ValidationSeverity.MAJOR),
            "Should report major issue for not starting with H1");
        doc.close();
    }
    
    @Test
    void testValidatorName() {
        assertEquals("Heading Validator", validator.getValidatorName());
    }
    
    /**
     * Helper method to create ThesisDocument from XWPFDocument.
     */
    private ThesisDocument createThesisDocument(XWPFDocument doc) throws IOException {
        return ThesisDocument.builder()
                .filePath(Paths.get("test-document.docx"))
                .xwpfDocument(doc)
                .build();
    }
}