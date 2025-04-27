package com.morpion.client.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Implémentation du chat utilisant WebView pour les animations avancées
 */
public class ChatWebView extends BorderPane {
    
    private WebView webView;
    private WebEngine webEngine;
    private ChatBridge bridge;
    private boolean jsLoaded = false;
    
    // File d'attente pour les messages en attente de chargement
    private Queue<MessageInfo> pendingMessages = new LinkedList<>();
    
    private static class MessageInfo {
        String message;
        boolean isSelf;
        
        MessageInfo(String message, boolean isSelf) {
            this.message = message;
            this.isSelf = isSelf;
        }
    }
    
    /**
     * Constructeur de la vue du chat WebView
     */
    public ChatWebView() {
        // Créer le WebView avec des dimensions explicites
        webView = new WebView();
        webView.setPrefWidth(350);
        webView.setPrefHeight(300);
        webView.setMinWidth(300);
        webView.setMinHeight(250);
        
        // Style pour s'assurer que le WebView est visible
        webView.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");
        this.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 1px;");
        
        webEngine = webView.getEngine();
        
        // Créer le pont Java-JavaScript
        bridge = new ChatBridge();
        
        // Charger le HTML
        String htmlContent = createHtmlContent();
        webEngine.loadContent(htmlContent);
        
        // Configurer le pont et les fonctions JS une fois la page chargée
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    // Définir le pont Java-JavaScript
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", bridge);
                    
                    // Vérifier que les fonctions JavaScript sont disponibles
                    Object result = webEngine.executeScript(
                            "if(typeof addMessage === 'function') { true } else { false }");
                    
                    if (result instanceof Boolean && (Boolean)result) {
                        // Marquer comme chargé
                        jsLoaded = true;
                        
                        System.out.println("WebView et JavaScript chargés avec succès!");
                        
                        // Traiter les messages en attente
                        processPendingMessages();
                    } else {
                        System.err.println("Les fonctions JavaScript ne sont pas disponibles!");
                        // Recharger le contenu en cas d'erreur
                        reloadContent();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'initialisation du JavaScript: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        // Ajouter WebView à ce panneau
        setCenter(webView);
    }
    
    /**
     * Affiche l'indicateur de frappe
     */
    public void showTypingIndicator() {
        if (jsLoaded) {
            try {
                webEngine.executeScript("if(typeof showTypingIndicator === 'function') { showTypingIndicator(); true; } else { false; }");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage de l'indicateur de frappe: " + e.getMessage());
            }
        }
    }
    
    /**
     * Masque l'indicateur de frappe
     */
    public void hideTypingIndicator() {
        if (jsLoaded) {
            try {
                webEngine.executeScript("if(typeof hideTypingIndicator === 'function') { hideTypingIndicator(); true; } else { false; }");
            } catch (Exception e) {
                System.err.println("Erreur lors du masquage de l'indicateur de frappe: " + e.getMessage());
            }
        }
    }
    
    /**
     * Efface tous les messages
     */
    public void clearChat() {
        if (jsLoaded) {
            try {
                webEngine.executeScript("if(typeof clearChat === 'function') { clearChat(); true; } else { false; }");
                pendingMessages.clear();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'effacement du chat: " + e.getMessage());
            }
        } else {
            pendingMessages.clear();
        }
    }
    
    /**
     * Échappe les caractères spéciaux pour JavaScript
     */
    private String escapeJavaScript(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("'", "\\'")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r");
    }
    
    /**
     * Ajoute un message au chat
     */
    public void addMessage(String message, boolean isSelf) {
        System.out.println("ChatWebView: tentative d'ajout du message: " + message);
        
        // Si JavaScript n'est pas encore chargé, mettre en file d'attente
        if (!jsLoaded) {
            System.out.println("JavaScript pas encore chargé, message mis en file d'attente: " + message);
            pendingMessages.add(new MessageInfo(message, isSelf));
            return;
        }
        
        if (webEngine != null) {
            final String escapedMessage = escapeJavaScript(message);
            final String script = "if(typeof addMessage === 'function') { " +
                                 "addMessage('" + escapedMessage + "', " + isSelf + "); " +
                                 "true; } else { false; }";
                                 
            System.out.println("Exécution du script JS: " + script);
            
            try {
                Object result = webEngine.executeScript(script);
                System.out.println("Résultat de l'exécution: " + result);
                
                if (result instanceof Boolean && !(Boolean)result) {
                    System.err.println("La fonction addMessage n'est pas disponible!");
                    // Forcer un rechargement de la page
                    reloadContent();
                } else {
                    System.out.println("Message ajouté avec succès: " + message);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout du message: " + e.getMessage());
                e.printStackTrace();
                
                // Recharger le contenu en cas d'erreur
                reloadContent();
            }
        } else {
            System.err.println("WebEngine est null!");
        }
    }
    
    /**
     * Traite les messages en attente
     */
    private void processPendingMessages() {
        if (jsLoaded && !pendingMessages.isEmpty()) {
            System.out.println("Traitement des messages en attente (" + pendingMessages.size() + ")");
            while (!pendingMessages.isEmpty()) {
                MessageInfo info = pendingMessages.poll();
                addMessage(info.message, info.isSelf);
            }
        }
    }
    
    /**
     * Recharge le contenu du WebView en cas de problème
     */
    private void reloadContent() {
        System.out.println("Rechargement du contenu WebView...");
        webEngine.loadContent(createHtmlContent());
        
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    // Redéfinir le pont Java-JavaScript
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", bridge);
                    
                    // Marquer comme chargé
                    jsLoaded = true;
                    
                    // Traiter les messages en attente
                    processPendingMessages();
                    
                    System.out.println("WebView rechargé avec succès!");
                } catch (Exception e) {
                    System.err.println("Erreur lors du rechargement: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Crée le contenu HTML pour le chat
     */
    private String createHtmlContent() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"fr\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <style>\n" +
               getChatStyles() +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"chat-container\">\n" +
               "        <div class=\"chat-messages\" id=\"chat-messages\"></div>\n" +
               "    </div>\n" +
               "    <script>\n" +
               getChatJavaScript() +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * Définit les styles CSS du chat
     */
    private String getChatStyles() {
        return "        * {\n" +
               "            margin: 0;\n" +
               "            padding: 0;\n" +
               "            box-sizing: border-box;\n" +
               "            font-family: 'Segoe UI', Arial, sans-serif;\n" +
               "        }\n" +
               "        body {\n" +
               "            background-color: transparent;\n" +
               "            height: 100%;\n" +
               "            width: 100%;\n" +
               "            overflow: hidden;\n" +
               "        }\n" +
               "        .chat-container {\n" +
               "            height: 100%;\n" +
               "            width: 100%;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            border: 1px solid #ccc;\n" +
               "        }\n" +
               "        .chat-messages {\n" +
               "            flex-grow: 1;\n" +
               "            padding: 15px;\n" +
               "            overflow-y: auto;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            gap: 10px;\n" +
               "            background-color: white;\n" +
               "        }\n" +
               "        .message {\n" +
               "            max-width: 80%;\n" +
               "            padding: 10px 15px;\n" +
               "            border-radius: 18px;\n" +
               "            position: relative;\n" +
               "            animation-duration: 0.3s;\n" +
               "            animation-fill-mode: both;\n" +
               "            word-wrap: break-word;\n" +
               "            margin: 5px 0;\n" +
               "        }\n" +
               "        .message-self {\n" +
               "            align-self: flex-end;\n" +
               "            background-color: #3498db;\n" +
               "            color: white;\n" +
               "            border-bottom-right-radius: 5px;\n" +
               "            animation-name: slideInRight;\n" +
               "            box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" +
               "            text-align: right;\n" +
               "        }\n" +
               "        .message-other {\n" +
               "            align-self: flex-start;\n" +
               "            background-color: #f5f5f5;\n" +
               "            color: #333333;\n" +
               "            border-bottom-left-radius: 5px;\n" +
               "            animation-name: slideInLeft;\n" +
               "            box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" +
               "            text-align: left;\n" +
               "        }\n" +
               "        .message-content {\n" +
               "            margin-bottom: 5px;\n" +
               "        }\n" +
               "        .message-time {\n" +
               "            font-size: 0.7em;\n" +
               "            opacity: 0.7;\n" +
               "            margin-top: 5px;\n" +
               "            text-align: right;\n" +
               "        }\n" +
               "        .typing-indicator {\n" +
               "            align-self: flex-start;\n" +
               "            background-color: #f5f5f5;\n" +
               "            border-radius: 18px;\n" +
               "            padding: 10px 15px;\n" +
               "            display: flex;\n" +
               "            align-items: center;\n" +
               "            animation: fadeIn 0.3s;\n" +
               "            margin: 5px 0;\n" +
               "        }\n" +
               "        .typing-dots {\n" +
               "            display: flex;\n" +
               "            gap: 4px;\n" +
               "        }\n" +
               "        .dot {\n" +
               "            width: 8px;\n" +
               "            height: 8px;\n" +
               "            background-color: #9e9e9e;\n" +
               "            border-radius: 50%;\n" +
               "            animation: bounce 1.5s infinite;\n" +
               "        }\n" +
               "        .dot:nth-child(2) {\n" +
               "            animation-delay: 0.2s;\n" +
               "        }\n" +
               "        .dot:nth-child(3) {\n" +
               "            animation-delay: 0.4s;\n" +
               "        }\n" +
               "        .test-element {\n" +
               "            padding: 10px;\n" +
               "            margin: 10px;\n" +
               "            background-color: #e74c3c;\n" +
               "            color: white;\n" +
               "            border-radius: 5px;\n" +
               "            text-align: center;\n" +
               "        }\n" +
               "        @keyframes bounce {\n" +
               "            0%, 60%, 100% {\n" +
               "                transform: translateY(0);\n" +
               "            }\n" +
               "            30% {\n" +
               "                transform: translateY(-5px);\n" +
               "            }\n" +
               "        }\n" +
               "        @keyframes fadeIn {\n" +
               "            from {\n" +
               "                opacity: 0;\n" +
               "            }\n" +
               "            to {\n" +
               "                opacity: 1;\n" +
               "            }\n" +
               "        }\n" +
               "        @keyframes pop {\n" +
               "            0% { transform: scale(0.8); }\n" +
               "            50% { transform: scale(1.05); }\n" +
               "            100% { transform: scale(1); }\n" +
               "        }\n" +
               "        @keyframes slideInLeft {\n" +
               "            from {\n" +
               "                transform: translateX(-20px);\n" +
               "                opacity: 0;\n" +
               "            }\n" +
               "            to {\n" +
               "                transform: translateX(0);\n" +
               "                opacity: 1;\n" +
               "            }\n" +
               "        }\n" +
               "        @keyframes slideInRight {\n" +
               "            from {\n" +
               "                transform: translateX(20px);\n" +
               "                opacity: 0;\n" +
               "            }\n" +
               "            to {\n" +
               "                transform: translateX(0);\n" +
               "                opacity: 1;\n" +
               "            }\n" +
               "        }\n" +
               "        /* Effet de focus sur le message */\n" +
               "        .message.focus {\n" +
               "            animation: pop 0.3s;\n" +
               "        }\n";
    }
    
    /**
     * Définit le JavaScript du chat
     */
    private String getChatJavaScript() {
        return "        // Attendre le chargement complet de la page\n" +
               "        document.addEventListener('DOMContentLoaded', function() {\n" +
               "            console.log('DOM chargé');\n" +
               "        });\n" +
               "\n" +
               "        // Variables globales\n" +
               "        const chatMessages = document.getElementById('chat-messages');\n" +
               "        let typingIndicator = null;\n" +
               "\n" +
               "        // Ajouter un élément visuel fixe pour test\n" +
               "        window.onload = function() {\n" +
               "            const testElement = document.createElement('div');\n" +
               "            testElement.className = 'test-element';\n" +
               "            testElement.textContent = 'CHAT INITIALISÉ';\n" +
               "            document.body.appendChild(testElement);\n" +
               "            \n" +
               "            // Également ajouter directement au conteneur de messages\n" +
               "            if (chatMessages) {\n" +
               "                const testMsg = document.createElement('div');\n" +
               "                testMsg.className = 'message message-other';\n" +
               "                testMsg.innerHTML = '<div class=\"message-content\">Le chat est prêt!</div>' +\n" +
               "                                    '<div class=\"message-time\">Système</div>';\n" +
               "                chatMessages.appendChild(testMsg);\n" +
               "            }\n" +
               "            \n" +
               "            // Notifier Java que la page est chargée\n" +
               "            if (window.javaBridge) {\n" +
               "                window.javaBridge.onMessageAdded('Le chat est prêt!', false);\n" +
               "            }\n" +
               "        };\n" +
               "\n" +
               "        // Indiquer que le JS est chargé\n" +
               "        window.jsLoaded = true;\n" +
               "        console.log('JS chargé');\n" +
               "\n" +
               "        // Fonction pour ajouter un message\n" +
               "        function addMessage(text, isSelf = true) {\n" +
               "            console.log('Ajout de message:', text, 'isSelf:', isSelf);\n" +
               "            \n" +
               "            // Supprimer l'indicateur de frappe s'il existe\n" +
               "            hideTypingIndicator();\n" +
               "            \n" +
               "            const time = new Date();\n" +
               "            const timeString = `${time.getHours()}:${time.getMinutes().toString().padStart(2, '0')}`;\n" +
               "            \n" +
               "            const messageDiv = document.createElement('div');\n" +
               "            messageDiv.className = `message ${isSelf ? 'message-self' : 'message-other'}`;\n" +
               "            \n" +
               "            messageDiv.innerHTML = `\n" +
               "                <div class=\"message-content\">${text}</div>\n" +
               "                <div class=\"message-time\">${timeString}</div>\n" +
               "            `;\n" +
               "            \n" +
               "            // S'assurer que chatMessages existe\n" +
               "            if (!chatMessages) {\n" +
               "                console.error('Container de messages non trouvé!');\n" +
               "                return false;\n" +
               "            }\n" +
               "            \n" +
               "            chatMessages.appendChild(messageDiv);\n" +
               "            chatMessages.scrollTop = chatMessages.scrollHeight;\n" +
               "            \n" +
               "            // Ajouter un effet de focus temporaire\n" +
               "            setTimeout(() => {\n" +
               "                messageDiv.classList.add('focus');\n" +
               "                setTimeout(() => {\n" +
               "                    messageDiv.classList.remove('focus');\n" +
               "                }, 300);\n" +
               "            }, 50);\n" +
               "            \n" +
               "            // Notification à Java que le message a été ajouté\n" +
               "            if (window.javaBridge) {\n" +
               "                window.javaBridge.onMessageAdded(text, isSelf);\n" +
               "            }\n" +
               "            \n" +
               "            console.log('Message ajouté avec succès');\n" +
               "            return true;\n" +
               "        }\n" +
               "\n" +
               "        // Fonction pour afficher l'indicateur de frappe\n" +
               "        function showTypingIndicator() {\n" +
               "            if (typingIndicator !== null) return true;\n" +
               "            \n" +
               "            // S'assurer que chatMessages existe\n" +
               "            if (!chatMessages) {\n" +
               "                console.error('Container de messages non trouvé!');\n" +
               "                return false;\n" +
               "            }\n" +
               "            \n" +
               "            typingIndicator = document.createElement('div');\n" +
               "            typingIndicator.className = 'typing-indicator';\n" +
               "            typingIndicator.innerHTML = `\n" +
               "                <div class=\"typing-dots\">\n" +
               "                    <span class=\"dot\"></span>\n" +
               "                    <span class=\"dot\"></span>\n" +
               "                    <span class=\"dot\"></span>\n" +
               "                </div>\n" +
               "            `;\n" +
               "            \n" +
               "            chatMessages.appendChild(typingIndicator);\n" +
               "            chatMessages.scrollTop = chatMessages.scrollHeight;\n" +
               "            return true;\n" +
               "        }\n" +
               "\n" +
               "        // Fonction pour masquer l'indicateur de frappe\n" +
               "        function hideTypingIndicator() {\n" +
               "            if (typingIndicator !== null && typingIndicator.parentNode) {\n" +
               "                typingIndicator.parentNode.removeChild(typingIndicator);\n" +
               "                typingIndicator = null;\n" +
               "                return true;\n" +
               "            }\n" +
               "            return false;\n" +
               "        }\n" +
               "\n" +
               "        // Fonction pour effacer tous les messages\n" +
               "        function clearChat() {\n" +
               "            // S'assurer que chatMessages existe\n" +
               "            if (!chatMessages) {\n" +
               "                console.error('Container de messages non trouvé!');\n" +
               "                return false;\n" +
               "            }\n" +
               "            \n" +
               "            while (chatMessages.firstChild) {\n" +
               "                chatMessages.removeChild(chatMessages.firstChild);\n" +
               "            }\n" +
               "            typingIndicator = null;\n" +
               "            console.log('Chat effacé');\n" +
               "            return true;\n" +
               "        }\n" +
               "\n" +
               "        // Test pour voir si les fonctions sont correctement définies\n" +
               "        console.log('Fonctions définies: addMessage, showTypingIndicator, hideTypingIndicator, clearChat');\n";
    }
    
    /**
     * Classe de pont entre Java et JavaScript
     */
    public class ChatBridge {
        /**
         * Méthode appelée par JavaScript quand un message est ajouté
         */
        public void onMessageAdded(String message, boolean isSelf) {
            System.out.println("Message ajouté (via bridge): " + message + 
                              " (envoyé par utilisateur: " + isSelf + ")");
        }
    }
    
    /**
     * Méthode pour obtenir le WebView
     * 
     * @return Le WebView utilisé pour le chat
     */
    public WebView getWebView() {
        return webView;
    }
}