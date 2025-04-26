package com.morpion.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur pour le menu principal qui permet de choisir le mode de jeu
 */
public class MainMenuController {
    
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    
    @FXML private Button localGameButton;
    @FXML private Button networkGameButton;
    @FXML private Button exitButton;
    
    /**
     * Action du bouton "Jeu local"
     */
    @FXML
    public void handleLocalGameButton() {
        try {
            // Charger l'interface FXML du jeu local
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/local-game.fxml"));
            Parent root = loader.load();
            
            // Créer une nouvelle scène
            Scene scene = new Scene(root, 400, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) localGameButton.getScene().getWindow();
            
            // Configurer et afficher la nouvelle scène
            stage.setTitle("Morpion - Mode Local");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de l'interface du jeu local", e);
        }
    }
    
    /**
     * Action du bouton "Jeu en réseau"
     */
    @FXML
    public void handleNetworkGameButton() {
        try {
            // Charger l'interface FXML du jeu en réseau
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game-client.fxml"));
            Parent root = loader.load();
            
            // Créer une nouvelle scène
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) networkGameButton.getScene().getWindow();
            
            // Configurer et afficher la nouvelle scène
            stage.setTitle("Morpion - Mode Réseau");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de l'interface du jeu en réseau", e);
        }
    }
    
    /**
     * Action du bouton "Quitter"
     */
    @FXML
    public void handleExitButton() {
        // Fermer l'application
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
}