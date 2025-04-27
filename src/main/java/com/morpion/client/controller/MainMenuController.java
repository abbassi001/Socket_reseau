package com.morpion.client.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Contrôleur pour le menu principal qui permet de choisir le mode de jeu
 */
public class MainMenuController {

    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    @FXML
    private Button networkGameButton;
    @FXML
    private Button exitButton;


// Par
    @FXML
    private Button aiGameButton;

    /**
     * Action du bouton "Jouer contre l'ordinateur"
     */
    @FXML
    public void handleAIGameButton() {
        try {
            // Charger l'interface FXML du jeu contre l'IA
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/ai-game.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root, 500, 600);
            String cssPath = "css/styles.css";
            if (getClass().getClassLoader().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getClassLoader().getResource(cssPath).toExternalForm());
            }

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) aiGameButton.getScene().getWindow();

            // Configurer et afficher la nouvelle scène
            stage.setTitle("Morpion - Contre l'ordinateur");
            stage.setScene(scene);
            stage.show();

            LOGGER.info("Interface du jeu contre l'IA chargée");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de l'interface du jeu contre l'IA", e);
            e.printStackTrace();

        }
    }

    /**
     * Action du bouton "Jeu en réseau"
     */
    @FXML
    public void handleNetworkGameButton() {
        try {
            // Charger l'interface FXML du jeu en réseau
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/game-client.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root, 700, 700);
            String cssPath = "css/styles.css";
            if (getClass().getClassLoader().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getClassLoader().getResource(cssPath).toExternalForm());
            }

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) networkGameButton.getScene().getWindow();

            // Configurer et afficher la nouvelle scène
            stage.setTitle("Morpion - Mode Réseau");
            stage.setScene(scene);
            stage.show();

            LOGGER.info("Interface du jeu en réseau chargée");

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

        LOGGER.info("Application fermée");
    }
}
