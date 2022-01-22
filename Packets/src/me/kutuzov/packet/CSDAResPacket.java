package me.kutuzov.packet;

public class CSDAResPacket extends Packet {
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_ERROR = "ERROR";
    public static final String RESPONSE_BUSY = "BUSY";

    @Override
    public boolean isServer() {
        return false;
    }

    public final String response;
    public CSDAResPacket(String response) {
        this.response = response;
    }
}