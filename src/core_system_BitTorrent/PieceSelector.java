package core_system_BitTorrent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class PieceSelector {
    /**
     * Stratégies pour sélectionner quelles pièces télécharger en priorité
     */
    private Map<Integer,Integer> pieceRarity; // combien de peers ont chaque pièce.
    private Set<Integer> requestedPieces;// Pièces déjà demandées

    public PieceSelector() {
        this.pieceRarity = new HashMap<>();
        this.requestedPieces = new HashSet<>();
    }


    /**
     * Met à jour la rareté des pièces basée sur les bitfields des peers
     */
    public void updateRarity(List<PieceBitfield> peerBitfields, int totalPieces) {
        pieceRarity.clear();

        for(int piece = 0; piece < totalPieces; piece++) {
            int count = 0;
            for(PieceBitfield pieceBitfield : peerBitfields) {
                if(pieceBitfield.hasPiece(piece)){
                    count++;
                }
            }
            pieceRarity.put(piece, count);
        }
        System.out.println("Rareté des pieces mises à jour");
    }

    /**
     * Stratégie "Rarest First" - sélectionne les pièces les plus rares
     * Cela améliore la distribution globale dans le swarm
     */

    public List<Integer> selectRarestPieces(PieceBitfield ourBitfield,
                                            PieceBitfield peerBitfield,
                                            int count) {
        List<Integer> availablePieces = peerBitfield.getMissingPieces(ourBitfield);

        //Trier par rareté (moins de peers = plus rare)
        availablePieces.sort((a,b)->pieceRarity.getOrDefault(a,0)-pieceRarity.getOrDefault(b,0));

        List<Integer> selected = new ArrayList<>();
        for(Integer piece : availablePieces) {
            if(!requestedPieces.contains(piece) && selected.size() < count) {
                selected.add(piece);
                requestedPieces.add(piece);
            }
        }
        System.out.println("Selectionné :"+selected.size()+" pièces rares "+selected);
        return selected;
    }
    /**
     * Envoie une requête pour une pièce spécifique
     * Format: <len><id><index><begin><length>
     */
    public void requestedPiece(DataOutputStream output,int pieceIndex,
                               int offset,int length) throws IOException {
        ByteBuffer request = ByteBuffer.allocate(17);
        request.putInt(13); // Longueur du message
        request.put((byte) 6); // ID du message REQUEST
        request.putInt(pieceIndex); // Index de la pièce
        request.putInt(offset); // Offset dans la pièce
        request.putInt(length); // Longueur demandée

        output.write(request.array());
        output.flush();


        System.out.println("Requête envoyée - Pièce: " + pieceIndex +
                ", Offset: " + offset + ", Longueur: " + length);
    }


}
