package org.obiz.discord.bot;

import java.util.concurrent.ExecutionException;

public class RadioV2 {

    private static final String key = "Nzc4NjkzODgyODY2NDM0MDY4.X7VtMQ.lpGDiBqbWLbVPHRWx5BY_cWY5w0";
    private static final long commandChannelId = 352890966459547660L;
    private static final long radioChannelId = 707315879666516059L;

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Bot radio = new Bot(
                args[0],
                Long.parseLong(args[1]),
                Long.parseLong(args[2]));
        
//        Bot radio = new Bot(
//                key,
//                commandChannelId,
//                radioChannelId);
//
        radio.start();

    }
}
