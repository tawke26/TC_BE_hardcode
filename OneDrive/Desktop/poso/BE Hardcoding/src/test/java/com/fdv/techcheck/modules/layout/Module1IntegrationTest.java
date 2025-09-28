package com.fdv.techcheck.modules.layout;

import com.fdv.techcheck.core.document.DocumentProcessor;
import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Module 1: Document Structure & Layout validators.
 * 
 * Tests that all four validators (PageFormat, Margin, Font, LineSpacing) 
 * can be instantiated and execute without compilation errors.
 */
class Module1IntegrationTest {

    private DocumentProcessor documentProcessor;
    private PageFormatValidator pageFormatValidator;
    private MarginValidator marginValidator;
    private FontValidator fontValidator;
    private LineSpacingValidator lineSpacingValidator;

    @BeforeEach
    void setUp() {
        documentProcessor = new DocumentProcessor();
        pageFormatValidator = new PageFormatValidator();
        marginValidator = new MarginValidator();
        fontValidator = new FontValidator();
        lineSpacingValidator = new LineSpacingValidator();
    }

    @Test
    @DisplayName("Module 1 validators should instantiate without errors")
    void testValidatorInstantiation() {
        assertNotNull(pageFormatValidator, "PageFormatValidator should instantiate");
        assertNotNull(marginValidator, "MarginValidator should instantiate");
        assertNotNull(fontValidator, "FontValidator should instantiate");
        assertNotNull(lineSpacingValidator, "LineSpacingValidator should instantiate");
    }

    @Test
    @DisplayName("All Module 1 validators should have correct names")
    void testValidatorNames() {
        assertEquals("Page Format Validator", pageFormatValidator.getValidatorName());
        assertEquals("Margin Validator", marginValidator.getValidatorName());
        assertEquals("Font Validator", fontValidator.getValidatorName());
        assertEquals("Line Spacing Validator", lineSpacingValidator.getValidatorName());
    }

    @Test
    @DisplayName("Validators should handle null document gracefully")
    void testNullDocumentHandling() {
        // Each validator should handle null input gracefully
        ValidationResult pageResult = pageFormatValidator.validate(null);
        ValidationResult marginResult = marginValidator.validate(null);
        ValidationResult fontResult = fontValidator.validate(null);
        ValidationResult lineSpacingResult = lineSpacingValidator.validate(null);

        // All should return error results for null input
        assertNotNull(pageResult, "PageFormatValidator should return result for null input");
        assertNotNull(marginResult, "MarginValidator should return result for null input");
        assertNotNull(fontResult, "FontValidator should return result for null input");
        assertNotNull(lineSpacingResult, "LineSpacingValidator should return result for null input");
    }
}