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
}