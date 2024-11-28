package com.sd56.ui;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.sd56.client.Client;

public class TextUI {
    private Scanner sc;
    private Client client;
    private BetterMenu menu;

    public TextUI(Client client) {
        this.sc = new Scanner(System.in);
        this.client = client;
        this.menu = new BetterMenu(new String[] {
                "Close Connection",
                "Authentication",
                "Send Get",
                "Send Put",
                "Send MultiGet",
                "Send MultiPut",
                "Send GetWhen"
        });
    }

    /**
     * Método que executa o menu principal.
     * Coloca a interface em execução.
     */
    public void run() {
        this.client.tryConnect();

        this.menu.setPreCondition(1, () -> !this.client.isAuthenticated());
        this.menu.setPreCondition(2, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(3, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(4, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(5, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(6, () -> this.client.isAuthenticated());

        this.menu.setHandler(0, this::close);
        this.menu.setHandler(1, this::authentication);
        this.menu.setHandler(2, this::get);
        this.menu.setHandler(3, this::put);
        this.menu.setHandler(4, this::multiGet);
        this.menu.setHandler(5, this::multiPut);
        this.menu.run();
    }

    private void close() {
        this.menu.clearScreen();
        System.out.println("Closing the connection.");
        client.close();
    }

    private void authentication() {
        this.menu.clearScreen();
        System.out.println("Insert username:");
        String username = this.sc.nextLine();

        while (username.isEmpty()) {
            System.out.println("The username can't be empty. Try again:");
            username = this.sc.nextLine();
        }
        System.out.println("Insert password:");
        String password = this.sc.nextLine();

        while (password.isEmpty()) {
            System.out.println("The password can't be empty. Try again:");
            password = this.sc.nextLine();
        }
        client.authenticate(username, password);
    }

    private void get() {
        this.menu.clearScreen();
        System.out.println("Insert key:");
        String key = this.sc.nextLine();

        while (key.isEmpty()) {
            System.out.println("The key can't be empty. Try again:");
            key = this.sc.nextLine();
        }
        client.get(key);
    }

    private void put() {
        this.menu.clearScreen();
        System.out.println("Insert key:");
        String key = this.sc.nextLine();

        while (key.isEmpty()) {
            System.out.println("The key can't be empty. Try again:");
            key = this.sc.nextLine();
        }
        System.out.println("Insert value:");
        String value = this.sc.nextLine(); // conversao para byte[] deve ser feita no cliente   // TODO: Convert to byte[] here

        while (value.isEmpty()) {
            System.out.println("The value can't be empty. Try again:");
            value = this.sc.nextLine();
        }
        client.put(key, value);
    }

    private void multiGet() {
        this.menu.clearScreen();
        Set<String> keys = new HashSet<>();
        System.out.println("How many keys do you wanna get?");
        int size = this.sc.nextInt();

        while (size <= 0) {
            System.out.println("The number of keys can't be negative or nil. Try again:");
            size = this.sc.nextInt();
        }
        this.sc.nextLine(); // Clear scanner

        for (int i = 0; i < size; i++) {
            System.out.println("Insert key: ");
            String key = this.sc.nextLine();

            while (key.isEmpty()) {
                System.out.println("The key can't be empty. Try again:");
                key = this.sc.nextLine();
            }
            keys.add(key);
        }
        client.multiGet(keys);
    }

    private void multiPut() {
        this.menu.clearScreen();
        Map<String, byte[]> values = new HashMap<>();
        System.out.println("How many values do you wanna put?");
        int size = this.sc.nextInt();

        while (size <= 0) {
            System.out.println("The number of values can't be negative or nil. Try again:");
            size = this.sc.nextInt();
        }
        this.sc.nextLine(); // Clear scanner

        for (int i = 0; i < size; i++) {
            System.out.println("Insert key: ");
            String key = this.sc.nextLine();

            while (key.isEmpty()) {
                System.out.println("The key can't be empty. Try again:");
                key = this.sc.nextLine();
            }
            System.out.println("Insert value: ");
            String value = this.sc.nextLine();

            while (value.isEmpty()) {
                System.out.println("The value can't be empty. Try again:");
                value = this.sc.nextLine();
            }
            values.put(key, value.getBytes(StandardCharsets.UTF_8));
        }
        client.multiPut(values);
    }
}
