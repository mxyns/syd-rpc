package fr.mxyns.rpc.compiler;

import java.util.HashMap;
import java.util.Objects;

import static java.lang.System.exit;

public class ArgsUtils {

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
