package me.kutuzov.client;

import me.kutuzov.packet.Packet;
import me.kutuzov.packet.SCUnixCommandPacket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class KutuzovUnixPackets {
    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCUnixCommandPacket) {
            new Thread(() -> {
                SCUnixCommandPacket commandPacket = (SCUnixCommandPacket) packet;

                // TODO: Do this xd
            });
        }
    }
}