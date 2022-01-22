package me.kutuzov.client;

import me.kutuzov.packet.CSDAResPacket;
import me.kutuzov.packet.SCDAPacket;
import me.kutuzov.packet.SCMessageBoxPacket;
import me.pk2.moodlyencryption.MoodlyEncryption;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class KutuzovEntry {
    private static ArrayList<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        for(int i = 0; i < 10; i++)
            sockets.add(new Socket("localhost", 33901));

        for(Socket socket : sockets) {
            new Thread(()->{
                ObjectInputStream ois = null;
                ObjectOutputStream oos = null;
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                    oos = new ObjectOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                while(true) {
                    if(socket.isClosed())
                        break;

                    try {
                        Object packet = ois.readObject();
                        if(packet instanceof SCMessageBoxPacket) {
                            SCMessageBoxPacket scMessageBoxPacket = (SCMessageBoxPacket) packet;
                            JOptionPane.showMessageDialog(null, scMessageBoxPacket.content, scMessageBoxPacket.title, JOptionPane.INFORMATION_MESSAGE);
                        } else if(packet instanceof SCDAPacket) {
                            SCDAPacket scDAPacket = (SCDAPacket) packet;
                            try {
                                MoodlyEncryption moodlyEncryption = new MoodlyEncryption();
                                moodlyEncryption.init(scDAPacket.data.substring(scDAPacket.data.length() - 16));
                                String decryptedData = moodlyEncryption.decrypt(scDAPacket.data.substring(0, scDAPacket.data.length() - 16).getBytes(StandardCharsets.UTF_8));
                                String[] data = decryptedData.split(";");
                                String method = data[0];
                                switch (method) {
                                    case "tcp":
                                        System.out.println("TCP[" + socket.getPort() + "]: " + data[1] + " -> " + data[2]);
                                        break;
                                    case "udp":
                                        System.out.println("UDP[" + socket.getPort() + "]: " + data[1] + " -> " + data[2]);
                                        break;
                                    default: break;
                                }

                                oos.writeObject(new CSDAResPacket(CSDAResPacket.RESPONSE_OK));
                            } catch (Exception e) {
                                oos.writeObject(new CSDAResPacket(CSDAResPacket.RESPONSE_ERROR));
                            }
                        }
                    } catch (Exception ignored) { }
                }
            }).start();
        }

        /*Thread.sleep(8000);
        for(Socket socket : sockets)
            socket.close();
            */
    }
}