package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationStatus;
import com.fdv.techcheck.core.validation.ValidationSeverity;
import com.fdv.techcheck.modules.content.HeadingValidator;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Module 2 validators: HeadingValidator, ParagraphValidator, ListValidator.
 * Tests combined validation scenarios across content structure and typography.
 */
class Module2IntegrationTest {

    private HeadingValidator headingValidator;
    private ParagraphValidator paragraphValidator;
    private ListValidator listValidator;

    @BeforeEach
    void setUp() {
        headingValidator = new HeadingValidator();
        paragraphValidator = new ParagraphValidator();
        listValidator = new ListValidator();
    }

    @Test
    void testValidModule2Document() throws IOException, ValidationException {
        // Create a document with valid headings, paragraphs, and lists
        XWPFDocument doc = createValidModule2Document();

        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("valid-module2.docx"))
                .xwpfDocument(doc)
                .build();

        // Run all validators
        ValidationResult headingResult = headingValidator.validate(thesisDoc);
        ValidationResult paragraphResult = paragraphValidator.validate(thesisDoc);
        ValidationResult listResult = listValidator.validate(thesisDoc);

        // All should pass
        assertEquals(ValidationStatus.PASS, headingResult.getStatus());
        assertEquals(ValidationStatus.PASS, paragraphResult.getStatus());
        assertEquals(ValidationStatus.PASS, listResult.getStatus());

        // No issues in any
        assertTrue(headingResult.getDetails().isEmpty());
        assertTrue(paragraphResult.getDetails().isEmpty());
        assertTrue(listResult.getDetails().isEmpty());

