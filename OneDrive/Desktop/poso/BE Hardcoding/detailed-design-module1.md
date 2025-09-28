# TechCheck - Module 1 Detailed Design
## Document Structure & Layout Module

---

## 📋 Class Diagram - Module 1: Document Structure & Layout

### Core Classes & Interfaces

```mermaid
classDiagram
    class IValidator {
        <<interface>>
        +validate(document: ThesisDocument): ValidationResult
        +getName(): String
        +getDescription(): String
        +getPriority(): int
    }

    class AbstractValidator {
        <<abstract>>
        -name: String
        -description: String
        -priority: int
        +validate(document: ThesisDocument): ValidationResult*
        +getName(): String
        +getDescription(): String
        +getPriority(): int
        #createResult(status: ValidationStatus, message: String): ValidationResult
    }

    class MarginValidator {
        -requiredMargin: double
        -tolerance: double
        +validate(document: ThesisDocument): ValidationResult
        -checkTopMargin(document: ThesisDocument): boolean
        -checkBottomMargin(document: ThesisDocument): boolean
        -checkLeftMargin(document: ThesisDocument): boolean
        -checkRightMargin(document: ThesisDocument): boolean
        -convertToPoints(cm: double): double
    }

    class FontValidator {
        -requiredFontFamily: String
        -requiredFontSize: int
        +validate(document: ThesisDocument): ValidationResult
        -checkFontFamily(paragraph: XWPFParagraph): boolean
        -checkFontSize(paragraph: XWPFParagraph): boolean
        -validateHeadingFonts(document: ThesisDocument): List~FontIssue~
    }

    class LineSpacingValidator {
        -requiredSpacing: double
        -tolerance: double
        +validate(document: ThesisDocument): ValidationResult
        -checkParagraphSpacing(paragraph: XWPFParagraph): boolean
        -calculateSpacingRatio(spacing: CTSpacing): double
    }

    class PageFormatValidator {
        -requiredFormat: PageFormat
        +validate(document: ThesisDocument): ValidationResult
        -checkPageSize(document: ThesisDocument): boolean
        -checkOrientation(document: ThesisDocument): boolean
    }

    IValidator <|.. AbstractValidator
    AbstractValidator <|-- MarginValidator
    AbstractValidator <|-- FontValidator
    AbstractValidator <|-- LineSpacingValidator
    AbstractValidator <|-- PageFormatValidator
```

### Data Models

```mermaid
classDiagram
    class ThesisDocument {
        -xwpfDocument: XWPFDocument
        -filePath: String
        -metadata: DocumentMetadata
        -sections: List~DocumentSection~
        +getDocument(): XWPFDocument
        +getFilePath(): String
        +getMetadata(): DocumentMetadata
        +getSections(): List~DocumentSection~
        +getParagraphs(): List~XWPFParagraph~
        +getTables(): List~XWPFTable~
        +getHeadersFooters(): List~XWPFHeaderFooter~
    }

    class DocumentMetadata {
        -title: String
        -author: String
        -creationDate: LocalDateTime
        -lastModified: LocalDateTime
        -wordCount: int
        -pageCount: int
        +getTitle(): String
        +getAuthor(): String
        +getCreationDate(): LocalDateTime
        +getWordCount(): int
        +getPageCount(): int
    }

    class DocumentSection {
        -type: SectionType
        -startPage: int
        -endPage: int
        -paragraphs: List~XWPFParagraph~
        +getType(): SectionType
        +getStartPage(): int
        +getEndPage(): int
        +getParagraphs(): List~XWPFParagraph~
    }

    class ValidationResult {
        -status: ValidationStatus
        -validatorName: String
        -message: String
        -details: List~ValidationDetail~
        -timestamp: LocalDateTime
        +getStatus(): ValidationStatus
        +getValidatorName(): String
        +getMessage(): String
        +getDetails(): List~ValidationDetail~
        +isSuccess(): boolean
        +addDetail(detail: ValidationDetail): void
    }

    class ValidationDetail {
        -location: String
        -expected: String
        -actual: String
        -severity: Severity
        -suggestion: String
        +getLocation(): String
        +getExpected(): String
        +getActual(): String
        +getSeverity(): Severity
        +getSuggestion(): String
    }

    ThesisDocument --> DocumentMetadata
    ThesisDocument --> DocumentSection
    ValidationResult --> ValidationDetail
```

### Enums & Constants

