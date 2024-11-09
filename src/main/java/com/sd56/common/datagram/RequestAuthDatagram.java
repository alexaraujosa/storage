package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestAuthDatagram extends Datagram {
    private final String username;
    private final String password;

    public RequestAuthDatagram(String username, String password) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_AUTHENTICATION);
        this.username = username;
        this.password = password;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeUTF(this.username);
        out.writeUTF(this.password);
    }

    public static RequestAuthDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_REQUEST_AUTHENTICATION) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String username = in.readUTF();
        String password = in.readUTF();

        return new RequestAuthDatagram(username, password);
    }
}
