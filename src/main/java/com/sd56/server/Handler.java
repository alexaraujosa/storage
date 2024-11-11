package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.sd56.common.datagram.*;

public class Handler implements Runnable {
    private Socket socket;
    private Server server;

    public Handler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        System.out.println("Hello! Me am a Thread");

        try{
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (!socket.isInputShutdown()) {
                Datagram datagram = Datagram.deserialize(in);
            
                switch (datagram.getType()) {
                    case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                        RequestAuthDatagram reqAuth = RequestAuthDatagram.deserialize(in, datagram);
                        Boolean authValidation = server.getDbManager().login(reqAuth.getUsername(), reqAuth.getPassword());
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