package core_system_BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PeerConnection {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private PeerInfo peerInfo;
    private boolean handshakeCompleted = false;

    public PeerConnection(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
    }

    public boolean connect(byte[] infoHash,String ourPeerId) {
        try {
            System.out.println("Connecting to " + peerInfo + "...");
            socket = new Socket(peerInfo.getIp(), peerInfo.getPort());
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            sendHandshake(infoHash, ourPeerId);

            if (receiveHandshake(infoHash)) {
                handshakeCompleted = true;
                System.out.println("Handshake réussi avec " + peerInfo);
                return true;
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public void sendHandshake(byte[] infoHash,String peerId) {
        try {
            ByteBuffer handshake = ByteBuffer.allocate(68);

            handshake.put((byte)19);
            handshake.put("BitTorrent protocol".getBytes());
            handshake.put(new byte[8]);;
            handshake.put(infoHash);
            handshake.put(peerId.getBytes());

            out.write(handshake.array());
            out.flush();
            System.out.println("Handshake sent to " + peerInfo + "...");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean receiveHandshake(byte[] expectedInfoHash) {
        try {
            byte pstrlen = in.readByte();

            if(pstrlen == 19){
                System.out.println("Handshake invalid");
                return false;
            }

            byte[] pstr = new byte[19];
            in.readFully(pstr);
            if(!Arrays.equals(pstr, "BitTorrent protocol".getBytes())) return false;

            byte[] reserved = new byte[8];
            in.readFully(reserved);

            byte[] receivedInfoHash = new byte[20];
            in.readFully(receivedInfoHash);
            if(!Arrays.equals(receivedInfoHash, expectedInfoHash)) return false;

            byte[] peerId = new byte[20];
            in.readFully(peerId);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Handshake received and valid");
        return true;
    }

    public boolean isConnected() {
        return handshakeCompleted && socket != null && socket.isConnected();
    }

    public void close() {
        try {
            if(socket != null) socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
