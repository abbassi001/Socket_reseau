package com.morpion.model;

import java.io.Serializable;

/**
 * Représente un mouvement dans le jeu de morpion.
 * Cette classe est sérialisable pour être transmise via le réseau.
 */
public class Move implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int row;        // Ligne (0-2)
    private int col;        // Colonne (0-2)
    private String playerId; // Identifiant du joueur qui effectue le mouvement
    
    /**
     * Constructeur par défaut (nécessaire pour la sérialisation)
     */
    public Move() {
    }
    
    /**
     * Constructeur avec paramètres
     * 
     * @param row Ligne (0-2)
     * @param col Colonne (0-2)
     * @param playerId Identifiant du joueur
     */
    public Move(int row, int col, String playerId) {
        this.row = row;
        this.col = col;
        this.playerId = playerId;
    }
    
    // Getters et setters
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setCol(int col) {
        this.col = col;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    @Override
    public String toString() {
        return "Move [row=" + row + ", col=" + col + ", playerId=" + playerId + "]";
    }
}