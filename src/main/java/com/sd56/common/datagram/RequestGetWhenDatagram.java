package com.sd56.common.datagram;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeUTF(this.key);
        out.writeUTF(this.keyCond);
        out.writeInt(this.valueCond.length);
        out.write(this.valueCond);
    }

    public static RequestGetWhenDatagram deserialize(DataInputStream in, Datagram dg) throws IOException {
        DatagramType type = dg.getType();
        if (type != DatagramType.DATAGRAM_TYPE_REQUEST_GETWHEN) {
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
