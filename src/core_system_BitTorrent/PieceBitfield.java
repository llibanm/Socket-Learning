package core_system_BitTorrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Gère les bitmaps indiquant quelles pièces chaque peer possède
 * Utilisé pour savoir à qui demander quelles pièces
 */

public class PieceBitfield {
    private BitSet bitfield;
    private int totalPieces;

    public PieceBitfield(int totalPieces) {
        this.bitfield = new BitSet(totalPieces);
    }

    public void setPiece(int pieceIndex) {
        bitfield.set(pieceIndex);
        System.out.println("Pièce: " + pieceIndex + " marqué comme possédée");
    }

    public boolean hasPiece(int pieceIndex) {
        return bitfield.get(pieceIndex);
    }

    /**
     * Envoie notre bitfield à un peer pour lui indiquer ce qu'on possède
     */
    public void sendBitfield(DataOutputStream out) throws IOException {
        byte[] bitfieldBytes = bitfield.toByteArray();

        // Message BitTorrent: <len><id><bitfield>
        ByteBuffer message = ByteBuffer.allocate(5 + bitfieldBytes.length);
        message.putInt(1 + bitfieldBytes.length); // Longueur
        message.put((byte) 5); // ID du message bitfield
        message.put(bitfieldBytes); // Données du bitfield

        out.write(message.array());
        out.flush();

        System.out.println("Bitfield envoyé (" + getCompletionPercentage() + "% complet)");
    }

    /**
     * Reçoit et traite le bitfield d'un peer
     */
    public static PieceBitfield readBitfield(DataInputStream in, int totalPieces) throws IOException {
        int messageLength = in.readInt();
        byte message = in.readByte();

        if(message != 5){
            throw new IOException("message reçu n'est pas un bitfield");
        }

        byte[] bitfieldBytes = new byte[messageLength-1];
        in.readFully(bitfieldBytes);

        PieceBitfield peerBitfield = new PieceBitfield(totalPieces);
        peerBitfield.bitfield = BitSet.valueOf(bitfieldBytes);

        System.out.println("Bitfield reçu du peer (" +
                peerBitfield.getCompletionPercentage() + "% complet)");

        return peerBitfield;
    }


    /**
     * Retourne les pièces que ce peer possède mais que nous n'avons pas
     */
    public List<Integer> getMissingPieces(PieceBitfield ourBitfield) {
        List<Integer> missingPieces = new ArrayList<>();

        for(int i = 0; i < totalPieces; i++) {
            if(this.hasPiece(i) && !ourBitfield.hasPiece(i)) {
                missingPieces.add(i);
            }
        }
        return missingPieces;
    }


    public double getCompletionPercentage() {
        return (double) bitfield.cardinality() / totalPieces * 100;
    }

}
