package com.sd56.common.datagram;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class RequestMultiGetDatagram extends Datagram {
    private Set<String> keys;

    public RequestMultiGetDatagram(Set<String> keys) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_MULTIGET);
        this.keys = keys;
    }   

    public Set<String> getKeys() { return this.keys; }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeInt(keys.size());
        for (String key : keys)
            out.writeUTF(key);

        out.flush();
        return baos.toByteArray();
    }

    public static RequestMultiGetDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_REQUEST_MULTIGET) {
            System.err.println("Invalid datagram type.");
            // TODO: Better error handler
        }

        int size = in.readInt();
        Set<String> keys = new HashSet<>();
        for (int i = 0 ; i < size ; i++) 
            keys.add(in.readUTF());

        return new RequestMultiGetDatagram(keys);
    }

    @Override
    public String toString() {
        return "RequestMultiGetDatagram{" +
                "keys=" + keys +
                '}';
    }
}
