package com.morpion.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilitaire pour les opérations réseau
 */
public class NetworkUtils {
    
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    
    /**
     * Port par défaut pour la communication
     */
    public static final int DEFAULT_PORT = 9876;
    
    /**
     * Obtient toutes les adresses IP locales de la machine
     * 
     * @return Liste des adresses IP locales
     */
    public static List<String> getLocalIpAddresses() {
        List<String> addresses = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                // Ignorer les interfaces loopback et celles qui ne sont pas actives
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    
                    // Ignorer les adresses IPv6 pour simplifier
                    if (inetAddress.getHostAddress().contains(":")) {
                        continue;
                    }
                    
                    addresses.add(inetAddress.getHostAddress());
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des adresses IP", e);
        }
        
        // Ajouter localhost si aucune adresse n'a été trouvée
        if (addresses.isEmpty()) {
            addresses.add("127.0.0.1");
        }
        
        return addresses;
    }
    
    /**
     * Obtient l'adresse IP locale principale
     * 
     * @return L'adresse IP locale principale, ou "127.0.0.1" en cas d'erreur
     */
    public static String getMainLocalIpAddress() {
        List<String> addresses = getLocalIpAddresses();
        
        // Préférer les adresses qui ne commencent pas par 127
        for (String address : addresses) {
            if (!address.startsWith("127.")) {
                return address;
            }
        }
        
        // Sinon, retourner la première adresse trouvée, ou localhost par défaut
        return addresses.isEmpty() ? "127.0.0.1" : addresses.get(0);
    }
    
    /**
     * Vérifie si l'adresse IP est valide
     * 
     * @param ip L'adresse IP à vérifier
     * @return true si l'adresse IP est valide, false sinon
     */
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        
        if (parts.length != 4) {
            return false;
        }
        
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Vérifie si le port est valide
     * 
     * @param port Le port à vérifier
     * @return true si le port est valide, false sinon
     */
    public static boolean isValidPort(int port) {
        return port >= 1024 && port <= 65535;
    }
}