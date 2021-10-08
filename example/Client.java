package fr.mxyns.rpc.example;

public class Client {

    public static void start() {

        System.out.println("Running Client");
        IVoiture v = new Voiture();
        System.out.println("Initial state : " + v);
        v.name("pouetpouet");
        System.out.println("Renamed : " + v);
        System.out.println("rouleChezToi : " + v.rouleChezToi("dummy"));
        System.out.println("State : " + v);
        Trajet t = v.roule("Paris", "Marseille");
        System.out.println("roule : " + t);
        System.out.println("State : " + v);
        System.out.println("Reversed Name : " + v.reversedName("(: "," :)"));
        System.out.println("State : " + v);
        System.out.println("Trying no arg method : " + v.name());
    }
}
