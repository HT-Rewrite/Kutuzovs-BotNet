package me.kutuzov.packet.python;

import me.kutuzov.packet.Packet;
import me.kutuzov.packet.python.status.InstalledStatus;

public class CSPythonStatusPacket extends Packet {
    public InstalledStatus installed;
    public String version;
    public String path;
    public CSPythonStatusPacket(InstalledStatus installed, String version, String path) {
        this.installed = installed;
        this.version = version;
        this.path = path;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}