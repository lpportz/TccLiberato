package com.example.ovep.models;

import java.io.Serializable;

public class Usuario implements Serializable {
    private final String id;
    private final String nome;



    public Usuario(String id, String nome, String ignoredEmail) {
        this.id = id;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

}
