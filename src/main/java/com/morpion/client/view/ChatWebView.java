package com.morpion.client.view;

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * Implémentation du chat utilisant WebView pour les animations avancées
 */
public class ChatWebView extends BorderPane {
    
    private WebView webView;
    private WebEngine webEngine;
    private ChatBridge bridge;
    
    /**
     * Constructeur de la vue du chat WebView
     */
    public ChatWebView() {
        // Créer le WebView
        webView = new WebView();
        webEngine = webView.getEngine();
        
        // Créer le pont Java-JavaScript
        bridge = new ChatBridge();
        
        // Charger le HTML
        String htmlContent = createHtmlContent();
        webEngine.loadContent(htmlContent);
        
        // Configurer le pont une fois que la page est chargée
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", bridge);
            }
        });
        
        // Ajouter WebView à ce panneau
        setCenter(webView);
    }

    
    /**
     * Affiche l'indicateur de frappe
     */
    public void showTypingIndicator() {
        if (webEngine != null) {
            webEngine.executeScript("showTypingIndicator();");
        }
    }
    
    /**
     * Masque l'indicateur de frappe
     */
    public void hideTypingIndicator() {
        if (webEngine != null) {
            webEngine.executeScript("hideTypingIndicator();");
        }
    }
    
    /**
     * Efface tous les messages
     */
    public void clearChat() {
        if (webEngine != null) {
            webEngine.executeScript("clearChat();");
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
               "        }\n" +
               "        .chat-messages {\n" +
               "            flex-grow: 1;\n" +
               "            padding: 15px;\n" +
               "            overflow-y: auto;\n" +
               "            display: flex;\n" +
               "            flex-direction: column;\n" +
               "            gap: 10px;\n" +
               "        }\n" +
               "        .message {\n" +
               "            max-width: 80%;\n" +
               "            padding: 10px 15px;\n" +
               "            border-radius: 18px;\n" +
               "            position: relative;\n" +
               "            animation-duration: 0.3s;\n" +
               "            animation-fill-mode: both;\n" +
               "            word-wrap: break-word;\n" +
               "        }\n" +
               "        .message-self {\n" +
               "            align-self: flex-end;\n" +
               "            background-color: #3498db;\n" +
               "            color: white;\n" +
               "            border-bottom-right-radius: 5px;\n" +
               "            animation-name: slideInRight;\n" +
               "            box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" +
               "        }\n" +
               "        .message-other {\n" +
               "            align-self: flex-start;\n" +
               "            background-color: #f5f5f5;\n" +
               "            color: #333333;\n" +
               "            border-bottom-left-radius: 5px;\n" +
               "            animation-name: slideInLeft;\n" +
               "            box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n" +
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
        return "        const chatMessages = document.getElementById('chat-messages');\n" +
               "        let typingIndicator = null;\n" +
               "\n" +
               "        // Fonction pour ajouter un message\n" +
               "        function addMessage(text, isSelf = true) {\n" +
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
               "        }\n" +
               "\n" +
               "        // Fonction pour afficher l'indicateur de frappe\n" +
               "        function showTypingIndicator() {\n" +
               "            if (typingIndicator !== null) return;\n" +
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
               "        }\n" +
               "\n" +
               "        // Fonction pour masquer l'indicateur de frappe\n" +
               "        function hideTypingIndicator() {\n" +
               "            if (typingIndicator !== null && typingIndicator.parentNode) {\n" +
               "                typingIndicator.parentNode.removeChild(typingIndicator);\n" +
               "                typingIndicator = null;\n" +
               "            }\n" +
               "        }\n" +
               "\n" +
               "        // Fonction pour effacer tous les messages\n" +
               "        function clearChat() {\n" +
               "            while (chatMessages.firstChild) {\n" +
               "                chatMessages.removeChild(chatMessages.firstChild);\n" +
               "            }\n" +
               "            typingIndicator = null;\n" +
               "        }\n" +
               "\n" +
               "        // Fonction pour simuler l'écriture\n" +
               "        function simulateTyping() {\n" +
               "            showTypingIndicator();\n" +
               "            \n" +
               "            setTimeout(() => {\n" +
               "                hideTypingIndicator();\n" +
               "                addMessage('Ceci est une réponse simulée.', false);\n" +
               "            }, 2000);\n" +
               "        }\n";
    }
    
    /**
     * Classe de pont entre Java et JavaScript
     */
    public class ChatBridge {
        /**
         * Méthode appelée par JavaScript quand un message est ajouté
         */
        public void onMessageAdded(String message, boolean isSelf) {
            // Vous pouvez ajouter ici du code pour réagir à l'ajout d'un message
            System.out.println("Message ajouté: " + message + " (envoyé par utilisateur: " + isSelf + ")");
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
    public void addMessage(String message, boolean isSelf) {
        if (webEngine != null) {
            String script = "addMessage('" + escapeJavaScript(message) + "', " + isSelf + ");";
            System.out.println("Exécution du script JS: " + script);
            try {
                webEngine.executeScript(script);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'exécution du script: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("WebEngine est null!");
        }
    }
}