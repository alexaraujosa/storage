package com.sd56.server;

import com.sd56.common.connectors.TaggedConnection;
import com.sd56.common.datagram.Datagram;

import javax.swing.text.html.HTML;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import com.sd56.common.util.LockedResource;

public class GetWhenTuple {
    private final LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?> responses;
    private final TaggedConnection.Frame frame;
    private String key;
    private String keyCond;
    private byte[] valueCond;
    private int flag;

    public GetWhenTuple(
        LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?> responses, 
        TaggedConnection.Frame frame, 
        String key,
        String keyCond, 
        byte[] valueCond
    ) {
        this.responses = responses;
        this.frame = frame;
        this.key = key;
        this.keyCond = keyCond;
        this.valueCond = valueCond;
        this.flag = 0;
    }

    public LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?> getResponses(){
        return this.responses;
    }

    public TaggedConnection.Frame getFrame() {
        return this.frame;
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
