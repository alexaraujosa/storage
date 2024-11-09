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

public class Client {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Insert username:");
        String username = scanner.nextLine();
        System.out.println("Insert password:");
        String password = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 1337)) {
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
                        // TODO: Display Main Menu
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
    }
}
