package com.example.linksuteis.dao;

import com.example.linksuteis.model.Link;

import java.util.ArrayList;
import java.util.List;

public class LinkDAO {

    private final static List<Link> links = new ArrayList<>();
    private static int contadorDeIds = 1;

    public void salva(Link link) {
        link.setId(contadorDeIds);
        links.add(link);

        atualizaIds();
    }

    private void atualizaIds() {
        contadorDeIds++;
    }

    public void edita(Link link){

        Link linkEncontrado = buscaLinkPeloId(link);

        if (linkEncontrado != null) {
            int posicaoDoLink = links.indexOf(linkEncontrado);
            links.set(posicaoDoLink, link);
        }
    }

    private Link buscaLinkPeloId(Link link) {
        //Link linkEncontrado = null;
        for (Link l:
             links) {
            if (l.getId() == link.getId()) {
                return l;
            }
        }
        return null;
    }

    public List<Link> todos() {
        return new ArrayList<>(links);
    }

    public void remove(Link link) {
        Link linkDevolvido = buscaLinkPeloId(link);
        if (linkDevolvido != null) {
            links.remove(linkDevolvido);
        }

    }

    // teste
    //public Link primeiro() { return links.get(0); }
}
