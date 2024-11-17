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

    public byte[] getValue(){
        return this.value;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        if (value != null) {
            out.writeBoolean(true);
            out.writeInt(value.length);
            out.write(value);
        } else {
            out.writeBoolean(false);
        }
    }

    public static ResponseGetDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_RESPONSE_GET) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        boolean exists = in.readBoolean();
        byte[] value = null;
        if (exists) {
            int length = in.readInt();
            value = in.readNBytes(length);
        }

        return new ResponseGetDatagram(value);
    } 
}
