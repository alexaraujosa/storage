package com.sd56.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.RequestAuthDatagram;
import com.sd56.common.datagram.RequestGetDatagram;
import com.sd56.common.datagram.RequestMultiGetDatagram;
import com.sd56.common.datagram.RequestMultiPutDatagram;
import com.sd56.common.datagram.RequestPutDatagram;
import com.sd56.common.datagram.ResponseAuthDatagram;
import com.sd56.common.datagram.ResponseGetDatagram;
import com.sd56.common.datagram.ResponseMultiGetDatagram;
import com.sd56.common.datagram.ResponseMultiPutDatagram;
import com.sd56.common.datagram.ResponsePutDatagram;
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

    public void get(String key) {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            RequestGetDatagram reqGet = new RequestGetDatagram(key);
            reqGet.serialize(out);
            out.flush();

            Datagram datagram = Datagram.deserialize(in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_GET:
                    ResponseGetDatagram resGet = ResponseGetDatagram.deserialize(in, datagram);
                    if (resGet.getValue() == null) {
                        System.err.println("Invalid key");
                        return;
                    } else{
                        System.out.println("Value: " + new String(resGet.getValue(), StandardCharsets.UTF_8));
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

    public void put(String key, String value) {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            RequestPutDatagram reqPut = new RequestPutDatagram(key, value.getBytes(StandardCharsets.UTF_8));
            reqPut.serialize(out);
            out.flush();

            Datagram datagram = Datagram.deserialize(in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_PUT:
                    ResponsePutDatagram resPut = ResponsePutDatagram.deserialize(in, datagram);
                    if (resPut.getValidation() == false) {
                        System.err.println("The operation put could not succeed.");
                        return;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void multiGet(Set<String> keys) {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            RequestMultiGetDatagram reqMultiGet = new RequestMultiGetDatagram(keys);
            reqMultiGet.serialize(out);
            out.flush();
            
            Datagram datagram = Datagram.deserialize(in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_MULTIGET:
                    ResponseMultiGetDatagram resMultiGet = ResponseMultiGetDatagram.deserialize(in, datagram);
                    Map<String, byte[]> values = resMultiGet.getValues();
                    for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                        if (entry.getValue() == null) {
                            System.out.println("Key doesn't exist.");
                        } else {
                            System.out.println("Key: " + entry.getKey() + " || Value: " + new String(entry.getValue(), StandardCharsets.UTF_8));
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void multiPut(Map<String, byte[]> values) {
        try {
            DataInputStream in = new DataInputStream(this.socket.getInputStream());
            DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
            RequestMultiPutDatagram reqMultiPut = new RequestMultiPutDatagram(values);
            reqMultiPut.serialize(out);
            out.flush();

            Datagram datagram = Datagram.deserialize(in);
            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_MULTIPUT:
                    ResponseMultiPutDatagram resMultiPut = ResponseMultiPutDatagram.deserialize(in, datagram);
                    Map<String, Boolean> validations = resMultiPut.getValidations();

                    for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
                        if (entry.getValue()) {
                            System.out.println("Key '" + entry.getKey() + "' successfully added to the database.");
                        } else {
                            System.out.println("Key '" + entry.getKey() + "' not added to the database.");
                        }
                    }
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
