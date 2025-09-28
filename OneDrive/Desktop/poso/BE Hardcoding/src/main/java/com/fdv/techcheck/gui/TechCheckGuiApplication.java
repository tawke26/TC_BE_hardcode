package com.fdv.techcheck.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Main JavaFX application for TechCheck GUI.
 * Provides user-friendly interface for non-technical staff at FDV Ljubljana.
 * 
 * @author TechCheck Development Team
 * @since 1.0.0
 */
public class TechCheckGuiApplication extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(TechCheckGuiApplication.class);
    
    private static final String APPLICATION_TITLE = "TechCheck - Thesis Validation System";
    private static final String APPLICATION_VERSION = "1.0.0";
    private static final String MAIN_WINDOW_FXML = "/com/fdv/techcheck/gui/MainWindow.fxml";
    private static final String APPLICATION_CSS = "/com/fdv/techcheck/gui/styles.css";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting TechCheck GUI Application v{}", APPLICATION_VERSION);
            
            // Load main window FXML
            FXMLLoader loader = new FXMLLoader();
            URL fxmlResource = getClass().getResource(MAIN_WINDOW_FXML);
            
            if (fxmlResource == null) {
                logger.error("Cannot find FXML resource: {}", MAIN_WINDOW_FXML);
                showErrorAndExit("Application startup failed", 
                               "Cannot load user interface. Please check installation.");
                return;
            }
            
            loader.setLocation(fxmlResource);
            Scene scene = new Scene(loader.load());
            
            // Load CSS stylesheet
            URL cssResource = getClass().getResource(APPLICATION_CSS);
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
                logger.debug("Loaded CSS stylesheet: {}", APPLICATION_CSS);
            } else {
                logger.warn("CSS stylesheet not found: {}", APPLICATION_CSS);
            }
            
            // Configure primary stage
            primaryStage.setTitle(APPLICATION_TITLE + " v" + APPLICATION_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            
            // Set application icon
            try {
                URL iconResource = getClass().getResource("/images/techcheck-icon.png");
                if (iconResource != null) {
                    primaryStage.getIcons().add(new Image(iconResource.toExternalForm()));
                }
            } catch (Exception e) {
                logger.warn("Could not load application icon: {}", e.getMessage());
            }
            
            // Get controller and initialize
            MainWindowController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                controller.initialize();
                logger.debug("MainWindow controller initialized");
            }
            
            // Show the window
            primaryStage.show();
            logger.info("TechCheck GUI Application started successfully");
            
        } catch (IOException e) {
            logger.error("Failed to load main window", e);
            showErrorAndExit("Application Startup Error", 
                           "Failed to initialize user interface: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during application startup", e);
            showErrorAndExit("Unexpected Error", 
                           "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() throws Exception {
        logger.info("TechCheck GUI Application shutting down");
        super.stop();
    }
    
    /**
     * Shows an error dialog and exits the application.
     * 
     * @param title Error dialog title
     * @param message Error message to display
     */
    private void showErrorAndExit(String title, String message) {
        logger.error("Critical error - showing error dialog and exiting: {}", message);
        
        // Since we can't use Alert easily without JavaFX being fully initialized,
        // we'll print to console and exit
        System.err.println("=".repeat(60));
        System.err.println("TECHCHECK STARTUP ERROR");
        System.err.println("=".repeat(60));
        System.err.println("Title: " + title);
        System.err.println("Message: " + message);
        System.err.println("=".repeat(60));
        System.err.println("Please check the installation and try again.");
        System.err.println("For support, contact: technical@fdv.uni-lj.si");
        
        System.exit(1);
    }
    
    /**
     * Main entry point for the GUI application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        logger.info("Launching TechCheck GUI Application");
        
        // Set system properties for better JavaFX behavior
        System.setProperty("javafx.application.Thread.startOnFirstThread", "true");
        
        try {
            launch(args);
        } catch (Exception e) {
            logger.error("Failed to launch JavaFX application", e);
            System.err.println("Failed to start TechCheck GUI: " + e.getMessage());
            System.exit(1);
        }
    }
}