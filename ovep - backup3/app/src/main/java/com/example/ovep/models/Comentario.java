package com.example.ovep.models;

import java.io.Serializable;
import java.util.Date;

public class Comentario implements Serializable {
    private String idDocumento; // ID do comentário no Firestore
    private String texto;
    private String autor;
    private Date data;

    // Construtor vazio obrigatório para Firebase
    public Comentario() {}

    // Construtor usado para criação com timestamp em milissegundos (se necessário)
    public Comentario(String autor, String texto, long timestamp) {
        this.autor = autor;
        this.texto = texto;
        this.data = new Date(timestamp);
    }

    // Construtor mais comum usado ao criar um novo comentário
    public Comentario(String texto, String autor, Date data) {
        this.texto = texto;
        this.autor = autor;
        this.data = data;
    }

    // Getters e Setters
    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}
