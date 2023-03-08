package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;
import me.kutuzov.packet.logger.types.TokenType;

public class SCTokenRequest extends Packet {
    @Override
    public boolean isServer() {
        return true;
    }

    public final TokenType type;
    public SCTokenRequest(TokenType type) {
        this.type = type;
    }
}