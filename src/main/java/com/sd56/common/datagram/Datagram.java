package com.sd56.common.datagram;

import java.io.*;

public class Datagram {
    protected final static int version = 1;
    private final DatagramType type;

    public enum DatagramType {
        DATAGRAM_TYPE_REQUEST_AUTHENTICATION,
        DATAGRAM_TYPE_REQUEST_PUT,
        DATAGRAM_TYPE_REQUEST_GET,
        DATAGRAM_TYPE_REQUEST_MULTIGET,
        DATAGRAM_TYPE_REQUEST_MULTIPUT,
        DATAGRAM_TYPE_REQUEST_GETWHEN,
        DATAGRAM_TYPE_REQUEST_CLOSE,
        DATAGRAM_TYPE_RESPONSE_AUTHENTICATION,
        DATAGRAM_TYPE_RESPONSE_PUT,
        DATAGRAM_TYPE_RESPONSE_GET,
        DATAGRAM_TYPE_RESPONSE_MULTIGET,
        DATAGRAM_TYPE_RESPONSE_MULTIPUT,
        DATAGRAM_TYPE_RESPONSE_GETWHEN
    };

    public Datagram(DatagramType type) {
        this.type = type;
    }

    public DatagramType getType() { return this.type; }

    @Override
    public String toString() {
        return "{[DATAGRAM] | VERSION: " + version + " | TYPE: " + type + "}";
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.writeInt(version);
        out.writeInt(this.type.ordinal());

        //TODO Acho que o flush não é necessário aqui mas podemos discutir isso
        //out.flush();

        return baos.toByteArray();
    }

    public static Datagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);

        int desVersion = in.readInt();
        if (desVersion != Datagram.version) {
            System.err.println("Invalid version value.");
            // TODO: Melhorar error handling, não esquecendo de ler os bytes que faltam do datagrama.
        }
        DatagramType type = DatagramType.values()[in.readInt()];
        return new Datagram(type);
    }

}
