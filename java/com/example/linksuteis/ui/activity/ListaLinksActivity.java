package com.example.linksuteis.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.linksuteis.R;
import com.example.linksuteis.dao.LinkDAO;
import com.example.linksuteis.model.Link;
import com.example.linksuteis.ui.adapter.ListaLinksAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.linksuteis.ui.activity.ConstantesActivities.CHAVE_LINK;

public class ListaLinksActivity extends AppCompatActivity {

    public static final String TITULO_APPBAR = "Lista de Links Úteis";

    private final LinkDAO dao = new LinkDAO();
    //private ArrayAdapter<Link> adapter;
    private int position;
    private String oldPath;
    private ListaLinksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_links);

        setTitle(TITULO_APPBAR);
        configuraFabNovoLink();
        configuraLista();

        dao.salva(new Link("Rodrigo",
                "www.rodrigo.com", "file:///data/user/0/com.example.linksuteis/cache/cropped1360465431290333118.jpg",
                Environment.getExternalStorageDirectory() + "/LinksUteis/imagens/cropped1360465431290333118.jpg"));
        dao.salva(new Link("Josi",
                "www.josi.com", "file:///data/user/0/com.example.linksuteis/cache/cropped1360465431290333118.jpg",
                Environment.getExternalStorageDirectory() + "/LinksUteis/imagens/cropped1360465431290333118.jpg"));

        //new Link("Rodrigo", "www", )

    }

    /**
     *       Vamos reescrever o método responsável pela criação do "menu de contexto", que é aquele que
     *      aparece com o botão direito do mouse. Ele proverá uma melhor UX no momento de excluir
     *      links
     * */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        /*
        *   Vinculando ContextMenu menu ao arquivo estático criado em res/menu/
        *   com seus itens de menu de contexto
        * */
        getMenuInflater().inflate(R.menu.activity_lista_links_menu, menu);
/*
        menu.add("Editar");
        menu.add("Remover");
        */
    }


    /**
    *   Uma espécie de listener dos itens do menu de contexto
    *   Funciona se a view é um adapterView
    * */
    @Override
    public boolean onContextItemSelected(MenuItem item) {


        int itemId = item.getItemId();
        if (itemId == R.id.activity_lista_links_menu_remover) {
            AdapterView.AdapterContextMenuInfo menuInfo =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Link linkEscolhido = adapter.getItem(menuInfo.position);
            remove(linkEscolhido);
        }
        if (itemId == R.id.activity_lista_links_menu_editar) {
            AdapterView.AdapterContextMenuInfo menuInfo =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Link linkEscolhido = adapter.getItem(menuInfo.position);
            abreFormularioModoEditaLink(linkEscolhido);
        }

        /*
        if (item.getTitle().equals("Remover")) {
            remove(linkEscolhido);
        }
        if (item.getTitle().equals("Editar")) {
            abreFormularioModoEditaLink(linkEscolhido);
        }
*/

        return super.onContextItemSelected(item);
    }

    private void configuraFabNovoLink() {
        FloatingActionButton botaoNovoLink = findViewById(R.id.activity_lista_links_plus_floating_action_button);
        botaoNovoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abreFormularioModoInsereLink();
            }
        });
    }

    private void abreFormularioModoInsereLink() {
        startActivity(new Intent(this,
                FormularioLinkActivity.class));
    }

    /**
     *  Criação da listaDeLinksUteis dentro do onResume e não no onCreate
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        atualizaLinks();
    }

    private void atualizaLinks() {
        adapter.clear();    // Limpa os dados
        adapter.addAll(dao.todos());
    }

    private void configuraLista() {
        ListView listaDeLinksUteis = findViewById(R.id.activity_lista_links_listview);
        /*
        *   Pudemos remover o final Lista abaixo, pois agora todos os itens estão sendo atualizados
        *   no onResume através do adapter.clear e adapter.addAll
        * */
        //final List<Link> links = dao.todos();

        configuraAdapter(listaDeLinksUteis);
        configuraListenerDeCliquePorItem(listaDeLinksUteis);
        //configuraListenerDeCliqueLongoPorItem(listaDeLinksUteis);
        /*
        *       Para "setar" algum menu de contexto a uma view usamos o registerForContextMenu
        * */
        registerForContextMenu(listaDeLinksUteis);
    }

/*    private void configuraListenerDeCliqueLongoPorItem(ListView listaDeLinksUteis) {
        listaDeLinksUteis.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Link linkEscolhido = (Link) adapterView.getItemAtPosition(position); //links.get(position);
                remove(linkEscolhido);
                *//*
                *   Problema de atualizar a lista pelo configuraLista é que refaz configuraAdapter
                *   e configuraListenerDeCliquePorItem sem necessidade. Então usamos o adapter.remove
                * *//*
                //configuraLista();

                *//*
                *   return false: permite que passe para frente o evento, abrindo menu de contexto
                *   return true: para consumir o evento inteiro e não passar para a tela seguinte
                * *//*
                return false;
                //return true;
            }
        });
    }*/

    private void remove(Link link) {
        oldPath = link.getCaminhoImagem();
        apagaImagemAnterior();  // remove imagem cadastrada para o link
        dao.remove(link);       // remove o link da persistência
        adapter.remove(link);   // remove o link do adapter, ou seja, da view
    }

    /**
     *       Apaga a imagem anterior em /LinksUteis/imagens/
     *       Método usado em ListaLinksActivity e FormularioLinkActivity
     * */
    private void apagaImagemAnterior() {
        //apagaImagemAnterior();
        File fdelete = new File(oldPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //System.out.println("file Deleted :" + oldPath);
                //Toast.makeText(FormularioLinkActivity.this, "Deleted!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ListaLinksActivity.this, "File doesn t exists!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void configuraListenerDeCliquePorItem(ListView listaDeLinksUteis) {
        /**
        *       Listener do item de listaDeLinksUteis, para abrir o FormularioLinkActivity
         *      carregando os dados do item, em modo edição
        * */
        listaDeLinksUteis.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /*
                *   Usamos em vez de links.get(position); o adapterView.getItemAtPosition(position)
                *   para não passarmos o links como parâmetro do configuraListenerDeCliquePorItem
                *   assim como já passamos como parâmetro no configuraAdapter, minimizando o risco
                *   de serem links diferentes.
                * */
                Link linkEscolhido = (Link) adapterView.getItemAtPosition(position); //links.get(position);
                //Log.i("posicao link", "" + linkEscolhido);

                //abreFormularioModoEditaLink(linkEscolhido);
                String url = linkEscolhido.getEndereco();
                browseTo(url);

            }
        });
    }

    private void browseTo(String url){

        if (!url.startsWith("http://") && !url.startsWith("https://")){
            url = "http://" + url;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void abreFormularioModoEditaLink(Link link) {
        Intent vaiParaFormularioLinkActivityIntent = new Intent(ListaLinksActivity.this, FormularioLinkActivity.class);
        // Para enviarmos dados à Activity, usamos o extra, que permite enviarmos dados primitivos e objetos.
        // Mas tem que ser serializado!
        vaiParaFormularioLinkActivityIntent.putExtra(CHAVE_LINK, link);
        startActivity(vaiParaFormularioLinkActivityIntent);
    }

    private void configuraAdapter(ListView listaDeLinksUteis) {

/*
        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_link);
*/

        /*
        *   adapter não dá suporte a lista de itens com mais de 1 textview
        *   Daí surge o BaseAdapter
        * */
        adapter = new ListaLinksAdapter(this); // Que é a classe criada

        listaDeLinksUteis.setAdapter(adapter);

    }
}
