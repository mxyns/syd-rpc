package fr.mxyns.rpc;

import java.io.IOException;
import java.io.Serializable;

public interface IVoiture extends Serializable {

    Trajet roule(String from, String to);
    String reversedName(String prefix, String suffix);
}

class Voiture implements IVoiture {

    public String name = "";

    @Override
    public Trajet roule(String from, String to) {
        try {
            return (Trajet) Client.genericFunctionCall(Client.TARGET, Client.COMM_PORT, this, "roule", from, to);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String reversedName(String prefix, String suffix) {
        try {
            return (String) Client.genericFunctionCall(Client.TARGET, Client.COMM_PORT, this, "reversedName", prefix, suffix);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String toString() {
        return "Voiture_server{" +
                "name='" + name + '\'' +
                '}';
    }
}

class Voiture_server implements IVoiture {

    public String name = "";

    @Override
    public Trajet roule(String from, String to) {
        return new Trajet(from, to);
    }

    @Override
    public String reversedName(String prefix, String suffix) {
        return prefix + new StringBuilder(this.name).reverse() + suffix;
    }

    @Override
    public String toString() {
        return "Voiture_server{" +
                "name='" + name + '\'' +
                '}';
    }
}