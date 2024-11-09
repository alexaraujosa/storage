package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestGetDatagram extends Datagram {
    private final String key;
    
    public RequestGetDatagram(String key) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_GET);
        this.key = key;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeUTF(key);
    }

    public static RequestGetDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_REQUEST_GET) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String key = in.readUTF();

        return new RequestGetDatagram(key);
    }
}
