package com.sd56.ui;

import java.awt.*;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private List<String> options;
    private int op;

    public Menu(String[] opcoes) {
        this.options = Arrays.asList(opcoes);
        this.op = 0;
    }

    public void executa() {
        do {
            showMenu();
            this.op = lerOpcao();
        } while (this.op == -1);
    }

    private void showMenu() {
        System.out.println("\n --- Menu --- ");
        for (int i=0; i<this.options.size(); i++) {
            System.out.print(i+1);
            System.out.print(" - ");
            System.out.println(this.options.get(i));
        }
        System.out.println("0 - Sair");
    }

    private int lerOpcao() {
        int op;
        Scanner is = new Scanner(System.in);

        System.out.print("Option: ");
        try {
            op = is.nextInt();
        }
        catch (InputMismatchException e) {
            op = -1;
        }
        if (op<0 || op > this.options.size()) {
            System.out.println("Invalid option! Try again.!!!");
            op = -1;
        }
        return op;
    }

    public int getOpcao() {
        return this.op;
    }
}
