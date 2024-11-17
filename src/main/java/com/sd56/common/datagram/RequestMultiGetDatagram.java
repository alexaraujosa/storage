package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(keys.size());
        for (String key : keys)
            out.writeUTF(key);
    }

    public static RequestMultiGetDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        if (dg.getType() != DatagramType.DATAGRAM_TYPE_REQUEST_MULTIGET) {
            System.err.println("Invalid datagram type.");
            // TODO: Better error handler
        }

        int size = in.readInt();
        Set<String> keys = new HashSet<>();
        for (int i = 0 ; i < size ; i++) 
            keys.add(in.readUTF());

        return new RequestMultiGetDatagram(keys);
    }
}
