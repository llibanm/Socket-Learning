package core_system_BitTorrent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BitTorrentClient {
    private TorrentFile torrent; // Fichier torrent à télécharger
    private List<Peer> peers;   // Liste des peers connus
    private List<Piece> pieces; // Liste des morceaux du fichier
    private BitSet downloadedPieces; // Morceaux complètement téléchargés
    private String clientId; // Notre identifiant unique
    private boolean isDownloading;  // État du téléchargement
    private int uploadPort; // Port pour les connexions entrantes

    private TrackerClient trackerClient;

    public BitTorrentClient(TorrentFile torrent,int uploadPort) {
        this.torrent = torrent;
        this.peers = new ArrayList<Peer>();
        this.pieces = new ArrayList<>();
        this.downloadedPieces = new BitSet(torrent.getTotalPieces());
        this.clientId = generateClientId();
        this.uploadPort = uploadPort;
        this.isDownloading = false;

        this.trackerClient = new TrackerClient(torrent,clientId,uploadPort);

        initializePieces();

        System.out.println("\n🚀 Client BitTorrent initialisé:");
        System.out.println("   - ID Client: " + clientId);
        System.out.println("   - Port d'écoute: " + uploadPort);
        System.out.println("   - Morceaux à télécharger: " + torrent.getTotalPieces());
    }

    private String generateClientId() {
        return "-BT0001-" + System.currentTimeMillis();
    }

    private void initializePieces() {
        System.out.println("\n🔧 Initialisation des morceaux...");

        for(int i=0; i<torrent.getTotalPieces(); i++) {

            int pieceSize;
            if(i == torrent.getTotalPieces() - 1) {
                //dernier morceau
                long remaining = torrent.getFileSize() % torrent.getTotalPieces();
                pieceSize = (remaining == 0) ? torrent.getTotalPieces() : (int) remaining;
            }
            else {
                pieceSize = torrent.getTotalPieces();
            }

            byte[] hash = ("piece"+i).getBytes();
            pieces.add(new Piece(i,pieceSize,hash));
        }

        System.out.println("✅ " + pieces.size() + " morceaux initialisés");

    }

    public void discoverPeers() {
        System.out.println("\n🔍 === DÉCOUVERTE DES PEERS ===");

        List<String> trackers = torrent.getTrackers();
        if(trackers.isEmpty()) {
            System.out.println("⚠️ Aucun tracker configuré !");
            return;
        }
    }

    // Affiche l'état actuel du client
    public void displayStatus() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📊 ÉTAT DU CLIENT BITTORRENT");
        System.out.println("=".repeat(50));
        System.out.println("Fichier: " + torrent.getFileName());
        System.out.println("Taille: " + torrent.getFileSize() + " bytes");
        System.out.println("Peers connectés: " + peers.size());
        System.out.println("Morceaux téléchargés: " + downloadedPieces.cardinality() + "/" + torrent.getTotalPieces());

        double progress = (double) downloadedPieces.cardinality() / torrent.getTotalPieces() * 100.0;
        System.out.printf("Progression globale: %.1f%%\n", progress);

        System.out.println("État: " + (isDownloading ? "Téléchargement en cours" : "Arrêté"));
        System.out.println("=".repeat(50));
    }

    public String getClientId() { return clientId; }
    public TorrentFile getTorrent() { return torrent; }
    public List<Peer> getPeers() { return peers; }
    public boolean isDownloading() { return isDownloading; }
    public int getUploadPort() {return uploadPort;}
}