        doc.close();
    }

    @Test
    void testDocumentWithMultipleModule2Issues() throws IOException, ValidationException {
        // Create document with various issues
        XWPFDocument doc = createDocumentWithModule2Issues();

        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("issues-module2.docx"))
                .xwpfDocument(doc)
                .build();

        // Run validators
        ValidationResult headingResult = headingValidator.validate(thesisDoc);
        ValidationResult paragraphResult = paragraphValidator.validate(thesisDoc);
        ValidationResult listResult = listValidator.validate(thesisDoc);

        // All should fail due to issues
        assertEquals(ValidationStatus.FAIL, headingResult.getStatus());
        assertEquals(ValidationStatus.FAIL, paragraphResult.getStatus());
        assertEquals(ValidationStatus.FAIL, listResult.getStatus());

        // Check for specific issues
        List<ValidationResult> results = List.of(headingResult, paragraphResult, listResult);
        int totalIssues = results.stream().mapToInt(r -> r.getDetails().size()).sum();
        assertTrue(totalIssues >= 5, "Should find multiple issues across validators");

        // Verify heading issues
        assertTrue(headingResult.getDetails().stream()
                .anyMatch(d -> d.getLocation().contains("Heading") && d.getSeverity() == ValidationSeverity.MINOR),
                "Should detect heading formatting issues");

        // Verify paragraph issues
        assertTrue(paragraphResult.getDetails().stream()
                .anyMatch(d -> d.getExpected().contains("50") || d.getExpected().contains("1.5")),
                "Should detect paragraph length or spacing issues");

        // Verify list issues
        assertTrue(listResult.getDetails().stream()
                .anyMatch(d -> d.getExpected().contains("3") || d.getLocation().contains("level")),
                "Should detect list nesting or consistency issues");

        doc.close();
    }

    @Test
    void testEmptyDocumentForModule2() {
        XWPFDocument doc = new XWPFDocument();

        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("empty.docx"))
                .xwpfDocument(doc)
                .build();

        // All validators should throw ValidationException for empty document
        ValidationException headingEx = assertThrows(ValidationException.class, () -> headingValidator.validate(thesisDoc));
        assertTrue(headingEx.getMessage().contains("no paragraphs") || headingEx.getMessage().contains("empty") || headingEx.getMessage().contains("insufficient"));

        ValidationException paragraphEx = assertThrows(ValidationException.class, () -> paragraphValidator.validate(thesisDoc));
        assertTrue(paragraphEx.getMessage().contains("empty") || paragraphEx.getMessage().contains("insufficient"));

        ValidationException listEx = assertThrows(ValidationException.class, () -> listValidator.validate(thesisDoc));
        assertTrue(listEx.getMessage().contains("empty") || listEx.getMessage().contains("insufficient"));

        try {
            doc.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    @Test
    void testMinimalDocumentPassesPreValidationButHasNoContentIssues() throws IOException, ValidationException {
        // Create minimal document that passes isEmpty() but has no headings/paragraphs/lists to validate
        XWPFDocument doc = new XWPFDocument();
        addFillerParagraphsWithAdequateContent(doc, 3); // From ParagraphValidatorTest helper, but inline here

        for (int i = 0; i < 3; i++) {
            XWPFParagraph filler = doc.createParagraph();
            filler.setAlignment(ParagraphAlignment.BOTH);
            setLineSpacing(filler, 1.5);
            XWPFRun fillerRun = filler.createRun();
            fillerRun.setText("This is filler content to pass isEmpty check. Standard paragraph with proper formatting and sufficient length to meet validation requirements. It has justified alignment and 1.5 line spacing as required.");
        }

        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("minimal.docx"))
                .xwpfDocument(doc)
                .build();

        // Run validators - should pass since no invalid content, but might have no-content warnings
        ValidationResult headingResult = headingValidator.validate(thesisDoc);
        ValidationResult paragraphResult = paragraphValidator.validate(thesisDoc);
        ValidationResult listResult = listValidator.validate(thesisDoc);

        // Paragraphs should pass (filler is valid)
        assertEquals(ValidationStatus.PASS, paragraphResult.getStatus());

        // Headings and lists will FAIL due to absence, but that's expected for minimal document
        assertEquals(ValidationStatus.FAIL, headingResult.getStatus());
        assertEquals(ValidationStatus.PASS, listResult.getStatus()); // No lists, but no violation if validator handles absence as PASS

        doc.close();
    }

    /**
     * Creates a document with valid Module 2 elements.
     */
    private XWPFDocument createValidModule2Document() throws IOException {
        XWPFDocument doc = new XWPFDocument();

        // Valid heading 1
        XWPFParagraph h1 = doc.createParagraph();
        h1.setStyle("Heading1");
        XWPFRun h1Run = h1.createRun();
        h1Run.setText("1. Chapter 1: Introduction");
        h1Run.setBold(true);
        h1Run.setFontSize(16); // Correct size for H1

        // Valid H2
        XWPFParagraph h2 = doc.createParagraph();
        h2.setStyle("Heading2");
        XWPFRun h2Run = h2.createRun();
        h2Run.setText("1.1 Section 1.1");
        h2Run.setBold(true);
        h2Run.setFontSize(14);

        // Valid paragraph
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(para, 1.5);
        XWPFRun paraRun = para.createRun();
        paraRun.setText("This is a valid paragraph with sufficient length (over 50 characters) and proper justified alignment. " +
                        "It demonstrates correct formatting for thesis documents according to FDV standards. Additional content added to ensure the paragraph meets the minimum length requirement of 50 characters while maintaining proper structure and readability.");

        // Another valid paragraph
        XWPFParagraph para2 = doc.createParagraph();
        para2.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(para2, 1.5);
        XWPFRun para2Run = para2.createRun();
        para2Run.setText("Another valid paragraph to ensure sufficient content and proper formatting throughout the document. This paragraph also meets the length requirements and uses justified alignment with 1.5 line spacing as specified in the thesis guidelines.");

        // Valid bulleted list (2 levels) with longer text
        createValidBulletedList(doc, 2);

        // Valid numbered list (3 levels max) with longer text
        createValidNumberedList(doc, 3);

        // Add filler to ensure isEmpty passes
        for (int i = 0; i < 2; i++) {
            XWPFParagraph filler = doc.createParagraph();
            filler.setAlignment(ParagraphAlignment.BOTH);
            setLineSpacing(filler, 1.5);
            XWPFRun fillerRun = filler.createRun();
            fillerRun.setText("Filler paragraph with adequate content to meet document requirements. Proper alignment and spacing applied.");
        }

        return doc;
    }

    /**
     * Creates a document with various Module 2 issues.
     */
    private XWPFDocument createDocumentWithModule2Issues() throws IOException {
        XWPFDocument doc = new XWPFDocument();

        // Invalid heading (wrong font size)
        XWPFParagraph badH1 = doc.createParagraph();
        badH1.setStyle("Heading1");
        XWPFRun badH1Run = badH1.createRun();
        badH1Run.setText("2. Invalid Heading"); // Wrong numbering
        badH1Run.setBold(true);
        badH1Run.setFontSize(18); // Wrong size for H1

        // Skipped level - directly H3
        XWPFParagraph badH3 = doc.createParagraph();
        badH3.setStyle("Heading3");
        XWPFRun badH3Run = badH3.createRun();
        badH3Run.setText("1.1.1 Skipped Section");
        badH3Run.setBold(true);
        badH3Run.setFontSize(12);

        // Short paragraph
        XWPFParagraph shortPara = doc.createParagraph();
        shortPara.setAlignment(ParagraphAlignment.LEFT); // Wrong alignment
        setLineSpacing(shortPara, 1.0); // Wrong spacing
        XWPFRun shortRun = shortPara.createRun();
        shortRun.setText("Too short.");

        // Long paragraph
        XWPFParagraph longPara = doc.createParagraph();
        longPara.setAlignment(ParagraphAlignment.BOTH);
        setLineSpacing(longPara, 1.5);
        XWPFRun longRun = longPara.createRun();
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longText.append("This is excessively long text that exceeds the maximum paragraph length limit. ");
        }
        longRun.setText(longText.toString());

        // Invalid list (4 levels nesting)
        createInvalidNestedList(doc, 4);

        // Inconsistent list styles - mix numbered and bulleted
        XWPFParagraph numList = doc.createParagraph();
        XWPFRun numRun = numList.createRun();
        numRun.setText("1. Numbered item");

        XWPFParagraph bulletList = doc.createParagraph();
        XWPFRun bulletRun = bulletList.createRun();
        bulletRun.setText("• Bulleted item in same list");

        // Add filler to pass isEmpty but preserve issues
        addFillerParagraphsWithAdequateContent(doc, 2);

        return doc;
    }

    private void createValidBulletedList(XWPFDocument doc, int levels) {
        for (int level = 0; level < levels; level++) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(String.format("• Level %d bullet item with proper indentation and sufficient text content to avoid paragraph length issues during validation.", level + 1));
            // Set indentation based on level
            // In real POI, would set numPr for bullets
        }
    }

    private void createValidNumberedList(XWPFDocument doc, int levels) {
        String[] prefixes = {"1.", "1.1", "1.1.1"};
        for (int level = 0; level < levels; level++) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(String.format("%s Level %d numbered item with adequate content length for validation purposes.", prefixes[level], level + 1));
            // Set numPr for numbering
        }
    }

    private void createInvalidNestedList(XWPFDocument doc, int levels) {
        String[] prefixes = {"1.", "1.1", "1.1.1", "1.1.1.1", "1.1.1.1.1"}; // Invalid deep nesting
        for (int level = 0; level < levels; level++) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(String.format("%s Nested level %d (too deep).", prefixes[level], level + 1));
            // Simulate deep nesting
        }
    }

    private void setLineSpacing(XWPFParagraph paragraph, double spacing) {
        var ctSpacing = paragraph.getCTP().getPPr().isSetSpacing() 
            ? paragraph.getCTP().getPPr().getSpacing() 
            : paragraph.getCTP().getPPr().addNewSpacing();
        
        ctSpacing.setLine(java.math.BigInteger.valueOf((long)(spacing * 240)));
        ctSpacing.setLineRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule.AUTO);
    }

    private void addFillerParagraphsWithAdequateContent(XWPFDocument doc, int count) {
        for (int i = 0; i < count; i++) {
            XWPFParagraph filler = doc.createParagraph();
            XWPFRun fillerRun = filler.createRun();
            fillerRun.setText("Filler paragraph to ensure document passes isEmpty check. This content is neutral and valid.");
        }
    }
}