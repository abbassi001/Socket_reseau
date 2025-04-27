package com.morpion.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitaire pour générer des bannières texte stylisées avec Figlet
 */
public class FigletUtils {
    
    private static final Logger LOGGER = Logger.getLogger(FigletUtils.class.getName());
    
    /**
     * Génère une bannière texte stylisée avec Figlet
     * 
     * @param text Texte à styliser
     * @param font Police Figlet à utiliser (null pour la police par défaut)
     * @return La bannière stylisée ou le texte original en cas d'erreur
     */
    public static String generateFiglet(String text, String font) {
        StringBuilder result = new StringBuilder();
        
        try {
            ProcessBuilder processBuilder;
            if (font != null && !font.isEmpty()) {
                processBuilder = new ProcessBuilder("figlet", "-f", font, text);
            } else {
                processBuilder = new ProcessBuilder("figlet", text);
            }
            
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.log(Level.WARNING, "Figlet a terminé avec le code de sortie: " + exitCode);
                return text; // Retourne le texte original en cas d'erreur
            }
            
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'exécution de Figlet", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return text; // Retourne le texte original en cas d'erreur
        }
        
        return result.toString();
    }
    
    /**
     * Affiche une bannière Figlet dans la console
     * 
     * @param text Texte à afficher
     * @param font Police Figlet à utiliser (null pour la police par défaut)
     */
    public static void printFiglet(String text, String font) {
        String figlet = generateFiglet(text, font);
        System.out.println(figlet);
    }
}