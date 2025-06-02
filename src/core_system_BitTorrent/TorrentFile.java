package core_system_BitTorrent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TorrentFile {

    private String fileName; // Nom du fichier à télécharger
    private long fileSize;  // Taille totale du fichier
    private int pieceLength; // Taille de chaque morceau (généralement 256KB)
    private List<byte[]> pieceHashes;  // Hash SHA-1 de chaque morceau pour vérification
    private List<String> trackers; // Liste des serveurs trackers
    private byte[] infoHash;  // Hash unique qui identifie ce torrent

    public TorrentFile(String fileName, long fileSize, int pieceLength) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceLength = pieceLength;
        this.pieceHashes = new ArrayList<>();
        this.trackers = new ArrayList<>();

        generateInfoHash();

        System.out.println("📁 TorrentFile créé:");
        System.out.println("   - Fichier: " + fileName);
        System.out.println("   - Taille: " + fileSize + " bytes");
        System.out.println("   - Taille par morceau: " + pieceLength + " bytes");
        System.out.println("   - Nombre de morceaux: " + getTotalPieces());
    }

    public void generateInfoHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            this.infoHash = md.digest(fileName.getBytes());
        } catch (Exception e) {
            this.infoHash = fileName.getBytes();
        }
    }

    // Calcule le nombre total de morceaux nécessaires
    public int getTotalPieces() {
        return (int) Math.ceil((double) fileSize / pieceLength);
    }

    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public int getPieceLength() { return pieceLength; }
    public byte[] getInfoHash() { return infoHash; }
    public List<String> getTrackers() { return trackers; }


    public void addTracker(String trackerUrl) {
        trackers.add(trackerUrl);
        System.out.println("🔗 Tracker ajouté: " + trackerUrl);
    }
}
