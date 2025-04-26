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
            // Charger l'interface FXML du jeu client (original)
            // FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/game-client.fxml"));
            // Charger l'interface FXML du menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main-menu.fxml"));
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root, 600, 500);

            // Charger les styles CSS
            String cssPath = "css/styles.css";
            if (getClass().getClassLoader().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getClassLoader().getResource(cssPath).toExternalForm());
            } else {
                LOGGER.warning("Le fichier CSS n'a pas été trouvé: " + cssPath);
            }

            primaryStage.setTitle("Morpion - Client");
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
