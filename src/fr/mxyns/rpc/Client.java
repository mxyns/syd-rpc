package fr.mxyns.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    public static final String TARGET = "localhost";
    public static final int COMM_PORT = 12345;

    public static void start() {

        System.out.println("Running Client");
        Voiture v = new Voiture();
        v.name = "pouetpouet";
        Trajet t = v.roule("Paris", "Marseille");
        System.out.println(t);
        System.out.println(v.reversedName("(: "," :)"));
    }

    static Object genericFunctionCall(String target, int commPort, Object callee, String functionName, Object... args) throws IOException, ClassNotFoundException {

        Object result;
        Socket socket = new Socket(target, commPort);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(callee);
        oos.writeUTF(functionName);
        oos.writeInt(args.length);
        for (Object o : args)
            oos.writeObject(o);

        result = new ObjectInputStream(socket.getInputStream()).readObject();
        socket.close();

        return result;
    }
}
