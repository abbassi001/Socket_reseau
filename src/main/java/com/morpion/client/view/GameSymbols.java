package com.morpion.client.view;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * Classe utilitaire pour dessiner les symboles du jeu (X et O) avec des animations
 */
public class GameSymbols {
    
    /**
     * Dessine un X dans une tuile avec animation
     * 
     * @param tile La tuile dans laquelle dessiner
     */
    public static void drawX(Pane tile) {
        double size = Math.min(tile.getWidth(), tile.getHeight());
        double margin = size * 0.2;
        
        Line line1 = new Line(margin, margin, size - margin, size - margin);
        Line line2 = new Line(size - margin, margin, margin, size - margin);
        
        // Style des lignes
        line1.setStrokeWidth(5);
        line2.setStrokeWidth(5);
        line1.setStroke(Color.valueOf("#3498db"));
        line2.setStroke(Color.valueOf("#3498db"));
        line1.setStrokeLineCap(StrokeLineCap.ROUND);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Ajouter les lignes à la tuile
        tile.getChildren().addAll(line1, line2);
        
        // Appliquer des animations
        line1.setOpacity(0);
        line2.setOpacity(0);
        
        FadeTransition fadeIn1 = new FadeTransition(Duration.millis(200), line1);
        fadeIn1.setFromValue(0);
        fadeIn1.setToValue(1);
        fadeIn1.play();
        
        FadeTransition fadeIn2 = new FadeTransition(Duration.millis(200), line2);
        fadeIn2.setFromValue(0);
        fadeIn2.setToValue(1);
        fadeIn2.setDelay(Duration.millis(100));
        fadeIn2.play();
    }
    
    /**
     * Dessine un O dans une tuile avec animation
     * 
     * @param tile La tuile dans laquelle dessiner
     */
    public static void drawO(Pane tile) {
        double size = Math.min(tile.getWidth(), tile.getHeight());
        double radius = size * 0.35;
        
        Circle circle = new Circle(size / 2, size / 2, radius);
        
        // Style du cercle
        circle.setStroke(Color.valueOf("#e74c3c"));
        circle.setFill(Color.TRANSPARENT);
        circle.setStrokeWidth(5);
        
        // Ajouter le cercle à la tuile
        tile.getChildren().add(circle);
        
        // Appliquer une animation
        circle.setScaleX(0);
        circle.setScaleY(0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), circle);
        scaleTransition.setFromX(0);
        scaleTransition.setFromY(0);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.play();
    }
}