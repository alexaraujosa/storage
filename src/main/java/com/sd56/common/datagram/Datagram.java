package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Datagram {
    protected final static int version = 1;
    private final DatagramType type;

    public enum DatagramType {
        DATAGRAM_TYPE_REQUEST_AUTHENTICATION,
        DATAGRAM_TYPE_REQUEST_PUT,
        DATAGRAM_TYPE_REQUEST_GET,
        DATAGRAM_TYPE_RESPONSE_AUTHENTICATION,
        DATAGRAM_TYPE_RESPONSE_PUT,
        DATAGRAM_TYPE_RESPONSE_GET
    };

    public Datagram(DatagramType type) {
        this.type = type;
    }

    public DatagramType getType() { return this.type; }

    @Override
    public String toString() {
        return "{[DATAGRAM] | VERSION: " + version + " | TYPE: " + type + "}";
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(this.version);
        out.writeInt(this.type.ordinal());
    }

    public static Datagram deserialize(DataInputStream in) throws IOException {
        int desVersion = in.readInt();
        if (desVersion != Datagram.version) {
            System.err.println("Invalid version value.");
            // TODO: Melhorar error handling, não esquecendo de ler os bytes que faltam do datagrama.
        }
        DatagramType type = DatagramType.values()[in.readInt()];
        return new Datagram(type);
    }

}
