package org.obiz.discord.bot;

import com.google.api.services.youtube.YouTube;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class TestYT {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        YouTube service = YTUtils.getService();
        YouTube.Videos.List list = service.videos().list("id,snippet,contentDetails,topicDetails");
        list.setKey(args[0]);
        list.setId("jQbgkcd2x7w");
        list.execute().getItems().forEach(video -> {
            System.out.println("video.getId() = " + video.getId());
            System.out.println("video.getContentDetails().getCaption() = " + video.getContentDetails().getCaption());
            System.out.println("video.getSnippet().getTitle() = " + video.getSnippet().getTitle());
            String description = video.getSnippet().getDescription();
            System.out.println("video.getSnippet().getDescription() = " + description);
            System.out.println("=================================");
            String timeStrPattern = ".+((?:\\d?\\d:){1,2}\\d?\\d).+";
            Pattern timePattern = Pattern.compile(timeStrPattern);
            Arrays.stream(description.split("\n"))
                    .filter(s -> {
                        return s.matches(timeStrPattern);
                    })
                    .map(s -> s.replaceAll("[^\\d\\w :-]+", ""))
                    .forEach(s -> {
                        System.out.println(s);
                    });
        });
    }
}
