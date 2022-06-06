package me.kutuzov.server.kftp;

import static me.kutuzov.server.util.ConsoleUtils.*;

import me.kutuzov.entry.SerializableEntry;
import me.kutuzov.packet.kftp.*;
import me.kutuzov.server.client.Client;
import me.kutuzov.server.util.LoadingWheel;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class KFTPPanel {
    private static final LoadingWheel loadingWheel = new LoadingWheel();

    private static String directory;
    private static String[] directories;
    private static SerializableEntry<String, Long>[] files;
    public static void panel_entry(Client client) {
        loadingWheel.status.set("Requesting server root directory...");
        loadingWheel.showing.set(true);
        CSKFTPDirectoryInfoPacket packet = null;
        try {
            client.getOutput().writeObject(new SCKFTPHandshakePacket());
            packet = (CSKFTPDirectoryInfoPacket) client.getInput().readObject();
            directory = packet.directory;
            directories = packet.directories;
            files = packet.files;
        } catch (Exception e) {
            e.printStackTrace();
            loadingWheel.showing.set(false);
            trySleep(1000);
            clearConsole();
            pnl("Requesting server root directory... FAILED");
            readLine();
            return;
        }

        loadingWheel.showing.set(false);
        trySleep(1000);
        panel_main(client);
    }

    private static void panel_main(Client client) {
        clearConsole();
        pnl("/// KFTP Server v1.0  [" + client.getIp() + "] \\\\\\");
        pnl("Directory: " + directory);
        pnl("  - Directories: " + directories.length);
        for(int i = 0; i < directories.length; i++)
            pnl("    - " + directories[i]);
        pnl("  - Files: " + files.length);
        for(int i = 0; i < files.length; i++)
            pnl("    - " + files[i].key + " (" + files[i].value + " bytes)");
        pnl("");
        pwl("Enter command: ");
        String command = readLine();
        String[] args = {};
        if(command.contains(" ")) {
            String[] tmp = command.split(" ");
            args = Arrays.copyOfRange(tmp, 1, tmp.length);

            command = tmp[0];
        }

        switch (command) {
            case "help": {
                pnl("Available commands:");
                pnl("  help - show this help");
                pnl("  home - go to server root directory");
                pnl("  ls [directory] - list directory");
                pnl("  cd <directory> - change directory");
                pnl("  get <file> <destination> - download file");
                pnl("  put <file> <destination> - upload file");
                pnl("  putUrl <url> <destination> - upload file from url");
                pnl("  mkdir <directory> - create directory");
                pnl("  rmdir <directory> - remove directory");
                pnl("  rm <file> - remove file");
                pnl("  exit - exit");

                readLine();
            } break;

            case "home": {
                try {
                    client.getOutput().writeObject(new SCKFTPHandshakePacket());
                    CSKFTPDirectoryInfoPacket packet = (CSKFTPDirectoryInfoPacket) client.getInput().readObject();
                    directory = packet.directory;
                    directories = packet.directories;
                    files = packet.files;
                } catch (Exception e) {
                    e.printStackTrace();
                    pnl("Failed to go to server root directory");
                    readLine();
                }
            } break;

            case "ls": {
                try {
                    String dir = directory;
                    if(args.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (String arg : args)
                            sb.append(arg).append(" ");

                        dir = sb.toString().trim();
                    }

                    client.getOutput().writeObject(new SCKFTPListDirectoryPacket(dir));
                    CSKFTPDirectoryInfoPacket packet = (CSKFTPDirectoryInfoPacket) client.getInput().readObject();
                    String nDirectory = packet.directory;
                    String[] nDirectories = packet.directories;
                    SerializableEntry<String, Long>[] nFiles = packet.files;

                    pnl("\n\nDirectory: " + nDirectory);
                    pnl("  - Directories: " + nDirectories.length);
                    for(int i = 0; i < nDirectories.length; i++)
                        pnl("    - " + nDirectories[i]);
                    pnl("  - Files: " + nFiles.length);
                    for(int i = 0; i < nFiles.length; i++)
                        pnl("    - " + nFiles[i].key + " (" + nFiles[i].value + " bytes)");
                    pnl("");

                    if(args.length == 0) {
                        directory = nDirectory;
                        directories = nDirectories;
                        files = nFiles;
                    }
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "cd": {
                try {
                    String dir = directory;
                    if(args.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (String arg : args)
                            sb.append(arg).append(" ");

                        dir = sb.toString().trim();

                        if(dir.equals(".."))
                            if(directory.equals("/"))
                                dir = directory;
                            else dir = directory.substring(0, directory.lastIndexOf("/"));
                    }

                    client.getOutput().writeObject(new SCKFTPChangeDirectoryPacket(dir));
                    CSKFTPDirectoryInfoPacket packet = (CSKFTPDirectoryInfoPacket) client.getInput().readObject();
                    directory = packet.directory;
                    directories = packet.directories;
                    files = packet.files;
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "get": {
                try {
                    if(args.length < 2) {
                        pnl("Error: missing arguments");
                        readLine();
                        break;
                    }

                    String file = args[0];
                    String path = args[1];

                    client.getOutput().writeObject(new SCKFTPDownloadFilePacket(file));
                    CSKFTPResponsePacket packet = (CSKFTPResponsePacket) client.getInput().readObject();
                    if(packet.response == CSKFTPResponsePacket.RESPONSE_ERROR) {
                        pnl("Error! Maybe the file doesn't exist or you don't have permission to access it");
                        readLine();
                        break;
                    }

                    CSKFTPFilePacket filePacket = (CSKFTPFilePacket) client.getInput().readObject();
                    FileOutputStream fos = new FileOutputStream(path);
                    fos.write(filePacket.data);
                    fos.close();

                    pnl("File downloaded successfully!");
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "put": {
                try {
                    if(args.length < 2) {
                        pnl("Error: missing arguments");
                        readLine();
                        break;
                    }

                    String file = args[0];
                    String path = args[1];

                    client.getOutput().writeObject(new SCKFTPStartUploadPacket(Files.size(Paths.get(file))));
                    CSKFTPResponsePacket packet = (CSKFTPResponsePacket) client.getInput().readObject();
                    if(packet.response == CSKFTPResponsePacket.RESPONSE_ERROR) {
                        pnl("Error! Maybe the file path is invalid or you don't have permission to access it");
                        readLine();
                        break;
                    }

                    SCKFTPFilePacket filePacket = new SCKFTPFilePacket(path, Files.readAllBytes(Paths.get(file)));
                    client.getOutput().writeObject(filePacket);

                    pnl("File uploaded successfully!");
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "putUrl": {
                if(args.length < 2) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String url = args[0];
                String path = args[1];

                try {
                    client.getOutput().writeObject(new SCKFTPUploadUrlPacket(url, path));

                    pnl("File uploaded successfully!");
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "mkdir": {
                if(args.length < 1) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String path = args[0];

                try {
                    client.getOutput().writeObject(new SCKFTPCreateDirectoryPacket(path));

                    pnl("Directory created successfully!");
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "rmdir": {
                if(args.length < 1) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String path = args[0];

                try {
                    client.getOutput().writeObject(new SCKFTPDeleteDirectoryPacket(path));

                    pnl("Directory deleted successfully!");
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "rm": {
                if(args.length < 1) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String path = args[0];

                try {
                    client.getOutput().writeObject(new SCKFTPDeleteFilePacket(path));

                    pnl("File deleted successfully!");
                    readLine();
                } catch (Exception e) {
                    pnl("Error: " + e.getMessage());
                    readLine();
                    break;
                }
            } break;

            case "exit":
                return;
            default:
                pnl("Unknown command");
                break;
        }

        panel_main(client);
    }
}