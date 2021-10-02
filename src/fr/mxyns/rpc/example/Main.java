package fr.mxyns.rpc.example;

import fr.mxyns.rpc.compiler.Server;

import java.io.IOException;
import java.util.HashMap;
import static fr.mxyns.rpc.compiler.ArgsUtils.*;

public class Main {

    public static void main(String[] args) throws IOException, NumberFormatException {

        if (args.length == 0) {
            System.err.println("Are you dumb stupid or dumb ? huh ? No arguments :(");
            System.exit(1);
        }

        HashMap<String, String> argsMap = parseArgs(args);

        Server.TARGET = getArg(argsMap, "-target", Server.TARGET);
        Server.COMM_PORT = Integer.parseInt(getArg(argsMap, "-port", String.valueOf(Server.COMM_PORT)));

        if (hasFlag(argsMap, "-server")) {

            Server.start();

        } else if (hasFlag(argsMap, "-client")) {

            Client.start();

        } else {
            System.out.println("blud are u dumb?");
        }
    }
}
