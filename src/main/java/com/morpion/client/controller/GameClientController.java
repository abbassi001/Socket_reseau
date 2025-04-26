package com.morpion.client.controller;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.morpion.client.view.GameClient;
import com.morpion.client.view.GameSymbols;
import com.morpion.common.network.GameCommand;
import com.morpion.common.network.GameProtocol;
import com.morpion.common.utils.NetworkUtils;
import com.morpion.model.GameState;
import com.morpion.model.Player;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Contrôleur pour l'interface du client
 */
public class GameClientController implements GameClient {

    private static final Logger LOGGER = Logger.getLogger(GameClientController.class.getName());

    // Composants FXML
    @FXML
    private TextField serverTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField nameTextField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private GridPane boardGrid;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextField chatTextField;
    @FXML
    private Button chatSendButton;
    @FXML
    private Button resetButton;

    // Propriétés du client
    private Socket socket;
    private Player localPlayer;
    private String playerId;
    private GameState gameState;
    private boolean connected;

    // Thread de gestion des communications
    private Thread communicationThread;
    private ExecutorService executorService;
    private volatile boolean running;

    // Tiles du jeu
    private Pane[][] tiles;

    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        // Générer un ID de joueur unique
        playerId = UUID.randomUUID().toString();

        // Initialiser l'état de connexion
        connected = false;

        // Initialiser les champs de connexion
        serverTextField.setText("localhost");
        portTextField.setText(String.valueOf(NetworkUtils.DEFAULT_PORT));
        nameTextField.setText("Joueur" + (int) (Math.random() * 1000));

