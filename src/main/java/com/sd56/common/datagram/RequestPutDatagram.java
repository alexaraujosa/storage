package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestPutDatagram extends Datagram {
    private final String key;
    private final byte[] value;

    public RequestPutDatagram(String key, byte[] value) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_PUT);
        this.key = key;
        this.value = value;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeUTF(key);
        out.writeInt(value.length);
        out.write(value);
    }

    public static RequestPutDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_REQUEST_PUT) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String key = in.readUTF();
        int length = in.readInt();
        byte[] value = in.readNBytes(length);

        return new RequestPutDatagram(key, value);
    }
}
