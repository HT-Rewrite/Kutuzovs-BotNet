package me.kutuzov.server.kftp;

import static me.kutuzov.server.util.ConsoleUtils.*;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.kftp.CSKFTPDirectoryInfoPacket;
import me.kutuzov.packet.kftp.SCKFTPHandshakePacket;
import me.kutuzov.packet.kftp.SCKFTPListDirectoryPacket;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.util.LoadingWheel;

import java.util.Arrays;
import java.util.Map;

public class KFTPPanel {
    private static final LoadingWheel loadingWheel = new LoadingWheel();

    private static String directory;
    private static String[] directories;
    private static SerializableEntry<String, Long>[] files;
    public static void panel_entry(Client client) {
        loadingWheel.status.set("Requesting server root directory...");
        loadingWheel.showing.set(true);
        CSKFTPDirectoryInfoPacket packet = null;
        try {
            client.getOutput().writeObject(new SCKFTPHandshakePacket());
            packet = (CSKFTPDirectoryInfoPacket) client.getInput().readObject();
            directory = packet.directory;
            directories = packet.directories;
            files = packet.files;
        } catch (Exception e) {
            e.printStackTrace();
            loadingWheel.showing.set(false);
            trySleep(1000);
            clearConsole();
            pnl("Requesting server root directory... FAILED");
            readLine();
            return;
        }

        loadingWheel.showing.set(false);
        trySleep(1000);
        panel_main(client);
    }

    private static void panel_main(Client client) {
        clearConsole();
        pnl("/// KFTP Server v1.0  [" + client.getIp() + "] \\\\\\");
        pnl("Directory: " + directory);
        pnl("  - Directories: " + directories.length);
        for(int i = 0; i < directories.length; i++)
            pnl("    - " + directories[i]);
        pnl("  - Files: " + files.length);
        for(int i = 0; i < files.length; i++)
            pnl("    - " + files[i].key + " (" + files[i].value + " bytes)");
        pnl("");
        pwl("Enter command: ");
        String command = readLine();
        String[] args = {};
        if(command.contains(" ")) {
            String[] tmp = command.split(" ");
            args = Arrays.copyOfRange(tmp, 1, tmp.length);
        }

        switch (command) {
            case "help": {
                pnl("Available commands:");
                pnl("  help - show this help");
                pnl("  ls - list directory");
                pnl("  exit - exit");

                readLine();
            } break;

            case "ls": {
                try {
                    client.getOutput().writeObject(new SCKFTPListDirectoryPacket(directory));
                    CSKFTPDirectoryInfoPacket packet = (CSKFTPDirectoryInfoPacket) client.getInput().readObject();
                    directory = packet.directory;
                    directories = packet.directories;
                    files = packet.files;
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    return;
                }
            } break;

            case "exit":
                return;
            default:
                pnl("Unknown command");
                break;
        }

        panel_main(client);
    }
}