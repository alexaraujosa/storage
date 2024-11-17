package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
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
    }

    public static ResponseMultiGetDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        if (dg.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_MULTIGET) {
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

}
