package com.sd56.ui;
import com.sd56.client.Client;

import java.util.Scanner;

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

        menu.setPreCondition(2, () -> this.client.isAuthenticated());
        //menu.setPreCondition(3, () -> this.client.isAuthenticated());
        //menu.setPreCondition(4, () -> this.client.isAuthenticated());
        //menu.setPreCondition(5, () -> this.client.isAuthenticated());
        //menu.setPreCondition(6, () -> this.client.isAuthenticated());

        menu.setHandler(1, this::authentication);
        //menu.setHandler(2, () -> alterarNomeEmpresa());
        menu.run();
    }

    private void authentication() {
        System.out.println("Insert username:");
        String username = this.sc.nextLine();
        System.out.println("Insert password:");
        String password = this.sc.nextLine();
        client.authenticate(username, password);
    }


    private void alterarNomeEmpresa() {
    }

    private void addVeiculo() {

    }

    private void listVeiculo() {

    }

    private void remVeiculo() {

    }

}