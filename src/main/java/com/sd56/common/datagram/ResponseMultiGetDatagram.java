package com.sd56.common.datagram;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResponseMultiGetDatagram extends Datagram {
    private Map<String, byte[]> values;

    public ResponseMultiGetDatagram(Map<String, byte[]> values) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_MULTIGET);
        this.values = values;
    }

    public Map<String, byte[]> getValues() { return this.values; }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeInt(values.size());
        for(Map.Entry<String, byte[]> entry : values.entrySet()) {
            out.writeUTF(entry.getKey());
            if (entry.getValue() != null) {
                out.writeBoolean(true);
                out.writeInt(entry.getValue().length);
                out.write(entry.getValue());
            } else {
                out.writeBoolean(false);
            }
        }

        out.flush();
        return baos.toByteArray();
    }

    public static ResponseMultiGetDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_MULTIGET) {
            System.err.println("Invalid datagram type.");
            // TODO: Better error handler
        }

        Map<String, byte[]> values = new HashMap<>();
        int size = in.readInt();
        for (int i = 0 ; i < size ; i++) {
            String key = in.readUTF();
            Boolean exists = in.readBoolean();
            byte[] value = null;
            if (exists) {
                int len = in.readInt();
                value = in.readNBytes(len);
            }
            values.put(key, value);
        }

        return new ResponseMultiGetDatagram(values);
    }

    @Override
    public String toString() {
        return "ResponseMultiGetDatagram{" +
                "values=" + values +
                '}';
    }
}
