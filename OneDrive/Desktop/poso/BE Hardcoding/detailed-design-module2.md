# TechCheck Module 2: Content Structure & Typography
## Detailed Design Document

---

## 📋 Module Overview

### Purpose
Module 2 validates the content structure and typography elements of thesis documents, ensuring proper heading hierarchy, paragraph formatting, and list structure compliance with FDV Ljubljana academic standards.

### Scope
- **Heading Validation**: Numbering hierarchy, font sizes, structure
- **Paragraph Validation**: Length, formatting, alignment, spacing
- **List Validation**: Bullet points, numbered lists, nesting levels

---

## 🏗️ Architecture Design

### Module Structure
```
src/main/java/com/fdv/techcheck/modules/content/
├── HeadingValidator.java           # H1-H6 validation
├── ParagraphValidator.java         # Paragraph formatting
├── ListValidator.java              # List structure validation
├── ContentModule.java              # Module coordinator
└── models/
    ├── HeadingInfo.java           # Heading analysis data
    ├── ParagraphInfo.java         # Paragraph analysis data
    └── ListInfo.java              # List analysis data
```

### Integration Points
- Extends existing [`AbstractDocumentValidator`](src/main/java/com/fdv/techcheck/core/validation/AbstractDocumentValidator.java)
- Integrates with [`ThesisDocument`](src/main/java/com/fdv/techcheck/core/document/ThesisDocument.java)
- Uses [`ValidationResult`](src/main/java/com/fdv/techcheck/core/validation/ValidationResult.java) framework
- Reports through [`PdfReportGenerator`](src/main/java/com/fdv/techcheck/reports/PdfReportGenerator.java)

---

## 🔤 HeadingValidator Design

### Validation Rules
```java
public class HeadingValidator extends AbstractDocumentValidator {
    
    // FDV Ljubljana Heading Requirements
    private static final Map<Integer, Integer> HEADING_FONT_SIZES = Map.of(
        1, 16,  // H1: 16pt
        2, 14,  // H2: 14pt  
        3, 12,  // H3: 12pt
        4, 12,  // H4: 12pt
        5, 10,  // H5: 10pt
        6, 10   // H6: 10pt
    );
    
    private static final String REQUIRED_FONT = "Times New Roman";
    private static final String NUMBERING_PATTERN = "^\\d+(\\.\\d+)*\\.?\\s+.*";
}
```

### Validation Checks
1. **Heading Hierarchy Validation**
   - Proper nesting (H1 → H2 → H3, no skipping)
   - Sequential numbering (1. 1.1 1.1.1)
   - No orphaned headings

2. **Font Size Validation**
   - H1 = 16pt, H2 = 14pt, H3-H4 = 12pt, H5-H6 = 10pt
   - Consistent font family (Times New Roman)
   - Bold formatting allowed

3. **Numbering Validation**
   - Decimal numbering system (1. 1.1 1.1.1)
   - Sequential progression
   - Proper spacing after numbers

### Implementation Details
```java
@Override
protected ValidationResult performValidation(ThesisDocument document) {
    List<ValidationDetail> issues = new ArrayList<>();
    List<HeadingInfo> headings = extractHeadings(document);
    
    // Validate hierarchy
    issues.addAll(validateHierarchy(headings));
    
    // Validate font sizes
    issues.addAll(validateFontSizes(headings));
    
    // Validate numbering
    issues.addAll(validateNumbering(headings));
    
    return issues.isEmpty() 
        ? ValidationResult.pass(getValidatorName())
        : ValidationResult.fail(getValidatorName(), issues);
}

private List<HeadingInfo> extractHeadings(ThesisDocument document) {
    List<HeadingInfo> headings = new ArrayList<>();
    
    for (XWPFParagraph paragraph : document.getParagraphs()) {
        String styleName = paragraph.getStyle();
        if (styleName != null && styleName.matches("Heading\\d+")) {
            int level = Integer.parseInt(styleName.replaceAll("\\D+", ""));
            HeadingInfo heading = HeadingInfo.builder()
                .level(level)
                .text(paragraph.getText())
                .fontSize(getFontSize(paragraph))
                .fontFamily(getFontFamily(paragraph))
                .isBold(isBold(paragraph))
                .paragraphIndex(document.getParagraphs().indexOf(paragraph))
                .build();
            headings.add(heading);
        }
    }
    
    return headings;
}
```

---

## 📝 ParagraphValidator Design

### Validation Rules
```java
public class ParagraphValidator extends AbstractDocumentValidator {
    
    // FDV Ljubljana Paragraph Requirements
    private static final int MIN_PARAGRAPH_LENGTH = 50;    // characters
    private static final int MAX_PARAGRAPH_LENGTH = 2000;  // characters
    private static final double EXPECTED_LINE_SPACING = 1.5;
    private static final String EXPECTED_ALIGNMENT = "LEFT";
    private static final boolean REQUIRE_INDENTATION = false;
}
```

