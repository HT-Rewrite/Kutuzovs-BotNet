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
import java.util.concurrent.atomic.AtomicReference;

public class KFTPPanel {
    private static final LoadingWheel loadingWheel = new LoadingWheel();

    private static String directory;
    private static String[] directories;
    private static SerializableEntry<String, Long>[] files;
    public static void panel_entry(Client client) {
        loadingWheel.status.set("Requesting server root directory...");
        loadingWheel.showing.set(true);
        AtomicReference<CSKFTPDirectoryInfoPacket> packet = new AtomicReference<>(null);
        try {
            client.addWait(() -> {
                try {
                    client.sendPacket(new SCKFTPHandshakePacket());
                    packet.set((CSKFTPDirectoryInfoPacket) client.readPacket());
                } catch (Exception exception) { exception.printStackTrace(); }
            });

            if(packet.get() == null)
                throw new Exception("Packet is null");
        } catch (Exception e) {
            e.printStackTrace();
            loadingWheel.showing.set(false);
            trySleep(1000);
            clearConsole();
            pnl("Requesting server root directory... FAILED");
            readLine();
            return;
        }

        directory = packet.get().directory;
        directories = packet.get().directories;
        files = packet.get().files;

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

        for(int i = 0; i < args.length; i++)
            args[i] = args[i].replaceAll("::", " ");

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
                    AtomicReference<CSKFTPDirectoryInfoPacket> packet = new AtomicReference<>(null);
                    client.addWait(() -> {
                        try {
                            client.sendPacket(new SCKFTPHandshakePacket());
                            packet.set((CSKFTPDirectoryInfoPacket) client.readPacket());
                        } catch (Exception exception) { exception.printStackTrace(); }
                    });

                    if(packet.get() == null)
                        throw new Exception("Packet is null");
                    directory = packet.get().directory;
                    directories = packet.get().directories;
                    files = packet.get().files;
                } catch (Exception e) {
                    e.printStackTrace();
                    pnl("Failed to go to server root directory");
                    readLine();
                }
            } break;

            case "ls": {
                try {
                    String dir;
                    if(args.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (String arg : args)
                            sb.append(arg).append(" ");

                        dir = sb.toString().trim();
                    } else {
                        dir = directory;
                    }

                    AtomicReference<CSKFTPDirectoryInfoPacket> packet = new AtomicReference<>(null);
                    client.addWait(() -> {
                        try {
                            client.sendPacket(new SCKFTPListDirectoryPacket(dir));
                            packet.set((CSKFTPDirectoryInfoPacket) client.readPacket());
                        } catch (Exception exception) { exception.printStackTrace(); }
                    });

                    if(packet.get() == null)
                        throw new Exception("Packet is null");

                    String nDirectory = packet.get().directory;
                    String[] nDirectories = packet.get().directories;
                    SerializableEntry<String, Long>[] nFiles = packet.get().files;

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

                    AtomicReference<CSKFTPDirectoryInfoPacket> packet = new AtomicReference<>(null);
                    String finalDir = dir;
                    client.addWait(() -> {
                        try {
                            client.sendPacket(new SCKFTPChangeDirectoryPacket(finalDir));
                            packet.set((CSKFTPDirectoryInfoPacket) client.readPacket());
                        } catch (Exception exception) { exception.printStackTrace(); }
                    });

                    if(packet.get() == null)
                        throw new Exception("Packet is null");

                    directory = packet.get().directory;
                    directories = packet.get().directories;
                    files = packet.get().files;
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

                    client.addWait(() -> {
                        try {
                            client.sendPacket(new SCKFTPDownloadFilePacket(file));
                            CSKFTPResponsePacket packet = (CSKFTPResponsePacket) client.readPacket();
                            if(packet.response == CSKFTPResponsePacket.RESPONSE_ERROR) {
                                pnl("Error! Maybe the file doesn't exist or you don't have permission to access it");
                                return;
                            }

                            CSKFTPFilePacket filePacket = (CSKFTPFilePacket) client.readPacket();
                            FileOutputStream fos = new FileOutputStream(path);
                            fos.write(filePacket.data);
                            fos.close();
                        } catch (Exception exception) { exception.printStackTrace(); }
                    });

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

                    AtomicReference<CSKFTPDirectoryInfoPacket> infoPacket = new AtomicReference<>(null);
                    client.addWait(() -> {
                        try {
                            client.sendPacket(new SCKFTPStartUploadPacket(Files.size(Paths.get(file))));
                            CSKFTPResponsePacket packet = (CSKFTPResponsePacket) client.readPacket();
                            if(packet.response == CSKFTPResponsePacket.RESPONSE_ERROR) {
                                pnl("Error! Maybe the file path is invalid or you don't have permission to access it");
                                return;
                            }

                            SCKFTPFilePacket filePacket = new SCKFTPFilePacket(path, Files.readAllBytes(Paths.get(file)));
                            client.sendPacket(filePacket);
                            infoPacket.set((CSKFTPDirectoryInfoPacket) client.readPacket());
                        } catch (Exception exception) { exception.printStackTrace(); }
                    });

                    if(infoPacket.get() != null) {
                        directory = infoPacket.get().directory;
                        directories = infoPacket.get().directories;
                        files = infoPacket.get().files;
                    }

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

                AtomicReference<CSKFTPDirectoryInfoPacket> infoPacket = new AtomicReference<>(null);
                client.addWait(() -> {
                    try {
                        client.sendPacket(new SCKFTPUploadUrlPacket(url, path));
                        infoPacket.set((CSKFTPDirectoryInfoPacket) client.readPacket());

                        pnl("File uploaded successfully!");
                    } catch (Exception e) {
                        pnl("Error: " + e.getMessage());
                    }
                });

                if(infoPacket.get() != null) {
                    directory = infoPacket.get().directory;
                    directories = infoPacket.get().directories;
                    files = infoPacket.get().files;
                }
                readLine();
            } break;

            case "mkdir": {
                if(args.length < 1) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String path = args[0];

                AtomicReference<CSKFTPDirectoryInfoPacket> infoPacket = new AtomicReference<>(null);
                client.addWait(() -> {
                    try {
                        client.sendPacket(new SCKFTPCreateDirectoryPacket(path));
                        infoPacket.set((CSKFTPDirectoryInfoPacket) client.readPacket());

                        pnl("Directory created successfully!");
                    } catch (Exception e) {
                        pnl("Error: " + e.getMessage());
                    }
                });

                if(infoPacket.get() != null) {
                    directory = infoPacket.get().directory;
                    directories = infoPacket.get().directories;
                    files = infoPacket.get().files;
                }

                readLine();
            } break;

            case "rmdir": {
                if(args.length < 1) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String path = args[0];

                AtomicReference<CSKFTPDirectoryInfoPacket> infoPacket = new AtomicReference<>(null);
                client.addWait(() -> {
                    try {
                        client.sendPacket(new SCKFTPDeleteDirectoryPacket(path));
                        infoPacket.set((CSKFTPDirectoryInfoPacket) client.readPacket());

                        pnl("Directory deleted successfully!");
                    } catch (Exception e) {
                        pnl("Error: " + e.getMessage());
                    }
                });

                if(infoPacket.get() != null) {
                    directory = infoPacket.get().directory;
                    directories = infoPacket.get().directories;
                    files = infoPacket.get().files;
                }

                readLine();
            } break;

            case "rm": {
                if(args.length < 1) {
                    pnl("Error: missing arguments");
                    readLine();
                    break;
                }

                String path = args[0];

                AtomicReference<CSKFTPDirectoryInfoPacket> infoPacket = new AtomicReference<>(null);
                client.addWait(() -> {
                    try {
                        client.sendPacket(new SCKFTPDeleteFilePacket(path));
                        infoPacket.set((CSKFTPDirectoryInfoPacket) client.readPacket());

                        pnl("File deleted successfully!");
                    } catch (Exception e) {
                        pnl("Error: " + e.getMessage());
                    }
                });

                if(infoPacket.get() != null) {
                    directory = infoPacket.get().directory;
                    directories = infoPacket.get().directories;
                    files = infoPacket.get().files;
                }

                readLine();
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