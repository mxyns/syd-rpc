package fr.mxyns.rpc.example;

public class Voiture_dist implements IVoiture {

    public String name = "";

    @Override
    public String name() {

        return name;
    }

    @Override
    public void name(String newName) {

        this.name = newName;
    }

    @Override
    public Trajet rouleChezToi(String str) {

        return new Trajet(this.name, new StringBuilder(this.name).reverse().toString());
    }

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

        return "Voiture_dist{" +
               "name='" + name + '\'' +
               '}';
    }
}
