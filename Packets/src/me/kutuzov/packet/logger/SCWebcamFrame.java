package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;

public class SCWebcamFrame extends Packet {
    public final int webcamId;

    @Override
    public boolean isServer() {
        return true;
    }

    public SCWebcamFrame(int webcamId) {
        this.webcamId = webcamId;
    }
}