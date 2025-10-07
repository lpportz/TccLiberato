package com.example.ovep.models;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Empresa implements Serializable {

    private String idDocumento;
    private String nome;
    private String segmento;
    private String estado;
    private String cidade;
    private String rua;
    private String bairro;
    private String cep;
    private String whatsapp;
    private String imagemUrl;

    // Construtor vazio obrigatório para Firestore
    public Empresa(String nome, String segmento) {
        this.nome = nome;
        this.segmento = segmento;
    }
    public Empresa() {
    }

    // Construtor para criação de nova empresa (sem ID de documento)
    public Empresa(String nome, String segmento, String estado, String cidade,
                   String rua, String bairro, String cep, String whatsapp, String imagemUrl) {
        this(null, nome, segmento, estado, cidade, rua, bairro, cep, whatsapp, imagemUrl);
    }

    // Construtor completo com ID de documento (edição ou leitura)
    public Empresa(String idDocumento, String nome, String segmento, String estado, String cidade,
                   String rua, String bairro, String cep, String whatsapp, String imagemUrl) {
        this.idDocumento = idDocumento;
        this.nome = nome;
        this.segmento = segmento;
        this.estado = estado;
        this.cidade = cidade;
        this.rua = rua;
        this.bairro = bairro;
        this.cep = cep;
        this.whatsapp = whatsapp;
        this.imagemUrl = imagemUrl;
    }

    // Getters e Setters

    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }

    public String getNome() {
        return nome;
    }

    public String getSegmento() {
        return segmento;
    }

    public String getEstado() {
        return estado;
    }

    public String getCidade() {
        return cidade;
    }

    public String getRua() {
        return rua;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCep() {
        return cep;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }
}
