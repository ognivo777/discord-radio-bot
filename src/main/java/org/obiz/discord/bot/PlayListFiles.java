package org.obiz.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayListFiles extends PlayListBase implements PlayList {

    ArrayList<AudioTrack> tracks = new ArrayList<>();

    public PlayListFiles(AudioPlayerManager playerManager) {
        super(playerManager);
        loadList();
    }

    @Override
    protected void loadList() {
        sourceManager = playerManager.source(LocalAudioSourceManager.class);
        try {
            AtomicInteger count = new AtomicInteger();
            String filesPath = RadioV2.files;
            System.out.println("Using directory with media files: " + filesPath);
            DirectoryStream<Path> tracks = Files.newDirectoryStream(Paths.get(filesPath));
            tracks.forEach(
                    path -> {
                        Path absolutePath = path.toAbsolutePath();
                        System.out.println(absolutePath);
                        //todo specify count
                        loadNextInfo(count.getAndIncrement(), 0, absolutePath.toString());
                    }
            );
            LinkedList<AudioTrack> newNextTracks = new LinkedList<>();
            for (int i = 0; i < nextTracks.size(); i++) {
                int index = random.nextInt(nextTracks.size());
                newNextTracks.add(nextTracks.get(index));
                nextTracks.remove(index);
            }
            nextTracks = newNextTracks;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void getLoadTrackDelay() throws InterruptedException {
        //Thread.sleep(500);
    }

    @Override
    protected Optional<AudioTrack> getTrackAfterLoad() {
        if(tracks.size()>0) {
            Optional<AudioTrack> result = null;
            do {
                result = getTrackAfterLoad(random.nextInt(tracks.size()));
            } while (!result.isPresent() || result.get().equals(getCurrent()));
            return result;
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected Optional<AudioTrack> getTrackAfterLoad(int num) {
        if(tracks.size()>num) {
            return Optional.of(tracks.get(num));
        }
        return Optional.empty();
    }
}
