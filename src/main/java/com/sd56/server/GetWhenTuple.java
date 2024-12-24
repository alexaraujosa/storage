package com.sd56.server;

public class GetWhenTuple {
    private String keyCond;
    private byte[] valueCond;
    private int flag;

    public GetWhenTuple(String keyCond, byte[] valueCond){
        this.keyCond = keyCond;
        this.valueCond = valueCond;
        this.flag = 0;
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


}
