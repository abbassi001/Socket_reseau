package com.morpion.server;

import com.morpion.common.network.GameSession;
import com.morpion.common.utils.NetworkUtils;
import com.morpion.server.view.ServerMonitor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Point d'entrée de l'application serveur
 */
public class ServerMain extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(ServerMain.class.getName());
    
    private ServerSocket serverSocket;
    private GameSession gameSession;
    private Thread serverThread;
    private volatile boolean running;
    
    private ServerMonitor serverMonitor;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'interface FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/server-monitor.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur
            serverMonitor = loader.getController();
            
            // Configurer la scène
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("Morpion - Serveur");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            // Ajouter un gestionnaire d'événements pour la fermeture de l'application
            primaryStage.setOnCloseRequest(event -> {
                stopServer();
                Platform.exit();
            });
            
            // Démarrer le serveur par défaut
            startServer(NetworkUtils.DEFAULT_PORT);
            
            // Définir les callbacks pour les boutons de l'interface
            serverMonitor.setStartServerCallback(this::startServer);
            serverMonitor.setStopServerCallback(this::stopServer);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de l'interface", e);
            Platform.exit();
        }
    }
    
    /**
     * Démarre le serveur sur le port spécifié
     * 
     * @param port Le port sur lequel démarrer le serveur
     */
    public void startServer(int port) {
        if (running) {
            stopServer();
        }
        
        try {
            // Créer la socket serveur
            serverSocket = new ServerSocket(port);
            gameSession = new GameSession();
            running = true;
            
            // Créer et démarrer le thread d'écoute
            serverThread = new Thread(this::acceptClientsLoop);
            serverThread.setDaemon(true);
            serverThread.start();
            
            // Mettre à jour l'interface
            String localIP = NetworkUtils.getMainLocalIpAddress();
            Platform.runLater(() -> {
                serverMonitor.updateServerStatus(true, localIP, port);
                serverMonitor.addLogMessage("Serveur démarré sur " + localIP + ":" + port);
            });
            
            LOGGER.info("Serveur démarré sur " + localIP + ":" + port);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage du serveur", e);
            Platform.runLater(() -> {
                serverMonitor.updateServerStatus(false, null, 0);
                serverMonitor.addLogMessage("Erreur lors du démarrage du serveur: " + e.getMessage());
            });
        }
    }
    
    /**
     * Arrête le serveur
     */
    public void stopServer() {
        if (!running) {
            return;
        }
        
        running = false;
        
        try {
            // Fermer la session de jeu
            if (gameSession != null) {
                gameSession.close();
                gameSession = null;
            }
            
            // Fermer la socket serveur
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
            
            // Interrompre le thread d'écoute
            if (serverThread != null) {
                serverThread.interrupt();
                serverThread = null;
            }
            
            // Mettre à jour l'interface
            Platform.runLater(() -> {
                serverMonitor.updateServerStatus(false, null, 0);
                serverMonitor.addLogMessage("Serveur arrêté");
            });
            
            LOGGER.info("Serveur arrêté");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'arrêt du serveur", e);
        }
    }
    
    /**
     * Boucle d'acceptation des connexions clients
     */
    private void acceptClientsLoop() {
        while (running) {
            try {
                // Attendre une connexion client
                Socket clientSocket = serverSocket.accept();
                
                // Ajouter le client à la session de jeu
                gameSession.addClient(clientSocket);
                
                // Mettre à jour l'interface
                Platform.runLater(() -> {
                    serverMonitor.addLogMessage("Nouveau client connecté: " + clientSocket.getInetAddress().getHostAddress());
                });
                
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.SEVERE, "Erreur lors de l'acceptation d'un client", e);
                    Platform.runLater(() -> {
                        serverMonitor.addLogMessage("Erreur: " + e.getMessage());
                    });
                }
            }
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
    
    @Override
    public void stop() {
        stopServer();
    }
}