package core_system_BitTorrent;

import java.util.BitSet;

public class Peer {

    private String ipAddress; // Adresse IP du peer
    private int port; // Port de connexion
    private String peerId; // Identifiant unique du peer (20 bytes)
    private BitSet availablePieces; // Bitmap des morceaux que ce peer possède
    private Boolean isConnected; // État de connexion
    private long lastSeen; // Dernière fois qu'on a eu des nouvelles

    public Peer(String ipAddress, int port,String peerId, int totalPieces) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.peerId = peerId;
        this.availablePieces = new BitSet(totalPieces);
        this.isConnected = false;
        this.lastSeen = System.currentTimeMillis();


        System.out.println("👤 Nouveau peer: " + peerId + " (" + ipAddress + ":" + port + ")");
    }

    // Marque un morceau comme disponible chez ce peer
    public void setPieceAvailable(int pieceIndex) {
        availablePieces.set(pieceIndex);
    }

    public boolean hasPiece(int pieceIndex) {
        return availablePieces.get(pieceIndex);
    }


    // Met à jour le timestamp de dernière activité
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    public String getIpAddress() { return ipAddress; }
    public int getPort() { return port; }
    public String getPeerId() { return peerId; }
    public BitSet getAvailablePieces() { return availablePieces; }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { this.isConnected = connected; }
}
