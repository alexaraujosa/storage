package com.sd56.common.datagram;

import java.io.*;

public class ResponseAuthDatagram extends Datagram {
    private final Boolean validation;

    public ResponseAuthDatagram(Boolean validation) {
        super(DatagramType.DATAGRAM_TYPE_RESPONSE_AUTHENTICATION);
        this.validation = validation;
    }

    public Boolean getValidation() { return this.validation; }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeBoolean(validation);

        out.flush();
        return baos.toByteArray();
    }

    public static ResponseAuthDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_RESPONSE_AUTHENTICATION) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        Boolean validation = in.readBoolean();

        return new ResponseAuthDatagram(validation);
    }

    @Override
    public String toString() {
        return "ResponseAuthDatagram{" +
                "validation=" + validation +
                '}';
    }
}
