package core_system_BitTorrent;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PieceVerifier {
    /**
     * Vérifie l'intégrité des pièces téléchargées contre leurs hashes
     */
    private List<byte[]> expectedHashes;
    private MessageDigest sha1;

    public PieceVerifier(List<byte[]> pieceHashes) throws NoSuchAlgorithmException {
        this.expectedHashes = pieceHashes;
        this.sha1 = MessageDigest.getInstance("SHA-1");
    }

    /**
     * Vérifie qu'une pièce téléchargée correspond à son hash attendu
     * Crucial pour détecter les corruptions ou manipulations
     */
    public boolean verifyPiece(int pieceIndex, byte[] pieceData) {
        if(pieceIndex >= expectedHashes.size()){
            System.err.println("Index de pièces invalide : "+pieceIndex);
            return false;
        }
        // Calculer le hash de la pièce reçue

        sha1.reset();
        byte[] actualHash = sha1.digest(pieceData);
        byte[] expectedHash = expectedHashes.get(pieceIndex);

        boolean isValid = Arrays.equals(actualHash, expectedHash);

        if(!isValid){
            System.out.println("✓ Pièce " + pieceIndex + " vérifiée avec succès");
        }else{
            System.err.println("✗ Pièce " + pieceIndex + " corrompue - re-téléchargement nécessaire");
            System.err.println("  Attendu: " + bytesToHex(expectedHash));
            System.err.println("  Reçu:    " + bytesToHex(actualHash));
        }
        return isValid;
    }
    /**
     * Traite une pièce reçue d'un peer
     */

    public boolean processPieceMessage(DataInputStream input, int pieceIndex,
                                       Map<Integer,byte[]> completedPieces) throws IOException {
        int messageLength = input.readInt();
        byte messageId = input.readByte();

        if (messageId != 7) { // 7 = PIECE message
            throw new IOException("Message reçu n'est pas une pièce");
        }

        int receivedIndex = input.readInt();
        int offset = input.readInt();
        int dataLength = messageLength - 9; // 9 = 1 (id) + 4 (index) + 4 (offset)

        byte[] pieceData = new byte[dataLength];
        input.readFully(pieceData);

        System.out.println("Pièce reçue - Index: " + receivedIndex +
                ", Offset: " + offset + ", Taille: " + dataLength);

        // Si c'est le début de la pièce, vérifier l'intégrité
        if (offset == 0 && verifyPiece(receivedIndex, pieceData)) {
            completedPieces.put(receivedIndex, pieceData);
            return true;
        }
        return false;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
