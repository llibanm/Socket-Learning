package core_system_BitTorrent;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TrackerClient {
    private String trackerUrl;

    public TrackerClient(String trackerUrl) {
        this.trackerUrl = trackerUrl;
    }

    public List<PeerInfo> announceToTracker(byte[] infoHash, String peerId, int port,
                                            long downloaded, long uploaded,long left) {
        List<PeerInfo> peerInfos = new ArrayList<>();

        try {
            String params = String.format("?info_hash=%s&peer_id=%s&port=%d&downloaded=%d&uploaded=%d&left=%d",
                    urlEncode(infoHash), peerId, port, downloaded, uploaded, left);

            URL url = new URL(trackerUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            System.out.println("Envoi requête au tracker: "+ url);

            peerInfos.add(new PeerInfo("192.168.1.100", 6881));
            peerInfos.add(new PeerInfo("192.168.1.101", 6882));
            peerInfos.add(new PeerInfo("10.0.0.50", 6883));

        }catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return peerInfos;
    }

    private String urlEncode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }


}

