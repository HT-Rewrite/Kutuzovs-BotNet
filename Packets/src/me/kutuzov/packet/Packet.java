package me.kutuzov.packet;

import java.io.Serializable;

public abstract class Packet implements Serializable {
    public abstract boolean isServer();
}