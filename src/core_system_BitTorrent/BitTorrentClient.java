package core_system_BitTorrent;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BitTorrentClient {
    private TorrentFile torrent; // Fichier torrent à télécharger
    private List<Peer> peers;   // Liste des peers connus
    private List<Piece> pieces; // Liste des morceaux du fichier
    private BitSet downloadedPieces; // Morceaux complètement téléchargés
    private String clientId; // Notre identifiant unique
    private boolean isDownloading;  // État du téléchargement
    private int uploadPort; // Port pour les connexions entrantes

    private TrackerClient trackerClient;

    private HandshakeManager handshakeManager;
    private ExecutorService connectionPool;
    private static final int MAX_CONCURRENT_CONNECTIONS = 5;
    private static final int CONNECTION_TIMEOUT_MILLIS = 10000; //10s

    public BitTorrentClient(TorrentFile torrent,int uploadPort) {
        this.torrent = torrent;
        this.peers = new ArrayList<Peer>();
        this.pieces = new ArrayList<>();
        this.downloadedPieces = new BitSet(torrent.getTotalPieces());
        this.clientId = generateClientId();
        this.uploadPort = uploadPort;
        this.isDownloading = false;

        this.trackerClient = new TrackerClient(torrent,clientId,uploadPort);
        this.handshakeManager = new HandshakeManager(torrent,clientId);
        this.connectionPool = Executors.newFixedThreadPool(MAX_CONCURRENT_CONNECTIONS);

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

    public void connectToPeer() {
        System.out.println("\n🤝 === CONNEXION AUX PEERS ===");

        if(peers.isEmpty()){
            System.out.println("⚠️ Aucun peer disponible pour connexion !");
            return;
        }

        List<Peer> peersToConnect = new ArrayList<>(peers);
        peersToConnect.sort((p1,p2) -> p1.getPeerId().compareTo(p2.getPeerId()));

        int maxConnections = Math.min(MAX_CONCURRENT_CONNECTIONS,peersToConnect.size());
        System.out.println("🎯 Tentative de connexion à " + maxConnections + " peers...");

        List<Future<Boolean>> connectionTasks = new ArrayList<>();

        for(int i=0; i<maxConnections; i++) {
            final Peer peer = peersToConnect.get(i);

            Future<Boolean> task = connectionPool.submit(() -> {
                return connectAndHandshakeWithPeer(peer);
            });
            connectionTasks.add(task);
        }

        int successfulConnections = 0;
        for(int i=0; i<connectionTasks.size(); i++) {
            try {
                Boolean success = connectionTasks.get(i).get(15, TimeUnit.SECONDS);
                if(success) {
                    successfulConnections++;
                }
            }catch (Exception e) {
                System.out.println("⏰ Timeout ou erreur pour connexion " + (i+1) + ": " + e.getMessage());
            }
        }

        System.out.println("\n✅ Résultat des connexions:");
        System.out.println("   - Connexions réussies: " + successfulConnections + "/" + maxConnections);
        System.out.println("   - Peers connectés: " + getConnectedPeers().size());

    }

    private boolean connectAndHandshakeWithPeer(Peer peer) {
        try {
            System.out.println("🔌 Connexion à " + peer.getPeerId() + " (" + peer.getIpAddress() + ":" + peer.getPort() + ")");

            // Étape 1: Connexion TCP
                if (!peer.connect(CONNECTION_TIMEOUT_MILLIS)) {
                return false;
            }

            // Étape 2: Handshake BitTorrent
            if (!handshakeManager.performHandshake(peer)) {
                peer.disconnect();
                return false;
            }

            System.out.println("🎉 Peer " + peer.getPeerId() + " connecté avec succès !");
            return true;

        } catch (Exception e) {
            System.out.println("❌ Erreur connexion " + peer.getPeerId() + ": " + e.getMessage());
            peer.disconnect();
            return false;
        }
    }

    public List<Peer> getConnectedPeers() {
        return peers.stream()
                .filter(p -> p.isConnected() && p.isHandshakeCompleted())
                .collect(java.util.stream.Collectors.toList());
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
    public TrackerClient getTrackerClient() { return trackerClient; }
    public HandshakeManager getHandshakeManager() { return handshakeManager; }
}
