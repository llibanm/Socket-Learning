package core_system_BitTorrent;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackerClient {
    private TorrentFile torrent;
    private String clientId;
    private int port;
    private long uploaded = 0;    // Total bytes uploadés
    private long downloaded;  // Total bytes téléchargés
    private long left;       // Bytes restant à télécharger


    public enum TrackerEvent{
        STARTED,
        STOPPED,
        COMPLETED,
        EMPTY
    }


    public TrackerClient(TorrentFile torrent,String clientId,int port) {
        this.torrent = torrent;
        this.clientId = clientId;
        this.port = port;
        this.uploaded = 0;
        this.downloaded = 0;
        this.left = torrent.getFileSize();

        System.out.println("📡 TrackerClient initialisé pour: " + torrent.getFileName());
    }

    /**
     * Contacte un tracker et récupère la liste des peers
     *
     * URL typique du tracker:
     * http://tracker.com/announce?info_hash=...&peer_id=...&port=...&uploaded=...&downloaded=...&left=...&event=started
     */

    public List<Peer> contactTracker(String trackerUrl, TrackerEvent event){
        System.out.println("\n🔍 Contact du tracker: " + trackerUrl);
        System.out.println("   Event: " + event);

        List<Peer> discoveredPeers = new ArrayList<Peer>();



            String requestUrl = buildTrackerUrl(trackerUrl, event);
            System.out.println("📤 Requête: " + requestUrl);


            //Map<String, Object> trackerResponse = simulateTrackerResponse();

            //
        return discoveredPeers;
    }

    /**
     * Construit l'URL de requête pour le tracker
     * Suit le protocole BitTorrent standard
     */

    private String buildTrackerUrl(String baseUrl,TrackerEvent event){
        StringBuilder url = new StringBuilder(baseUrl);

        //Ajouter le ? si pas déja présent

        if(!baseUrl.endsWith("?")){
            url.append("?");
        }else url.append("&");

        try {
            // Paramètres obligatoires du protocole BitTorrent
            url.append("info_hash=").append(urlEncoder(torrent.getInfoHash()));
            url.append("&peer_id=").append(URLEncoder.encode(clientId,"UTF-8"));
            url.append("&port=").append(port);
            url.append("&uploaded=").append(uploaded);
            url.append("&downloaded=").append(downloaded);
            url.append("&left=").append(left);
            url.append("&compact=1");

            if(event != TrackerEvent.EMPTY){
                url.append("&event=").append(URLEncoder.encode(event.toString().toLowerCase()));
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur construction URL: " + e.getMessage());
        }
        return url.toString();
    }


    /**
     * Encode les bytes en format URL (pour info_hash)
     */

    private String urlEncoder(byte[] bytes){
        StringBuilder result = new StringBuilder();
        for(byte b : bytes){
            result.append(String.format("%%%02X",b & 0xFF));
        }
        return result.toString();
    }

    /**
     * Parse la réponse du tracker et crée des objets Peer
     */

    @SuppressWarnings("unchecked")
    private List<Peer> parseTrackerResponse(Map<String, Object> response) {
        List<Peer> peers = new ArrayList<>();
        try {
            List<Map<String,Object>> peerList = (List<Map<String, Object>>) response.get("peers");

            for(Map<String, Object> peerData : peerList){
                String ip = (String) peerData.get("ip");
                Integer port = (Integer) peerData.get("port");
                String peerId = (String) peerData.get("peer_id");

                Peer peer = new Peer(ip, port,peerId,torrent.getTotalPieces());

                peers.add(peer);
            }
        }catch (Exception e){
            System.out.println("❌ Erreur parsing réponse tracker: " + e.getMessage());
        }
        return peers;
    }

    /**
     * Met à jour les statistiques de téléchargement
     */
    public void updateStats(long newUploaded, long newDownloaded, long newLeft) {
        this.uploaded = newUploaded;
        this.downloaded = newDownloaded;
        this.left = newLeft;

        System.out.println("📈 Stats mises à jour:");
        System.out.println("   Uploadé: " + uploaded + " bytes");
        System.out.println("   Téléchargé: " + downloaded + " bytes");
        System.out.println("   Restant: " + left + " bytes");
    }
}
