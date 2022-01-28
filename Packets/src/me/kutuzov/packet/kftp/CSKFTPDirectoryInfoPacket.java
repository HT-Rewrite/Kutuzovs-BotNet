package me.kutuzov.packet.kftp;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.Packet;

import java.util.Map;

public class CSKFTPDirectoryInfoPacket extends Packet {
    // serialVersion
    private static final long serialVersionUID = -85936261378337724L;

    public final String directory;
    public final String[] directories;
    public final SerializableEntry<String, Long>[] files;
    public CSKFTPDirectoryInfoPacket(String directory, String[] directories, SerializableEntry<String, Long>[] files) {
        this.directory = directory;
        this.directories = directories;
        this.files = files;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}