package com.sd56.common.datagram;

import java.io.*;

public class ResponseGetWhenDatagram extends Datagram {
    private final byte[] value;

    public ResponseGetWhenDatagram(byte[] value) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_GETWHEN);
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


        return baos.toByteArray();
    }

    public static ResponseGetWhenDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_GETWHEN) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }

        boolean exists = in.readBoolean();
        byte[] value = null;
        if (exists) {
            int length = in.readInt();
            value = in.readNBytes(length);
        }

        return new ResponseGetWhenDatagram(value);
    }
}
