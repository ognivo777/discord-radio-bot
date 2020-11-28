package org.obiz.discord.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Bot {

    private static final String NEXT_EMOJI = "\u23ED";
    private static final String PREV_EMOJI = "\u23EE";
    private static final String PLAY_EMOJI = "\u23EF";
    private static final String REPEAT_EMOJI = "\uD83D\uDD01"; //U+1F501
    private static final String ROLL_EMOJI = "\uD83D\uDCDC"; //U+1F4DC
    private static final String CLEAR_EMOJI = "\uD83C\uDD91"; //U+1F191  //U+1F191

    private static final String NUM_1 = "\u0031\u20E3"; //U+E21C "\uE21C"- "\u0031\u20E3"+
    private static final String NUM_2 = "\u0032\u20E3"; //U+E21D "\uFE0F"- "\u0032\u20E3"
    private static final String NUM_3 = "\u0033\u20E3"; //U+E21E "\uE21E" "\u0033\u20E3"+
    private static final String NUM_4 = "\u0034\u20E3"; //U+E21F "\uE21F"
    private static final String NUM_5 = "\u0035\u20E3"; //U+E220 "\uE220"
    private static final String NUM_6 = "\u0036\u20E3"; //U+E221
    private static final String NUM_7 = "\u0037\u20E3"; //U+E222
    private static final String NUM_8 = "\u0038\u20E3"; //U+E223
    private static final String NUM_9 = "\u0039\u20E3"; //0xE224
    private static final String NUM_0 = "\u0030\u20E3"; //U+E225


    private DiscordApi api;
    private final ServerVoiceChannel radioChannel;
    private final TextChannel generalChannel;
    private PlayList playList;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private Message myMessage;
    private boolean isRepeatOn = false;
    private Map<Long, List<Integer>> privateMessagesWithTracks = new HashMap<>();


    public Bot(String key, long commandChannelId, long radioChannelId) {
        System.out.println("Key: " + key);
        api = new DiscordApiBuilder().setToken(key).login().join();

        radioChannel = api.getServerVoiceChannelById(radioChannelId).get();

        generalChannel = api.getTextChannelById(commandChannelId).get();

        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        player = playerManager.createPlayer();
        playList = new PlayList(playerManager);
    }

    public void start() throws ExecutionException, InterruptedException {
        Optional<Message> foundMessage = generalChannel.getMessagesAsStream().limit(10).filter(message -> {
            Optional<User> userAuthor = message.getUserAuthor();
            return userAuthor.isPresent() && userAuthor.get().isYourself();
        }).findFirst();
        if(foundMessage.isPresent()) {
            myMessage = foundMessage.get();
            System.out.println("Found old message at: " + myMessage.getCreationTimestamp());
            System.out.println("Try to use it..");
            myMessage.edit("Hi there!");
        } else {
            myMessage = generalChannel.sendMessage("Hi!").get();
            myMessage.addReaction(PREV_EMOJI).join();
            myMessage.addReaction(NEXT_EMOJI).join();
            myMessage.addReaction(PLAY_EMOJI).join();
            myMessage.addReaction(REPEAT_EMOJI).join();
            myMessage.addReaction(CLEAR_EMOJI).join();
            myMessage.addReaction(ROLL_EMOJI).join();
        }


        myMessage.addReactionAddListener(this::handleEmoji);
        myMessage.addReactionRemoveListener(this::handleEmoji);

        radioChannel.addServerVoiceChannelMemberJoinListener(this::handleJoin);
        radioChannel.addServerVoiceChannelMemberLeaveListener(this::handleLeave);

        player.addListener(new AudioEventAdapter() {
           public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
               //ToDo try to use endReason.mayStartNext for check instead of '"REPLACED"'
               if(!endReason.name().equals("REPLACED")) {
                   if(isRepeatOn) {
                       playTrack(playList.getCurrentFromStart());
                   } else {
                       player.stopTrack();
                       playList.getCurrent().setPosition(0);
                       playTrack(playList.getNext());
                   }
               }
           }
        });

        long delay = 3000L;
        new Timer("Timer").scheduleAtFixedRate(new TimerTask() {
            public void run() { updateMessage(); }
        }, delay, delay);

        radioChannel.connect().thenAccept(audioConnection -> {
            AudioSource source = new LavaplayerAudioSource(api, player);
            audioConnection.setAudioSource(source);
            playTrack(playList.getNext());
        });

    }

    private void playTrack(AudioTrack track) {
        player.playTrack(track);
        updateMessage();
    }

    private void updateMessage() {
        AudioTrack track = playList.getCurrent();
        if(track!=null) {
            myMessage.edit((isRepeatOn ? REPEAT_EMOJI + " " : "") + "now playing: " + track.getInfo().title
                    + " (" + timeInMsAsString(track.getPosition())
                    + " of " + timeInMsAsString(track.getDuration())
                    + ")"
                    + "\n```" + playList.showPosInQueue()
                    + "\n" + track.getInfo().uri
                    + "```"
            );
        }
    }

    private void handleJoin(ServerVoiceChannelMemberJoinEvent event) {
        System.out.println("Sombdy join");
        //ToDo check if player paused - unpause them
    }

    private void handleLeave(ServerVoiceChannelMemberLeaveEvent event) {
        System.out.println("Sombdy leave");
        //Todo check if nobody left in channel - pause player
    }

    private void handleEmoji(SingleReactionEvent event) {
        Optional<User> optionalUser = event.getUser();
        if(optionalUser.isPresent()) {
            System.out.println("User " + optionalUser.get().getNicknameMentionTag() + " use reaction");
        } else {
            System.out.println("Reaction user not found!");
        }

        Optional<String> optionalEmoji = event.getEmoji().asUnicodeEmoji();
        if(!optionalEmoji.isPresent()) {
            System.out.println("Emoji not found!");
        } else {
            String emoji = optionalEmoji.get();
            for (char c : emoji.toCharArray()) {
                System.out.println("Name of emoji: " + Character.getName(c));
                System.out.println("Num val of emoji: " + Character.getNumericValue(c));

            }
            if (event.getUserId() == api.getYourself().getId()) {
                return;
            }
            if (event.getMessageId() == myMessage.getId()) {
                if (!mainButtonsHandler(event)) {
                    System.out.println("unknown emoji [1]: " + event.getEmoji().getMentionTag());
                }
            } else {
                if (!privatePlayListButtonsHandler(event)) {
                    System.out.println("unknown emoji [2]: " + event.getEmoji().getMentionTag());
                }
            }
        }
    }

    private boolean mainButtonsHandler(SingleReactionEvent event) {
        switch (event.getEmoji().asUnicodeEmoji().get()) {
            case NEXT_EMOJI:
                playTrack(playList.getNext());
                break;
            case PREV_EMOJI:
                playTrack(playList.getPrev());
                break;
            case PLAY_EMOJI:
                playTrack(playList.getCurrentFromStart());
                break;
            case CLEAR_EMOJI:
                playList.clearList();
                updateMessage();
                break;
            case ROLL_EMOJI:
                showPlayListFor(event.getUserId());
                break;
            case REPEAT_EMOJI:
                isRepeatOn = ! isRepeatOn;
                updateMessage();
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean privatePlayListButtonsHandler(SingleReactionEvent event) {
        int subNum = 0;
        switch (event.getEmoji().asUnicodeEmoji().get()) {
            case NUM_1:
                subNum = 1;
                break;
            case NUM_2:
                subNum = 2;
                break;
            case NUM_3:
                subNum = 3;
                break;
            case NUM_4:
                subNum = 4;
                break;
            case NUM_5:
                subNum = 5;
                break;
            default:
                return false;
        }
        Integer trackNum = privateMessagesWithTracks.get(event.getMessageId()).get(subNum-1);
        playTrack(playList.trackByNum(trackNum));
        return true;
    }

    /*
    * print list of tracks to private message and save message id for react on track num reactions*/
    private void showPlayListFor(long userId) {
        Map<Integer, AudioTrackInfo> trackInfoMap = playList.getTrackInfoMap();
        System.out.println("Show play list for: " + userId);
        StringBuilder message = new StringBuilder();
        message.append("Current track list:\n```");
        int bunchSize = 5;
        List<Integer> trackInCurrentMesage = new ArrayList<>();
        for (Map.Entry<Integer, AudioTrackInfo> item : trackInfoMap.entrySet()) {
            trackInCurrentMesage.add(item.getKey());
            message.append('\n')
                    .append(trackInCurrentMesage.size());
            if(!item.getValue().isStream)
                message.append(" ").append(timeInMsAsString(item.getValue().length));
            else
                message.append(" _STREAM_");
            message.append(" ").append(item.getValue().title)
                    .append(" [").append(item.getKey()).append("]")
                    .append("").append(item.getValue().uri);
            if(trackInCurrentMesage.size()==bunchSize) {
                privateMessagesWithTracks.put(sendNextBunchOfSongs(message, userId), trackInCurrentMesage);
                trackInCurrentMesage = new ArrayList<>();
            }
        }
        if(trackInCurrentMesage.size()>0) {
            privateMessagesWithTracks.put(sendNextBunchOfSongs(message, userId), trackInCurrentMesage);
        }
    }

    private long sendNextBunchOfSongs(StringBuilder message, long userId) {
        message.append("```");
        long messageID = 0;
        try {
            Message sentMessage = api.getUserById(userId)
                    .get(2, TimeUnit.SECONDS)
                    .openPrivateChannel()
                    .get(2, TimeUnit.SECONDS)
                    .sendMessage(message.toString()).get(3, TimeUnit.SECONDS);
            messageID = sentMessage.getId();

            System.out.println("Sent messageID = " + messageID);
            sentMessage.addReaction(NUM_1).join();
            sentMessage.addReaction(NUM_2).join();
            sentMessage.addReaction(NUM_3).join();
            sentMessage.addReaction(NUM_4).join();
            sentMessage.addReaction(NUM_5).join();

            sentMessage.addReactionAddListener(this::handleEmoji);
            sentMessage.addReactionRemoveListener(this::handleEmoji);

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Can't send message for " + userId);
            e.printStackTrace();
        }
        message.setLength(0);
        message.append("```");
//        if()
        return messageID;
    }


    public static String timeInMsAsString(long duration) {
//        StringBuilder b = new StringBuilder();
        long totalSeconds = duration / 1000;
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long hours = totalMinutes / 60;
        //TODO: replace with String.format
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
//        return b
//                .append(hours)
//                .append(":")
//                .append(minutes)
//                .append(":")
//                .append(seconds)
//                .toString();
    }
}
