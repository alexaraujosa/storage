package com.sd56.common.datagram;

import java.io.*;
import java.util.Arrays;

public class RequestPutDatagram extends Datagram {
    private final String key;
    private final byte[] value;

    public RequestPutDatagram(String key, byte[] value) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_PUT);
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeUTF(key);
        out.writeInt(value.length);
        out.write(value);

        out.flush();
        return baos.toByteArray();
    }

    public static RequestPutDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_REQUEST_PUT) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String key = in.readUTF();
        int length = in.readInt();
        byte[] value = in.readNBytes(length);

        return new RequestPutDatagram(key, value);
    }

    @Override
    public String toString() {
        return "RequestPutDatagram{" +
                "key='" + key + '\'' +
                ", value=" + Arrays.toString(value) +
                '}';
    }
}