### Validation Checks
1. **Length Validation**
   - Minimum 50 characters per paragraph
   - Maximum 2000 characters per paragraph
   - Exclude headings and lists from count

2. **Formatting Validation**
   - Left alignment (justified allowed)
   - 1.5 line spacing consistency
   - No excessive indentation
   - Proper spacing between paragraphs

3. **Structure Validation**
   - No single-sentence paragraphs (academic writing)
   - No excessive long paragraphs
   - Consistent formatting throughout

### Implementation Details
```java
@Override
protected ValidationResult performValidation(ThesisDocument document) {
    List<ValidationDetail> issues = new ArrayList<>();
    List<ParagraphInfo> paragraphs = extractParagraphs(document);
    
    for (ParagraphInfo para : paragraphs) {
        // Skip headings and lists
        if (para.isHeading() || para.isList()) {
            continue;
        }
        
        // Validate length
        if (para.getCharacterCount() < MIN_PARAGRAPH_LENGTH) {
            issues.add(ValidationDetail.builder()
                .location("Paragraph " + para.getIndex())
                .expected("Minimum " + MIN_PARAGRAPH_LENGTH + " characters")
                .actual(para.getCharacterCount() + " characters")
                .severity(ValidationSeverity.MINOR)
                .recommendation("Expand paragraph content or merge with adjacent text")
                .build());
        }
        
        // Validate spacing
        if (!isWithinTolerance(para.getLineSpacing(), EXPECTED_LINE_SPACING)) {
            issues.add(ValidationDetail.builder()
                .location("Paragraph " + para.getIndex())
                .expected("Line spacing: " + EXPECTED_LINE_SPACING)
                .actual("Line spacing: " + para.getLineSpacing())
                .severity(ValidationSeverity.MAJOR)
                .recommendation("Set line spacing to 1.5")
                .build());
        }
    }
    
    return issues.isEmpty() 
        ? ValidationResult.pass(getValidatorName())
        : ValidationResult.fail(getValidatorName(), issues);
}
```

---

## 📋 ListValidator Design

### Validation Rules
```java
public class ListValidator extends AbstractDocumentValidator {
    
    // FDV Ljubljana List Requirements
    private static final String BULLET_STYLE = "•";
    private static final String NUMBERED_STYLE_PATTERN = "^\\d+\\.[a-z]\\.[i-v]+\\.";
    private static final int MAX_NESTING_LEVEL = 3;
    private static final double EXPECTED_INDENTATION = 1.27; // cm per level
}
```

### Validation Checks
1. **List Structure Validation**
   - Consistent bullet style (• for unordered)
   - Proper numbering (1. a. i. for ordered)
   - Maximum 3 nesting levels

2. **Formatting Validation**
   - Proper indentation (1.27cm per level)
   - Consistent spacing
   - Aligned list items

3. **Content Validation**
   - No empty list items
   - Consistent punctuation
   - Parallel structure in content

### Implementation Details
```java
@Override
protected ValidationResult performValidation(ThesisDocument document) {
    List<ValidationDetail> issues = new ArrayList<>();
    List<ListInfo> lists = extractLists(document);
    
    for (ListInfo listInfo : lists) {
        // Validate nesting depth
        if (listInfo.getMaxNestingLevel() > MAX_NESTING_LEVEL) {
            issues.add(ValidationDetail.builder()
                .location("List starting at paragraph " + listInfo.getStartIndex())
                .expected("Maximum " + MAX_NESTING_LEVEL + " nesting levels")
                .actual(listInfo.getMaxNestingLevel() + " nesting levels")
                .severity(ValidationSeverity.MAJOR)
                .recommendation("Reduce list nesting to maximum 3 levels")
                .build());
        }
        
        // Validate bullet consistency
        if (listInfo.getType() == ListType.BULLET) {
            for (String bullet : listInfo.getBulletStyles()) {
                if (!BULLET_STYLE.equals(bullet)) {
                    issues.add(ValidationDetail.builder()
                        .location("List item in paragraph " + listInfo.getStartIndex())
                        .expected("Bullet style: " + BULLET_STYLE)
                        .actual("Bullet style: " + bullet)
                        .severity(ValidationSeverity.MINOR)
                        .recommendation("Use consistent bullet style: " + BULLET_STYLE)
                        .build());
                }
            }
        }
    }
    
    return issues.isEmpty() 
        ? ValidationResult.pass(getValidatorName())
        : ValidationResult.fail(getValidatorName(), issues);
}
```

