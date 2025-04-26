package com.morpion.server.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import com.morpion.common.utils.NetworkUtils;
import com.morpion.server.view.ServerMonitor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Contrôleur pour l'interface du serveur
 */
public class GameServerController implements ServerMonitor {
    
    @FXML private Label statusLabel;
    @FXML private Label ipAddressLabel;
    @FXML private TextField portTextField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private TextArea logTextArea;
    
    private Consumer<Integer> startServerCallback;
    private Runnable stopServerCallback;
    private DateTimeFormatter timeFormatter;
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        // Initialiser l'interface
        updateServerStatus(false, null, 0);
        portTextField.setText(String.valueOf(NetworkUtils.DEFAULT_PORT));
        
        // Initialiser le formatteur de date/heure pour les logs
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        // Ajouter des validateurs pour le champ de port
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTextField.setText(oldValue);
            }
        });
        
        // Message initial dans le log
        addLogMessage("Serveur prêt à démarrer. Utilisez le bouton 'Démarrer'.");
    }
    
    /**
     * Action du bouton "Démarrer"
     */
    @FXML
    public void handleStartButton() {
        if (startServerCallback != null) {
            try {
                int port = Integer.parseInt(portTextField.getText());
                if (NetworkUtils.isValidPort(port)) {
                    startServerCallback.accept(port);
                } else {
                    addLogMessage("Port invalide. Veuillez entrer un nombre entre 1024 et 65535.");
                }
            } catch (NumberFormatException e) {
                addLogMessage("Port invalide. Veuillez entrer un nombre valide.");
            }
        }
    }
    
    /**
     * Action du bouton "Arrêter"
     */
    @FXML
    public void handleStopButton() {
        if (stopServerCallback != null) {
            stopServerCallback.run();
        }
    }
    
    /**
     * Définit le callback pour le démarrage du serveur
     * 
     * @param callback Le callback à appeler lors du démarrage du serveur
     */
    @Override
    public void setStartServerCallback(Consumer<Integer> callback) {
        this.startServerCallback = callback;
    }
    
    /**
     * Définit le callback pour l'arrêt du serveur
     * 
     * @param callback Le callback à appeler lors de l'arrêt du serveur
     */
    @Override
    public void setStopServerCallback(Runnable callback) {
        this.stopServerCallback = callback;
    }
    
    /**
     * Met à jour l'état du serveur dans l'interface
     * 
     * @param running Indique si le serveur est en cours d'exécution
     * @param ipAddress L'adresse IP du serveur
     * @param port Le port du serveur
     */
    @Override
    public void updateServerStatus(boolean running, String ipAddress, int port) {
        Platform.runLater(() -> {
            if (running) {
                statusLabel.setText("En cours d'exécution");
                statusLabel.setTextFill(Color.GREEN);
                ipAddressLabel.setText(ipAddress + ":" + port);
                
                startButton.setDisable(true);
                stopButton.setDisable(false);
                portTextField.setDisable(true);
            } else {
                statusLabel.setText("Arrêté");
                statusLabel.setTextFill(Color.RED);
                ipAddressLabel.setText("--");
                
                startButton.setDisable(false);
                stopButton.setDisable(true);
                portTextField.setDisable(false);
            }
        });
    }
    
    /**
     * Ajoute un message dans la zone de log
     * 
     * @param message Le message à ajouter
     */
    @Override
    public void addLogMessage(String message) {
        Platform.runLater(() -> {
            // Ajouter l'horodatage au message
            String timestamp = LocalDateTime.now().format(timeFormatter);
            String formattedMessage = "[" + timestamp + "] " + message;
            
            // Ajouter le message au log
            logTextArea.appendText(formattedMessage + "\n");
            
            // Faire défiler automatiquement vers le bas
            logTextArea.setScrollTop(Double.MAX_VALUE);
        });
    }
}