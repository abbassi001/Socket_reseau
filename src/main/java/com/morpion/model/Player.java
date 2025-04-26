package com.morpion.model;

import java.io.Serializable;

/**
 * Représente un joueur dans le jeu de morpion.
 * Cette classe est sérialisable pour être transmise via le réseau.
 */
public class Player implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;       // Identifiant unique du joueur
    private String name;     // Nom du joueur
    private int playerNumber; // Numéro du joueur (1 ou 2)
    
    /**
     * Constructeur par défaut (nécessaire pour la sérialisation)
     */
    public Player() {
    }
    
    /**
     * Constructeur avec paramètres
     * 
     * @param id Identifiant unique du joueur
     * @param name Nom du joueur
     * @param playerNumber Numéro du joueur (1 ou 2)
     */
    public Player(String id, String name, int playerNumber) {
        this.id = id;
        this.name = name;
        this.playerNumber = playerNumber;
    }
    
    // Getters et setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPlayerNumber() {
        return playerNumber;
    }
    
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }
    
    /**
     * Détermine si ce joueur est le joueur X (joueur 1)
     * 
     * @return true si c'est le joueur 1, false sinon
     */
    public boolean isPlayerX() {
        return playerNumber == 1;
    }
    
    /**
     * Détermine si c'est le tour de ce joueur
     * 
     * @param currentPlayer Le joueur dont c'est le tour selon l'état du jeu
     * @return true si c'est le tour de ce joueur, false sinon
     */
    public boolean isMyTurn(int currentPlayer) {
        return playerNumber == currentPlayer;
    }
    
    @Override
    public String toString() {
        return "Player [id=" + id + ", name=" + name + ", playerNumber=" + playerNumber + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player other = (Player) obj;
        return id != null && id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}