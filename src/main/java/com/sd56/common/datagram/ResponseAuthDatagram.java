package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ResponseAuthDatagram extends Datagram {
    private final Boolean validation;

    public ResponseAuthDatagram(Boolean validation) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_AUTHENTICATION);
        this.validation = validation;
    }

    public Boolean getValidation() { return this.validation; }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeBoolean(validation);
    }

    public static ResponseAuthDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_RESPONSE_AUTHENTICATION) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        Boolean validation = in.readBoolean();

        return new ResponseAuthDatagram(validation);
    }
}
