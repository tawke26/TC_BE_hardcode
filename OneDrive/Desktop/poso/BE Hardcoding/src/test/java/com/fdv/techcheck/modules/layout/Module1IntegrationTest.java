package com.fdv.techcheck.modules.layout;

import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationException;
import com.fdv.techcheck.core.validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Module 1: Document Structure & Layout validators.
 * 
 * Tests that all four validators (PageFormat, Margin, Font, LineSpacing) 
 * can be instantiated and execute without compilation errors.
 */
class Module1IntegrationTest {

    private PageFormatValidator pageFormatValidator;
    private MarginValidator marginValidator;
    private FontValidator fontValidator;
    private LineSpacingValidator lineSpacingValidator;

    @BeforeEach
    void setUp() {
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
        // Each validator should throw ValidationException for null input
        assertThrows(ValidationException.class, () -> pageFormatValidator.validate(null),
                "PageFormatValidator should throw ValidationException for null input");
        assertThrows(ValidationException.class, () -> marginValidator.validate(null),
                "MarginValidator should throw ValidationException for null input");
        assertThrows(ValidationException.class, () -> fontValidator.validate(null),
                "FontValidator should throw ValidationException for null input");
        assertThrows(ValidationException.class, () -> lineSpacingValidator.validate(null),
                "LineSpacingValidator should throw ValidationException for null input");
    }
}