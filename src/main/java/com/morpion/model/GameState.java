
package com.morpion.model;

import java.io.Serializable;

/**
 * Représente l'état du jeu de morpion.
 * Cette classe est sérialisable pour être transmise via le réseau.
 */
public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Tableau représentant la grille du morpion (3x3)
    // 0: case vide, 1: joueur 1 (X), 2: joueur 2 (O)
    private final int[][] grid;
    
    // Joueur dont c'est le tour (1 ou 2)
    private int currentPlayer;
    
    // Statut de la partie
    private GameStatus status;
    
    // Identifiant des joueurs
    private String player1Id;
    private String player2Id;
    
    /**
     * Énumération des statuts possibles du jeu
     */
    public enum GameStatus {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        PLAYER1_WON,
        PLAYER2_WON,
        DRAW
    }
    
    /**
     * Constructeur par défaut. Initialise une nouvelle partie.
     */
    public GameState() {
        this.grid = new int[3][3];
        this.currentPlayer = 1;  // Le joueur 1 commence
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.player1Id = null;
        this.player2Id = null;
    }
    
    /**
     * Effectue un mouvement sur la grille.
     * 
     * @param row Ligne (0-2)
     * @param col Colonne (0-2)
     * @param playerId Identifiant du joueur qui effectue le mouvement
     * @return true si le mouvement est valide, false sinon
     */
    public boolean makeMove(int row, int col, String playerId) {
        // Vérifier que la partie est en cours
        if (status != GameStatus.IN_PROGRESS) {
            return false;
        }
        
        // Vérifier que c'est bien le tour du joueur
        if ((currentPlayer == 1 && !playerId.equals(player1Id)) || 
            (currentPlayer == 2 && !playerId.equals(player2Id))) {
            return false;
        }
        
        // Vérifier que les coordonnées sont valides
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            return false;
        }
        
        // Vérifier que la case est vide
        if (grid[row][col] != 0) {
            return false;
        }
        
        // Effectuer le mouvement
        grid[row][col] = currentPlayer;
        
        // Vérifier si le jeu est terminé
        checkGameStatus();
        
        // Changer de joueur si le jeu n'est pas terminé
        if (status == GameStatus.IN_PROGRESS) {
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
        
        return true;
    }
    
    /**
     * Vérifie si le jeu est terminé (victoire ou match nul)
     */
    private void checkGameStatus() {
        // Vérifier les lignes
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] != 0 && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
                status = (grid[i][0] == 1) ? GameStatus.PLAYER1_WON : GameStatus.PLAYER2_WON;
                return;
            }
        }
        
        // Vérifier les colonnes
        for (int i = 0; i < 3; i++) {
            if (grid[0][i] != 0 && grid[0][i] == grid[1][i] && grid[1][i] == grid[2][i]) {
                status = (grid[0][i] == 1) ? GameStatus.PLAYER1_WON : GameStatus.PLAYER2_WON;
                return;
            }
        }
        
        // Vérifier la diagonale principale
        if (grid[0][0] != 0 && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            status = (grid[0][0] == 1) ? GameStatus.PLAYER1_WON : GameStatus.PLAYER2_WON;
            return;
        }
        
        // Vérifier l'autre diagonale
        if (grid[0][2] != 0 && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
            status = (grid[0][2] == 1) ? GameStatus.PLAYER1_WON : GameStatus.PLAYER2_WON;
            return;
        }
        
        // Vérifier s'il y a match nul (toutes les cases sont remplies)
        boolean isFull = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i][j] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (!isFull) break;
        }
        
        if (isFull) {
            status = GameStatus.DRAW;
        }
    }
    
    /**
     * Réinitialise la partie
     */
    public void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = 0;
            }
        }
        currentPlayer = 1;
        if (player1Id != null && player2Id != null) {
            status = GameStatus.IN_PROGRESS;
        } else {
            status = GameStatus.WAITING_FOR_PLAYERS;
        }
    }
    
    // Getters et setters
    
    public int[][] getGrid() {
        return grid;
    }
    
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    
    public GameStatus getStatus() {
        return status;
    }
    
    public void setStatus(GameStatus status) {
        this.status = status;
    }
    
    public String getPlayer1Id() {
        return player1Id;
    }
    
    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
        checkBothPlayersJoined();
    }
    
    public String getPlayer2Id() {
        return player2Id;
    }
    
    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
        checkBothPlayersJoined();
    }
    
    /**
     * Vérifie si les deux joueurs ont rejoint la partie et met à jour le statut si nécessaire
     */
    private void checkBothPlayersJoined() {
        if (player1Id != null && player2Id != null && status == GameStatus.WAITING_FOR_PLAYERS) {
            status = GameStatus.IN_PROGRESS;
        }
    }
    
    /**
     * Vérifie si un joueur a quitté et met à jour l'état du jeu
     */
    public void playerDisconnected(String playerId) {
        if (playerId.equals(player1Id)) {
            player1Id = null;
        } else if (playerId.equals(player2Id)) {
            player2Id = null;
        }
        
        if (status == GameStatus.IN_PROGRESS) {
            status = GameStatus.WAITING_FOR_PLAYERS;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("État du jeu:\n");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                switch (grid[i][j]) {
                    case 0:
                        sb.append("  ");
                        break;
                    case 1:
                        sb.append("X ");
                        break;
                    case 2:
                        sb.append("O ");
                        break;
                }
            }
            sb.append("\n");
        }
        sb.append("Joueur actuel: ").append(currentPlayer).append("\n");
        sb.append("Statut: ").append(status);
        return sb.toString();
    }
}