        // Ajouter des validateurs pour le champ de port
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTextField.setText(oldValue);
            }
        });

        // Initialiser l'état du jeu
        gameState = new GameState();

        // Initialiser les tuiles du plateau
        initializeBoard();

        // Mettre à jour l'interface
        updateUI();

        // Créer l'exécuteur de service pour les tâches en arrière-plan
        executorService = Executors.newCachedThreadPool();
    }

    // /**
    //  * Initialise le plateau de jeu
    //  */
    // private void initializeBoard() {
    //     tiles = new Pane[3][3];

    //     for (int row = 0; row < 3; row++) {
    //         for (int col = 0; col < 3; col++) {
    //             Pane tile = new Pane();
    //             tile.getStyleClass().add("game-tile");

    //             final int finalRow = row;
    //             final int finalCol = col;

    //             tile.setOnMouseClicked(event -> handleTileClick(finalRow, finalCol));

    //             tiles[row][col] = tile;
    //             boardGrid.add(tile, col, row);
    //         }
    //     }
    // }

    private void initializeBoard() {
        System.out.println("Initialisation du plateau: " + boardGrid.getWidth() + "x" + boardGrid.getHeight());
        tiles = new Pane[3][3];
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane tile = new Pane();
                tile.getStyleClass().add("game-tile");
                tile.setMinSize(70, 70); // Forcer une taille minimale
                tile.setPrefSize(80, 80); // Taille préférée
                
                System.out.println("Création tuile " + row + "," + col);
                
                final int finalRow = row;
                final int finalCol = col;
                
                tile.setOnMouseClicked(event -> handleTileClick(finalRow, finalCol));
                
                tiles[row][col] = tile;
                boardGrid.add(tile, col, row);
            }
        }
        System.out.println("Plateau initialisé avec " + 3*3 + " tuiles");
    }

    /**
     * Se déconnecte du serveur
     */
    @Override
    public void disconnect() {
        if (!connected) {
            return;
        }

        try {
            // Envoyer la commande de déconnexion
            if (socket != null && !socket.isClosed()) {
                GameCommand disconnectCommand = GameCommand.createDisconnectCommand(playerId);
                GameProtocol.sendCommand(disconnectCommand, socket.getOutputStream());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'envoi de la commande de déconnexion", e);
        }

        running = false;

        try {
            // Fermer la socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }

            // Interrompre le thread de communication
            if (communicationThread != null) {
                communicationThread.interrupt();
                communicationThread = null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la socket", e);
        }

        // Réinitialiser l'état
        connected = false;
        localPlayer = null;
        gameState = new GameState();

        // Mettre à jour l'interface
        Platform.runLater(this::updateUI);

        LOGGER.info("Déconnecté du serveur");
    }

    /**
     * Gère le clic sur une tuile du plateau
     *
     * @param row La ligne de la tuile
     * @param col La colonne de la tuile
     */
    private void handleTileClick(int row, int col) {
        if (!connected) {
            showAlert(Alert.AlertType.WARNING, "Non connecté", "Vous n'êtes pas connecté à un serveur.");
            return;
        }

        if (gameState.getStatus() != GameState.GameStatus.IN_PROGRESS) {
            showAlert(Alert.AlertType.INFORMATION, "Partie non en cours",
                    "La partie n'est pas en cours. Attendez qu'un autre joueur se connecte ou réinitialisez la partie.");
            return;
        }

        if (!localPlayer.isMyTurn(gameState.getCurrentPlayer())) {
            showAlert(Alert.AlertType.INFORMATION, "Pas votre tour", "Ce n'est pas votre tour.");
            return;
        }

        if (gameState.getGrid()[row][col] != 0) {
            showAlert(Alert.AlertType.INFORMATION, "Case déjà occupée", "Cette case est déjà occupée.");
            return;
        }

        try {
            // Envoyer le mouvement au serveur
            GameCommand moveCommand = GameCommand.createMoveCommand(playerId, row, col);
            GameProtocol.sendCommand(moveCommand, socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi du mouvement", e);
            disconnect();
        }
    }

    /**
     * Action du bouton "Connecter"
     */
    @FXML
    public void handleConnectButton() {
        if (connected) {
            return;
        }

        String server = serverTextField.getText().trim();
        String portText = portTextField.getText().trim();
        String name = nameTextField.getText().trim();

        if (server.isEmpty() || portText.isEmpty() || name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir tous les champs avant de vous connecter.");
            return;
        }

        try {
            int port = Integer.parseInt(portText);

            if (!NetworkUtils.isValidPort(port)) {
                showAlert(Alert.AlertType.WARNING, "Port invalide",
                        "Le port doit être compris entre 1024 et 65535.");
                return;
            }

            connect(server, port, name);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Port invalide",
                    "Le port doit être un nombre valide.");
        }
    }

    /**
     * Action du bouton "Déconnecter"
     */
    @FXML
    public void handleDisconnectButton() {
        disconnect();
    }

    /**
     * Action du bouton "Réinitialiser"
     */
    @FXML
    public void handleResetButton() {
        if (!connected) {
            showAlert(Alert.AlertType.WARNING, "Non connecté",
                    "Vous n'êtes pas connecté à un serveur.");
            return;
        }

        try {
            // Envoyer la commande de réinitialisation au serveur
            GameCommand resetCommand = GameCommand.createResetGameCommand(playerId);
            GameProtocol.sendCommand(resetCommand, socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de la commande de réinitialisation", e);
            disconnect();
        }
    }

    /**
     * Action du bouton "Envoyer" pour le chat
     */
    @FXML
    public void handleChatSendButton() {
        if (!connected) {
            showAlert(Alert.AlertType.WARNING, "Non connecté",
                    "Vous n'êtes pas connecté à un serveur.");
            return;
        }

        String message = chatTextField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        try {
            // Envoyer le message de chat au serveur
            GameCommand chatCommand = GameCommand.createChatMessageCommand(playerId, message);
            GameProtocol.sendCommand(chatCommand, socket.getOutputStream());

            // Effacer le champ de texte
            chatTextField.clear();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi du message de chat", e);
            disconnect();
        }
    }

    /**
     * Se connecte au serveur
     *
     * @param server L'adresse du serveur
     * @param port Le port du serveur
     * @param playerName Le nom du joueur
     */
    private void connect(String server, int port, String playerName) {
        try {
            // Se connecter au serveur
            socket = new Socket(server, port);
            running = true;

            // Envoyer la commande de connexion
            GameCommand connectCommand = GameCommand.createConnectCommand(playerId, playerName);
            GameProtocol.sendCommand(connectCommand, socket.getOutputStream());

            // Démarrer le thread de communication
            communicationThread = new Thread(this::communicationLoop);
            communicationThread.setDaemon(true);
            communicationThread.start();

            // Mettre à jour l'état de connexion
            connected = true;

            // Mettre à jour l'interface
            updateUI();

            LOGGER.info("Connecté au serveur " + server + ":" + port);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion au serveur", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                    "Impossible de se connecter au serveur: " + e.getMessage());
        }
    }

    // /**
    //  * Se déconnecte du serveur
    //  */
    // @Override
    // public void disconnect() {
    //     if (!connected) {
    //         return;
    //     }
    //     try {
    //         // Envoyer la commande de déconnexion
    //         if (socket != null && !socket.isClosed()) {
    //             GameCommand disconnectCommand = GameCommand.createDisconnectCommand(playerId);
    //             GameProtocol.sendCommand(disconnectCommand, socket.getOutputStream());
    //         }
    //     } catch (IOException e) {
    //         LOGGER.log(Level.WARNING, "Erreur lors de l'envoi de la commande de déconnexion", e);
    //     }
    //     running = false;
    //     try {
    //         // Fermer la socket
    //         if (socket != null && !socket.isClosed()) {
    //             socket.close();
    //             socket = null;
    //         }
    //         // Interrompre le thread de communication
    //         if (communicationThread != null) {
    //             communicationThread.interrupt();
    //             communicationThread = null;
    //         }
    //     } catch (IOException e) {
    //         LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la socket", e);
    //     }
    //     // Réinitialiser l'état
    //     connected = false;
    //     localPlayer = null;
    //     gameState = new GameState();
    //     // Mettre à jour l'interface
    //     Platform.runLater(this::updateUI);
    //     LOGGER.info("Déconnecté du serveur");
    // }
    /**
     * Boucle de communication avec le serveur
     */
    private void communicationLoop() {
        try {
            while (running && socket != null && !socket.isClosed()) {
                // Recevoir une commande
                GameCommand command = GameProtocol.receiveCommand(socket.getInputStream());

                // Traiter la commande
                processCommand(command);
            }
        } catch (SocketException e) {
            if (running) {
                LOGGER.log(Level.WARNING, "Connexion perdue avec le serveur", e);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Connexion perdue",
                            "La connexion avec le serveur a été perdue.");
                    disconnect();
                });
            }
        } catch (IOException | ClassNotFoundException e) {
            if (running) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la communication avec le serveur", e);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur de communication",
                            "Erreur lors de la communication avec le serveur: " + e.getMessage());
                    disconnect();
                });
            }
        }
    }

    /**
     * Traite une commande reçue du serveur
     *
     * @param command La commande à traiter
     */
    private void processCommand(GameCommand command) {
        switch (command.getType()) {
            case CONNECT_ACK:
                handleConnectAck(command);
                break;

            case GAME_STATE:
                handleGameState(command);
                break;

            case CHAT_MESSAGE:
                handleChatMessage(command);
                break;

            case ERROR:
                handleError(command);
                break;

            default:
                LOGGER.warning("Commande non gérée: " + command.getType());
        }
    }

    /**
     * Gère une commande de confirmation de connexion
     *
     * @param command La commande à traiter
     */
    private void handleConnectAck(GameCommand command) {
        localPlayer = command.getPlayer();

        Platform.runLater(() -> {
            updateUI();

            String playerType = localPlayer.isPlayerX() ? "X" : "O";
            showAlert(Alert.AlertType.INFORMATION, "Connecté",
                    "Vous êtes connecté en tant que joueur " + playerType + ".");
        });

        LOGGER.info("Connecté en tant que joueur " + localPlayer.getPlayerNumber());
    }

    /**
     * Gère une commande d'état du jeu
     *
     * @param command La commande à traiter
     */
    private void handleGameState(GameCommand command) {
        gameState = command.getGameState();

        Platform.runLater(this::updateUI);

        LOGGER.info("État du jeu mis à jour: " + gameState.getStatus());
    }

    /**
     * Gère une commande de message de chat
     *
     * @param command La commande à traiter
     */
    private void handleChatMessage(GameCommand command) {
        String sender = command.getSenderId().equals(playerId) ? "Vous" : "Adversaire";
        String message = sender + ": " + command.getMessage();

        Platform.runLater(() -> {
            chatTextArea.appendText(message + "\n");
            chatTextArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom
        });
    }

    /**
     * Gère une commande d'erreur
     *
     * @param command La commande à traiter
     */
    private void handleError(GameCommand command) {
        Platform.runLater(() -> {
            showAlert(Alert.AlertType.ERROR, "Erreur", command.getMessage());
        });

        LOGGER.warning("Erreur reçue du serveur: " + command.getMessage());
    }

    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        // Mettre à jour l'état des boutons
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        serverTextField.setDisable(connected);
        portTextField.setDisable(connected);
        nameTextField.setDisable(connected);
        resetButton.setDisable(!connected);
        chatSendButton.setDisable(!connected);
        chatTextField.setDisable(!connected);

        // Mettre à jour le plateau
        updateBoard();

        // Mettre à jour le statut
        updateStatus();
    }

    /**
     * Met à jour le plateau de jeu
     */
    private void updateBoard() {
        if (gameState == null) {
            return;
        }

        int[][] grid = gameState.getGrid();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane tile = tiles[row][col];
                tile.getChildren().clear();

                switch (grid[row][col]) {
                    case 1: // Joueur X
                        drawX(tile);
                        break;

                    case 2: // Joueur O
                        drawO(tile);
                        break;
                }
            }
        }
    }

