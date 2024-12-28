package com.sd56.server.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.sd56.common.connectors.TaggedConnection;
import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestGetDatagram;
import com.sd56.common.datagram.RequestGetWhenDatagram;
import com.sd56.common.datagram.RequestMultiGetDatagram;
import com.sd56.common.datagram.RequestMultiPutDatagram;
import com.sd56.common.datagram.RequestPutDatagram;
import com.sd56.common.datagram.ResponseGetDatagram;
import com.sd56.common.datagram.ResponseGetWhenDatagram;
import com.sd56.common.datagram.ResponseMultiGetDatagram;
import com.sd56.common.datagram.ResponseMultiPutDatagram;
import com.sd56.common.datagram.ResponsePutDatagram;
import com.sd56.common.util.LockedResource;
import com.sd56.server.DatabaseManager;
import com.sd56.server.GetWhenTuple;

public class RequestHandler implements Runnable {
    private final LockedResource<LinkedList<Map.Entry<LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?>, TaggedConnection.Frame>>, ?> requests;
    private final LockedResource<LinkedList<GetWhenTuple>, ?> getWhen;
    private final DatabaseManager dbManager;

    public RequestHandler(
        LockedResource<LinkedList<Map.Entry<LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?>, TaggedConnection.Frame>>, ?> requests,
        LockedResource<LinkedList<GetWhenTuple>, ?> getWhen,
        DatabaseManager dbManager
    ) {
        this.requests = requests;
        this.getWhen = getWhen;
        this.dbManager = dbManager;
    }

    private Map.Entry<LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?>, TaggedConnection.Frame> getRequestEntry() throws InterruptedException {
        this.requests.lock();
        try {
            while (this.requests.getResource().isEmpty())
                this.requests.await();
            
            return this.requests.getResource().poll();
        } finally {
            this.requests.unlock();
        }
    }

    @Override
    public void run() {
        while (true) { 
            try {
                Map.Entry<LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?>, TaggedConnection.Frame> entry = this.getRequestEntry();
                TaggedConnection.Frame frame = entry.getValue();
                int tag = frame.tag;
                byte[] dg = frame.data;

                Datagram request = Datagram.deserialize(dg);
                LockedResource<LinkedList<Map.Entry<TaggedConnection.Frame, Datagram>>, ?> responses = entry.getKey();
                switch (request.getType()) {
                    case DATAGRAM_TYPE_REQUEST_GET:
                        RequestGetDatagram getDg = RequestGetDatagram.deserialize(dg);
                        String getKey = getDg.getKey();
                        byte[] getValue = this.dbManager.get(getKey);
                        ResponseGetDatagram resGet = new ResponseGetDatagram(getValue);
                        responses.lock();
                        try {
                            responses.getResource().add(new AbstractMap.SimpleEntry<>(frame, resGet));
                        } finally {
                            responses.unlock();
                        }
                        // con.send(tag, resGet.serialize());
                        break;
                    case DATAGRAM_TYPE_REQUEST_PUT:
                        RequestPutDatagram putDg = RequestPutDatagram.deserialize(dg);
                        String putKey = putDg.getKey();
                        byte[] putValue = putDg.getValue();
                        this.dbManager.put(putKey, putValue);
                        boolean putValidation = this.dbManager.getDb().containsKey(putKey) && Arrays.equals(this.dbManager.getDb().get(putKey), putValue);
                        ResponsePutDatagram resPut = new ResponsePutDatagram(putValidation);
                        responses.lock();
                        try {
                            responses.getResource().add(new AbstractMap.SimpleEntry<>(frame, resPut));
                        } finally {
                            responses.unlock();
                        }
                        // con.send(tag, resPut.serialize());
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIGET:
                        RequestMultiGetDatagram multigetDg = RequestMultiGetDatagram.deserialize(dg);
                        Set<String> multiGetKeys = multigetDg.getKeys();
                        Map<String, byte[]> multiGetValues = new HashMap<>();
                        for (String key : multiGetKeys) {
                            multiGetValues.put(key, this.dbManager.getDb().get(key));
                        }
                        ResponseMultiGetDatagram resMultiGet = new ResponseMultiGetDatagram(multiGetValues);
                        responses.lock();
                        try {
                            responses.getResource().add(new AbstractMap.SimpleEntry<>(frame, resMultiGet));
                        } finally {
                            responses.unlock();
                        }
                        // con.send(tag, resMultiGet.serialize());
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIPUT:
                        RequestMultiPutDatagram multiputDg = RequestMultiPutDatagram.deserialize(dg);
                        Map<String, byte[]> multiPutValues = multiputDg.getValues();
                        Map<String, Boolean> multiPutValidations = new HashMap<>();
                        for (Map.Entry<String, byte[]> multiPutEntry : multiPutValues.entrySet()) {
                            this.dbManager.put(multiPutEntry.getKey(), multiPutEntry.getValue());
                            boolean multiPutValidation = this.dbManager.getDb().containsKey(multiPutEntry.getKey()) &&
                                    Arrays.equals(this.dbManager.getDb().get(multiPutEntry.getKey()), multiPutEntry.getValue());
                            multiPutValidations.put(multiPutEntry.getKey(), multiPutValidation);
                        }

                        ResponseMultiPutDatagram resMultiPut = new ResponseMultiPutDatagram(multiPutValidations);
                        responses.lock();
                        try {
                            responses.getResource().add(new AbstractMap.SimpleEntry<>(frame, resMultiPut));
                        } finally {
                            responses.unlock();
                        }
                        // con.send(tag, resMultiPut.serialize());
                        break;
                    case DATAGRAM_TYPE_REQUEST_GETWHEN:
                        RequestGetWhenDatagram getWhenDg = RequestGetWhenDatagram.deserialize(dg);
                        String getKey2 = getWhenDg.getKey();
                        String getKeyCond = getWhenDg.getKeyCond();
                        byte[] getValueCond = getWhenDg.getValueCond();

                        //System.out.println("\nKey to search: " + getKey2);
                        //System.out.println("\nValueCond got: " + Arrays.toString(this.dbManager.get(getKeyCond)));
                        //System.out.println("\nValueCond wanted: " + Arrays.toString(getValueCond));
                        if (Arrays.equals(this.dbManager.get(getKeyCond), getValueCond)) {
                            byte[] getValue2 = this.dbManager.get(getKey2);
                            //System.out.println("\nValue obtained: " + Arrays.toString(getValue2));
                            ResponseGetWhenDatagram resGetWhen = new ResponseGetWhenDatagram(getValue2);
                            responses.lock();
                            try {
                                responses.getResource().add(new AbstractMap.SimpleEntry<>(frame, resGetWhen));
                            } finally {
                                responses.unlock();
                            }
                            // con.send(tag, resGetWhen.serialize());
                        } else {
                            // colocar na queue
                            GetWhenTuple tuple = new GetWhenTuple(responses, frame, getKey2, getKeyCond, getValueCond);
                            this.getWhen.lock();
                            try {
                                this.getWhen.getResource().add(tuple);
                            } finally {
                                this.getWhen.unlock();
                            }
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }
}
