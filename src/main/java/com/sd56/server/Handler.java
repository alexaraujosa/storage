package com.sd56.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.RequestPutDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;
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
                        String key = reqPut.getKey();
                        byte[] value = reqPut.getValue();
                        server.getDbManager().put(key, value);
                        Boolean putValidation = false;
                        if(server.getDbManager().getDb().containsKey(key) && Arrays.equals(server.getDbManager().getDb().get(key),value))
                            putValidation = true;
                        ResponsePutDatagram resPut = new ResponsePutDatagram(putValidation);
                        resPut.serialize(out);
                        break;
                    case DATAGRAM_TYPE_REQUEST_GET:
                        //TODO
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