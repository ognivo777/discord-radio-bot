package org.obiz.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.*;
import java.util.concurrent.*;

public class PlayList {

    private static String[] urls_ = new String[] {
            "https://www.youtube.com/watch?v=fsbpWD-bAC0", //18 2 second test video
            "https://www.youtube.com/watch?v=umh2XG5hZWw" //19 16 sec test video
    };

    private static String[] urls = new String[] {
            "https://www.youtube.com/watch?v=V68T7BxLsfk", // 0 jazz +
            "https://www.youtube.com/watch?v=2TvWZEVf6go", // 1 work +
            "https://www.youtube.com/watch?v=5qap5aO4i9A", // 2 ip hop stream X
//            "https://www.youtube.com/watch?v=DqtD9f9OYLk", // 4 Christmas Jazz Instrumental
            "https://www.youtube.com/watch?v=j1w9_z4FssM", // 5 Rotoscoping +
            "https://www.youtube.com/watch?v=c_iRx2Un07k", // 6 Kygo - Piano Jam For Studying and Sleeping[1 HOUR] [2020] +
            "https://www.youtube.com/watch?v=_dmOgDlWAkU", // 7 10 Pieces by Ludovico Einaudi \\ Relaxing Piano [1 HOUR] +
//            "https://www.youtube.com/watch?v=-5KAN9_CzSA", // 8 coffee shop radio // 24/7 lofi hip-hop beats
            "https://www.youtube.com/watch?v=lTRiuFIWV54", // 9 1 A.M Study Session 📚 - [lofi hip hop/chill beats]
            "https://www.youtube.com/watch?v=wAPCSnAhhC8", //10 A.M Study Session 📚 - [lofi hip hop/chill beats]
//            "https://www.youtube.com/watch?v=yhsKEw9quf0", //11 vintage songs but you're a teenager in love
//            "https://www.youtube.com/watch?v=tb0B3auGbtA", //12 VINTAGE RADIO (LIVE OLDIES 24/7!)
            "https://www.youtube.com/watch?v=lcYJhHqotIQ", //13 24/7 같이해요, 로파이 노동요 relax/study with lofi
//            "https://www.youtube.com/watch?v=oVi5gtzTDx0", //14 Indie / Bedroom / Pop / Surf Rock - 24/7 Radio - Nice Guys Chill FM
            "https://www.youtube.com/watch?v=WI4-HUn8dFc", //15 ＳＰＡＣＥ　ＴＲＩＰ　ＩＩ [ Chillwave - Synthwave - Retrowave Mix ]
            "https://www.youtube.com/watch?v=2LBThuN9nP8", //16 vietnam war rock n roll
//            "https://www.youtube.com/watch?v=0h5aHJaZSn4", //17 30 seconds test video
//            "https://www.youtube.com/watch?v=fsbpWD-bAC0", //18 2 second test video
//            "https://www.youtube.com/watch?v=umh2XG5hZWw", //19 16 sec test video
//            "https://www.youtube.com/watch?v=KvRVky0r7YM", //20 Progressive House · Relaxing Focus Music · 24/7 Live Radio
            "https://www.youtube.com/watch?v=HaHBao4W2oI", //Oriental Chillout — Relaxing Music — Inspiring Hong Kong
            "",
            "https://www.youtube.com/watch?v=3bTittbGYGk", //Best Of 2020 Mix ♫ 1 Hour Gaming Music ♫ Trap x House x Dubstep x EDM x Bass
            "https://www.youtube.com/watch?v=ygUe7woTJMM", //1 Hour - Best Music for Relaxing Studying Vol 1 - Anime Edition
            "https://www.youtube.com/watch?v=-7jjo8UICjQ", //Most Iconic Classical Music Masterpieces Everyone Knows in One Single Video
            "https://www.youtube.com/watch?v=-gDinVAmtA0", //Chopin Nocturnes
            "https://www.youtube.com/watch?v=47tWcmDtG6U", //20 classical composers masterpieces
            "https://www.youtube.com/watch?v=-G1SioNco3Y", //1 hour of aesthetic music
            "https://www.youtube.com/watch?v=IqiTJK_uzUY", //Hans Zimmer | ULTIMATE Soundtrack Compilation Mix
            "https://www.youtube.com/watch?v=dB_CuFcJyXI", //1 HOUR of Instrumental Dire Straits and Mark Knopfler
            "",
            "https://www.youtube.com/watch?v=aJoo79OwZEI", //Liquid Drum and Bass Mix #60
            "https://www.youtube.com/watch?v=OiuKZAkYqyE", //Liquid Drum and Bass Mix #84
            "",
            "",
            "https://www.youtube.com/watch?v=MoKv33fiMWo", //Hardest Hardcore Mix 2014 -1 Hour- Melody Only (HQ+HD) TRACKLIST IN DESCRIPTION!!
            "https://www.youtube.com/watch?v=p8VOzcwHbAE", //Angerfist - The Smashup
            "",
            "https://www.youtube.com/watch?v=BxDiQhNO780", //Смешать хэви-метал и хард-рок музыку инструментальных метала, чтобы поднять настроение сильным
            "https://www.youtube.com/watch?v=FrbKBvyaVts", //Best of instrumental Soft Rock
            "https://www.youtube.com/watch?v=SHmYRXNURTo", //Rock Instrumental Music - Acoustic guitar covers of rock popular songs
            "https://www.youtube.com/watch?v=htCcgpisgtk", //Ultimate Hard Rock/Metal Mix Playlist
            "",
            "https://www.youtube.com/watch?v=vC2xJ3JDokk", //1 HOUR BEST GAMING MIX {ELECTRO, HARD DANCE, DUBSTEP, DRUMSTEP}
            "https://www.youtube.com/watch?v=ku65o0B64d8", //POWER HOUR 2018 | 1 Hour Of Best Hardstyle & Hardcore Songs
            "https://www.youtube.com/watch?v=YGdZFwhev8o" //Best Brutal Dubstep Mix 2016 [2 HOUR LONG GAMING MUSIC]
//            ,""
//            ,""
    };