```mermaid
classDiagram
    class ValidationStatus {
        <<enumeration>>
        PASS
        FAIL
        WARNING
        SKIPPED
    }

    class SectionType {
        <<enumeration>>
        TITLE_PAGE
        ABSTRACT
        TABLE_OF_CONTENTS
        INTRODUCTION
        MAIN_CONTENT
        CONCLUSION
        BIBLIOGRAPHY
        APPENDIX
    }

    class Severity {
        <<enumeration>>
        CRITICAL
        MAJOR
        MINOR
        INFO
    }

    class PageFormat {
        <<enumeration>>
        A4
        LETTER
        LEGAL
    }

    class FDVConstants {
        <<utility>>
        +REQUIRED_MARGIN_CM: double = 2.5
        +REQUIRED_FONT_FAMILY: String = "Times New Roman"
        +REQUIRED_FONT_SIZE: int = 12
        +REQUIRED_LINE_SPACING: double = 1.5
        +PAGE_FORMAT: PageFormat = A4
        +MARGIN_TOLERANCE: double = 0.1
        +SPACING_TOLERANCE: double = 0.05
    }
```

---

## 🎨 User Interface Design

### Main Application Window

```mermaid
graph TD
    A[TechCheck - Main Window] --> B[Menu Bar]
    A --> C[Toolbar]
    A --> D[Document Selection Panel]
    A --> E[Validation Progress Panel]
    A --> F[Results Display Panel]
    A --> G[Status Bar]

    B --> B1[File Menu]
    B --> B2[Tools Menu]
    B --> B3[Help Menu]

    C --> C1[Open Document Button]
    C --> C2[Start Validation Button]
    C --> C3[Export Report Button]
    C --> C4[Settings Button]

    D --> D1[Selected File Path]
    D --> D2[Document Info Display]
    D --> D3[Browse Button]

    E --> E1[Module Progress Bars]
    E --> E2[Current Validation Status]
    E --> E3[Stop/Pause Buttons]

    F --> F1[Results Tree View]
    F --> F2[Detail Panel]
    F --> F3[Issue Statistics]
```

### UI Component Specifications

#### 1. **Main Window Layout**
```
┌─────────────────────────────────────────────────────────────┐
│ File  Tools  Help                                  [_][□][X] │
├─────────────────────────────────────────────────────────────┤
│ [📁] [▶️] [📊] [⚙️]                                          │
├─────────────────────────────────────────────────────────────┤
│ Document Selection                                          │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ File: C:\Documents\thesis.docx              [Browse...] │ │
│ │ Pages: 45 | Words: 12,345 | Size: 2.3 MB              │ │
│ └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│ Validation Progress                                         │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ ✓ Module 1: Document Structure    [████████████] 100%  │ │
│ │ ⚠ Module 2: Content Structure     [██████░░░░░░]  60%  │ │
│ │ ○ Module 3: Tables & Figures      [░░░░░░░░░░░░]   0%  │ │
│ │ ○ Module 4: Citations             [░░░░░░░░░░░░]   0%  │ │
│ │ ○ Module 5: Language & Grammar    [░░░░░░░░░░░░]   0%  │ │
│ │ ○ Module 6: Technical Compliance  [░░░░░░░░░░░░]   0%  │ │
│ └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│ Validation Results                                          │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ 📋 Document Structure & Layout                          │ │
│ │ ├─ ✓ Margins (2.5cm) - PASS                           │ │
│ │ ├─ ✗ Font Family - FAIL (Arial found, Times required) │ │
│ │ ├─ ⚠ Line Spacing - WARNING (1.4 found, 1.5 required)│ │
│ │ └─ ✓ Page Format (A4) - PASS                          │ │
│ │                                                         │ │
│ │ Details Panel:                                          │ │
│ │ Issue: Font Family Violation                            │ │
│ │ Location: Page 3, Paragraph 12                         │ │
│ │ Expected: Times New Roman, 12pt                        │ │
│ │ Found: Arial, 12pt                                     │ │
│ │ Suggestion: Change font to Times New Roman             │ │
│ └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│ Issues: 2 Critical, 3 Major, 1 Minor | Progress: 16.7%     │
└─────────────────────────────────────────────────────────────┘
```

