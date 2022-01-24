package me.kutuzov.packet;

public class CSHandshakePacket extends Packet {
    public final String identifierName, localIp, os;
    public CSHandshakePacket(String identifierName, String localIp, String os) {
        this.identifierName = identifierName;
        this.localIp = localIp;
        this.os = os;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}