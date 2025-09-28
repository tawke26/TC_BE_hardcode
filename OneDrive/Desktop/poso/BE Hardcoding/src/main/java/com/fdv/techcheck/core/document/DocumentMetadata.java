package com.fdv.techcheck.core.document;

import java.time.Instant;
import java.util.Objects;

/**
 * Contains metadata information about a thesis document.
 * Includes statistics and properties extracted during document loading.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class DocumentMetadata {
    
    private final String title;
    private final String author;
    private final String subject;
    private final Instant createdDate;
    private final Instant modifiedDate;
    private final int pageCount;
    private final int wordCount;
    private final int characterCount;
    private final int paragraphCount;
    private final long fileSizeBytes;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private DocumentMetadata(Builder builder) {
        this.title = builder.title;
        this.author = builder.author;
        this.subject = builder.subject;
        this.createdDate = builder.createdDate;
        this.modifiedDate = builder.modifiedDate;
        this.pageCount = builder.pageCount;
        this.wordCount = builder.wordCount;
        this.characterCount = builder.characterCount;
        this.paragraphCount = builder.paragraphCount;
        this.fileSizeBytes = builder.fileSizeBytes;
    }
    
    /**
     * Creates a new builder for constructing DocumentMetadata instances.
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public Instant getCreatedDate() {
        return createdDate;
    }
    
    public Instant getModifiedDate() {
        return modifiedDate;
    }
    
    public int getPageCount() {
        return pageCount;
    }
    
    public int getWordCount() {
        return wordCount;
    }
    
    public int getCharacterCount() {
        return characterCount;
    }
    
    public int getParagraphCount() {
        return paragraphCount;
    }
    
    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    /**
     * Gets the file size in a human-readable format.
     * 
     * @return Formatted file size (e.g., "2.5 MB")
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Estimates reading time based on word count.
     * 
     * @return Estimated reading time in minutes
     */
    public int getEstimatedReadingTimeMinutes() {
        // Average reading speed: 200-250 words per minute
        return Math.max(1, wordCount / 225);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentMetadata that = (DocumentMetadata) o;
        return pageCount == that.pageCount &&
               wordCount == that.wordCount &&
               characterCount == that.characterCount &&
               paragraphCount == that.paragraphCount &&
               fileSizeBytes == that.fileSizeBytes &&
               Objects.equals(title, that.title) &&
               Objects.equals(author, that.author) &&
               Objects.equals(subject, that.subject);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, author, subject, pageCount, wordCount, 
                          characterCount, paragraphCount, fileSizeBytes);
    }
    
    @Override
    public String toString() {
        return String.format("DocumentMetadata{title='%s', author='%s', pages=%d, words=%d, size=%s}", 
                           title, author, pageCount, wordCount, getFormattedFileSize());
    }
    
    /**
     * Builder class for constructing DocumentMetadata instances.
     */
    public static class Builder {
        private String title;
        private String author;
        private String subject;
        private Instant createdDate;
        private Instant modifiedDate;
        private int pageCount;
        private int wordCount;
        private int characterCount;
        private int paragraphCount;
        private long fileSizeBytes;
        
        private Builder() {}
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder createdDate(Instant createdDate) {
            this.createdDate = createdDate;
            return this;
        }
        
        public Builder modifiedDate(Instant modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }
        
        public Builder pageCount(int pageCount) {
            this.pageCount = pageCount;
            return this;
        }
        
        public Builder wordCount(int wordCount) {
            this.wordCount = wordCount;
            return this;
        }
        
        public Builder characterCount(int characterCount) {
            this.characterCount = characterCount;
            return this;
        }
        
        public Builder paragraphCount(int paragraphCount) {
            this.paragraphCount = paragraphCount;
            return this;
        }
        
        public Builder fileSizeBytes(long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
            return this;
        }
        
        public DocumentMetadata build() {
            return new DocumentMetadata(this);
        }
    }
}