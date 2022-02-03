package me.kutuzov.server.kftp.util;

public class KFTPDirParser {
    public static String parse(String path, String dir) {
        if (dir.equals(".")) {
            return path;
        }
        if (dir.equals("..")) {
            if (path.length() == 0) {
                return path;
            }
            int i = path.lastIndexOf('/');
            if (i == -1) {
                return "";
            }
            return path.substring(0, i);
        }
        if (path.length() == 0) {
            return dir;
        }
        return path + "/" + dir;
    }
}