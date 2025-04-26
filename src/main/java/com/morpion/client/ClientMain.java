package com.morpion.client;

import com.morpion.client.controller.GameClientController;
import com.morpion.common.utils.NetworkUtils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point d'entrée de l'application client
 */
public class ClientMain extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'interface FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-client.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur
            GameClientController controller = loader.getController();
            
            // Configurer la scène
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setTitle("Morpion - Client");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Ajouter un gestionnaire d'événements pour la fermeture de l'application
            primaryStage.setOnCloseRequest(event -> {
                controller.disconnect();
            });
            
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