#### 2. **Settings Dialog**
```
┌─────────────────────────────────────────┐
│ TechCheck Settings                [X]   │
├─────────────────────────────────────────┤
│ [Validation Rules] [General] [Reports]  │
├─────────────────────────────────────────┤
│ Document Structure & Layout             │
│ ┌─────────────────────────────────────┐ │
│ │ ☑ Enable margin validation          │ │
│ │   Margin size: [2.5] cm ±[0.1] cm  │ │
│ │                                     │ │
│ │ ☑ Enable font validation            │ │
│ │   Font family: [Times New Roman ▼] │ │
│ │   Font size: [12] pt ±[0] pt       │ │
│ │                                     │ │
│ │ ☑ Enable line spacing validation    │ │
│ │   Line spacing: [1.5] ±[0.05]      │ │
│ │                                     │ │
│ │ ☑ Enable page format validation     │ │
│ │   Page format: [A4 ▼]              │ │
│ └─────────────────────────────────────┘ │
├─────────────────────────────────────────┤
│              [OK] [Cancel] [Apply]      │
└─────────────────────────────────────────┘
```

#### 3. **Report Export Dialog**
```
┌─────────────────────────────────────────┐
│ Export Validation Report          [X]   │
├─────────────────────────────────────────┤
│ Report Format:                          │
│ ○ PDF Report (Recommended)              │
│ ○ Excel Spreadsheet (.xlsx)             │
│ ○ HTML Report                           │
│ ○ Plain Text (.txt)                     │
│                                         │
│ Include in Report:                      │
│ ☑ Executive Summary                     │
│ ☑ Detailed Findings                     │
│ ☑ Issue Statistics                      │
│ ☑ Recommendations                       │
│ ☑ Technical Details                     │
│                                         │
│ Output Location:                        │
│ [C:\Reports\thesis_review.pdf] [📁]     │
│                                         │
│ ☑ Open report after export              │
├─────────────────────────────────────────┤
│              [Export] [Cancel]          │
└─────────────────────────────────────────┘
```

---

## 🔧 Technical Implementation Details

### Module 1 Validator Specifications

#### **MarginValidator Implementation**
```java
public class MarginValidator extends AbstractValidator {
    
    // Configuration
    private static final double REQUIRED_MARGIN_CM = 2.5;
    private static final double TOLERANCE_CM = 0.1;
    private static final double POINTS_PER_CM = 28.35;
    
    @Override
    public ValidationResult validate(ThesisDocument document) {
        ValidationResult result = new ValidationResult(getName());
        
        // Check each margin type
        checkMargin(document, MarginType.TOP, result);
        checkMargin(document, MarginType.BOTTOM, result);
        checkMargin(document, MarginType.LEFT, result);
        checkMargin(document, MarginType.RIGHT, result);
        
        return result;
    }
    
    private void checkMargin(ThesisDocument document, MarginType type, ValidationResult result) {
        double actualMarginPoints = getMarginValue(document, type);
        double actualMarginCm = actualMarginPoints / POINTS_PER_CM;
        double requiredMarginPoints = REQUIRED_MARGIN_CM * POINTS_PER_CM;
        
        if (Math.abs(actualMarginPoints - requiredMarginPoints) > TOLERANCE_CM * POINTS_PER_CM) {
            ValidationDetail detail = new ValidationDetail(
                String.format("%s margin", type.name()),
                String.format("%.1f cm", REQUIRED_MARGIN_CM),
                String.format("%.1f cm", actualMarginCm),
                Severity.MAJOR,
                String.format("Adjust %s margin to %.1f cm", type.name().toLowerCase(), REQUIRED_MARGIN_CM)
            );
            result.addDetail(detail);
        }
    }
}
```

