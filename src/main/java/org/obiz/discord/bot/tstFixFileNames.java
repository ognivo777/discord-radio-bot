package org.obiz.discord.bot;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.obiz.discord.bot.YTUtils.getService;
import static org.obiz.discord.bot.YTUtils.getYouTubeData;

public class tstFixFileNames {

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        DirectoryStream<Path> tracks = Files.newDirectoryStream(Paths.get("D:\\Media\\botmusic\\yt"));
        YouTube yt = getService();
        tracks.forEach(
                path -> {
                    String fileName = path.getFileName().toString();
                    if (!fileName.endsWith("m4a"))
                        return;
                    String key = fileName.substring(0, 11);
                    System.out.println(fileName + ":" + key);
                    Video ytVid = getYouTubeData(yt, key);
                    //ytVid.getSnippet().getThumbnails().getStandard();
                    try {
                        AudioFile audioFile = AudioFileIO.read(path.toFile());
                        Tag tag = audioFile.getTag();
                        tag.setField(FieldKey.TITLE, ytVid.getSnippet().getTitle());
                        tag.setField(FieldKey.COMMENT, ytVid.getSnippet().getDescription());
                        tag.setField(FieldKey.KEY, ytVid.getId());
                        List<String> tags = ytVid.getSnippet().getTags();
                        if (tags != null) {
                            String joinedTags = tags.stream().collect(Collectors.joining(","));
                            tag.setField(FieldKey.TAGS, joinedTags);
                        }
                        audioFile.commit();
                    } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
                        e.printStackTrace();
                    }
//                    fixNames(path, fileName, key);
                }
        );
    }

    protected static void fixNames(Path path, String fileName, String substring) {
        substring = substring.substring(3);
        String finalSubstring = substring;
        Optional<String> link = Arrays.stream(PlayListFixedYoutubeList.urls).filter(s -> s.contains(finalSubstring)).findFirst();
        if (link.isPresent()) {
            System.out.println("Link: " + link.get());
            String newName = link.get().split("=")[1] + "." + fileName.split("\\.")[1];
            System.out.println(fileName);
            System.out.println(newName);
            try {
                Files.move(path, path.resolveSibling(newName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
