package core_system_BitTorrent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class HandshakeManager {
    private static final String PROTOCOL_NAME = "BitTorrent protocol";
    private static final int PROTOCOL_NAME_LENGTH = 19;
    private static final int HANDSHAKE_SIZE = 68; // 1 + 19 + 8 + 20 + 20

    private TorrentFile torrent;
    private String clientId;

    public HandshakeManager(TorrentFile torrent,String clientId) {
        this.torrent = torrent;
        this.clientId = clientId;
    }

    public boolean performHandshake(Peer peer) {
        if(!peer.isConnected() || peer.getSocket() == null) {
            System.out.println("❌ Peer " + peer.getPeerId() + " n'est pas connecté");
            return false;
        }

        System.out.println("🤝 Début handshake avec " + peer.getPeerId());
        peer.setConnectionState(ConnectionState.HANDSHAKING);

        try {
            if(!sendHanshake(peer)) {
                System.out.println("❌ Échec envoi handshake à " + peer.getPeerId());
                return false;
            }
            // 2. Reçoit et valide le handshake du peer
            if (!receiveAndValidateHandshake(peer)) {
                System.out.println("❌ Échec réception handshake de " + peer.getPeerId());
                return false;
            }

            peer.setHandshakeCompleted(true);
            peer.setConnectionState(ConnectionState.CONNECTED);

            System.out.println("✅ Handshake réussi avec " + peer.getPeerId());
            return true;
        }catch (Exception e) {
            System.out.println("❌ Erreur lors du handshake avec " + peer.getPeerId() + ": " + e.getMessage());
            peer.setConnectionState(ConnectionState.ERROR);
            return false;
        }
    }

    private boolean sendHanshake(Peer peer) {
        try {
            System.out.println("📤 Envoi handshake à " + peer.getPeerId());

            ByteArrayOutputStream handshakeBuffer = new ByteArrayOutputStream();

            handshakeBuffer.write(PROTOCOL_NAME_LENGTH);

            handshakeBuffer.write(PROTOCOL_NAME.getBytes());

            handshakeBuffer.write(new byte[8]);

            byte[] infoHash = torrent.getInfoHash();

            if(infoHash.length < 20){

                byte[] paddedHash = new byte[20];
                System.arraycopy(infoHash, 0, paddedHash, 0, infoHash.length);
                handshakeBuffer.write(paddedHash);
            }else{
                handshakeBuffer.write(infoHash,0,20);
            }

            byte[] handshakeData = handshakeBuffer.toByteArray();
            peer.getOutputStream().write(handshakeData);
            peer.getOutputStream().flush();

            System.out.println("   📦 Handshake envoyé (" + handshakeData.length + " bytes)");
            return true;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean receiveAndValidateHandshake(Peer peer) {
        try {
            System.out.println("📥 Réception handshake de " + peer.getPeerId());

            InputStream input = peer.getInputStream();

            int pstrlen = input.read();

            if(pstrlen != PROTOCOL_NAME_LENGTH) {
                System.out.println("❌ Longueur protocole invalide: " + pstrlen + " (attendu: " + PROTOCOL_NAME_LENGTH + ")");
                return false;
            }

            byte[] psrt = new byte[pstrlen];
            if(input.read(psrt) != pstrlen) {
                System.out.println("❌ Lecture incomplète du nom du protocole");
                return false;
            }

            String protocolName = new String(psrt);

            if(!protocolName.equals(PROTOCOL_NAME)) {
                System.out.println("❌ Nom de protocole invalide: " + protocolName);
                return false;
            }

            byte[] reserved = new byte[8];
            if(input.read(reserved) != reserved.length) {
                System.out.println("❌ Lecture incomplète de l'info hash");
                return false;
            }

            // 4. Lit et valide l'info hash (20 bytes)
            byte[] receivedInfoHash = new byte[20];
            if (input.read(receivedInfoHash) != 20) {
                System.out.println("❌ Lecture incomplète de l'info hash");
                return false;
            }

            byte[] ourInfoHash = torrent.getInfoHash();
            byte[] ourPaddedHash = new byte[20];
            System.arraycopy(ourInfoHash, 0, ourPaddedHash, 0, Math.min(ourInfoHash.length, 20));

            if (!Arrays.equals(receivedInfoHash, ourPaddedHash)) {
                System.out.println("❌ Info hash ne correspond pas - ce peer ne partage pas le même fichier");
                return false;
            }

            byte[] receivedPeerId = new byte[20];
            if(input.read(receivedPeerId) != 20) {
                System.out.println("❌ Lecture incomplète du peer ID");
                return false;
            }

            String remotePeerId = new String(receivedPeerId).trim();
            System.out.println("   👤 Peer ID distant: " + remotePeerId);
            System.out.println("   🔍 Info hash validé ✅");
            System.out.println("   📦 Handshake reçu et validé (" + HANDSHAKE_SIZE + " bytes)");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
