package com.sd56.common.datagram;

import java.io.*;

public class RequestGetWhenDatagram extends Datagram {
    private final String key;
    private final String keyCond;
    private final byte[] valueCond;

    public RequestGetWhenDatagram(String key, String keyCond, byte[] valueCond) {
        super(DatagramType.DATAGRAM_TYPE_REQUEST_GETWHEN);
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond;
    }

    public String getKey(){
        return this.key;
    }

    public String getKeyCond() { return this.keyCond; }

    public byte[] getValueCond() { return this.valueCond; }

    @Override
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.write(super.serialize());

        out.writeUTF(this.key);
        out.writeUTF(this.keyCond);
        out.writeInt(this.valueCond.length);
        out.write(this.valueCond);


        return baos.toByteArray();
    }

    public static RequestGetWhenDatagram deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data,8,data.length-8);
        DataInputStream in = new DataInputStream(bais);

        Datagram baseDatagram = Datagram.deserialize(data);

        if (baseDatagram.getType() != DatagramType.DATAGRAM_TYPE_REQUEST_GETWHEN) {
            System.err.println("Invalid datagram type.");
            // TODO: Melhorar aqui também.
        }
        String key = in.readUTF();
        String keyCond = in.readUTF();
        int length = in.readInt();
        byte[] valueCond = in.readNBytes(length);

        return new RequestGetWhenDatagram(key, keyCond, valueCond);
    }
}
