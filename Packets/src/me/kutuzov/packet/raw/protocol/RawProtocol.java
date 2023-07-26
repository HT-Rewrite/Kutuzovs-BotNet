package me.kutuzov.packet.raw.protocol;

import me.kutuzov.utils.Pair;

public enum RawProtocol {
    UNKNOWN(0xFF, 0xFF),
    HELLO(0x00, 0xF0),
    BYE(0x00, 0xFF),

    /* FIELD 1 */
    PYTHON(0x00, 0x00),

    /* FIELD 2 */
    INSTALL_PROGRESS(0x01, 0x01),
    INSTALL_SUCCESS(0x01, 0x02),
    INSTALL_FAILED(0x01, 0x03),
    ASK(0x01, 0x00) // UNIVERSAL

    ;

    public byte field, b;
    RawProtocol(int field, int b) {
        this.field = (byte)field;
        this.b = (byte)b;
    }

    public static Pair<RawProtocol, RawProtocol> parse(byte x, byte y) {
        Pair<RawProtocol, RawProtocol> ret = new Pair<>(UNKNOWN, UNKNOWN);
        for(RawProtocol protocol : RawProtocol.values())
            if(protocol.field == 0x00 && protocol.b == x)
                ret.x = protocol;
            else if(protocol.field == 0x01 && protocol.b == y)
                ret.y = protocol;

        return ret;
    }
}