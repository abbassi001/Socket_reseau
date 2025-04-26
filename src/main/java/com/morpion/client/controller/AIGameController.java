package com.morpion.client.controller;

import com.morpion.client.view.GameSymbols;
import com.morpion.model.GameState;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contrôleur pour le jeu de morpion contre l'ordinateur
 */
public class AIGameController {
    
    private static final Logger LOGGER = Logger.getLogger(AIGameController.class.getName());
    
    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Button resetButton;
    
    private GameState gameState;
    private Pane[][] tiles;
    private Random random = new Random();
    
    // IDs des joueurs
    private final String PLAYER_ID = "human_player";
    private final String AI_ID = "ai_player";
    
    // Le joueur humain est X (1), l'AI est O (2)
    private final int HUMAN_VALUE = 1;
    private final int AI_VALUE = 2;
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        // Initialiser l'état du jeu
        gameState = new GameState();
        gameState.setStatus(GameState.GameStatus.IN_PROGRESS);
        
        // Initialiser les joueurs
        gameState.setPlayer1Id(PLAYER_ID);
        gameState.setPlayer2Id(AI_ID);
        
        // Initialiser les tuiles du plateau
        initializeBoard();
        
        // Mettre à jour l'interface
        updateUI();
    }
    
    /**
     * Initialise le plateau de jeu
     */
    private void initializeBoard() {
        System.out.println("Initialisation du plateau: " + boardGrid.getWidth() + "x" + boardGrid.getHeight());
        tiles = new Pane[3][3];
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane tile = new Pane();
                tile.getStyleClass().add("game-tile");
                tile.setMinSize(70, 70);
                tile.setPrefSize(80, 80);
                
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
     * Gère le clic sur une tuile du plateau
     * 
     * @param row La ligne de la tuile
     * @param col La colonne de la tuile
     */
    private void handleTileClick(int row, int col) {
        if (gameState.getStatus() != GameState.GameStatus.IN_PROGRESS) {
            showAlert(Alert.AlertType.INFORMATION, "Partie terminée", 
                    "La partie est terminée. Cliquez sur Réinitialiser pour rejouer.");
            return;
        }
        
        // Vérifier que c'est le tour du joueur humain
        if (gameState.getCurrentPlayer() != HUMAN_VALUE) {
            showAlert(Alert.AlertType.INFORMATION, "Ce n'est pas votre tour", 
                    "Veuillez attendre que l'ordinateur joue.");
            return;
        }
        
        // Effectuer le mouvement
        boolean valid = gameState.makeMove(row, col, PLAYER_ID);
        
        if (valid) {
            // Mettre à jour l'interface
            updateUI();
            
            // Si la partie est toujours en cours, c'est au tour de l'IA
            if (gameState.getStatus() == GameState.GameStatus.IN_PROGRESS) {
                // Laisser un petit délai pour simuler la "réflexion" de l'IA
                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Attendre 500ms avant que l'IA joue
                        Platform.runLater(this::makeAIMove);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        }
    }
    
    /**
     * Fait jouer l'IA avec une stratégie intelligente
     */
    private void makeAIMove() {
        if (gameState.getStatus() != GameState.GameStatus.IN_PROGRESS) {
            return;
        }
        
        // Trouver le meilleur coup
        int[] bestMove = findBestMove();
        
        // Jouer le coup
        gameState.makeMove(bestMove[0], bestMove[1], AI_ID);
        
        // Mettre à jour l'interface
        updateUI();
    }
    
    /**
     * Trouve le meilleur coup pour l'IA
     * 
     * @return Un tableau de 2 entiers [row, col] représentant le meilleur coup
     */
    private int[] findBestMove() {
        int[][] grid = gameState.getGrid();
        
        // 1. Vérifier si l'IA peut gagner en un coup
        int[] winningMove = findWinningMove(grid, AI_VALUE);
        if (winningMove != null) {
            return winningMove;
        }
        
        // 2. Bloquer le joueur s'il peut gagner au prochain coup
        int[] blockingMove = findWinningMove(grid, HUMAN_VALUE);
        if (blockingMove != null) {
            return blockingMove;
        }
        
        // 3. Prendre le centre s'il est libre
        if (grid[1][1] == 0) {
            return new int[] {1, 1};
        }
        
        // 4. Prendre un coin libre
        List<int[]> corners = new ArrayList<>();
        int[][] cornerPositions = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] corner : cornerPositions) {
            if (grid[corner[0]][corner[1]] == 0) {
                corners.add(corner);
            }
        }
        if (!corners.isEmpty()) {
            return corners.get(random.nextInt(corners.size()));
        }
        
        // 5. Prendre un côté libre
        List<int[]> sides = new ArrayList<>();
        int[][] sidePositions = {{0, 1}, {1, 0}, {1, 2}, {2, 1}};
        for (int[] side : sidePositions) {
            if (grid[side[0]][side[1]] == 0) {
                sides.add(side);
            }
        }
        if (!sides.isEmpty()) {
            return sides.get(random.nextInt(sides.size()));
        }
        
        // Cas improbable : aucun mouvement disponible
        return null;
    }
    
    /**
     * Trouve un coup gagnant pour le joueur spécifié
     * 
     * @param grid La grille de jeu
     * @param playerValue La valeur du joueur (1 ou 2)
     * @return Un coup gagnant s'il existe, null sinon
     */
    private int[] findWinningMove(int[][] grid, int playerValue) {
        // Vérifier les lignes
        for (int row = 0; row < 3; row++) {
            int count = 0;
            int emptyCol = -1;
            for (int col = 0; col < 3; col++) {
                if (grid[row][col] == playerValue) {
                    count++;
                } else if (grid[row][col] == 0) {
                    emptyCol = col;
                }
            }
            if (count == 2 && emptyCol != -1) {
                return new int[] {row, emptyCol};
            }
        }
        
        // Vérifier les colonnes
        for (int col = 0; col < 3; col++) {
            int count = 0;
            int emptyRow = -1;
            for (int row = 0; row < 3; row++) {
                if (grid[row][col] == playerValue) {
                    count++;
                } else if (grid[row][col] == 0) {
                    emptyRow = row;
                }
            }
            if (count == 2 && emptyRow != -1) {
                return new int[] {emptyRow, col};
            }
        }
        
        // Vérifier la diagonale principale
        int count = 0;
        int emptyIndex = -1;
        for (int i = 0; i < 3; i++) {
            if (grid[i][i] == playerValue) {
                count++;
            } else if (grid[i][i] == 0) {
                emptyIndex = i;
            }
        }
        if (count == 2 && emptyIndex != -1) {
            return new int[] {emptyIndex, emptyIndex};
        }
        
        // Vérifier l'autre diagonale
        count = 0;
        emptyIndex = -1;
        for (int i = 0; i < 3; i++) {
            if (grid[i][2-i] == playerValue) {
                count++;
            } else if (grid[i][2-i] == 0) {
                emptyIndex = i;
            }
        }
        if (count == 2 && emptyIndex != -1) {
            return new int[] {emptyIndex, 2-emptyIndex};
        }
        
        return null;
    }
    
    /**
     * Action du bouton "Réinitialiser"
     */
    @FXML
    public void handleResetButton() {
        gameState.resetGame();
        updateUI();
    }
    
    /**
     * Met à jour l'interface utilisateur
     */
    private void updateUI() {
        // Mettre à jour le plateau
        updateBoard();
        
        // Mettre à jour le statut
        updateStatus();
    }
    
    /**
     * Met à jour le plateau de jeu
     */
    private void updateBoard() {
        int[][] grid = gameState.getGrid();
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane tile = tiles[row][col];
                tile.getChildren().clear();
                
                switch (grid[row][col]) {
                    case 1: // Joueur humain (X)
                        drawX(tile);
                        break;
                        
                    case 2: // IA (O)
                        drawO(tile);
                        break;
                }
            }
        }
    }
    
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
    
    /**
     * Met à jour le statut du jeu
     */
    private void updateStatus() {
        String status;
        
        switch (gameState.getStatus()) {
            case IN_PROGRESS:
                status = (gameState.getCurrentPlayer() == HUMAN_VALUE) 
                       ? "À votre tour" 
                       : "L'ordinateur réfléchit...";
                break;
                
            case PLAYER1_WON:
                status = "Vous avez gagné !";
                break;
                
            case PLAYER2_WON:
                status = "L'ordinateur a gagné !";
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
            Stage stage = (Stage) resetButton.getScene().getWindow();
            
            // Configurer et afficher la nouvelle scène
            stage.setTitle("Morpion");
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du menu principal", e);
        }
    }
}