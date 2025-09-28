package com.fdv.techcheck.core.document;

/**
 * Exception thrown when document loading or processing operations fail.
 * This is a checked exception that forces callers to handle document processing failures explicitly.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class DocumentProcessingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private final String filePath;
    private final ProcessingStage stage;
    
    /**
     * Enumeration of document processing stages where errors can occur.
     */
    public enum ProcessingStage {
        FILE_VALIDATION("File validation"),
        DOCUMENT_LOADING("Document loading"),
        METADATA_EXTRACTION("Metadata extraction"),
        CONTENT_ANALYSIS("Content analysis"),
        STRUCTURE_PARSING("Structure parsing");
        
        private final String description;
        
        ProcessingStage(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Constructs a new document processing exception with the specified detail message.
     * 
     * @param message The detail message
     */
    public DocumentProcessingException(String message) {
        super(message);
        this.filePath = null;
        this.stage = null;
    }
    
    /**
     * Constructs a new document processing exception with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The cause of this exception
     */
    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.filePath = null;
        this.stage = null;
    }
    
    /**
     * Constructs a new document processing exception with file context.
     * 
     * @param filePath Path to the file being processed
     * @param message The detail message
     */
    public DocumentProcessingException(String filePath, String message) {
        super(message);
        this.filePath = filePath;
        this.stage = null;
    }
    
    /**
     * Constructs a new document processing exception with full context.
     * 
     * @param filePath Path to the file being processed
     * @param stage Processing stage where error occurred
     * @param message The detail message
     * @param cause The cause of this exception
     */
    public DocumentProcessingException(String filePath, ProcessingStage stage, String message, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
        this.stage = stage;
    }
    
    /**
     * Gets the path of the file that was being processed when this exception occurred.
     * 
     * @return File path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the processing stage where this exception occurred.
     * 
     * @return Processing stage, or null if not specified
     */
    public ProcessingStage getStage() {
        return stage;
    }
    
    /**
     * Checks if this exception occurred during a specific processing stage.
     * 
     * @param stage The stage to check
     * @return true if this exception occurred during the specified stage
     */
    public boolean isStage(ProcessingStage stage) {
        return this.stage == stage;
    }
    
    /**
     * Gets a user-friendly error message that can be displayed in the UI.
     * 
     * @return Formatted error message
     */
    public String getUserFriendlyMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (stage != null) {
            sb.append(stage.getDescription()).append(" failed");
        } else {
            sb.append("Document processing failed");
        }
        
        if (filePath != null) {
            sb.append(" for file: ").append(filePath);
        }
        
        String message = getMessage();
        if (message != null && !message.isEmpty()) {
            sb.append(". ").append(message);
        }
        
        return sb.toString();
    }
    
    /**
     * Gets suggestions for resolving this type of error.
     * 
     * @return Array of suggestion strings
     */
    public String[] getSuggestions() {
        if (stage != null) {
            return switch (stage) {
                case FILE_VALIDATION -> new String[] {
                    "Verify the file exists and is not corrupted",
                    "Check file permissions and ensure it's readable",
                    "Make sure the file has a .docx extension",
                    "Try opening the file in Microsoft Word to verify it's valid"
                };
                case DOCUMENT_LOADING -> new String[] {
                    "Check if the file is password protected",
                    "Verify the file is not in use by another application", 
                    "Try re-saving the document in Microsoft Word",
                    "Check available system memory"
                };
                case METADATA_EXTRACTION -> new String[] {
                    "The document may have corrupted metadata",
                    "Try re-saving the document to fix metadata issues",
                    "Check if the document was created with a compatible version of Word"
                };
                case CONTENT_ANALYSIS -> new String[] {
                    "The document may contain unsupported formatting",
                    "Check for embedded objects or complex elements",
                    "Try simplifying the document structure"
                };
                case STRUCTURE_PARSING -> new String[] {
                    "The document structure may be corrupted",
                    "Try re-creating the document with standard formatting",
                    "Check for unusual page layouts or complex sections"
                };
            };
        }
        
        return new String[] {
            "Verify the file is a valid DOCX document",
            "Try re-saving the document in Microsoft Word",
            "Check system resources and try again",
            "Contact technical support if the problem persists"
        };
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DocumentProcessingException");
        
        if (filePath != null || stage != null) {
            sb.append("[");
            if (stage != null) {
                sb.append("stage=").append(stage);
                if (filePath != null) {
                    sb.append(", ");
                }
            }
            if (filePath != null) {
                sb.append("file=").append(filePath);
            }
            sb.append("]");
        }
        
        sb.append(": ").append(getMessage());
        
        return sb.toString();
    }
}