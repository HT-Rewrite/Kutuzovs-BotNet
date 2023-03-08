package me.kutuzov.packet.logger;

import me.kutuzov.packet.Packet;
import me.kutuzov.packet.logger.types.TokenType;

public class CSTokenResponse extends Packet {
    @Override
    public boolean isServer() {
        return false;
    }

    public final TokenType type;
    public final String[] tokens;
    public CSTokenResponse(TokenType type, String[] tokens) {
        this.type = type;
        this.tokens = tokens;
    }
}