---

## 📊 Data Models

### HeadingInfo Class
```java
public class HeadingInfo {
    private final int level;                    // 1-6
    private final String text;                  // Heading text
    private final int fontSize;                 // Font size in points
    private final String fontFamily;            // Font family name
    private final boolean isBold;               // Bold formatting
    private final int paragraphIndex;           // Position in document
    private final String numberingText;         // Extracted numbering (1.1.1)
    
    // Builder pattern implementation
    public static class Builder {
        // Builder implementation...
    }
}
```

### ParagraphInfo Class
```java
public class ParagraphInfo {
    private final int index;                    // Paragraph position
    private final String text;                  // Paragraph content
    private final int characterCount;           // Character count
    private final int wordCount;                // Word count
    private final double lineSpacing;           // Line spacing value
    private final String alignment;             // Text alignment
    private final boolean isHeading;            // Is heading paragraph
    private final boolean isList;               // Is list item
    private final double indentation;           // Left indentation in cm
    
    // Builder pattern implementation
    public static class Builder {
        // Builder implementation...
    }
}
```

### ListInfo Class
```java
public class ListInfo {
    private final ListType type;                // BULLET, NUMBERED, MIXED
    private final int startIndex;               // Starting paragraph index
    private final int endIndex;                 // Ending paragraph index
    private final int maxNestingLevel;          // Maximum nesting depth
    private final List<String> bulletStyles;   // Used bullet characters
    private final List<String> numberingStyles; // Used numbering patterns
    private final boolean hasConsistentIndentation; // Indentation consistency
    
    public enum ListType {
        BULLET, NUMBERED, MIXED, UNKNOWN
    }
    
    // Builder pattern implementation
    public static class Builder {
        // Builder implementation...
    }
}
```

---

## 🔧 ContentModule Integration

### Module Coordinator
```java
public class ContentModule {
    
    private final HeadingValidator headingValidator;
    private final ParagraphValidator paragraphValidator;
    private final ListValidator listValidator;
    
    public ContentModule() {
        this.headingValidator = new HeadingValidator();
        this.paragraphValidator = new ParagraphValidator();
        this.listValidator = new ListValidator();
    }
    
    public List<ValidationResult> validateContent(ThesisDocument document) {
        List<ValidationResult> results = new ArrayList<>();
        
        results.add(headingValidator.validate(document));
        results.add(paragraphValidator.validate(document));
        results.add(listValidator.validate(document));
        
        return results;
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests
```java
// HeadingValidatorTest.java
@Test
void testHeadingHierarchy() {
    // Test proper H1 → H2 → H3 progression
}

@Test
void testHeadingFontSizes() {
    // Test H1=16pt, H2=14pt, etc.
}

@Test
void testHeadingNumbering() {
    // Test 1. 1.1 1.1.1 numbering pattern
}

// ParagraphValidatorTest.java
@Test
void testParagraphLength() {
    // Test min/max character limits
}

@Test
void testLineSpacing() {
    // Test 1.5 line spacing requirement
}

// ListValidatorTest.java
@Test
void testListNesting() {
    // Test maximum 3 nesting levels
}

@Test
void testBulletConsistency() {
    // Test consistent bullet style usage
}
```

### Integration Tests
```java
// Module2IntegrationTest.java
@Test
void testCompleteContentValidation() {
    // Test all content validators together
}

@Test
void testRealThesisDocument() {
    // Test with actual thesis document
}
```

---

## 📈 Performance Considerations

### Optimization Strategies
1. **Lazy Loading**: Extract document elements only when needed
2. **Caching**: Cache expensive operations like font analysis
3. **Parallel Processing**: Run validators concurrently where possible
4. **Memory Management**: Process large documents in chunks

### Memory Usage
- Estimated 50-100MB for 500-page document
- Efficient string handling for text analysis
- Cleanup temporary objects after validation

---

## 🔗 Integration Points

### GUI Integration
- Add Module 2 progress indicators to [`MainWindowController`](src/main/java/com/fdv/techcheck/gui/MainWindowController.java)
- Display content validation results in results panel
- Show detailed heading/paragraph/list analysis

### Reporting Integration
- Extend [`PdfReportGenerator`](src/main/java/com/fdv/techcheck/reports/PdfReportGenerator.java) with content validation section
- Include heading structure overview
- Display paragraph and list statistics

### Configuration Integration
- Add content validation rules to configuration system
- Allow customization of heading font sizes
- Configurable paragraph length limits

---

This design provides a solid foundation for implementing Module 2: Content Structure & Typography, following the same proven patterns established in Module 1 while addressing the specific requirements for academic content validation.