package org.obiz.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.*;
import java.util.concurrent.*;

public abstract class PlayListBase implements PlayList {

    protected Map<Integer, AudioTrackInfo> trackInfoMap = new TreeMap<>();

    protected final Random random = new Random(System.currentTimeMillis());

    protected AudioPlayerManager playerManager;

    private LinkedList<AudioTrack> prevTracks = new LinkedList<>();
    protected LinkedList<AudioTrack> nextTracks = new LinkedList<>();
    private AudioTrack currTrack;

    protected AudioSourceManager sourceManager;

    public PlayListBase(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    protected abstract void loadList();

    protected void loadNextInfo(int i, int length, String url) {
        if(!url.isEmpty()) {
            AudioTrackInfo info = null;
            try {
                System.out.println("Load info for : " + i + "/" +length + ": " + url);
                getLoadTrackDelay();
                Optional<AudioTrack> optionalAudioTrack = loadTrack(url).get(5, TimeUnit.SECONDS);
                if(optionalAudioTrack.isPresent()) {
                    AudioTrack track = optionalAudioTrack.get();
                    nextTracks.add(track);
                    info = track.getInfo();
                    trackInfoMap.put(i, info);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.out.println("Can't load info for: " + url);
                e.printStackTrace();
            }
        }
    }

    protected void getLoadTrackDelay() throws InterruptedException {
        Thread.sleep(500);
    }

    private AudioTrack cloneTrack(AudioTrack track) {
        AudioTrack result = track.makeClone();
        result.setPosition(track.getPosition());
        return result;
    }


    @Override
    public AudioTrack trackByNum(Integer trackNum) {
        System.out.println("Load trackByNum for trackNum = " + trackNum);
        if(currTrack!=null) {
            prevTracks.add(cloneTrack(currTrack));
        }
        prevTracks.addAll(nextTracks);

//        while(prevTracks.size()>15) {
//            prevTracks.removeFirst();
//        }

        AudioTrack track = getTrackAfterLoad(trackNum).orElseGet(() -> currTrack);
        nextTracks.clear();

        currTrack = track;
        return track;
    }

    @Override
    public AudioTrack getNext() {
        if(currTrack!=null) {
            prevTracks.add(cloneTrack(currTrack));
        }

        if(nextTracks.isEmpty()) {
            return currTrack = prevTracks.pollFirst();
        } else {
            return currTrack = nextTracks.pollLast();
        }
    }


    @Override
    public void insert(AudioTrack next) {
        if(currTrack!=null) {
            prevTracks.add(cloneTrack(currTrack));
        }

        currTrack = next;

    }

    abstract protected Optional<AudioTrack> getTrackAfterLoad();
    abstract protected Optional<AudioTrack> getTrackAfterLoad(int num);

    @Override
    public AudioTrack getPrev() {
        if(currTrack!=null) {
            nextTracks.add(cloneTrack(currTrack));
//            while(nextTracks.size()>15) {
//                nextTracks.removeFirst();
//            }
        }

        if(prevTracks.isEmpty()) {
            return currTrack = nextTracks.pollFirst();
//            AudioTrack track = getTrackAfterLoad().orElseGet(() -> currTrack);
//            currTrack = track;
//            return track;
        } else  {
            return currTrack = prevTracks.pollLast();
        }
    }


    protected Optional<AudioTrack> getTrackAfterLoad(String url) {
        try {
            Optional<AudioTrack> optionalAudioTrack = loadTrack(url).get(5, TimeUnit.SECONDS);
            return optionalAudioTrack;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Future<Optional<AudioTrack>> loadTrack(String url) {

        BlockingQueue<Optional<AudioTrack>> reply = new ArrayBlockingQueue<>(1);

        Future<Optional<AudioTrack>> result =  Executors.newSingleThreadExecutor().submit(reply::take);

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                reply.add(Optional.ofNullable(track));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {}

            @Override
            public void noMatches() {
                System.out.println("No matches on LOAD_TRACK");
                reply.add(Optional.empty());
            }

            @Override
            public void loadFailed(FriendlyException exception) {
//                System.out.println(exception);
                exception.printStackTrace();
                reply.add(Optional.empty());
            }
        });

        return result;
    }

    @Override
    public AudioTrack getCurrentFromStart() {
        return currTrack = currTrack.makeClone();
    }

    @Override
    public AudioTrack getCurrent() {
        if(currTrack==null) {
            return getNext();
        }
        return currTrack;
    }

    @Override
    public AudioTrack getCurrentWithClone() {
        if(currTrack==null) {
            return getNext();
        }
        currTrack = currTrack.makeClone();
        return currTrack;
    }

    @Override
    public String showPosInQueue() {
        StringBuilder result = new StringBuilder();
        char[] before = new char[prevTracks.size()];
        Arrays.fill(before, '*');
        result.append(before).append("|");
        char[] after = new char[nextTracks.size()];
        Arrays.fill(after, '*');
        result.append(after);
        return result.toString();
    }

    @Override
    public Map<Integer, AudioTrackInfo> getTrackInfoMap() {
        return trackInfoMap;
    }

    @Override
    public void clearList() {
        System.out.println("Clear queue");
        nextTracks.clear();
        prevTracks.clear();
    }

    @Override
    public String getUriInfo() {
        String uri = getCurrent().getInfo().uri;
        String [] uriParts = uri.split("[\\\\/]");
        return uriParts[uriParts.length-1];
    }
}