// Dans la classe GameClientController, remplacez les méthodes drawX et drawO actuelles
// par ces versions qui utilisent la classe GameSymbols :
    /**
     * Dessine un X dans une tuile
     *
     * @param tile La tuile dans laquelle dessiner
     */
    private void drawX(Pane tile) {
        GameSymbols.drawX(tile);
    }

    /**
     * Dessine un O dans une tuile
     *
     * @param tile La tuile dans laquelle dessiner
     */
    private void drawO(Pane tile) {
        GameSymbols.drawO(tile);
    }

// N'oubliez pas d'ajouter l'import:
// import com.morpion.client.view.GameSymbols;
    /**
     * Met à jour le statut du jeu
     */
    private void updateStatus() {
        if (!connected || gameState == null || localPlayer == null) {
            statusLabel.setText("Non connecté");
            return;
        }

        String status;

        switch (gameState.getStatus()) {
            case WAITING_FOR_PLAYERS:
                status = "En attente d'un autre joueur...";
                break;

            case IN_PROGRESS:
                if (localPlayer.isMyTurn(gameState.getCurrentPlayer())) {
                    status = "C'est votre tour";
                } else {
                    status = "Tour de l'adversaire";
                }
                break;

            case PLAYER1_WON:
                if (localPlayer.getPlayerNumber() == 1) {
                    status = "Vous avez gagné !";
                } else {
                    status = "Vous avez perdu !";
                }
                break;

            case PLAYER2_WON:
                if (localPlayer.getPlayerNumber() == 2) {
                    status = "Vous avez gagné !";
                } else {
                    status = "Vous avez perdu !";
                }
                break;

            case DRAW:
                status = "Match nul !";
                break;

            default:
                status = "État inconnu";
        }

        statusLabel.setText(status);
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     *
     * @param type Le type d'alerte
     * @param title Le titre de l'alerte
     * @param message Le message de l'alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Action du bouton "Retour au menu"
     */
    @FXML
    public void handleBackToMenuButton() {
        try {
            // Déconnecter d'abord si connecté
            if (connected) {
                disconnect();
            }

            // Charger l'interface FXML du menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main-menu.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root, 500, 500);
            String cssPath = "css/styles.css";
            if (getClass().getClassLoader().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getClassLoader().getResource(cssPath).toExternalForm());
            }

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) connectButton.getScene().getWindow();

            // Configurer et afficher la nouvelle scène
            stage.setTitle("Morpion");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du menu principal", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du menu principal: " + e.getMessage());
        }


    }
}