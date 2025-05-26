package core_system_BitTorrent;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SimpleBitTorrent {
    private static final int PIECE_SIZE = 16 * 1024;//16KB package
    private static final int port = 6881;

    public static void main(String[] args) {
        System.out.println("BitTorrent is running...");
    }
}

    class TorrentInfo{
        private String announce;
        private String name;
        private long length;
        private int pieceLength;
        private List<byte[]> pieceHashes;
    }


    //represent a peer in the network
    class Peer{
        private String ip;
        private int port;
        private byte[] peerId;
        private Socket connection;
        private boolean[] availablePieces;
    }

    class TrackerClient{
        private String trackerUrl;

        public TrackerClient(String trackerUrl){
            this.trackerUrl = trackerUrl;
        }

        public List<Peer> AnnounceAndGetPeers(TorrentInfo torrentInfo,byte[] peerId){
            return new ArrayList<>();
        }
    }

    // Manages downloading and uploading of pieces
    class PieceManager {
        private TorrentInfo torrent;
        private boolean[] havePieces;    // Which pieces we have
        private boolean[] requestedPieces; // Which pieces we've requested
        private byte[][] pieces;         // Storage for piece data

        public PieceManager(TorrentInfo torrent) {
            this.torrent = torrent;
            int numPieces = calculateNumPieces();
            this.havePieces = new boolean[numPieces];
            this.requestedPieces = new boolean[numPieces];
            this.pieces = new byte[numPieces][];
        }

        private int calculateNumPieces() {
            // Calculate total number of pieces
            return 0; // Will implement
        }

        // Methods for piece management will be implemented
    }

class MessageHandler {
    // BitTorrent message types
    public static final byte CHOKE = 0;
    public static final byte UNCHOKE = 1;
    public static final byte INTERESTED = 2;
    public static final byte NOT_INTERESTED = 3;
    public static final byte HAVE = 4;
    public static final byte BITFIELD = 5;
    public static final byte REQUEST = 6;
    public static final byte PIECE = 7;
    public static final byte CANCEL = 8;

    // Methods to create and parse messages will be implemented
}
