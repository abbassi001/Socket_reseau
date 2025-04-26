package com.morpion.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Classe utilitaire pour gérer le protocole de communication entre le client et le serveur.
 * Permet de sérialiser et désérialiser les commandes envoyées sur le réseau.
 */
public class GameProtocol {
    
    /**
     * Sérialise une commande en tableau d'octets pour l'envoyer sur le réseau
     * 
     * @param command La commande à sérialiser
     * @return Le tableau d'octets contenant la commande sérialisée
     * @throws IOException En cas d'erreur de sérialisation
     */
    public static byte[] serializeCommand(GameCommand command) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(command);
            oos.flush();
            return baos.toByteArray();
        }
    }
    
    /**
     * Désérialise un tableau d'octets en une commande
     * 
     * @param data Le tableau d'octets à désérialiser
     * @return La commande désérialisée
     * @throws IOException En cas d'erreur de désérialisation
     * @throws ClassNotFoundException Si la classe de l'objet désérialisé n'est pas trouvée
     */
    public static GameCommand deserializeCommand(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            
            return (GameCommand) ois.readObject();
        }
    }
    
    /**
     * Envoie une commande sur le flux de sortie spécifié
     * 
     * @param command La commande à envoyer
     * @param outputStream Le flux de sortie
     * @throws IOException En cas d'erreur d'envoi
     */
    public static void sendCommand(GameCommand command, OutputStream outputStream) throws IOException {
        byte[] data = serializeCommand(command);
        
        // On envoie d'abord la taille du message (sur 4 octets)
        DataOutputStream dos = new DataOutputStream(outputStream);
        dos.writeInt(data.length);
        
        // Puis on envoie le message lui-même
        dos.write(data);
        dos.flush();
    }
    
    /**
     * Reçoit une commande depuis le flux d'entrée spécifié
     * 
     * @param inputStream Le flux d'entrée
     * @return La commande reçue
     * @throws IOException En cas d'erreur de réception
     * @throws ClassNotFoundException Si la classe de l'objet désérialisé n'est pas trouvée
     */
    public static GameCommand receiveCommand(InputStream inputStream) throws IOException, ClassNotFoundException {
        // On lit d'abord la taille du message
        DataInputStream dis = new DataInputStream(inputStream);
        int length = dis.readInt();
        
        // Puis on lit le message lui-même
        byte[] data = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int count = dis.read(data, bytesRead, length - bytesRead);
            if (count < 0) {
                throw new EOFException("Fin de flux inattendue");
            }
            bytesRead += count;
        }
        
        return deserializeCommand(data);
    }
}