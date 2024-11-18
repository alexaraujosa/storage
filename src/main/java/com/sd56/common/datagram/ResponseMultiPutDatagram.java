package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(validations.size());
        for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeBoolean(entry.getValue());
        }
    }

    public static ResponseMultiPutDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        if (dg.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_MULTIPUT) {
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
}
