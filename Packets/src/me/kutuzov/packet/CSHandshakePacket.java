package me.kutuzov.packet;

public class CSHandshakePacket extends Packet {
    public final String identifierName, localIp, os, version;
    public CSHandshakePacket(String identifierName, String localIp, String os, String version) {
        this.identifierName = identifierName;
        this.localIp = localIp;
        this.os = os;
        this.version = version;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}