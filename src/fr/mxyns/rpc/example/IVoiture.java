package fr.mxyns.rpc.example;

import java.io.Serializable;

public interface IVoiture extends Serializable {

    String name();
    void name(String newName);

    Trajet rouleChezToi(String str);
    Trajet roule(String from, String to);
    String reversedName(String prefix, String suffix);
}

