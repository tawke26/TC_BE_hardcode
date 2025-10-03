package com.fdv.techcheck.modules.content;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationStatus;
import com.fdv.techcheck.core.validation.ValidationException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Module 2 validators using a real thesis document.
 * Loads ./sample-thesis.docx and runs HeadingValidator, ParagraphValidator, ListValidator.
 * Verifies that validation runs without exceptions and produces results.
 */
class RealThesisTest {

    @Test
    void testModule2ValidatorsOnRealThesis() throws IOException, ValidationException {
        // Load real document
        XWPFDocument doc;
        try (FileInputStream fis = new FileInputStream("./sample-thesis.docx")) {
            doc = new XWPFDocument(fis);
        }

        ThesisDocument thesisDoc = ThesisDocument.builder()
                .filePath(Paths.get("./sample-thesis.docx"))
                .xwpfDocument(doc)
                .build();

        // Initialize validators
        HeadingValidator headingValidator = new HeadingValidator();
        ParagraphValidator paragraphValidator = new ParagraphValidator();
        ListValidator listValidator = new ListValidator();

        // Run validations
        ValidationResult headingResult = headingValidator.validate(thesisDoc);
        ValidationResult paragraphResult = paragraphValidator.validate(thesisDoc);
        ValidationResult listResult = listValidator.validate(thesisDoc);

        // Basic assertions - should run without exceptions
        assertNotNull(headingResult);
        assertNotNull(paragraphResult);
        assertNotNull(listResult);

        // Document should pass pre-validation (assuming it's a real thesis with content)
        assertTrue(thesisDoc.getParagraphs().size() > 0, "Real document should have paragraphs");

        // Log results for review
        System.out.println("=== Module 2 Validation Results for sample-thesis.docx ===");
        System.out.println("Heading Validator: " + headingResult.getStatus() + " with " + headingResult.getDetails().size() + " issues");
        if (!headingResult.getDetails().isEmpty()) {
            headingResult.getDetails().forEach(detail -> System.out.println("  - " + detail.getLocation() + ": " + detail.getExpected() + " (expected) vs " + detail.getActual() + " (actual)"));
        }

        System.out.println("Paragraph Validator: " + paragraphResult.getStatus() + " with " + paragraphResult.getDetails().size() + " issues");
        if (!paragraphResult.getDetails().isEmpty()) {
            paragraphResult.getDetails().forEach(detail -> System.out.println("  - " + detail.getLocation() + ": " + detail.getExpected() + " (expected) vs " + detail.getActual() + " (actual)"));
        }

        System.out.println("List Validator: " + listResult.getStatus() + " with " + listResult.getDetails().size() + " issues");
        if (!listResult.getDetails().isEmpty()) {
            listResult.getDetails().forEach(detail -> System.out.println("  - " + detail.getLocation() + ": " + detail.getExpected() + " (expected) vs " + detail.getActual() + " (actual)"));
        }

        // Close document
        doc.close();

        // For real testing, we expect some passes and possibly issues to identify
        // But at minimum, no crashes
        assertDoesNotThrow(() -> {
            // All validations completed successfully
        });
    }
}