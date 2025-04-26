package com.morpion.server.view;

import java.util.function.Consumer;

/**
 * Interface pour le moniteur du serveur
 */
public interface ServerMonitor {
    
    /**
     * Définit le callback pour le démarrage du serveur
     * 
     * @param callback Le callback à appeler lors du démarrage du serveur
     */
    void setStartServerCallback(Consumer<Integer> callback);
    
    /**
     * Définit le callback pour l'arrêt du serveur
     * 
     * @param callback Le callback à appeler lors de l'arrêt du serveur
     */
    void setStopServerCallback(Runnable callback);
    
    /**
     * Met à jour l'état du serveur dans l'interface
     * 
     * @param running Indique si le serveur est en cours d'exécution
     * @param ipAddress L'adresse IP du serveur
     * @param port Le port du serveur
     */
    void updateServerStatus(boolean running, String ipAddress, int port);
    
    /**
     * Ajoute un message dans la zone de log
     * 
     * @param message Le message à ajouter
     */
    void addLogMessage(String message);
    
    /**
     * Ajoute un client à la liste des clients connectés
     * 
     * @param clientId L'ID du client
     * @param clientName Le nom du client
     * @param clientAddress L'adresse du client
     */
    void addClient(String clientId, String clientName, String clientAddress);
    
    /**
     * Supprime un client de la liste des clients connectés
     * 
     * @param clientId L'ID du client
     */
    void removeClient(String clientId);
    
    /**
     * Enregistre une commande échangée entre le serveur et un client
     * 
     * @param clientId L'ID du client concerné
     * @param direction Direction de la commande (SEND ou RECEIVE)
     * @param commandType Type de la commande
     * @param details Détails supplémentaires
     */
    void logCommand(String clientId, String direction, String commandType, String details);
    
    /**
     * Met à jour l'affichage de l'état du jeu
     * 
     * @param gameStateText Représentation textuelle de l'état du jeu
     */
    void updateGameState(String gameStateText);
}