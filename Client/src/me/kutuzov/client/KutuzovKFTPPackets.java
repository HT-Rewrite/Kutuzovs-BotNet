package me.kutuzov.client;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.kftp.*;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class KutuzovKFTPPackets {
    private static SerializableEntry<String, Long>[] buildEntry(SerializableEntry<String, Long>... entry) { return entry; }

    private static String directory = "";
    private static void sendList(ObjectInputStream ois, ObjectOutputStream oos, String directory) {
        File file = new File(directory);
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
        String[] files = file.list((current, name) -> new File(current, name).isFile());
        ArrayList<SerializableEntry<String, Long>> entries = new ArrayList<>();
        for(String fileString : files) {
            String actualFile = Paths.get(directory, fileString).toString();

            try {
                entries.add(new SerializableEntry<>(fileString, Files.size(Paths.get(actualFile))));
            } catch (Exception e) {
                entries.add(new SerializableEntry<>(fileString, -1L));
            }
        }

        try {
            oos.writeObject(new CSKFTPDirectoryInfoPacket(directory, directories, entries.toArray(buildEntry())));
        } catch (Exception e) { }
    }

    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCKFTPHandshakePacket) {
            directory = System.getProperty("user.dir");
            sendList(ois, oos, directory);
        } else if(packet instanceof SCKFTPListDirectoryPacket) {
            SCKFTPListDirectoryPacket listDirectoryPacket = (SCKFTPListDirectoryPacket) packet;
            sendList(ois, oos, listDirectoryPacket.directory);
        } else if(packet instanceof SCKFTPChangeDirectoryPacket) {
            SCKFTPChangeDirectoryPacket changeDirectoryPacket = (SCKFTPChangeDirectoryPacket) packet;
            if(new File(changeDirectoryPacket.directory).isDirectory())
                directory = changeDirectoryPacket.directory;

            sendList(ois, oos, directory);
        } else if(packet instanceof SCKFTPDownloadFilePacket) {
            SCKFTPDownloadFilePacket downloadFilePacket = (SCKFTPDownloadFilePacket) packet;
            try {
                File file = new File(directory, downloadFilePacket.fileName);
                if(!file.exists() && file.isFile() && file.canRead()) {
                    oos.writeObject(new CSKFTPResponsePacket(CSKFTPResponsePacket.RESPONSE_ERROR));
                    return;
                }

                oos.writeObject(new CSKFTPResponsePacket(CSKFTPResponsePacket.RESPONSE_OK));

                byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                oos.writeObject(new CSKFTPFilePacket(file.getPath(), bytes));
            } catch (Exception e) {
                try {
                    oos.writeObject(new CSKFTPFilePacket("", new byte[]{}));
                } catch (Exception e1) { }
            }
        }
    }
}