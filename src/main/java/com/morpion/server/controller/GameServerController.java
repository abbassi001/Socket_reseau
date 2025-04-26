package com.morpion.server.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.morpion.common.utils.NetworkUtils;
import com.morpion.server.view.ServerMonitor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
    @FXML private TabPane detailsTabPane;
    @FXML private ListView<String> clientsListView;
    @FXML private TreeView<String> commandsTreeView;
    @FXML private TextArea gameStateTextArea;
    
    private Consumer<Integer> startServerCallback;
    private Runnable stopServerCallback;
    private DateTimeFormatter timeFormatter;
    private TreeItem<String> rootItem;
    private List<String> clients = new ArrayList<>();
    
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
        
        // Initialiser l'arbre des commandes
        rootItem = new TreeItem<>("Commandes échangées");
        rootItem.setExpanded(true);
        commandsTreeView.setRoot(rootItem);
        
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
                
                // Réinitialiser les listes
                clientsListView.getItems().clear();
                clients.clear();
                rootItem.getChildren().clear();
                gameStateTextArea.clear();
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
    
    /**
     * Ajoute un client à la liste des clients connectés
     * 
     * @param clientId L'ID du client
     * @param clientName Le nom du client
     * @param clientAddress L'adresse du client
     */
    @Override
    public void addClient(String clientId, String clientName, String clientAddress) {
        Platform.runLater(() -> {
            String clientInfo = clientName + " (" + clientAddress + ")";
            clients.add(clientId);
            clientsListView.getItems().add(clientInfo);
            
            // Ajouter un nœud pour ce client dans l'arbre des commandes
            TreeItem<String> clientItem = new TreeItem<>(clientInfo);
            clientItem.setExpanded(true);
            rootItem.getChildren().add(clientItem);
        });
    }
    
    /**
     * Supprime un client de la liste des clients connectés
     * 
     * @param clientId L'ID du client
     */
    @Override
    public void removeClient(String clientId) {
        Platform.runLater(() -> {
            int index = clients.indexOf(clientId);
            if (index >= 0) {
                clients.remove(index);
                clientsListView.getItems().remove(index);
                rootItem.getChildren().remove(index);
            }
        });
    }
    
    /**
     * Enregistre une commande échangée entre le serveur et un client
     * 
     * @param clientId L'ID du client concerné
     * @param direction Direction de la commande (SEND ou RECEIVE)
     * @param commandType Type de la commande
     * @param details Détails supplémentaires
     */
    @Override
    public void logCommand(String clientId, String direction, String commandType, String details) {
        Platform.runLater(() -> {
            int index = clients.indexOf(clientId);
            if (index >= 0) {
                TreeItem<String> clientItem = rootItem.getChildren().get(index);
                
                // Formater l'horodatage
                String timestamp = LocalDateTime.now().format(timeFormatter);
                String commandInfo = "[" + timestamp + "] " + direction + ": " + commandType;
                
                TreeItem<String> commandItem = new TreeItem<>(commandInfo);
                if (details != null && !details.isEmpty()) {
                    commandItem.getChildren().add(new TreeItem<>(details));
                }
                
                clientItem.getChildren().add(commandItem);
                
                // Automatiquement développer l'élément parent
                clientItem.setExpanded(true);
            }
        });
    }
    
    /**
     * Met à jour l'affichage de l'état du jeu
     * 
     * @param gameStateText Représentation textuelle de l'état du jeu
     */
    @Override
    public void updateGameState(String gameStateText) {
        Platform.runLater(() -> {
            gameStateTextArea.setText(gameStateText);
        });
    }
}