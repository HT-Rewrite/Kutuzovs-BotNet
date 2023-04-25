package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;

public class CSWebcamList extends Packet {
    public String[] webcamNames;

    @Override
    public boolean isServer() {
        return true;
    }

    public CSWebcamList(String[] webcamNames) {
        this.webcamNames = webcamNames;
    }
}