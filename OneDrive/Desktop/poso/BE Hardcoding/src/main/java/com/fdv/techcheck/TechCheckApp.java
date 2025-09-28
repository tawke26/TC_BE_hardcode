package com.fdv.techcheck;

import com.fdv.techcheck.core.document.DocumentProcessor;
import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.IValidator;
import com.fdv.techcheck.modules.layout.MarginValidator;
import com.fdv.techcheck.modules.layout.FontValidator;
import com.fdv.techcheck.modules.layout.LineSpacingValidator;
import com.fdv.techcheck.modules.layout.PageFormatValidator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line application to test all TechCheck Module 1 validators.
 * 
 * This application allows technical staff at FDV to test all document structure
 * and layout validation on actual thesis documents.
 *
 * @author TechCheck System
 * @version 1.0
 */
public class TechCheckApp {
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("FDV TechCheck - Thesis Validation System");
        System.out.println("==========================================");
        System.out.println();
        
        // Get document path from user or command line
        String documentPath = getDocumentPath(args);
        
        if (documentPath == null) {
            System.err.println("No document path provided. Exiting.");
            return;
        }
        
        // Validate the document
        try {
            validateDocument(documentPath);
        } catch (Exception e) {
            System.err.println("Error during validation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the document path from command line arguments or user input.
     */
    private static String getDocumentPath(String[] args) {
        if (args.length > 0) {
            return args[0];
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the thesis document (.docx): ");
        String path = scanner.nextLine().trim();
        scanner.close();
        
        return path.isEmpty() ? null : path;
    }
    
    /**
     * Validates the specified document and displays results.
     */
    private static void validateDocument(String documentPath) {
        System.out.println("Testing document: " + documentPath);
        System.out.println("----------------------------------------");
        
        // Check if file exists
        File docFile = new File(documentPath);
        if (!docFile.exists()) {
            System.err.println("Error: File not found - " + documentPath);
            return;
        }
        
        if (!docFile.getName().toLowerCase().endsWith(".docx")) {
            System.err.println("Error: File must be a .docx document");
            return;
        }
        
        try {
            // Load the document
            System.out.println("Loading document...");
            ThesisDocument document = DocumentProcessor.loadDocument(docFile.toPath());
            
            System.out.println("Document loaded successfully!");
            System.out.println("Title: " + document.getMetadata().getTitle());
            System.out.println("Author: " + document.getMetadata().getAuthor());
            System.out.println("Pages: " + document.getMetadata().getPageCount());
            System.out.println();
            
            // Test all Module 1 validators
            List<IValidator> validators = Arrays.asList(
                new MarginValidator(),
                new FontValidator(),
                new LineSpacingValidator(),
                new PageFormatValidator()
            );
            
            for (IValidator validator : validators) {
                testValidator(validator, document);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to process document: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests a single validator and displays results.
     */
    private static void testValidator(IValidator validator, ThesisDocument document) {
        try {
            String validatorName = validator.getClass().getSimpleName();
            System.out.println("TESTING " + validatorName.replace("Validator", "").toUpperCase() + " VALIDATION");
            System.out.println("=".repeat(25 + validatorName.length()));
            
            ValidationResult result = validator.validate(document);
            displayValidationResult(result);
            
        } catch (Exception e) {
            System.err.println("Validator error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays validation results in a user-friendly format.
     */
    private static void displayValidationResult(ValidationResult result) {
        System.out.println("Validation Status: " + result.getStatus());
        System.out.println("Validator: " + result.getValidatorName());
        
        if (result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()) {
            System.out.println("Message: " + result.getErrorMessage());
        }
        
        if (result.getDetails() != null && !result.getDetails().isEmpty()) {
            System.out.println();
            System.out.println("Issues Found:");
            System.out.println("-------------");
            
            for (ValidationDetail detail : result.getDetails()) {
                String severity = getSeveritySymbol(detail.getSeverity());
                System.out.printf("%s %s [%s]%n", severity, detail.getLocation(), detail.getSeverity());
                System.out.printf("  Expected: %s%n", detail.getExpected());
                System.out.printf("  Actual:   %s%n", detail.getActual());
                System.out.println();
            }
        } else {
            System.out.println("✓ No issues found - document complies with requirements");
        }
        
        System.out.println("Validation completed in: " + result.getProcessingTime().toMillis() + " ms");
        System.out.println();
    }
    
    /**
     * Gets a visual symbol for different severity levels.
     */
    private static String getSeveritySymbol(com.fdv.techcheck.core.validation.ValidationSeverity severity) {
        switch (severity) {
            case CRITICAL: return "✗";
            case MAJOR: return "⚠";
            case MINOR: return "ℹ";
            default: return "?";
        }
    }
}