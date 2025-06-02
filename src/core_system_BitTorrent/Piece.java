package core_system_BitTorrent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Piece {
    private int index;  //index du morceau
    private int size; // Taille de ce morceau
    private byte[] data; // Données du morceau
    private byte[] expectedHash; // Hash SHA-1 attendu pour vérification
    private boolean isComplete; // Si le morceau est entièrement téléchargé
    private boolean isVerified; // Si le hash a été vérifié
    private List<Integer> downloadedBlocks; // Blocs déjà téléchargés (un morceau = plusieurs blocs)

    public static final int BLOCK_SIZE = 16 * 1024;

    public Piece(int index, int size, byte[] expectedHash) {
        this.index = index;
        this.size = size;
        this.data = new byte[BLOCK_SIZE];
        this.expectedHash = expectedHash;
        this.isComplete = false;
        this.isVerified = false;
        this.downloadedBlocks = new ArrayList<Integer>();

        System.out.println("🧩 Morceau " + index + " initialisé (" + size + " bytes)");
    }

    // Calcule combien de blocs sont nécessaires pour ce morceau
    public int getTotalBlocks() {
        return (int)Math.ceil((double) size / BLOCK_SIZE);
    }

    // Vérifie si un bloc spécifique est téléchargé
    public boolean isBlockDownloaded(int blockIndex) {
        return downloadedBlocks.contains(blockIndex);
    }

    // Marque un bloc comme téléchargé
    public void markBlockDownloaded(int blockIndex) {
        if(!downloadedBlocks.contains(blockIndex)) {
            downloadedBlocks.add(blockIndex);
            System.out.println("  📦 Bloc " + blockIndex + " du morceau " + index + " téléchargé");

            //
        }
    }

    public void checkCompletion(){
        if(downloadedBlocks.size() == getTotalBlocks()) {
            isComplete = true;
            //
        }
    }

    public boolean verifyIntegrity() {
        if(!isComplete) return false;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(data);
            isVerified = Arrays.equals(expectedHash, hash);

            if(isVerified) {
                System.out.println("✅ Morceau " + index + " vérifié avec succès");
            }
            else {
                System.out.println("❌ Morceau " + index + " corrompu - hash invalide");
            }

            return isVerified;

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la vérification du morceau " + index);
            return false;
        }
    }


    public int getIndex() { return index; }
    public int getSize() { return size; }
    public byte[] getData() { return data; }
    public boolean isComplete() { return isComplete; }
    public boolean isVerified() { return isVerified; }
    public double getProgress() {
        return (double) downloadedBlocks.size() / getTotalBlocks() * 100.0;
    }

}
