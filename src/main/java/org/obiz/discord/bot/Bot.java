package org.obiz.discord.bot;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.reaction.SingleReactionEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Bot {

    private static final String NEXT_EMOJI = "\u23ED";
    private static final String PREV_EMOJI = "\u23EE";
    private static final String PLAY_PAUSE_EMOJI = "\u23EF";
    private static final String PAUSE_EMOJI = "\u23F8";
    private static final String REPEAT_EMOJI = "\uD83D\uDD01"; //U+1F501
    private static final String ROLL_EMOJI = "\uD83D\uDCDC"; //U+1F4DC
    private static final String CLEAR_EMOJI = "\uD83C\uDD91"; //U+1F191  //U+1F191
    private static final String FAST_DOWN_EMOJI = "\u23EC";
    private static final String PLAY_EMOJI = "\uE23A";
    private static final String HEADPHONES_EMOJI = "\uE30A";
    private static final String FLYING_SAUCER_EMOJI = "\uD83D\uDEF8";
    private static final String MUTED_SPEAKER_EMOJI = "\ud83d\udd07"; //""U+1F507";
    private static final String STOP_EMOJI = "\u23F9";
//    private static final String LOUDSPEAKER_EMOJI = "\uE142";
//    private static final String LOUDSPEAKER_EMOJI = "U+1F4E2";
    private static final String LOUDSPEAKER_EMOJI = "\ud83d\udce2";
    private static final String MUSICNOTES_EMOJI = "U+1F3B6";
    private static final String LOW_SOUND_EMOJI = "\ud83d\udd08"; //"U+1F508";
    private static final String MID_SOUND_EMOJI = "\ud83d\udd09"; //"U+1F509";
    private static final String HIGH_SOUND_EMOJI = "\ud83d\udd0a"; //"U+1F50A";

    private static final String FAST_FORWARD_EMOJI = "\u23E9";
    private static final String FAST_REVERSE_EMOJI = "\u23EA";
    private static final String EXCL_QUEST_EMOJI = "\u2049\uFE0F";

    private static final String LINK_EMOJI = "\ud83d\udd17";


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

    private  static final String[] NUMS_1_to_9 = new String[]{
            NUM_1,
            NUM_2,
            NUM_3,
            NUM_4,
            NUM_5,
            NUM_6,
            NUM_7,
            NUM_8,
            NUM_9

    };


    private DiscordApi api;
    private ServerVoiceChannel radioChannel;
    private final TextChannel generalChannel;
    private PlayList playList;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private Message myMessage;
    private boolean isRepeatOn = false;
    private Map<Long, List<Integer>> privateMessagesWithTracks = new HashMap<>();
    private String prevContent = "";
    private final AudioSource source;
    private AudioConnection audioConnection;
    private long beforeLeaveOrPausePosition;
    private YouTube youTube = null;


    public Bot(String key, long commandChannelId, String mode) {
        System.out.println("Key: " + key);
        api = new DiscordApiBuilder().setToken(key).login().join();
        api.createBotInvite(Permissions.fromBitmask(171044160));
//        api.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));

        generalChannel = api.getTextChannelById(commandChannelId).get();

        generalChannel.addMessageCreateListener(this::onMessageCreate);
        generalChannel.addReactionAddListener(this::playOnLinkButtonsHandler);
        generalChannel.addReactionRemoveListener(this::playOnLinkButtonsHandler);
        playerManager = new DefaultAudioPlayerManager();
        player = playerManager.createPlayer();
        source = new LavaplayerAudioSource(api, player);
        if(mode.equals("local")) {
            playerManager.registerSourceManager(new LocalAudioSourceManager());
            playList = new PlayListFiles(playerManager);
//            System.exit(0);
        } else {
            playerManager.registerSourceManager(new YoutubeAudioSourceManager());
            playList = new PlayListFixedYoutubeList(playerManager, "");
        }
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());

        try {
            youTube = YTUtils.getService();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        System.out.println("Disconnect from discord");
        api.disconnect();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 5000);
    }

    public void start() {
        createControlMessage();

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

        long delay = 10000L;
        new Timer("Timer").scheduleAtFixedRate(new TimerTask() {
            public void run() { updateMessage(); }
        }, delay, delay);

    }

    public void onMessageCreate(MessageCreateEvent event) {
        String text = event.getReadableMessageContent();
        newMessageProcessor(text, Optional.of(event.getMessage()));
    }

    private void newMessageProcessor(String text, Optional<Message> newMessage) {
        if(text !=null) {
            if (text.startsWith("r!")) {
                System.out.println("COMMAND: " + text);
                if (text.startsWith("r!y ")) {
                    String url = text.substring(4).trim();
                    loadYTAndPlay(url);
                    newMessage.ifPresent(message -> message.addReaction(PLAY_PAUSE_EMOJI));
                } else if (text.startsWith("r!ys ")) {
                    String term = text.substring(5).trim();
                    if (term.length() > 3) {
                        try {
                            YouTube.Search.List search = youTube.search().list("id,snippet");
                            search.setKey(RadioV2.GOOGLE_DEVELOPER_KEY);
                            search.setQ(term);
                            search.setMaxResults(9L);
                            List<SearchResult> items = search.execute().getItems();
                            String result = "";
                            int num = 0;
                            Map<String, String> numsToIds = new HashMap<>();
                            for (SearchResult item : items) {
                                String id = item.getId().getVideoId();
                                numsToIds.put(NUMS_1_to_9[num], id);
//                                result += NUMS_1_to_9[num] + "`" + item.getSnippet().getTitle() + "` (" + id + ")\n";
                                result += NUMS_1_to_9[num] + " " + item.getSnippet().getTitle() + "\n:link: https://www.youtube.com/watch?v=" + id + "\n";
                                num++;
                            }
                            result += "";
                            generalChannel.sendMessage(result, new EmbedBuilder()).thenAccept(message -> {
                                for (int i = 0; i < NUMS_1_to_9.length; i++) {
                                    String s = NUMS_1_to_9[i];
                                    if (numsToIds.containsKey(s)) {
                                        message.addReaction(s).join();
                                    }
                                }
                                try {
                                    Thread.sleep(2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                numsToIds.forEach((k, v) -> message.addReaction(k).join());
                                message.addReactionAddListener(addReactionEven -> onSearchResultReaction(numsToIds, addReactionEven));
                                message.addReactionRemoveListener(addReactionEven -> onSearchResultReaction(numsToIds, addReactionEven));
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (text.startsWith("r!st ")) {
                    String timeString = text.substring(5);
                    if(timeString.matches("(\\d{1,2}:){0,2}\\d{1,2}")) {
                        String timeParts[] = timeString.split(":");
                        int seconds = 0, minutes = 0, hours = 0;
                        int k = timeParts.length-1;
                        seconds = k<0?0:Integer.parseInt(timeParts[k--]);
                        minutes = k<0?0:Integer.parseInt(timeParts[k--]);
                        hours = k<0?0:Integer.parseInt(timeParts[k--]);
                        System.out.println("seconds = " + seconds);
                        System.out.println("minutes = " + minutes);
                        System.out.println("hours = " + hours);
                        if(!player.isPaused()) {
                            AudioTrack playingTrack = player.getPlayingTrack();
                            int secondsToMove = hours * 60 * 60 + minutes * 60 + seconds;
                            System.out.println("secondsToMove = " + secondsToMove);
                            playingTrack.setPosition(Math.min(playingTrack.getDuration(), secondsToMove *1000));
                            updateMessage();
                        }
                    } else {
                        System.out.println("Wrong time string.");
                    }
                }
            }
        }
    }

    protected void onSearchResultReaction(Map<String, String> numsToIds, SingleReactionEvent addReactionEven) {
        for (String nextKey : numsToIds.keySet()) {
            String emojiAdded = addReactionEven.getEmoji().asUnicodeEmoji().get();
            if(emojiAdded.equals(nextKey)) {
                String url = "https://www.youtube.com/watch?v=" + numsToIds.get(nextKey);
                loadYTAndPlay(url);
            }
        }
    }

    protected void loadYTAndPlay(String url) {
        if(url.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$")) {
            System.out.println("try to load and play..");
            player.setPaused(true);
            //playerManager.registerSourceManager(new YoutubeAudioSourceManager());
            PlayList tmpPlayList = new PlayListFixedYoutubeList(playerManager, url);
            AudioTrack next = tmpPlayList.getNext();
            playList.insert(next);
            playTrack(next);
        }
    }


    protected void createControlMessage() {
        if(myMessage!=null) {
            myMessage.removeAllReactions();
            myMessage.getMessageAttachableListeners().forEach(
                    (l, k) -> myMessage.removeMessageAttachableListener(l)
            );
            myMessage.delete();
        }
        if(true) { //очистка своих сообщений
            generalChannel.getMessagesAsStream().limit(200).filter(message -> {
                Optional<User> userAuthor = message.getUserAuthor();
                return userAuthor.isPresent() && userAuthor.get().isYourself();
            }).forEach(Message::delete);
        }

        Map<String, Message> prevCommands = new HashMap<>();
        generalChannel.getMessagesAsStream().limit(200)
                .filter(message -> {
                            String messageContent = message.getContent();
                            if(messageContent.startsWith("r!y h")) {
                                Message prevSameMessage = prevCommands.put(messageContent, message);
                                if(prevSameMessage!=null) {
                                    prevSameMessage.delete();
                                    return false;
                                }
                                return true;
                            }
                            return false;
                }
                )
                .filter(message -> !message.getReactionByEmoji(PLAY_PAUSE_EMOJI).isPresent())
                .forEach(
                        message -> message.addReaction(PLAY_PAUSE_EMOJI)
                );

        myMessage = generalChannel.sendMessage("Hi!").join();
        myMessage.addReaction(LOUDSPEAKER_EMOJI);
        myMessage.addReaction(MUTED_SPEAKER_EMOJI);
//        myMessage.addReaction(STOP_EMOJI);
        myMessage.addReaction(PREV_EMOJI);
        myMessage.addReaction(NEXT_EMOJI);
        myMessage.addReaction(PLAY_PAUSE_EMOJI);
        myMessage.addReaction(REPEAT_EMOJI);


        myMessage.addReaction(FAST_REVERSE_EMOJI);
        myMessage.addReaction(FAST_FORWARD_EMOJI);

//        myMessage.addReaction(LOW_SOUND_EMOJI);
//        myMessage.addReaction(MID_SOUND_EMOJI);
//        myMessage.addReaction(HIGH_SOUND_EMOJI);

//        myMessage.addReaction(CLEAR_EMOJI);
//        myMessage.addReaction(ROLL_EMOJI);
        myMessage.addReaction(FAST_DOWN_EMOJI);
        myMessage.addReaction(EXCL_QUEST_EMOJI);

        myMessage.addReactionAddListener(this::handleEmoji);
        myMessage.addReactionRemoveListener(this::handleEmoji);
    }

    private String getUnicodeEmoji(String codepoint) {
        String emoji = String.valueOf(Character.toChars(Integer.parseInt(codepoint.substring(2), 16)));
        String hexString = unicodeToStringRepresentation(emoji);
        System.out.println(codepoint + " emoji = " + emoji + " " + hexString);
        return emoji;
    }

    private String unicodeToStringRepresentation(String emoji) {
        String hexString = "";
        for (int i = 0; i < emoji.length(); i++) {
            hexString+=Integer.toHexString(emoji.charAt(i));
        }
        return hexString;

    }

    private void playTrack(AudioTrack track) {
        player.playTrack(track);
        player.setVolume(90);
        player.setPaused(false);
        updateMessage();
    }

    private void updateMessage() {
        AudioTrack track = playList.getCurrent();
        if(track!=null) {
            String content = ""
                    + (isRepeatOn ? REPEAT_EMOJI + " " : "")
                    + (player.isPaused() ? PAUSE_EMOJI + " " : "Now playing: ")
                    + track.getInfo().title
                    + " (" + timeInMsAsString(track.getPosition())
                    + " of " + timeInMsAsString(track.getDuration())
                    + ")"
                    + "\n```"
//                    + playList.showPosInQueue() + "\n"
                    + playList.getUriInfo()
                    + "```";
            if (!content.equals(prevContent)) {
                myMessage.edit(content);
                prevContent = content;
            }
        }
    }

    private void handleJoin(ServerVoiceChannelMemberJoinEvent event) {
        if(radioChannel!=null && radioChannel.getId() == event.getChannel().getId()) {
            System.out.println("Sombdy join");
            //ToDo check if player paused - unpause them
            if (player.isPaused()) {
                System.out.println("setPaused(false)");
                player.setPaused(false);
                updateMessage();
            }
        }
    }

    private void handleLeave(ServerVoiceChannelMemberLeaveEvent event) {
        if(radioChannel!=null && radioChannel.getId() == event.getChannel().getId()) {
            System.out.println("Sombdy leave");
            if(radioChannel.getConnectedUserIds().size()==1) {
                System.out.println("setPaused(true)");
                player.setPaused(true);
                beforeLeaveOrPausePosition = player.getPlayingTrack().getPosition();
                updateMessage();

            }
        }
    }

    private void handleEmoji(SingleReactionEvent event) {
        Optional<User> optionalUser = event.getUser();
        if(optionalUser.isPresent()) {
            System.out.println("User " + optionalUser.get().getNicknameMentionTag() + " use reaction");
        } else {
            System.out.println("Reaction user not found! (" + event.getUserId() + ")" );
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
                System.out.println("it's myself reaction");
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
        String emojiFromEvent = event.getEmoji().asUnicodeEmoji().get();
        System.out.println("emojiFromEvent = " + unicodeToStringRepresentation(emojiFromEvent));
        switch (emojiFromEvent) {
            case NEXT_EMOJI:
                playTrack(playList.getNext());
                break;
            case PREV_EMOJI:
                playTrack(playList.getPrev());
                break;
            case PLAY_PAUSE_EMOJI:
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
            case FAST_DOWN_EMOJI:
                createControlMessage();
                updateMessage();
                break;
            case LOUDSPEAKER_EMOJI:
                joinToUser(event);
                break;
            case FAST_FORWARD_EMOJI:
                moveForward();
                break;
            case FAST_REVERSE_EMOJI:
                moveBackard();
                break;
            case LOW_SOUND_EMOJI:
                setLowVolume();
                break;
            case MID_SOUND_EMOJI:
                setMidVolume();
                break;
            case HIGH_SOUND_EMOJI:
                setHighVolume();
                break;
            case MUTED_SPEAKER_EMOJI:
                leave();
                break;
             case EXCL_QUEST_EMOJI:
                showHelp();
                break;
            default:
                return false;
        }
        return true;
    }

    private void showHelp() {
        String message =
                "**Кнопки:**\n"
                + LOUDSPEAKER_EMOJI + " - пригласить к себе в голосовой канал\n"
                + MUTED_SPEAKER_EMOJI + " - выгнать из голосового канала\n"
                + PREV_EMOJI + " - предыдущий трек\n"
                + NEXT_EMOJI + " - следуюший трек\n"
                + PLAY_PAUSE_EMOJI + " - трек с начала\n"
                + REPEAT_EMOJI + " - повторять трек\n"
                + FAST_REVERSE_EMOJI + " - перемотка на 30 секунд назад\n"
                + FAST_FORWARD_EMOJI + " - перемотка на 30 секунд вперёд\n"
                + FAST_DOWN_EMOJI + " - обновить панельку управления (спустить вниз)\n"
                + EXCL_QUEST_EMOJI + " - вывести это сообщение\n"
                + "**Команды:**\n"
                + "`r!y <URL>` - включить ссылку <URL> YouTube \n"
                + "`r!ys <text>` - поиск <text> на YouTube\n"
                + ""
                ;
        Message helpMessage = generalChannel.sendMessage(message).join();
//        createControlMessage();
//        updateMessage();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                helpMessage.delete();
            }
        }, 15000);
    }

    private void setLowVolume() {
        if(!player.isPaused()) {
            player.setVolume(7);
        }
    }

    private void setMidVolume() {
        if(!player.isPaused()) {
            player.setVolume(45);
        }
    }

    private void setHighVolume() {
        if(!player.isPaused()) {
            player.setVolume(100);
        }
    }

    private void leave() {
        if(radioChannel!=null && audioConnection!=null) {
            System.out.println("Try to stop playing and leave.");
            radioChannel.getVoiceChannelAttachableListeners().forEach( (l, k) -> radioChannel.removeVoiceChannelAttachableListener(l));
            player.setPaused(true);
            beforeLeaveOrPausePosition = player.getPlayingTrack().getPosition();
            audioConnection.removeAudioSource(); //is really needed??
//        player.stopTrack();
            try {
                audioConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            radioChannel = null;
            audioConnection = null;
        } else {
            System.out.println("Do nothing! I'm not in any channel!");
        }
    }

    private void moveForward() {
        if(!player.isPaused()) {
            AudioTrack playingTrack = player.getPlayingTrack();
            playingTrack.setPosition(Math.min(playingTrack.getDuration(), playingTrack.getPosition()+30*1000));
            updateMessage();
        }
    }

    private void moveBackard() {
        if(!player.isPaused()) {
            AudioTrack playingTrack = player.getPlayingTrack();
            playingTrack.setPosition(Math.max(0, playingTrack.getPosition()-30*1000));
            updateMessage();
        }
    }

    private void joinToUser(SingleReactionEvent event) {
        Optional<User> optionalUser = event.getUser();
        Optional<ServerVoiceChannel> serverVoiceChannelOptional;
        if(optionalUser.isPresent()) {
            User u = optionalUser.get();
            System.out.println("join to user: " + u.getNicknameMentionTag());
            serverVoiceChannelOptional = u.getConnectedVoiceChannel(((ServerTextChannel) generalChannel).getServer());
        } else {
            try {
                serverVoiceChannelOptional = api.getUserById(event.getUserId()).get().getConnectedVoiceChannel(((ServerTextChannel) generalChannel).getServer());
                if(serverVoiceChannelOptional.isPresent()) {
                    ServerVoiceChannel serverVoiceChannel = serverVoiceChannelOptional.get();
                    String serverVoiceChannelName = serverVoiceChannel.getName();
                    System.out.println("serverVoiceChannelName = " + serverVoiceChannelName);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return;
            }
        }

        if(serverVoiceChannelOptional.isPresent()) {
            ServerVoiceChannel channel = serverVoiceChannelOptional.get();
            String serverVoiceChannelName = channel.getName();
            System.out.println("serverVoiceChannelName = " + serverVoiceChannelName);
            if(channel.getServer().getId()==((ServerTextChannel)generalChannel).getServer().getId()) {
                System.out.println("Join to channel: " + channel.getName());
                if (radioChannel!=null && audioConnection!=null) {
                    if(radioChannel.getId()==channel.getId()) {
                        System.out.println("Already here! Do nothing.");
                        return;
                    }
                    System.out.println("radioChannel != null && audioConnection!=null");
                    player.setPaused(true);
                    beforeLeaveOrPausePosition = player.getPlayingTrack().getPosition();
                    radioChannel.getVoiceChannelAttachableListeners().forEach( (l, k) -> radioChannel.removeVoiceChannelAttachableListener(l));
                    audioConnection.removeAudioSource();
                    audioConnection.close();
                    audioConnection = null;
                    radioChannel = null;
                }
                System.out.println("Connect to channel");
                channel.connect().thenAccept(audioConnection -> {
                    this.audioConnection = audioConnection;
                    this.audioConnection.setAudioSource(source);
                    AudioTrack current = playList.getCurrentWithClone();
                    AudioTrack clone = current;
                    clone.setPosition(beforeLeaveOrPausePosition);
                    playTrack(clone);
                });

                this.radioChannel = channel;
                this.radioChannel.addServerVoiceChannelMemberJoinListener(this::handleJoin);
                this.radioChannel.addServerVoiceChannelMemberLeaveListener(this::handleLeave);
            }
        }
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

    public void playOnLinkButtonsHandler(SingleReactionEvent event) {
        event.getUser().ifPresent(user -> {
            if(!user.isYourself()) {
                event.getMessage().ifPresent(message -> {
                    if(message.getId()!=myMessage.getId()) {
                        String emojiFromEvent = event.getEmoji().asUnicodeEmoji().get();
                        if (emojiFromEvent.equals(PLAY_PAUSE_EMOJI)) {
                            newMessageProcessor(message.getContent(), Optional.empty());
                        }
                    }
                });
            }
        });
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
