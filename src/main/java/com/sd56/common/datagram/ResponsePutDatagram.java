package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResponsePutDatagram extends Datagram {
    private final boolean validation;

    public ResponsePutDatagram(Boolean validation) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_PUT);
        this.validation = validation;
    }

    public Boolean getValidation() { return this.validation; }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeBoolean(validation);
    }

    public static ResponsePutDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_RESPONSE_PUT) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        Boolean validation = in.readBoolean();

        return new ResponsePutDatagram(validation);
    }
}
