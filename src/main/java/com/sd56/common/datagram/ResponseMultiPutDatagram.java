package com.sd56.common.datagram;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ResponseMultiPutDatagram extends Datagram {
    private Map<String, Boolean> validations;

    public ResponseMultiPutDatagram(Map<String, Boolean> validations) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_MULTIPUT);
        this.validations = validations;
    }

    public Map<String, Boolean> getValidations() { return this.validations; }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeInt(validations.size());
        for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeBoolean(entry.getValue());
        }

        out.flush();
        return baos.toByteArray();
    }

    public static ResponseMultiPutDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_MULTIPUT) {
            System.err.println("Invalid datagram type.");
            // TODO: Better error handler
        }

        Map<String, Boolean> validations = new HashMap<>();
        int size = in.readInt();
        for (int i = 0 ; i < size ; i++) {
            String key = in.readUTF();
            Boolean validation = in.readBoolean();
            validations.put(key, validation);
        }

        return new ResponseMultiPutDatagram(validations);
    }

    @Override
    public String toString() {
        return "ResponseMultiPutDatagram{" +
                "validations=" + validations +
                '}';
    }
}
