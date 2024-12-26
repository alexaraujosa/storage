package com.sd56.common.datagram;

import java.io.*;
import java.util.Arrays;

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
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        if (value != null) {
            out.writeBoolean(true);
            out.writeInt(value.length);
            out.write(value);
        } else {
            out.writeBoolean(false);
        }

        out.flush();
        return baos.toByteArray();
    }

    public static ResponseGetDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_GET) {
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

    @Override
    public String toString() {
        return "ResponseGetDatagram{" +
                "value=" + Arrays.toString(value) +
                '}';
    }
}
