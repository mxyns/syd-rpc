package fr.mxyns.rpc.compiler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

import static java.lang.System.exit;

public class RPCUtils {

    public static String TARGET = "localhost";
    public static int COMM_PORT = 12345;

    /**
     * RPC Related functions
     */
    public static Object genericFunctionCall(String target, int commPort, Object callee, String functionName, Object... args) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        Object result;
        Socket socket = new Socket(target, commPort);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(callee);
        oos.writeUTF(functionName);
        oos.writeInt(args.length);

        if (args.length == 0) {
            /* FIXME needed when argc == 0 for some reason
             * without this the functionName = ois.readUTF() never returns ...
             */
            oos.writeObject(null);
        } else
            for (Object o : args) {
                oos.writeObject(o);
            }

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        Object newState = ois.readObject();
        result = ois.readObject();

        socket.close();

        Class serverClass = newState.getClass();
        for (Field f : callee.getClass().getFields()) {
            if (!Modifier.isFinal(f.getModifiers())) {
                f.set(callee, serverClass.getField(f.getName()).get(newState));
            }
        }

        return result;
    }

    /**
     * CLI Parameter related functions
     */

    public static HashMap<String, String> parseArgs(String[] args) {

        HashMap<String, String> map = new HashMap<>();

        String key = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                key = arg;
                map.put(key, "");
            } else {
                String curr = map.get(key);
                map.put(key, ((curr == null ? "" : curr) + " " + arg).trim());
            }
        }

        return map;
    }

    public static boolean hasFlag(HashMap<String, String> argsMap, String flag) {

        return argsMap.containsKey(flag) && Objects.equals(argsMap.get(flag), "");
    }

    public static String getReqArg(HashMap<String, String> argsMap, String key) {

        String arg = argsMap.get(key);

        if (arg == null) {
            System.err.println("Unspecified argument " + key + ", check documentation for more info");
            exit(1);
        }

        return arg;
    }

    public static String getArg(HashMap<String, String> argsMap, String key, String defaultValue) {

        String arg = argsMap.get(key);
        return arg == null ? defaultValue : arg;
    }

    public static String[] getArgs(HashMap<String, String> argsMap, String key, String[] defaultValue) {

        String arg = getArg(argsMap, key, null);
        return (arg == null ? defaultValue : arg.split(" "));
    }

    public static String[] getReqArgs(HashMap<String, String> argsMap, String key) {

        return getReqArg(argsMap, key).split(" ");
    }

    public static String getLoneArgs(HashMap<String, String> argsMap) {

        return argsMap.get(null);
    }
}
