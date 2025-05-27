package core_system_BitTorrent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TorrentFile {

    private String announce;
    private String name;
    private long length;
    private final int  pieceLength = 256 * 1024; // 256 kb
    private List<byte[]> pieceHashes; // HASH SHA 1 de la piece

    public TorrentFile(String announce, String name, long length) {
        this.announce = announce;
        this.name = name;
        this.length = length;
        this.pieceHashes = new ArrayList<>();
    }


    public void generatePieceHashes(byte[] fileData) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            int numberOfPieces = (int)Math.ceil((double)fileData.length / pieceLength);
            //permet de sensibiliser la perte de donnée

            for (int i = 0; i < numberOfPieces; i++) {
                int start = i * pieceLength;
                int end = Math.min(start + pieceLength, fileData.length);

                byte[] piece = Arrays.copyOfRange(fileData, start, end);
                byte[] hash = sha1.digest(piece);
                pieceHashes.add(hash);

                System.out.println("Pièce " + i + " - Hash: " + bytesToHex(hash));
            }

        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public String getAnnounce() { return announce; }
    public List<byte[]> getPieceHashes() { return pieceHashes; }
    public int getPieceLength() { return pieceLength; }
}
