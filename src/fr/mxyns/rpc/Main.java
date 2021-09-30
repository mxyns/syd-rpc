package fr.mxyns.rpc;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args[0].equalsIgnoreCase("-server")) {

            Server.start();

        } else if (args[0].equalsIgnoreCase("-client")) {

            Client.start();

        } else {
            System.out.println("blud are u dumb?");
        }
    }
}
