package com.sd56.common.datagram;

import java.io.*;

public class RequestGetDatagram extends Datagram {
    private final String key;
    
    public RequestGetDatagram(String key) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_GET);
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeUTF(key);

        out.flush();
        return baos.toByteArray();
    }

    public static RequestGetDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_REQUEST_GET) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String key = in.readUTF();

        return new RequestGetDatagram(key);
    }

    @Override
    public String toString() {
        return "RequestGetDatagram{" +
                "key='" + key + '\'' +
                '}';
    }
}
