package com.sd56.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;
import com.sd56.common.datagram.ResponseGetDatagram;
import com.sd56.common.datagram.ResponsePutDatagram;
import com.sd56.ui.BetterMenu;
import com.sd56.ui.TextUI;

public class Client {
    private final String HOST = "localhost";
    private final int PORT = 1337;

    private boolean authenticated;
    private Socket socket;

    public Client() {
        this.authenticated = false;
    }

    public boolean isAuthenticated(){
        return this.authenticated;
    }

    public void setAuthenticated(boolean authenticated){
        this.authenticated = authenticated;
    }

    public void tryConnect() {
        try {
            this.socket = new Socket(HOST, PORT);
            //Acho que isto não é necessário
            //this.socket.setKeepAlive(true);
            System.out.println("Connection established.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authenticate(String username, String password) {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            RequestAuthDatagram reqAuth = new RequestAuthDatagram(username, password);
            reqAuth.serialize(out);
            out.flush();

            Datagram datagram = Datagram.deserialize(in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_AUTHENTICATION:
                    ResponseAuthDatagram resAuth = ResponseAuthDatagram.deserialize(in, datagram);
                    if (resAuth.getValidation() == false) {
                        System.err.println("Invalid credencials.");
                        return;
                    }
                    //Colocar authenticated igual a true para disponibilizar novas opções
                    this.authenticated = true;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void tryConnection(String username, String password) {
        try (Socket socket = new Socket(this.HOST, this.PORT)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connection established.");

            RequestAuthDatagram reqAuth = new RequestAuthDatagram(username, password);
            reqAuth.serialize(out);
            out.flush();

            while (!socket.isInputShutdown()) {
                Datagram datagram = Datagram.deserialize(in);

                switch (datagram.getType()) {
                    case DATAGRAM_TYPE_RESPONSE_AUTHENTICATION:
                        ResponseAuthDatagram resAuth = ResponseAuthDatagram.deserialize(in, datagram);
                        if (resAuth.getValidation() == false) {
                            System.err.println("Invalid credencials.");
                            return;
                        }
                        //Colocar authenticated igual a true para disponibilizar novas opções
                        this.authenticated = true;
                        break;
                    case DATAGRAM_TYPE_RESPONSE_PUT:
                        ResponsePutDatagram resPut = ResponsePutDatagram.deserialize(in, datagram);
                        // TODO
                        break;
                    case DATAGRAM_TYPE_RESPONSE_GET:
                        ResponseGetDatagram resGet = ResponseGetDatagram.deserialize(in, datagram);
                        // TODO
                        break;
                    default:
                        // Just ignore it.
                        break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        TextUI menu = new TextUI(client);

        menu.run();
    }
}
