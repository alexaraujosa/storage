package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResponseGetDatagram extends Datagram {
    private final byte[] value;

    public ResponseGetDatagram(byte[] value) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_GET);
        this.value = value;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(value.length);
        out.write(value);
    }

    public static ResponseGetDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_RESPONSE_GET) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        int length = in.readInt();
        byte[] value = in.readNBytes(length);

        return new ResponseGetDatagram(value);
    } 
}
