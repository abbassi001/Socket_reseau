package com.morpion.common.network;

import com.morpion.model.GameState;
import com.morpion.model.Move;
import com.morpion.model.Player;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Représente une session de jeu côté serveur.
 * Gère la communication avec les clients et l'état du jeu.
 */
public class GameSession {
    
    private static final Logger LOGGER = Logger.getLogger(GameSession.class.getName());
    
    private final String sessionId;
    private final GameState gameState;
    private final ConcurrentHashMap<String, ClientHandler> clients;
    private final ExecutorService executorService;
    
    /**
     * Constructeur de la session de jeu
     */
    public GameSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.gameState = new GameState();
        this.clients = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        
        LOGGER.info("Nouvelle session de jeu créée : " + sessionId);
    }
    
    /**
     * Ajoute un nouveau client à la session
     * 
     * @param socket La socket du client
     * @throws IOException En cas d'erreur d'E/S
     */
    public void addClient(Socket socket) throws IOException {
        String clientId = UUID.randomUUID().toString();
        ClientHandler clientHandler = new ClientHandler(clientId, socket);
        
        clients.put(clientId, clientHandler);
        executorService.submit(clientHandler);
        
        LOGGER.info("Nouveau client connecté : " + clientId);
    }
    
    /**
     * Ferme la session et déconnecte tous les clients
     */
    public void close() {
        for (ClientHandler client : clients.values()) {
            client.close();
        }
        
        clients.clear();
        executorService.shutdown();
        
        LOGGER.info("Session fermée : " + sessionId);
    }
    
    /**
     * Diffuse une commande à tous les clients
     * 
     * @param command La commande à diffuser
     */
    private void broadcastCommand(GameCommand command) {
        for (ClientHandler client : clients.values()) {
            try {
                client.sendCommand(command);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de l'envoi d'une commande au client : " + client.getClientId(), e);
            }
        }
    }
    
    /**
     * Envoie une commande à un client spécifique
     * 
     * @param clientId L'ID du client
     * @param command La commande à envoyer
     * @throws IOException En cas d'erreur d'E/S
     */
    private void sendCommand(String clientId, GameCommand command) throws IOException {
        ClientHandler client = clients.get(clientId);
        if (client != null) {
            client.sendCommand(command);
        } else {
            LOGGER.warning("Tentative d'envoi d'une commande à un client inexistant : " + clientId);
        }
    }
    
    /**
     * Traite une commande reçue d'un client
     * 
     * @param clientId L'ID du client
     * @param command La commande reçue
     */
    private void processCommand(String clientId, GameCommand command) {
        try {
            switch (command.getType()) {
                case CONNECT:
                    handleConnect(clientId, command);
                    break;
                
                case DISCONNECT:
                    handleDisconnect(clientId);
                    break;
                
                case MOVE:
                    handleMove(clientId, command);
                    break;
                
                case RESET_GAME:
                    handleResetGame();
                    break;
                
                case CHAT_MESSAGE:
                    handleChatMessage(command);
                    break;
                
                default:
                    LOGGER.warning("Commande non gérée : " + command.getType());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement d'une commande", e);
            try {
                sendCommand(clientId, GameCommand.createErrorCommand("Erreur interne du serveur"));
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Impossible d'envoyer une commande d'erreur au client", ioe);
            }
        }
    }
    
    /**
     * Gère une commande de connexion
     * 
     * @param clientId L'ID du client
     * @param command La commande de connexion
     * @throws IOException En cas d'erreur d'E/S
     */
    private void handleConnect(String clientId, GameCommand command) throws IOException {
        Player player = command.getPlayer();
        player.setId(clientId);
        
        // Attribuer un numéro de joueur
        if (gameState.getPlayer1Id() == null) {
            player.setPlayerNumber(1);
            gameState.setPlayer1Id(clientId);
        } else if (gameState.getPlayer2Id() == null) {
            player.setPlayerNumber(2);
            gameState.setPlayer2Id(clientId);
        } else {
            // La partie est déjà pleine
            sendCommand(clientId, GameCommand.createErrorCommand("La partie est déjà pleine"));
            return;
        }
        
        // Confirmer la connexion
        sendCommand(clientId, GameCommand.createConnectAckCommand(player));
        
        // Envoyer l'état du jeu à tous les clients
        broadcastCommand(GameCommand.createGameStateCommand(gameState));
        
        LOGGER.info("Joueur connecté : " + player);
    }
    
    /**
     * Gère une commande de déconnexion
     * 
     * @param clientId L'ID du client
     */
    private void handleDisconnect(String clientId) {
        // Mettre à jour l'état du jeu
        gameState.playerDisconnected(clientId);
        
        // Supprimer le client de la liste
        ClientHandler client = clients.remove(clientId);
        if (client != null) {
            client.close();
        }
        
        // Informer les autres clients
        broadcastCommand(GameCommand.createGameStateCommand(gameState));
        
        LOGGER.info("Joueur déconnecté : " + clientId);
    }
    
    /**
     * Gère une commande de mouvement
     * 
     * @param clientId L'ID du client
     * @param command La commande de mouvement
     */
    private void handleMove(String clientId, GameCommand command) {
        Move move = command.getMove();
        boolean valid = gameState.makeMove(move.getRow(), move.getCol(), clientId);
        
        if (valid) {
            // Diffuser l'état du jeu mis à jour
            broadcastCommand(GameCommand.createGameStateCommand(gameState));
            LOGGER.info("Mouvement effectué : " + move);
        } else {
            // Informer le client que le mouvement est invalide
            try {
                sendCommand(clientId, GameCommand.createErrorCommand("Mouvement invalide"));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi d'une commande d'erreur", e);
            }
        }
    }
    
    /**
     * Gère une commande de réinitialisation du jeu
     */
    private void handleResetGame() {
        gameState.resetGame();
        broadcastCommand(GameCommand.createGameStateCommand(gameState));
        LOGGER.info("Jeu réinitialisé");
    }
    
    /**
     * Gère une commande de message de chat
     * 
     * @param command La commande de message de chat
     */
    private void handleChatMessage(GameCommand command) {
        // Rediffuser le message à tous les clients
        broadcastCommand(command);
        LOGGER.info("Message de chat reçu de " + command.getSenderId() + " : " + command.getMessage());
    }
    
    /**
     * Classe interne qui gère la connexion avec un client
     */
    private class ClientHandler implements Runnable {
        
        private final String clientId;
        private final Socket socket;
        private volatile boolean running;
        
        /**
         * Constructeur du gestionnaire de client
         * 
         * @param clientId L'ID du client
         * @param socket La socket du client
         */
        public ClientHandler(String clientId, Socket socket) {
            this.clientId = clientId;
            this.socket = socket;
            this.running = true;
        }
        
        /**
         * Obtient l'ID du client
         * 
         * @return L'ID du client
         */
        public String getClientId() {
            return clientId;
        }
        
        /**
         * Envoie une commande au client
         * 
         * @param command La commande à envoyer
         * @throws IOException En cas d'erreur d'E/S
         */
        public void sendCommand(GameCommand command) throws IOException {
            synchronized (socket) {
                GameProtocol.sendCommand(command, socket.getOutputStream());
            }
        }
        
        /**
         * Ferme la connexion avec le client
         */
        public void close() {
            running = false;
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture de la socket client", e);
            }
        }
        
        @Override
        public void run() {
            try {
                while (running) {
                    GameCommand command = GameProtocol.receiveCommand(socket.getInputStream());
                    processCommand(clientId, command);
                }
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.WARNING, "Erreur de communication avec le client : " + clientId, e);
                // Gérer la déconnexion inattendue
                handleDisconnect(clientId);
            }
        }
    }
}