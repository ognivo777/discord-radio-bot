package org.obiz.discord.bot;

import java.util.concurrent.ExecutionException;

public class RadioV2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        String key = args[0];
        long commandChannelId = Long.parseLong(args[1]);
        long radioChannelId = Long.parseLong(args[2]);

        Bot radio = new Bot(
        key,
        commandChannelId,
        radioChannelId);

        radio.start();

    }
}
