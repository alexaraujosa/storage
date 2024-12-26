package com.sd56.common.datagram;

import java.io.*;

public class RequestAuthDatagram extends Datagram {
    private final String username;
    private final String password;

    public RequestAuthDatagram(String username, String password) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_AUTHENTICATION);
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        byte[] baseData = super.serialize();
        out.write(baseData);

        out.writeUTF(this.username);
        out.writeUTF(this.password);

        out.flush();
        return baos.toByteArray();
    }

    public static RequestAuthDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_REQUEST_AUTHENTICATION) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String username = in.readUTF();
        String password = in.readUTF();

        return new RequestAuthDatagram(username, password);
    }

    @Override
    public String toString() {
        return "RequestAuthDatagram{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
