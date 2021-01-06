package org.obiz.discord.bot;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class YTUtils {

    private static final String APPLICATION_NAME = "Discord radio";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    protected static Video getYouTubeData(YouTube yt, String key) {
        try {
            YouTube.Videos.List search = yt.videos().list("id,snippet").set("id", key);
            search.setKey(RadioV2.GOOGLE_DEVELOPER_KEY);
            VideoListResponse listResponse = search.execute();
            List<Video> items = listResponse.getItems();
            for (Video item : items) {
                System.out.println("item.getId() = " + item.getId());
                System.out.println("item.getSnippet().getTitle() = " + item.getSnippet().getTitle());
                if (item.getSnippet().getTags() != null) {
                    for (String tag : item.getSnippet().getTags()) {
                        System.out.print("[" + tag + "]");
                    }
                    System.out.println();
                }
                return item;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
