package org.obiz.discord.bot;

import java.io.File;
import java.nio.file.Paths;

public class RadioV2 {
    public static String GOOGLE_DEVELOPER_KEY = "";
    public static String files;

    public static void main(String[] args) {

        if(args.length!=4) {
            System.out.println("Wrong params. Use <discord api key> <control channel id> <mode> <folder with music> <google api key>");
        }
        String key = args[0];
        long commandChannelId = Long.parseLong(args[1]);
        String mode = "old";
        if(args.length > 2) {
            mode = args[2];

            if(args.length>3) {
                files = args[3];
                File file = Paths.get(files).toFile();
                if(!file.exists() || !file.isDirectory()) {
                    files = "";
                }
                if(args.length>4) {
                    GOOGLE_DEVELOPER_KEY = args[4];
                }
            }
        }

        Bot radio = new Bot(
            key,
            commandChannelId,
            mode);

        radio.start();

    }
}
