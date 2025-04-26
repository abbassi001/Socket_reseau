package com.morpion.client.view;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

/**
 * Classe utilitaire pour gérer l'interface de chat stylisée
 */
public class ChatView {
    
    private final VBox messagesContainer;
    private final ScrollPane scrollPane;
    private final DateTimeFormatter timeFormatter;
    
    /**
     * Constructeur de la vue du chat
     * 
     * @param messagesContainer Le conteneur des messages
     * @param scrollPane Le ScrollPane qui contient les messages
     */
    public ChatView(VBox messagesContainer, ScrollPane scrollPane) {
        this.messagesContainer = messagesContainer;
        this.scrollPane = scrollPane;
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        // Configuration du conteneur de messages
        this.messagesContainer.setSpacing(10);
        this.messagesContainer.setPadding(new Insets(10));
    }
    
    /**
     * Ajoute un message envoyé par l'utilisateur local
     * 
     * @param messageText Le contenu du message
     */
    public void addSelfMessage(String messageText) {
        addMessage(messageText, true);
    }
    
    /**
     * Ajoute un message reçu de l'adversaire
     * 
     * @param messageText Le contenu du message
     */
    public void addOtherMessage(String messageText) {
        addMessage(messageText, false);
    }
    
    /**
     * Ajoute un message à la conversation avec animation
     * 
     * @param messageText Le contenu du message
     * @param isSelf Vrai si le message est de l'utilisateur local, faux sinon
     */
    private void addMessage(String messageText, boolean isSelf) {
        Platform.runLater(() -> {
            // Créer le conteneur du message
            HBox messageBox = new HBox();
            messageBox.setPadding(new Insets(5));
            messageBox.setAlignment(isSelf ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            
            // Créer le contenu du message
            VBox messageContent = new VBox();
            messageContent.getStyleClass().add(isSelf ? "message-self" : "message-other");
            messageContent.setSpacing(5);
            
            // Ajouter le texte du message
            Text text = new Text(messageText);
            text.setWrappingWidth(230);
            TextFlow textFlow = new TextFlow(text);
            
            // Ajouter l'heure du message
            Label timeLabel = new Label(LocalTime.now().format(timeFormatter));
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isSelf ? "rgba(255,255,255,0.7)" : "rgba(0,0,0,0.7)"));
            timeLabel.setAlignment(Pos.CENTER_RIGHT);
            
            // Assembler le message
            messageContent.getChildren().addAll(textFlow, timeLabel);
            messageBox.getChildren().add(messageContent);
            
            // Ajouter au conteneur de messages
            messagesContainer.getChildren().add(messageBox);
            
            // Animer l'apparition du message
            applyEntryAnimation(messageContent, isSelf);
            
            // Défiler vers le nouveau message
            scrollToBottom();
        });
    }
    
    /**
     * Affiche un indicateur de frappe (typing)
     */
    public void showTypingIndicator() {
        Platform.runLater(() -> {
            HBox typingBox = new HBox();
            typingBox.setAlignment(Pos.CENTER_LEFT);
            typingBox.setPadding(new Insets(5));
            
            HBox dotsContainer = new HBox();
            dotsContainer.getStyleClass().add("typing-indicator");
            dotsContainer.setSpacing(5);
            
            for (int i = 0; i < 3; i++) {
                Node dot = createTypingDot(i);
                dotsContainer.getChildren().add(dot);
            }
            
            typingBox.getChildren().add(dotsContainer);
            messagesContainer.getChildren().add(typingBox);
            
            scrollToBottom();
        });
    }
    
    /**
     * Crée un point pour l'indicateur de frappe avec animation
     * 
     * @param index Index du point (pour décaler l'animation)
     * @return Le nœud représentant le point
     */
    private Node createTypingDot(int index) {
        Node dot = new VBox();
        dot.getStyleClass().add("typing-dot");
        
        // Animation de rebond
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(600), dot);
        translateTransition.setFromY(0);
        translateTransition.setToY(-5);
        translateTransition.setAutoReverse(true);
        translateTransition.setCycleCount(TranslateTransition.INDEFINITE);
        translateTransition.setDelay(Duration.millis(index * 120));
        
        translateTransition.play();
        
        return dot;
    }
    
    /**
     * Supprime l'indicateur de frappe
     */
    public void hideTypingIndicator() {
        Platform.runLater(() -> {
            // Supprimer le dernier élément s'il s'agit d'un indicateur de frappe
            if (!messagesContainer.getChildren().isEmpty()) {
                Node lastNode = messagesContainer.getChildren().get(messagesContainer.getChildren().size() - 1);
                if (lastNode instanceof HBox) {
                    HBox hbox = (HBox) lastNode;
                    if (!hbox.getChildren().isEmpty() && hbox.getChildren().get(0).getStyleClass().contains("typing-indicator")) {
                        messagesContainer.getChildren().remove(lastNode);
                    }
                }
            }
        });
    }
    
    /**
     * Applique une animation d'entrée au message
     * 
     * @param messageNode Le nœud à animer
     * @param isSelf Vrai si le message est de l'utilisateur local, faux sinon
     */
    private void applyEntryAnimation(Node messageNode, boolean isSelf) {
        // Animation de fondu
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), messageNode);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Animation de glissement
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), messageNode);
        slideIn.setFromX(isSelf ? 50 : -50);
        slideIn.setToX(0);
        
        // Animation de rebond/échelle
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), messageNode);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setDelay(Duration.millis(200));
        
        // Combiner les animations
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);
        parallelTransition.play();
        
        parallelTransition.setOnFinished(event -> scaleTransition.play());
    }
    
    /**
     * Défile automatiquement vers le bas du chat
     */
    private void scrollToBottom() {
        scrollPane.setVvalue(1.0);
    }
    
    /**
     * Configure le champ de texte pour envoyer des messages avec la touche Entrée
     * 
     * @param textField Le champ de texte d'entrée
     * @param onSendAction L'action à exécuter lors de l'envoi
     */
    public void setupTextField(TextField textField, Runnable onSendAction) {
        textField.setOnAction(event -> {
            if (!textField.getText().trim().isEmpty()) {
                onSendAction.run();
            }
        });
    }
    
    /**
     * Efface tous les messages du chat
     */
    public void clearChat() {
        Platform.runLater(() -> messagesContainer.getChildren().clear());
    }
}