package com.sd56.ui;
import java.util.Scanner;

import com.sd56.client.Client;

public class TextUI {
    private Scanner sc;
    private Client client;

    public TextUI(Client client) {
        sc = new Scanner(System.in);
        this.client = client;
    }


    /**
     * Método que executa o menu principal.
     * Coloca a interface em execução.
     */
    public void run() {
        this.client.tryConnect();
        BetterMenu menu = new BetterMenu(new String[] {
                "Authentication",
                "Send Get",
                "Send Put",
                "Send MultiGet",
                "Send MultiPut",
                "Send GetWhen"
        });

        menu.setPreCondition(1, () -> !this.client.isAuthenticated());
        menu.setPreCondition(2, () -> this.client.isAuthenticated());
        menu.setPreCondition(3, () -> this.client.isAuthenticated());
        menu.setPreCondition(4, () -> this.client.isAuthenticated());
        menu.setPreCondition(5, () -> this.client.isAuthenticated());
        menu.setPreCondition(6, () -> this.client.isAuthenticated());

        menu.setHandler(1, this::authentication);
        menu.setHandler(2, this::get);
        menu.setHandler(3, this::put);
        menu.run();
    }

    private void authentication() {
        System.out.println("Insert username:");
        String username = this.sc.nextLine();
        while (username.isEmpty())  {
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

    private void get(){
        System.out.println("Insert key:");
        String key = this.sc.nextLine();
        client.get(key);
    }

    private void put(){
        System.out.println("Insert key:");
        String key = this.sc.nextLine();
        System.out.println("Insert value:");
        String value = this.sc.nextLine(); // conversao para byte[] deve ser feita no cliente
        client.put(key,value);
    }

}