package fr.mxyns.rpc.compiler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    public static String TARGET = "localhost";
    public static int COMM_PORT = 12345;

    public static void start() throws IOException {

        ServerSocket socket = new ServerSocket(Server.COMM_PORT);

        System.out.println("Starting server on 0.0.0.0:" + Server.COMM_PORT);

        AtomicInteger i = new AtomicInteger();
        while (true) {
            Socket client = socket.accept();

            new Thread(() -> {
                System.out.println("Handling new client #" + i.getAndIncrement());
                handleClient(client);
            }).start();
        }
    }

    private static void handleClient(Socket client) {

        try {
            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

            Object callee = ois.readObject();

            Class calleeClass = callee.getClass();
            Class serverClass = Class.forName(callee.getClass().getName() + "_dist");

            String functionName = ois.readUTF();
            int argsCount = ois.readInt();

            Object[] params = new Object[argsCount];
            if (argsCount == 0) {
                //System.out.println("Got expected null (argc == 0)");
                // FIXME read the expected null in case of argc == 0
                ois.readObject();
            } else {
                for (int i = 0; i < argsCount; i++) {
                    params[i] = ois.readObject();
                }
            }

            Class<?>[] paramsTypes = new Class[argsCount];
            for (int i = 0; i < argsCount; i++) {
                paramsTypes[i] = params[i].getClass();
            }

            Method method = serverClass.getDeclaredMethod(functionName, paramsTypes);
            Object serverInstance = serverClass.getDeclaredConstructor().newInstance();
            for (Field f : serverClass.getFields()) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    f.set(serverInstance, calleeClass.getField(f.getName()).get(callee));
                }
            }
            Object result = method.invoke(serverInstance, params);

            oos.writeObject(serverInstance);
            oos.writeObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
}
