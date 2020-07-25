package com.example.linksuteis.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Link implements Serializable {
    private int id = 0;
    private String nome;
    private String endereco;
    private String fotoUri;
    private String caminhoImagem;


    public Link(String nome, String endereco, String foto, String imagePath) {

        this.nome = nome;
        this.endereco = endereco;
        this.fotoUri = foto;
        this.caminhoImagem = imagePath;
    }

    public Link() {

    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }

    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    @NonNull
    @Override
    public String toString() {
        return nome;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean temIdValido() {
        return id > 0;  // Condição para id válido
    }
}
