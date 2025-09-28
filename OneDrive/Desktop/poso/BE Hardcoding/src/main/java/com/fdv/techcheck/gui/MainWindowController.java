package com.fdv.techcheck.gui;

import com.fdv.techcheck.core.document.DocumentMetadata;
import com.fdv.techcheck.core.document.DocumentProcessor;
import com.fdv.techcheck.core.document.ThesisDocument;
import com.fdv.techcheck.core.validation.IValidator;
import com.fdv.techcheck.core.validation.ValidationResult;
import com.fdv.techcheck.core.validation.ValidationDetail;
import com.fdv.techcheck.core.validation.ValidationStatus;
import com.fdv.techcheck.modules.layout.MarginValidator;
import com.fdv.techcheck.modules.layout.FontValidator;
import com.fdv.techcheck.modules.layout.LineSpacingValidator;
import com.fdv.techcheck.modules.layout.PageFormatValidator;
import com.fdv.techcheck.reports.PdfReportGenerator;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the main TechCheck GUI window.
 * Handles user interactions and coordinates validation process.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class MainWindowController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);
    
    private Stage primaryStage;
    private File selectedDocument;
    private ThesisDocument loadedDocument;
    private boolean validationInProgress = false;
    
    // Validation results storage
    private DocumentMetadata currentMetadata;
    private List<ValidationResult> currentResults = new ArrayList<>();
    private Map<String, ValidationResult> currentResultsMap = new HashMap<>();
    
    // FXML injected components
    @FXML private TextField documentPathField;
    @FXML private Button browseButton;
    @FXML private Button startValidationButton;
    @FXML private Button exportReportButton;
    @FXML private ProgressBar validationProgressBar;
    @FXML private Label progressLabel;
    @FXML private VBox resultsContainer;
    @FXML private ScrollPane resultsScrollPane;
    @FXML private Label documentInfoLabel;
    @FXML private TextArea logTextArea;
    
    // Validation modules
    private final List<IValidator> validators = Arrays.asList(
        new MarginValidator(),
        new FontValidator(), 
        new LineSpacingValidator(),
        new PageFormatValidator()
    );
    
    /**
     * Called after FXML loading to initialize the controller.
     */
    public void initialize() {
        logger.debug("Initializing MainWindowController");
        
        // Set initial state
        updateUIState();
        
        // Configure components
        validationProgressBar.setProgress(0.0);
        progressLabel.setText("Ready to validate documents");
        exportReportButton.setDisable(true);
        
        // Set up document path field
        documentPathField.setEditable(false);
        documentPathField.setPromptText("No document selected");
        
        // Configure results area
        resultsScrollPane.setFitToWidth(true);
        resultsContainer.setSpacing(10);
        
        // Configure log area
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        logTextArea.appendText("TechCheck GUI initialized - Ready for document validation\\n");
        
        logger.debug("MainWindowController initialization completed");
    }
    
    /**
     * Sets the primary stage reference.
     * 
     * @param primaryStage The primary application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Handles the browse button click to select a document.
     */
    @FXML
    private void handleBrowseDocument() {
        logger.debug("Browse document button clicked");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Thesis Document");
        
        // Set file filters
        FileChooser.ExtensionFilter docxFilter = new FileChooser.ExtensionFilter(
            "Microsoft Word Documents (*.docx)", "*.docx");
        fileChooser.getExtensionFilters().add(docxFilter);
        
        // Set initial directory
        File initialDir = new File(System.getProperty("user.home", "."));
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        
        if (selectedFile != null) {
            selectedDocument = selectedFile;
            documentPathField.setText(selectedFile.getAbsolutePath());
            
            logMessage("Document selected: " + selectedFile.getName());
            
            // Try to load document metadata
            loadDocumentMetadata();
            updateUIState();
        }
    }
    
    /**
     * Loads document metadata for display.
     */
    private void loadDocumentMetadata() {
        if (selectedDocument == null) return;
        
        try {
            loadedDocument = DocumentProcessor.loadDocument(selectedDocument.toPath());
            
            String info = String.format("Document: %s | Pages: %d | Words: %d", 
                selectedDocument.getName(),
                loadedDocument.getMetadata().getPageCount(),
                loadedDocument.getMetadata().getWordCount());
            
            documentInfoLabel.setText(info);
            logMessage("Document loaded successfully - " + info);
            
        } catch (Exception e) {
            logger.error("Failed to load document metadata", e);
            documentInfoLabel.setText("Error loading document: " + e.getMessage());
            logMessage("ERROR: Failed to load document - " + e.getMessage());
            loadedDocument = null;
        }
    }
    
    /**
     * Handles the start validation button click.
     */
    @FXML
    private void handleStartValidation() {
        if (selectedDocument == null || loadedDocument == null) {
            showAlert("No Document", "Please select a document first.", Alert.AlertType.WARNING);
            return;
        }
        
        if (validationInProgress) {
            showAlert("Validation In Progress", "Please wait for current validation to complete.", Alert.AlertType.INFORMATION);
            return;
        }
        
        logger.info("Starting validation for document: {}", selectedDocument.getName());
        logMessage("Starting validation process...");
        
        // Clear previous results
        resultsContainer.getChildren().clear();
        currentResults.clear();
        currentResultsMap.clear();
        currentMetadata = loadedDocument.getMetadata();
        
        // Start validation in background thread
        startValidationTask();
    }
    
    /**
     * Starts the validation task in a background thread.
     */
    private void startValidationTask() {
        validationInProgress = true;
        updateUIState();
        
        Task<Void> validationTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                
                for (int i = 0; i < validators.size(); i++) {
                    IValidator validator = validators.get(i);
                    
                    // Update progress
                    final int currentStep = i;
                    final String validatorName = validator.getClass().getSimpleName().replace("Validator", "");
                    
                    Platform.runLater(() -> {
                        double progress = (double) currentStep / validators.size();
                        validationProgressBar.setProgress(progress);
                        progressLabel.setText("Validating: " + validatorName + "...");
                        logMessage("Running " + validatorName + " validation...");
                    });
                    
                    try {
                        // Perform validation
                        ValidationResult result = validator.validate(loadedDocument);
                        
                        // Store result for export
                        currentResults.add(result);
                        currentResultsMap.put(validatorName, result);
                        
                        // Update UI with result
                        Platform.runLater(() -> {
                            addValidationResult(validatorName, result);
                        });
                        
                        // Small delay to show progress
                        Thread.sleep(200);
                        
                    } catch (Exception e) {
                        logger.error("Validation failed for {}", validatorName, e);
                        Platform.runLater(() -> {
                            logMessage("ERROR in " + validatorName + ": " + e.getMessage());
                        });
                    }
                }
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    validationProgressBar.setProgress(1.0);
                    progressLabel.setText("Validation completed successfully");
                    logMessage("Validation completed successfully");
                    validationInProgress = false;
                    updateUIState();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    validationProgressBar.setProgress(0.0);
                    progressLabel.setText("Validation failed");
                    logMessage("ERROR: Validation process failed");
                    validationInProgress = false;
                    updateUIState();
                });
            }
        };
        
        Thread validationThread = new Thread(validationTask);
        validationThread.setDaemon(true);
        validationThread.start();
    }
    
    /**
     * Adds a validation result to the results display.
     * 
     * @param validatorName Name of the validator
     * @param result Validation result
     */
    private void addValidationResult(String validatorName, ValidationResult result) {
        VBox resultBox = new VBox(5);
        resultBox.getStyleClass().add("validation-result");
        
        // Create header
        Label headerLabel = new Label(validatorName + " Validation");
        headerLabel.getStyleClass().add("validation-header");
        
        // Create status label
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("validation-status");
        
        String statusText;
        String statusClass;
        switch (result.getStatus()) {
            case PASS:
                statusText = "✓ PASSED";
                statusClass = "status-pass";
                break;
            case WARNING:
                statusText = "⚠ WARNING";
                statusClass = "status-warning";
                break;
            case FAIL:
                statusText = "✗ FAILED";
                statusClass = "status-fail";
                break;
            case ERROR:
                statusText = "! ERROR";
                statusClass = "status-error";
                break;
            default:
                statusText = "? UNKNOWN";
                statusClass = "status-unknown";
        }
        
        statusLabel.setText(statusText);
        statusLabel.getStyleClass().add(statusClass);
        
        resultBox.getChildren().addAll(headerLabel, statusLabel);
        
        // Add details if there are issues
        if (result.hasIssues() && !result.getDetails().isEmpty()) {
            VBox detailsBox = new VBox(3);
            detailsBox.getStyleClass().add("validation-details");
            
            for (ValidationDetail detail : result.getDetails()) {
                Label detailLabel = new Label(String.format(
                    "%s: Expected %s, Found %s", 
                    detail.getLocation(),
                    detail.getExpected(),
                    detail.getActual()
                ));
                detailLabel.getStyleClass().add("validation-detail");
                detailsBox.getChildren().add(detailLabel);
            }
            
            resultBox.getChildren().add(detailsBox);
        }
        
        resultsContainer.getChildren().add(resultBox);
    }
    
    /**
     * Handles the export report button click.
     */
    @FXML
    private void handleExportReport() {
        if (currentResultsMap == null || currentResultsMap.isEmpty()) {
            showAlert("Export Report", "No validation results to export. Please run validation first.", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        // Set default filename with timestamp
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        );
        fileChooser.setInitialFileName("TechCheck_Report_" + timestamp + ".pdf");
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            Task<Void> exportTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage("Generating PDF report...");
                    
                    PdfReportGenerator generator = new PdfReportGenerator();
                    generator.generateReport(file, loadedDocument, currentResultsMap);
                    
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        showAlert("Export Complete",
                            "PDF report has been saved successfully to:\n" + file.getAbsolutePath(),
                            Alert.AlertType.INFORMATION);
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showAlert("Export Failed",
                            "Failed to generate PDF report: " + getException().getMessage(),
                            Alert.AlertType.ERROR);
                    });
                }
            };
            
            // Show progress during export
            validationProgressBar.progressProperty().bind(exportTask.progressProperty());
            progressLabel.textProperty().bind(exportTask.messageProperty());
            
            Thread exportThread = new Thread(exportTask);
            exportThread.setDaemon(true);
            exportThread.start();
        }
    }
    
    /**
     * Updates the UI state based on current conditions.
     */
    private void updateUIState() {
        boolean hasDocument = selectedDocument != null && loadedDocument != null;
        boolean canValidate = hasDocument && !validationInProgress;
        boolean hasResults = !resultsContainer.getChildren().isEmpty();
        
        startValidationButton.setDisable(!canValidate);
        exportReportButton.setDisable(!hasResults || validationInProgress);
        browseButton.setDisable(validationInProgress);
    }
    
    /**
     * Adds a timestamped message to the log area.
     * 
     * @param message Message to log
     */
    private void logMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = String.format("[%s] %s%n", timestamp, message);
        logTextArea.appendText(logEntry);
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param title Dialog title
     * @param message Dialog message
     * @param alertType Type of alert
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Handles the application exit action.
     */
    @FXML
    private void handleExit() {
        if (validationInProgress) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit Application");
            alert.setHeaderText("Validation in progress");
            alert.setContentText("A validation is currently running. Are you sure you want to exit?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() != ButtonType.OK) {
                return;
            }
        }
        
        logger.info("Application exit requested");
        Platform.exit();
    }
    
    /**
     * Handles the about dialog action.
     */
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About TechCheck");
        alert.setHeaderText("TechCheck - Thesis Validation System");
        alert.setContentText(
            "Version: 1.0.0\n" +
            "Faculty of Social Sciences\n" +
            "University of Ljubljana\n\n" +
            "Automated thesis technical review system\n" +
            "for academic document compliance validation.\n\n" +
            "For support: technical@fdv.uni-lj.si"
        );
        alert.showAndWait();
    }
}