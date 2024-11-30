package com.sd56.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.sd56.common.datagram.Datagram;
import com.sd56.common.datagram.Datagram.DatagramType;
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

public class Client {
    private final String HOST = "localhost";
    private final int PORT = 1337;

    private boolean authenticated;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

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
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connection established.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            Datagram closeDg = new Datagram(DatagramType.DATAGRAM_TYPE_REQUEST_CLOSE);
            closeDg.serialize(this.out);
            this.out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authenticate(String username, String password) {
        try {
            RequestAuthDatagram reqAuth = new RequestAuthDatagram(username, password);
            reqAuth.serialize(this.out);
            this.out.flush();

            System.out.println("Awaiting for authentication...");

            Datagram datagram = Datagram.deserialize(this.in);
            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_AUTHENTICATION:
                    ResponseAuthDatagram resAuth = ResponseAuthDatagram.deserialize(this.in, datagram);
                    if (!resAuth.getValidation()) {
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
            RequestGetDatagram reqGet = new RequestGetDatagram(key);
            reqGet.serialize(this.out);
            this.out.flush();

            Datagram datagram = Datagram.deserialize(this.in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_GET:
                    ResponseGetDatagram resGet = ResponseGetDatagram.deserialize(this.in, datagram);
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
            RequestPutDatagram reqPut = new RequestPutDatagram(key, value.getBytes(StandardCharsets.UTF_8));
            reqPut.serialize(this.out);
            this.out.flush();

            Datagram datagram = Datagram.deserialize(this.in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_PUT:
                    ResponsePutDatagram resPut = ResponsePutDatagram.deserialize(this.in, datagram);
                    if (!resPut.getValidation()) {
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
            RequestMultiGetDatagram reqMultiGet = new RequestMultiGetDatagram(keys);
            reqMultiGet.serialize(this.out);
            this.out.flush();
            
            Datagram datagram = Datagram.deserialize(this.in);

            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_MULTIGET:
                    ResponseMultiGetDatagram resMultiGet = ResponseMultiGetDatagram.deserialize(this.in, datagram);
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
            RequestMultiPutDatagram reqMultiPut = new RequestMultiPutDatagram(values);
            reqMultiPut.serialize(this.out);
            this.out.flush();

            Datagram datagram = Datagram.deserialize(this.in);
            switch (datagram.getType()) {
                case DATAGRAM_TYPE_RESPONSE_MULTIPUT:
                    ResponseMultiPutDatagram resMultiPut = ResponseMultiPutDatagram.deserialize(this.in, datagram);
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

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        ClientUI menu = new ClientUI(client);

        menu.run();
    }
}
