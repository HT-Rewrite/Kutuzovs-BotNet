package me.kutuzov.packet;

public class CSHandshakePacket extends Packet {
    public final String identifierName, localIp, os, version;
    public final boolean isMC;
    public CSHandshakePacket(String identifierName, String localIp, String os, String version, boolean isMC) {
        this.identifierName = identifierName;
        this.localIp = localIp;
        this.os = os;
        this.version = version;
        this.isMC = isMC;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}