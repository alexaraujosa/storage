package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

public class GetWhenTuple {
    private final DataOutputStream out;
    private String key;
    private String keyCond;
    private byte[] valueCond;
    private int flag;

    public GetWhenTuple(DataOutputStream out, String key,String keyCond, byte[] valueCond){
        this.out = out;
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond;
        this.flag = 0;
    }

    public DataOutputStream getDataOutputStream(){
        return this.out;
    }

    public String getKey(){
        return this.key;
    }

    public String getKeyCond(){
        return this.keyCond;
    }

    public byte[] getValueCond(){
        return this.valueCond;
    }

    public int getFlag(){
        return this.flag;
    }

    public void setFlagTrue() {
        this.flag = 1;
    }

    public void setFlagFalse() {
        this.flag = 0;
    }

    @Override
    public String toString(){
        return "\n(Key: " + this.key + ", KeyCond: " + this.keyCond + ", ValueCond: " + Arrays.toString(this.valueCond) + ", Flag: " + this.flag + ")\n";
    }


}
