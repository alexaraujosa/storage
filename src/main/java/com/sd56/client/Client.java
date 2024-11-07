package com.sd56.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import com.sd56.common.datagram.Datagram;

public class Client {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Insert username:");
        String username = scanner.nextLine();
        System.out.println("Insert password:");
        String password = scanner.nextLine();

        try (Socket socket = new Socket("localhost", 1337)) {
            System.out.println("Connection established.");
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Datagram datagram = new Datagram(Datagram.DatagramType.DATAGRAM_TYPE_REQUEST_AUTHENTICATION);
            datagram.serialize(out);
            System.out.println("Datagram sent.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
