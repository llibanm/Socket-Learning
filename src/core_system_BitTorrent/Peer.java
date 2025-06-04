package core_system_BitTorrent;

import com.sun.tools.jconsole.JConsoleContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.BitSet;

public class Peer {

    private String ipAddress; // Adresse IP du peer
    private int port; // Port de connexion
    private String peerId; // Identifiant unique du peer (20 bytes)
    private BitSet availablePieces; // Bitmap des morceaux que ce peer possède
    private Boolean isConnected; // État de connexion
    private long lastSeen; // Dernière fois qu'on a eu des nouvelles

    private boolean isSeeder; // True si le peer a le fichier complet
    private long uploaded; // Bytes uploadés par ce peer
    private long downloaded; // Bytes téléchargés par ce peer

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean handshakeCompleted;
    private long connectionTime;
    private ConnectionState connectionState;



    public Peer(String ipAddress, int port,String peerId, int totalPieces) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.peerId = peerId;
        this.availablePieces = new BitSet(totalPieces);
        this.isConnected = false;
        this.lastSeen = System.currentTimeMillis();

        this.isSeeder = false;
        this.uploaded = 0;
        this.downloaded = 0;

        this.socket = null;
        this.inputStream = null;
        this.outputStream = null;
        this.handshakeCompleted = false;
        this.connectionTime = 0;
        this.connectionState = ConnectionState.DISCONNECTED;


        System.out.println("👤 Nouveau peer: " + peerId + " (" + ipAddress + ":" + port + ")" +
                (isSeeder ? " [SEEDER]" : " [LEECHER]"));
    }

    public boolean connect(int timeout) {
        if(connectionState != ConnectionState.DISCONNECTED) {
            System.out.println("⚠️ Peer " + peerId + " déjà en cours de connexion ou connecté");
            return false;
        }
        System.out.println("🔗 Tentative de connexion à " + ipAddress + ":" + port);
        connectionState = ConnectionState.CONNECTING;

        try {
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(ipAddress,port),timeout);
            socket.setSoTimeout(30000); //30S

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            connectionTime = System.currentTimeMillis();
            connectionState = ConnectionState.CONNECTED;
            isConnected = true;

            System.out.println("✅ Connexion établie avec " + peerId);
            return true;


        } catch (IOException e) {
            System.out.println("❌ Échec connexion à " + peerId + ": " + e.getMessage());
            connectionState = ConnectionState.ERROR;
            cleanup();
            return false;
        }
    }

    public void cleanup() {
        isConnected = false;
        handshakeCompleted = false;
        connectionState = ConnectionState.DISCONNECTED;

        try {
            if(outputStream != null) outputStream.close();
            if(inputStream != null) inputStream.close();
            if(socket != null) socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Marque un morceau comme disponible chez ce peer
    public void setPieceAvailable(int pieceIndex) {
        availablePieces.set(pieceIndex);
    }

    public boolean hasPiece(int pieceIndex) {
        return availablePieces.get(pieceIndex);
    }

    public void disconnect() {
        System.out.println("🔌 Déconnexion de " + peerId);
        cleanup();
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
    public boolean isSeeder() { return isSeeder; }
    public void setSeeder(boolean seeder) { this.isSeeder = seeder; }
    public long getUploaded() { return uploaded; }
    public long getDownloaded() { return downloaded; }

    public Socket getSocket() { return socket; }
    public InputStream getInputStream() { return inputStream; }
    public OutputStream getOutputStream() { return outputStream; }
    public boolean isHandshakeCompleted() { return handshakeCompleted; }
    public void setHandshakeCompleted(boolean completed) { this.handshakeCompleted = completed; }
    public ConnectionState getConnectionState() { return connectionState; }
    public void setConnectionState(ConnectionState state) { this.connectionState = state; }
    public long getConnectionTime() { return connectionTime; }
}
