package com.example.linksuteis.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.linksuteis.R;
import com.example.linksuteis.model.Link;

import java.util.ArrayList;
import java.util.List;

public class ListaLinksAdapter extends BaseAdapter {

    // Dataset do adapter:
    private final List<Link> links = new ArrayList<>();
    private Context context;
    private TextView nome;
    private TextView endereco;
    private ImageView imagem;

    public ListaLinksAdapter(Context context) {
        this.context = context;
    }

    // Indica a qtde de elementos de um adapter
    @Override
    public int getCount() {
        return links.size();
    }

    // Representa o item que pegamos a partir de uma posição
    @Override
    public Link getItem(int position) {
        return links.get(position);
    }

    // Representa o link que estamos pegando
    @Override
    public long getItemId(int position) {
        return links.get(position).getId();
    }

    // Representa a view que estamos apresentando para cada elemento do adapter
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        /*
        *   Com false dizemos que não vamos nos responsabilizar em criar uma view e adicionar
        *   diretamente na viewGroup
        * */
        View viewCriada = LayoutInflater
                .from(context)
                .inflate(R.layout.item_link, viewGroup, false);

        Link linkDevolvido = links.get(position);

        nome = viewCriada.findViewById(R.id.item_link_nome);
        endereco = viewCriada.findViewById(R.id.item_link_endereco);
        imagem = viewCriada.findViewById(R.id.item_link_imagem);

        nome.setText(linkDevolvido.getNome());
        endereco.setText(linkDevolvido.getEndereco());

/*        String caminho = linkDevolvido.getCaminhoImagem();
        Bitmap imagemBitmap = (BitmapFactory.decodeFile(caminho));
        imagem.setImageBitmap(imagemBitmap);*/

        Uri imageUri = Uri.parse(linkDevolvido.getFotoUri());
        imagem.setImageURI(imageUri);

        return viewCriada;
    }

    public void clear() {
        links.clear();
    }

    public void addAll(List<Link> links) {
        this.links.addAll(links);
    }

    public void remove(Link link) {
        links.remove(link);
    }
}
