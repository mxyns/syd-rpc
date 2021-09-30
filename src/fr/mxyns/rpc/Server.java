package fr.mxyns.rpc;

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

    public static final int COMM_PORT = 12345;

    public static void start() throws IOException {
        ServerSocket socket = new ServerSocket(Server.COMM_PORT);

        System.out.println("Starting server on 0.0.0.0:" + Server.COMM_PORT);
        while (true) {
            Socket client = socket.accept();
            AtomicInteger i = new AtomicInteger();

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
            System.out.println("Got : " + callee);

            Class calleeClass = callee.getClass();
            Class serverClass = Class.forName(callee.getClass().getName() + "_server");
            // TODO cast callee to "server" type : e.g. : Voiture -> Voiture_server

            String functionName = ois.readUTF();
            int argsCount = ois.readInt();

            Object[] params = new Object[argsCount];
            for (int i = 0; i < argsCount; i++) {
                params[i] = ois.readObject();
            }

            Class<?>[] paramsTypes = new Class[argsCount];
            for (int i = 0; i < argsCount; i++) {
                paramsTypes[i] = params[i].getClass();
            }

            Method method = serverClass.getDeclaredMethod(functionName, paramsTypes);
            Object serverInstance = serverClass.getDeclaredConstructor().newInstance();
            for (Field f : serverClass.getFields()) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    System.out.println(f.getName() + " => " + calleeClass.getField(f.getName()).get(callee) + serverInstance.getClass().getField(f.getName()).get(serverInstance));
                    f.set(serverInstance, calleeClass.getField(f.getName()).get(callee));
                }
            }

            Object result = method.invoke(serverInstance, params);
            oos.writeObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