    private Map<Integer, AudioTrackInfo> trackInfoMap = new TreeMap<>();

    private AudioPlayerManager playerManager;

    private LinkedList<AudioTrack> prevTracks = new LinkedList<>();
    private LinkedList<AudioTrack> nextTracks = new LinkedList<>();
    private AudioTrack currTrack;
    private String currentUrl="";

    private Random random;

    public PlayList(AudioPlayerManager playerManager) {
        this.playerManager = playerManager;
        random = new Random(System.currentTimeMillis());

        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            if(!url.isEmpty()) {
                AudioTrackInfo info = null;
                try {
                    System.out.println("Load info for : " + i + "/" + (url.length()-1) + " " + url);
                    Thread.sleep(500);
                    Future<AudioTrack> audioTrackFuture = loadTrack(url);
                    info = audioTrackFuture.get(5, TimeUnit.SECONDS).getInfo();
                    trackInfoMap.put(i, info);
//                    trackInfoMap.put(i, new AudioTrackInfo("tst", "", 0, "", false, url ));
                } catch (InterruptedException
                        | ExecutionException | TimeoutException
                        e) {
                    System.out.println("Can't load info for: " + url);
                    e.printStackTrace();
                }
            }
        }
    }

    private AudioTrack cloneTrack(AudioTrack track) {
        AudioTrack result = track.makeClone();
        result.setPosition(track.getPosition());
        return result;
    }


    public AudioTrack trackByNum(Integer trackNum) {
        System.out.println("Load trackByNum for trackNum = " + trackNum);
        if(currTrack!=null) {
            prevTracks.add(cloneTrack(currTrack));
        }
        prevTracks.addAll(nextTracks);

//        while(prevTracks.size()>15) {
//            prevTracks.removeFirst();
//        }

        AudioTrack track = getTrackAfterLoad(urls[trackNum]);
        nextTracks.clear();

        currTrack = track;
        return track;
    }

    public AudioTrack getNext() {
        if(currTrack!=null) {
            prevTracks.add(cloneTrack(currTrack));
//            while(prevTracks.size()>15) {
//                prevTracks.removeFirst();
//            }
        }

        if(nextTracks.isEmpty()) {
            AudioTrack track = getTrackAfterLoad(getNextRandomUrl());
            currTrack = track;
            return track;
        } else {
            return currTrack = nextTracks.pollLast();
        }
    }

    public AudioTrack getPrev() {
        if(currTrack!=null) {
            nextTracks.add(cloneTrack(currTrack));
//            while(nextTracks.size()>15) {
//                nextTracks.removeFirst();
//            }
        }

        if(prevTracks.isEmpty()) {
            AudioTrack track = getTrackAfterLoad(getNextRandomUrl());
            currTrack = track;
            return track;
        } else  {
            return currTrack = prevTracks.pollLast();
        }
    }

    private String getNextRandomUrl() {
        String result;
        do {
            result = urls[random.nextInt(urls.length)];
        } while (result.isEmpty() || result.equals(currentUrl));
        return currentUrl=result;
    }


    private AudioTrack getTrackAfterLoad(String url) {
        try {
            return loadTrack(url).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Future<AudioTrack> loadTrack(String url) {

        BlockingQueue<AudioTrack> reply = new ArrayBlockingQueue<>(1);

        Future<AudioTrack> result =  Executors.newSingleThreadExecutor().submit(reply::take);

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                reply.add(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {}

            @Override
            public void noMatches() {
                System.out.println("No matches on LOAD_TRACK");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println(exception);
            }
        });

        return result;
    }

    //не работает
    private AudioTrack loadTrackV2(String url) {

        final AudioTrack[] reply = {null};

        try {
            playerManager.loadItem(url, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    reply[0] = track;
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {}

                @Override
                public void noMatches() {}

                @Override
                public void loadFailed(FriendlyException exception) {}
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return reply[0];
    }

    public AudioTrack getCurrentFromStart() {
        return currTrack = currTrack.makeClone();
    }

    public AudioTrack getCurrent() {
        return currTrack;
    }

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

    public Map<Integer, AudioTrackInfo> getTrackInfoMap() {
        return trackInfoMap;
    }

    public void clearList() {
        System.out.println("Clear queue");
        nextTracks.clear();
        prevTracks.clear();
    }
}
