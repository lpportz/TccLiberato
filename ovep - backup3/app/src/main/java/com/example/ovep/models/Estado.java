package com.example.ovep.models;

public class Estado {
    private final int id;
    private final String nome;

    public Estado(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }


}

