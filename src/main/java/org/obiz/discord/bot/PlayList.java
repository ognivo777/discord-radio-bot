package org.obiz.discord.bot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.Map;

public interface PlayList {
    AudioTrack trackByNum(Integer trackNum);

    AudioTrack getNext();

    AudioTrack getPrev();

    AudioTrack getCurrentFromStart();

    AudioTrack getCurrent();

    String showPosInQueue();

    Map<Integer, AudioTrackInfo> getTrackInfoMap();

    void clearList();

    String getUriInfo();

    AudioTrack getCurrentWithClone();

    void insert(AudioTrack next);
}
