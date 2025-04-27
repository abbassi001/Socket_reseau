package com.morpion.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de gestion des statistiques des joueurs
 */
public class PlayerStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    // Statistiques pour chaque joueur
    private Map<String, PlayerStats> playerStatsMap;

    public PlayerStatistics() {
        this.playerStatsMap = new HashMap<>();
    }

    /**
     * Enregistre le résultat d'une partie pour un joueur
     * @param playerId Identifiant du joueur
     * @param result Résultat de la partie (victoire, défaite, draw)
     */
    public void recordGameResult(String playerId, GameResult result) {
        PlayerStats stats = playerStatsMap.computeIfAbsent(playerId, k -> new PlayerStats());
        
        switch (result) {
            case WIN:
                stats.incrementWins();
                break;
            case LOSE:
                stats.incrementLosses();
                break;
            case DRAW:
                stats.incrementDraws();
                break;
        }
    }

    /**
     * Obtient les statistiques d'un joueur
     * @param playerId Identifiant du joueur
     * @return Statistiques du joueur
     */
    public PlayerStats getPlayerStats(String playerId) {
        return playerStatsMap.get(playerId);
    }

    /**
     * Énumération des résultats possibles
     */
    public enum GameResult {
        WIN, LOSE, DRAW
    }

    /**
     * Classe interne représentant les statistiques d'un joueur
     */
    public static class PlayerStats implements Serializable {
        private static final long serialVersionUID = 1L;

        private int wins = 0;
        private int losses = 0;
        private int draws = 0;

        public void incrementWins() {
            wins++;
        }

        public void incrementLosses() {
            losses++;
        }

        public void incrementDraws() {
            draws++;
        }

        public int getWins() {
            return wins;
        }

        public int getLosses() {
            return losses;
        }

        public int getDraws() {
            return draws;
        }

        public double getWinRate() {
            int totalGames = wins + losses + draws;
            return totalGames > 0 ? (double) wins / totalGames : 0.0;
        }

        @Override
        public String toString() {
            return String.format("Victoires: %d, Défaites: %d, Nuls: %d, Taux de victoire: %.2f%%", 
                wins, losses, draws, getWinRate() * 100);
        }
    }
}