package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.RequestGetDatagram;
import com.sd56.common.datagram.RequestMultiGetDatagram;
import com.sd56.common.datagram.RequestPutDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;
import com.sd56.common.datagram.ResponseGetDatagram;
import com.sd56.common.datagram.ResponseMultiGetDatagram;
import com.sd56.common.datagram.ResponsePutDatagram;

public class Handler implements Runnable {
    private Socket socket;
    private Server server;

    public Handler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("[SERVER] - New client connection established.");

        System.out.println("[SERVER/DEBUG] - USERS");
        for (Entry<String,String> entry : server.getDbManager().getUsers().entrySet())
            System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + entry.getValue());

        System.out.println("[SERVER/DEBUG] - DATABASE");
        for (Entry<String, byte[]> entry : server.getDbManager().getDb().entrySet())
            System.out.println("[SERVER/DEBUG] " + entry.getKey() + " : " + new String(entry.getValue()));

        try{
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (!socket.isInputShutdown()) {
                Datagram datagram = Datagram.deserialize(in);
            
                switch (datagram.getType()) {
                    case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                        RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(in, datagram);
                        Boolean authValidation = server.getDbManager().authentication(reqAuth.getUsername(), reqAuth.getPassword());
                        ResponseAuthDatagram resAuth = new ResponseAuthDatagram(authValidation);
                        resAuth.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_PUT:
                        RequestPutDatagram reqPut = RequestPutDatagram.deserialize(in,datagram);
                        String putKey = reqPut.getKey();
                        byte[] value = reqPut.getValue();
                        server.getDbManager().put(putKey, value);
                        Boolean putValidation = false;
                        if(server.getDbManager().getDb().containsKey(putKey) && Arrays.equals(server.getDbManager().getDb().get(putKey),value))
                            putValidation = true;
                        ResponsePutDatagram resPut = new ResponsePutDatagram(putValidation);
                        resPut.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_GET:
                        RequestGetDatagram reqGet = RequestGetDatagram.deserialize(in,datagram);
                        String getKey = reqGet.getKey();
                        byte[] getValue = server.getDbManager().get(getKey);
                        ResponseGetDatagram resGet = new ResponseGetDatagram(getValue);
                        resGet.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIGET:
                        RequestMultiGetDatagram reqMultiGet = RequestMultiGetDatagram.deserialize(in, datagram);
                        Set<String> keys = reqMultiGet.getKeys();
                        Map<String, byte[]> values = new HashMap<>();
                        for (String key : keys) {
                            values.put(key, server.getDbManager().getDb().get(key));
                        }
                        ResponseMultiGetDatagram resMultiGet = new ResponseMultiGetDatagram(values);
                        resMultiGet.serialize(out);
                        break;
                    default:
                        // Ignore it ig?
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("A conexão foi encerrada, já não há mais nada a ler!");;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}