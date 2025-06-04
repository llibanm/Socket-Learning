package core_system_BitTorrent;

public class BitTorrentDemo {
    public static void main(String[] args) {
        System.out.println("🌟 DÉMONSTRATION BITTORRENT - ÉTAPE 1: SQUELETTE");
        System.out.println("=" + "=".repeat(60));

        // Crée un fichier torrent d'exemple
        TorrentFile torrent = new TorrentFile("exemple.zip", 2048, 512); // 2KB en morceaux de 512 bytes
        torrent.addTracker("http://tracker.example.com:8080/announce");

        // Crée le client BitTorrent
        BitTorrentClient client = new BitTorrentClient(torrent, 6881);

        // Affiche l'état initial
        client.displayStatus();

        System.out.println("\n✅ Squelette de base créé avec succès !");
        System.out.println("📋 Prochaines étapes:");
        System.out.println("   - Étape 2: Communication avec les trackers");
        System.out.println("   - Étape 3: Connexion aux peers");
        System.out.println("   - Étape 4: Protocole de téléchargement");
        System.out.println("   - Étape 5: Reconstruction du fichier");
    }
}