#### **FontValidator Implementation**
```java
public class FontValidator extends AbstractValidator {
    
    private static final String REQUIRED_FONT_FAMILY = "Times New Roman";
    private static final int REQUIRED_FONT_SIZE = 12;
    
    @Override
    public ValidationResult validate(ThesisDocument document) {
        ValidationResult result = new ValidationResult(getName());
        
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        int paragraphNumber = 1;
        
        for (XWPFParagraph paragraph : paragraphs) {
            validateParagraphFont(paragraph, paragraphNumber, result);
            paragraphNumber++;
        }
        
        return result;
    }
    
    private void validateParagraphFont(XWPFParagraph paragraph, int paragraphNumber, ValidationResult result) {
        List<XWPFRun> runs = paragraph.getRuns();
        
        for (int runIndex = 0; runIndex < runs.size(); runIndex++) {
            XWPFRun run = runs.get(runIndex);
            
            // Check font family
            String fontFamily = run.getFontFamily();
            if (fontFamily != null && !REQUIRED_FONT_FAMILY.equals(fontFamily)) {
                ValidationDetail detail = new ValidationDetail(
                    String.format("Paragraph %d, Run %d", paragraphNumber, runIndex + 1),
                    REQUIRED_FONT_FAMILY,
                    fontFamily,
                    Severity.MAJOR,
                    String.format("Change font to %s", REQUIRED_FONT_FAMILY)
                );
                result.addDetail(detail);
            }
            
            // Check font size
            int fontSize = run.getFontSize();
            if (fontSize != -1 && fontSize != REQUIRED_FONT_SIZE) {
                ValidationDetail detail = new ValidationDetail(
                    String.format("Paragraph %d, Run %d", paragraphNumber, runIndex + 1),
                    String.format("%d pt", REQUIRED_FONT_SIZE),
                    String.format("%d pt", fontSize),
                    Severity.MAJOR,
                    String.format("Change font size to %d pt", REQUIRED_FONT_SIZE)
                );
                result.addDetail(detail);
            }
        }
    }
}
```

### Integration Architecture

```mermaid
graph TB
    A[TechCheckApplication] --> B[DocumentProcessor]
    B --> C[ValidationEngine]
    C --> D[Module1Coordinator]
    
    D --> E[MarginValidator]
    D --> F[FontValidator]
    D --> G[LineSpacingValidator]
    D --> H[PageFormatValidator]
    
    E --> I[ValidationResult]
    F --> I
    G --> I
    H --> I
    
    I --> J[ReportGenerator]
    J --> K[PDFReport]
    J --> L[ExcelReport]
    J --> M[HTMLReport]
    
    B --> N[ThesisDocument]
    N --> O[Apache POI XWPF]
```

---

## 📊 Validation Flow Sequence

```mermaid
sequenceDiagram
    participant UI as User Interface
    participant Engine as ValidationEngine
    participant M1 as Module1Coordinator
    participant MV as MarginValidator
    participant FV as FontValidator
    participant Report as ReportGenerator

    UI->>Engine: validateDocument(thesisDocument)
    Engine->>M1: execute(thesisDocument)
    
    M1->>MV: validate(thesisDocument)
    MV-->>M1: ValidationResult
    
    M1->>FV: validate(thesisDocument)
    FV-->>M1: ValidationResult
    
    M1-->>Engine: Module1Results
    Engine->>Report: generateReport(allResults)
    Report-->>Engine: GeneratedReport
    Engine-->>UI: ValidationComplete
```

---

## 🎯 Success Criteria for Module 1

### **Functional Requirements**
- ✅ **Margin Validation**: Accurately detect margins outside 2.5cm ±0.1cm tolerance
- ✅ **Font Validation**: Identify non-Times New Roman fonts and incorrect sizes
- ✅ **Line Spacing**: Verify 1.5 line spacing with ±0.05 tolerance
- ✅ **Page Format**: Confirm A4 page size and portrait orientation

### **Technical Requirements**
- ✅ **Performance**: Process 100-page document in <30 seconds
- ✅ **Accuracy**: 99%+ accuracy in identifying formatting violations
- ✅ **Error Handling**: Graceful handling of corrupted or protected documents
- ✅ **Reporting**: Clear, actionable feedback with specific locations

### **User Experience Requirements**
- ✅ **Intuitive Interface**: Technical staff can use without training
- ✅ **Progress Feedback**: Real-time validation progress updates
- ✅ **Detailed Results**: Specific locations and recommendations for fixes
- ✅ **Export Options**: Multiple report formats for different stakeholders

---

## 🚀 Next Steps

After completing this detailed design, the next phases will be:

1. **Implementation Preparation**
   - Set up Maven project structure
   - Configure Apache POI dependencies
   - Create base classes and interfaces

2. **Module 1 Development**
   - Implement MarginValidator (highest priority)
   - Develop FontValidator 
   - Create LineSpacingValidator
   - Build PageFormatValidator

3. **Integration & Testing**
   - Unit tests for each validator
   - Integration testing with sample documents
   - Performance optimization

4. **User Interface Implementation**
   - JavaFX UI components
   - Event handling and data binding
   - Report generation and export

This detailed design provides the roadmap for implementing a robust, maintainable, and user-friendly thesis validation system for FDV Ljubljana's technical service staff.