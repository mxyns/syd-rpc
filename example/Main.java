package fr.mxyns.rpc.example;

import fr.mxyns.rpc.compiler.Server;

import java.io.IOException;
import java.util.HashMap;
import fr.mxyns.rpc.compiler.RPCUtils;
import static fr.mxyns.rpc.compiler.RPCUtils.*;

public class Main {

    public static void main(String[] args) throws IOException, NumberFormatException {

        if (args.length == 0) {
            System.err.println("Are you dumb stupid or dumb ? huh ? No arguments :(");
            System.exit(1);
        }

        HashMap<String, String> argsMap = parseArgs(args);

        RPCUtils.TARGET = getArg(argsMap, "-target", RPCUtils.TARGET);
        RPCUtils.COMM_PORT = Integer.parseInt(getArg(argsMap, "-port", String.valueOf(RPCUtils.COMM_PORT)));

        if (hasFlag(argsMap, "-server")) {

            Server.start();

        } else if (hasFlag(argsMap, "-client")) {

            Client.start();

        } else {
            System.out.println("blud are u dumb?");
        }
    }
}
