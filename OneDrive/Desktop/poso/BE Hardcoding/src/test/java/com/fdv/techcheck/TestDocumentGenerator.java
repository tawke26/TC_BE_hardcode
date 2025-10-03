package com.fdv.techcheck;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Utility class to generate test documents with various formatting characteristics
 * for testing the TechCheck validation system.
 */
public class TestDocumentGenerator {

    public static void main(String[] args) {
        try {
            generatePerfectDocument();
            generateMarginViolationDocument();
            generateFontViolationDocument();
            generateSpacingViolationDocument();
            generateEmptyDocument();
            generateMinimalDocument();
            generateMixedFormattingDocument();
            System.out.println("All test documents generated successfully!");
        } catch (IOException e) {
            System.err.println("Error generating test documents: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a perfect document that meets all requirements
     */
    public static void generatePerfectDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set page margins (3cm = 1700 twips)
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(1700));
        sectPr.getPgMar().setRight(BigInteger.valueOf(1700));
        sectPr.getPgMar().setTop(BigInteger.valueOf(1700));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(1700));
        
        // Set A4 page size
        CTPageSz pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11906)); // A4 width in twips
        pageSize.setH(BigInteger.valueOf(16838)); // A4 height in twips
        
        // Add content with correct formatting
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBetween(1.5); // 1.5 line spacing
        
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("This is a perfect thesis document that meets all technical requirements. " +
                   "The margins are set to 3cm on all sides, the font is Times New Roman 12pt, " +
                   "and the line spacing is 1.5. This document should pass all validation checks.");
        
        // Add more paragraphs for realistic content
        for (int i = 0; i < 5; i++) {
            XWPFParagraph p = document.createParagraph();
            p.setSpacingBetween(1.5);
            XWPFRun r = p.createRun();
            r.setFontFamily("Times New Roman");
            r.setFontSize(12);
            r.setText("This is paragraph " + (i + 2) + " of the perfect document. " +
                     "It maintains consistent formatting throughout the document. " +
                     "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
                     "tempor incididunt ut labore et dolore magna aliqua.");
        }
        
        saveDocument(document, "test-documents/perfect-document.docx");
    }

    /**
     * Generate a document with margin violations (1cm margins)
     */
    public static void generateMarginViolationDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set small margins (1cm = 567 twips)
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(567));
        sectPr.getPgMar().setRight(BigInteger.valueOf(567));
        sectPr.getPgMar().setTop(BigInteger.valueOf(567));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(567));
        
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBetween(1.5);
        
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("This document has incorrect margins (1cm instead of 3cm). " +
                   "This should trigger margin validation errors.");
        
        saveDocument(document, "test-documents/margin-violation.docx");
    }

    /**
     * Generate a document with font violations (Arial 10pt)
     */
    public static void generateFontViolationDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set correct margins
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(1700));
        sectPr.getPgMar().setRight(BigInteger.valueOf(1700));
        sectPr.getPgMar().setTop(BigInteger.valueOf(1700));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(1700));
        
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBetween(1.5);
        
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Arial"); // Wrong font
        run.setFontSize(10); // Wrong size
        run.setText("This document uses Arial 10pt instead of Times New Roman 12pt. " +
                   "This should trigger font validation errors.");
        
        saveDocument(document, "test-documents/font-violation.docx");
    }

    /**
     * Generate a document with spacing violations (single spacing)
     */
    public static void generateSpacingViolationDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set correct margins
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(1700));
        sectPr.getPgMar().setRight(BigInteger.valueOf(1700));
        sectPr.getPgMar().setTop(BigInteger.valueOf(1700));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(1700));
        
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBetween(1.0); // Wrong spacing
        
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("This document uses single line spacing instead of 1.5. " +
                   "This should trigger line spacing validation errors.");
        
        saveDocument(document, "test-documents/spacing-violation.docx");
    }

    /**
     * Generate an empty document
     */
    public static void generateEmptyDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set correct margins but no content
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(1700));
        sectPr.getPgMar().setRight(BigInteger.valueOf(1700));
        sectPr.getPgMar().setTop(BigInteger.valueOf(1700));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(1700));
        
        saveDocument(document, "test-documents/empty-document.docx");
    }

    /**
     * Generate a minimal document with single paragraph
     */
    public static void generateMinimalDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set correct margins
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(1700));
        sectPr.getPgMar().setRight(BigInteger.valueOf(1700));
        sectPr.getPgMar().setTop(BigInteger.valueOf(1700));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(1700));
        
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBetween(1.5);
        
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("Minimal document.");
        
        saveDocument(document, "test-documents/minimal-document.docx");
    }

    /**
     * Generate a document with mixed formatting throughout
     */
    public static void generateMixedFormattingDocument() throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Set correct margins
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        sectPr.addNewPgMar();
        sectPr.getPgMar().setLeft(BigInteger.valueOf(1700));
        sectPr.getPgMar().setRight(BigInteger.valueOf(1700));
        sectPr.getPgMar().setTop(BigInteger.valueOf(1700));
        sectPr.getPgMar().setBottom(BigInteger.valueOf(1700));
        
        // Paragraph 1 - Correct formatting
        XWPFParagraph p1 = document.createParagraph();
        p1.setSpacingBetween(1.5);
        XWPFRun r1 = p1.createRun();
        r1.setFontFamily("Times New Roman");
        r1.setFontSize(12);
        r1.setText("This paragraph has correct formatting.");
        
        // Paragraph 2 - Wrong font
        XWPFParagraph p2 = document.createParagraph();
        p2.setSpacingBetween(1.5);
        XWPFRun r2 = p2.createRun();
        r2.setFontFamily("Arial");
        r2.setFontSize(12);
        r2.setText("This paragraph uses Arial font.");
        
        // Paragraph 3 - Wrong size
        XWPFParagraph p3 = document.createParagraph();
        p3.setSpacingBetween(1.5);
        XWPFRun r3 = p3.createRun();
        r3.setFontFamily("Times New Roman");
        r3.setFontSize(14);
        r3.setText("This paragraph uses 14pt font size.");
        
        // Paragraph 4 - Wrong spacing
        XWPFParagraph p4 = document.createParagraph();
        p4.setSpacingBetween(2.0);
        XWPFRun r4 = p4.createRun();
        r4.setFontFamily("Times New Roman");
        r4.setFontSize(12);
        r4.setText("This paragraph uses double line spacing.");
        
        saveDocument(document, "test-documents/mixed-formatting.docx");
    }

    private static void saveDocument(XWPFDocument document, String filename) throws IOException {
        // Create directory if it doesn't exist
        java.io.File directory = new java.io.File("test-documents");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        try (FileOutputStream out = new FileOutputStream(filename)) {
            document.write(out);
        }
        document.close();
        System.out.println("Generated: " + filename);
    }
}