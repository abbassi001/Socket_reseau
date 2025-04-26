package com.morpion.common.network;

import java.io.Serializable;
import com.morpion.model.Move;
import com.morpion.model.GameState;
import com.morpion.model.Player;

/**
 * Représente une commande échangée entre le client et le serveur.
 * Cette classe est sérialisable pour être transmise via le réseau.
 */
public class GameCommand implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Types de commandes
    public enum CommandType {
        CONNECT,        // Client demande à se connecter au serveur
        CONNECT_ACK,    // Serveur accepte la connexion
        DISCONNECT,     // Client se déconnecte
        MOVE,           // Client envoie un mouvement
        GAME_STATE,     // Serveur envoie l'état du jeu mis à jour
        RESET_GAME,     // Demande de réinitialisation du jeu
        CHAT_MESSAGE,   // Message de chat
        ERROR           // Erreur
    }
    
    private CommandType type;      // Type de commande
    private String senderId;       // ID de l'expéditeur
    private Move move;             // Mouvement (pour CommandType.MOVE)
    private GameState gameState;   // État du jeu (pour CommandType.GAME_STATE)
    private Player player;         // Informations sur le joueur (pour CommandType.CONNECT et CONNECT_ACK)
    private String message;        // Message supplémentaire (pour CommandType.CHAT_MESSAGE ou ERROR)
    
    /**
     * Constructeur par défaut (nécessaire pour la sérialisation)
     */
    public GameCommand() {
    }
    
    /**
     * Crée une commande de connexion
     * 
     * @param playerId ID du joueur
     * @param playerName Nom du joueur
     * @return Commande de connexion
     */
    public static GameCommand createConnectCommand(String playerId, String playerName) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.CONNECT;
        cmd.senderId = playerId;
        cmd.player = new Player(playerId, playerName, 0); // Le numéro du joueur sera attribué par le serveur
        return cmd;
    }
    
    /**
     * Crée une commande de confirmation de connexion
     * 
     * @param player Informations sur le joueur, y compris son numéro
     * @return Commande de confirmation de connexion
     */
    public static GameCommand createConnectAckCommand(Player player) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.CONNECT_ACK;
        cmd.senderId = "SERVER";
        cmd.player = player;
        return cmd;
    }
    
    /**
     * Crée une commande de déconnexion
     * 
     * @param playerId ID du joueur qui se déconnecte
     * @return Commande de déconnexion
     */
    public static GameCommand createDisconnectCommand(String playerId) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.DISCONNECT;
        cmd.senderId = playerId;
        return cmd;
    }
    
    /**
     * Crée une commande de mouvement
     * 
     * @param playerId ID du joueur qui effectue le mouvement
     * @param row Ligne (0-2)
     * @param col Colonne (0-2)
     * @return Commande de mouvement
     */
    public static GameCommand createMoveCommand(String playerId, int row, int col) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.MOVE;
        cmd.senderId = playerId;
        cmd.move = new Move(row, col, playerId);
        return cmd;
    }
    
    /**
     * Crée une commande d'état du jeu
     * 
     * @param gameState État du jeu
     * @return Commande d'état du jeu
     */
    public static GameCommand createGameStateCommand(GameState gameState) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.GAME_STATE;
        cmd.senderId = "SERVER";
        cmd.gameState = gameState;
        return cmd;
    }
    
    /**
     * Crée une commande de réinitialisation du jeu
     * 
     * @param playerId ID du joueur qui demande la réinitialisation
     * @return Commande de réinitialisation
     */
    public static GameCommand createResetGameCommand(String playerId) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.RESET_GAME;
        cmd.senderId = playerId;
        return cmd;
    }
    
    /**
     * Crée une commande de message de chat
     * 
     * @param playerId ID du joueur qui envoie le message
     * @param message Contenu du message
     * @return Commande de message de chat
     */
    public static GameCommand createChatMessageCommand(String playerId, String message) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.CHAT_MESSAGE;
        cmd.senderId = playerId;
        cmd.message = message;
        return cmd;
    }
    
    /**
     * Crée une commande d'erreur
     * 
     * @param errorMessage Message d'erreur
     * @return Commande d'erreur
     */
    public static GameCommand createErrorCommand(String errorMessage) {
        GameCommand cmd = new GameCommand();
        cmd.type = CommandType.ERROR;
        cmd.senderId = "SERVER";
        cmd.message = errorMessage;
        return cmd;
    }
    
    // Getters et setters
    
    public CommandType getType() {
        return type;
    }
    
    public void setType(CommandType type) {
        this.type = type;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public Move getMove() {
        return move;
    }
    
    public void setMove(Move move) {
        this.move = move;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "GameCommand [type=" + type + ", senderId=" + senderId + "]";
    }
}