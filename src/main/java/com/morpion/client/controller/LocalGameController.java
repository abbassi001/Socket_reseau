package com.morpion.client.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.morpion.client.view.GameSymbols;
import com.morpion.model.GameState;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * Contrôleur pour le jeu de morpion en mode local (sans réseau)
 */
public class LocalGameController {
    
    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Button resetButton;
    
    
    private GameState gameState;
    private Pane[][] tiles;
    
    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        // Initialiser l'état du jeu
        gameState = new GameState();
        gameState.setStatus(GameState.GameStatus.IN_PROGRESS);
        
        // Initialiser les joueurs locaux (pas besoin d'ID réseau)
        gameState.setPlayer1Id("local_player1");
        gameState.setPlayer2Id("local_player2");
        
        // Initialiser les tuiles du plateau
        initializeBoard();
        
        // Mettre à jour l'interface
        updateUI();
    }


    private void initializeBoard() {
    tiles = new Pane[3][3];
    
    // Rendre les lignes de la grille visibles pour le débogage
    boardGrid.setGridLinesVisible(true);
    
    // Attendre que le GridPane soit complètement chargé
    Platform.runLater(() -> {
        System.out.println("Initialisation du plateau: " + boardGrid.getWidth() + "x" + boardGrid.getHeight());
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Pane tile = new Pane();
                tile.getStyleClass().add("game-tile");
                
                // Forcer des dimensions fixes pour les tuiles
                tile.setMinSize(70, 70);
                tile.setPrefSize(80, 80);
                tile.setMaxSize(90, 90);
                
                // Ajouter une bordure visible pour le débogage
                tile.setStyle("-fx-border-color: #cccccc; -fx-border-width: 2; -fx-background-color: white;");
                
                System.out.println("Création tuile " + row + "," + col);
                
                final int finalRow = row;
                final int finalCol = col;
                
                tile.setOnMouseClicked(event -> handleTileClick(finalRow, finalCol));
                
                tiles[row][col] = tile;
                boardGrid.add(tile, col, row);
                
                // Tester si la tuile est visible en ajoutant un X
                if (row == 1 && col == 1) {
                    Line line1 = new Line(10, 10, 70, 70);
                    Line line2 = new Line(70, 10, 10, 70);
                    line1.setStrokeWidth(3);
                    line2.setStrokeWidth(3);
                    tile.getChildren().addAll(line1, line2);
                }
            }
        }
        System.out.println("Plateau initialisé avec " + 3*3 + " tuiles");
    });
}
    
    // /**
    //  * Initialise le plateau de jeu
    //  */
    // private void initializeBoard() {
    //     tiles = new Pane[3][3];
        
    //     for (int row = 0; row < 3; row++) {
    //         for (int col = 0; col < 3; col++) {
    //             Pane tile = new Pane();
    //             tile.getStyleClass().add("game-tile");
                
    //             final int finalRow = row;
    //             final int finalCol = col;
                
    //             tile.setOnMouseClicked(event -> handleTileClick(finalRow, finalCol));
                
    //             tiles[row][col] = tile;
    //             boardGrid.add(tile, col, row);
    //         }
    //     }
    // }
    
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
        
        // Déterminer quel joueur est en train de jouer
        String currentPlayerId = gameState.getCurrentPlayer() == 1 ? "local_player1" : "local_player2";
        
        // Effectuer le mouvement
        boolean valid = gameState.makeMove(row, col, currentPlayerId);
        
        if (valid) {
            // Mettre à jour l'interface
            updateUI();
        }
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
                    case 1: // Joueur X
                        drawX(tile);
                        break;
                        
                    case 2: // Joueur O
                        drawO(tile);
                        break;
                }
            }
        }
    }
    
// Dans la classe LocalGameController, remplacez les méthodes drawX et drawO actuelles
// par ces versions qui utilisent la classe GameSymbols :

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

// N'oubliez pas d'ajouter l'import:
// import com.morpion.client.view.GameSymbols;
    
private void updateStatus() {
    String status;
    
    switch (gameState.getStatus()) {
        case IN_PROGRESS:
            status = "Tour du Joueur " + (gameState.getCurrentPlayer() == 1 ? "X" : "O");
            statusLabel.setText(status);
            break;
            
        case PLAYER1_WON:
            status = "Le Joueur X a gagné !";
            statusLabel.setText(status);
            // Afficher une boîte de dialogue
            Platform.runLater(() -> showGameEndDialog("Victoire !", "Le Joueur X a gagné la partie !"));
            break;
            
        case PLAYER2_WON:
            status = "Le Joueur O a gagné !";
            statusLabel.setText(status);
            // Afficher une boîte de dialogue
            Platform.runLater(() -> showGameEndDialog("Victoire !", "Le Joueur O a gagné la partie !"));
            break;
            
        case DRAW:
            status = "Match nul !";
            statusLabel.setText(status);
            // Afficher une boîte de dialogue
            Platform.runLater(() -> showGameEndDialog("Match nul", "La partie s'est terminée sur un match nul !"));
            break;
            
        default:
            status = "État inconnu";
            statusLabel.setText(status);
    }
}

/**
 * Affiche une boîte de dialogue à la fin d'une partie avec option de rejouer
 */
private void showGameEndDialog(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message + "\n\nVoulez-vous jouer une autre partie ?");
    
    // Ajouter des boutons personnalisés
    ButtonType rejouerBtn = new ButtonType("Rejouer");
    ButtonType menuBtn = new ButtonType("Retour au menu");
    
    alert.getButtonTypes().setAll(rejouerBtn, menuBtn);
    
    // Attendre la réponse de l'utilisateur
    alert.showAndWait().ifPresent(response -> {
        if (response == rejouerBtn) {
            // Réinitialiser la partie
            gameState.resetGame();
            updateUI();
        } else if (response == menuBtn) {
            // Retourner au menu principal
            handleBackToMenuButton();
        }
    });
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
        Scene scene = new Scene(root, 800, 800);
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
        
    }
    catch (IOException e) {
        Logger.getLogger(LocalGameController.class.getName()).log(Level.SEVERE, 
                "Erreur lors du chargement du menu principal", e);
    }
}
}