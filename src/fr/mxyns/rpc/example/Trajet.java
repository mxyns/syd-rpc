package fr.mxyns.rpc.example;

import java.io.Serializable;

public class Trajet implements Serializable {

    public String from, to;

    public Trajet(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "Trajet{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}
