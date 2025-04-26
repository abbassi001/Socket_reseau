package com.morpion.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application client
 */
public class ClientMain extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'interface FXML du menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-menu.fxml"));
            Parent root = loader.load();
            
            // Configurer la scène
            Scene scene = new Scene(root, 400, 400);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("Morpion");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de l'interface", e);
        }
    }
    
    /**
     * Point d'entrée principal
     * 
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}