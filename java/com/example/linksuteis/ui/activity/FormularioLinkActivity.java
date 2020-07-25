package com.example.linksuteis.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.linksuteis.R;
import com.example.linksuteis.dao.LinkDAO;
import com.example.linksuteis.model.Link;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import static com.example.linksuteis.ui.activity.ConstantesActivities.CHAVE_LINK;

public class FormularioLinkActivity extends AppCompatActivity {

    private static final int GALERIA_IMAGENS = 1;       // Código de retorno da Galeria
    private static final int PERMISSAO_REQUEST = 2;     // Código de retorno da permissão

    private static final int CODE_PERMISSION = 12;      // Código de retorno de outra permissão
    private static final String DIRETORIO = "/LinksUteis/imagens/";
    public static final int CAMERA_REQUEST_LOW_DEFINITION = 3;
    public static final int CAMERA_REQUEST_HIGH_DEFINITION = 4;
    public static final String TITULO_APPBAR_NOVO_LINK = "Novo Link";
    private static final String TITULO_APPBAR_EDITA_LINK = "Edita Link";
    private static String picturePath = "";
    private static Uri selectedImageUri;
    private static String currentPhotoPath;

    private ImageView campoImagem;
    private EditText campoNomeDoLink;
    private EditText campoEnderecoDoLink;
    private final LinkDAO dao = new LinkDAO();
    private Link link;
    private boolean deveApagarImagemAnterior;
    private String oldPath;
    private Uri oldUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_link);

        inicializacaoDosCampos();
        configuraBotaoSelecionarImagem();
        configuraBotaoResetarImagem();
        //configuraBotaoSalvar();
        configuraClickNaImagem();
        deveApagarImagemAnterior = false;
        carregaLink();

        /**
         *   Código do botão EXIBE IAMGEM SALVA,
         *   para exibir imagem a partir de um Uri previamente salvo em link
         */
        //Button testeExibeButton = findViewById(R.id.activity_formulario_link_teste_exibe_button);

/*        testeExibeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Link link = dao.primeiro();
                String caminho = link.getCaminhoImagem();
                Bitmap imagemGaleria = (BitmapFactory.decodeFile(caminho));
                campoImagem.setImageBitmap(imagemGaleria);
            }
        });*/

    }

    /**
    *       Para criar o menu de opções, onde implementamos o SALVAR no alto da tela do formulário
     *      Inicialmente aparece o menu de 3 pontinhos. Mas podemos alterá-lo adicionando
     *      em activity_fomulario_link_menu o atributo showAsAction
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_formulario_link_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
    *   Código a ser executado quando for selecionado algum item de OptionMenu, ícone SALVAR no caso
     *   Tipo um listener de onCreateOptionsMenu
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.activity_formulario_link_menu_salvar) {
            if (campoNomeDoLink.getText().toString().equals("") ||
                    campoEnderecoDoLink.getText().toString().equals("")  ||
                    selectedImageUri == null) {
                Toast.makeText(FormularioLinkActivity.this,
                        "Preencha todos os campos e selecione uma imagem",
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "vai para finalizaFormuilario", Toast.LENGTH_SHORT).show();

                finalizaFormulario();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void carregaLink() {
        /*
        *   If para saber se os dados vem de um form já preenchido ou se vamos começar
        *   um novo, caso do else
        * */
        Intent dadosRecebidosDoFormularioParaEdicaoIntent = getIntent();
        if (dadosRecebidosDoFormularioParaEdicaoIntent.hasExtra(CHAVE_LINK)) {
            setTitle(TITULO_APPBAR_EDITA_LINK);
            Serializable linkSerializable = dadosRecebidosDoFormularioParaEdicaoIntent.getSerializableExtra(CHAVE_LINK);
            link = (Link) linkSerializable;

            preencheCampos();
        } else {
            setTitle(TITULO_APPBAR_NOVO_LINK);
            link = new Link();
        }
    }

    private void preencheCampos() {
        campoNomeDoLink.setText(link.getNome());
        campoEnderecoDoLink.setText(link.getEndereco());
        selectedImageUri = Uri.parse(link.getFotoUri());
        picturePath = link.getCaminhoImagem();

        String caminho = link.getCaminhoImagem();
        Bitmap imagem = (BitmapFactory.decodeFile(caminho));
        campoImagem.setImageBitmap(imagem);
    }

    private void configuraClickNaImagem() {
        /**
        *   Listener do click na imagem para nova edição
        * */
        campoImagem = findViewById(R.id.activity_formulario_link_logo_imageview);

        campoImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deveApagarImagemAnterior = true;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        0);
                if (hasImage(campoImagem)) {
                    startCrop(selectedImageUri);
                } else {
                    startDialogCamOrGallery();
                }

            }
        });
    }

    private void configuraBotaoResetarImagem() {
        /**
         *  Listener do botão RESETAR IMAGEM, para o usuário poder apagar imagem
         *  e selecionar outra, sem sair do FormularioLinkActivity
         * */
        Button resetImageButton = findViewById(R.id.activity_formulario_link_reset_image_button);

        resetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                campoImagem.setImageBitmap(null);
            }
        });
    }

    private void configuraBotaoSelecionarImagem() {
        /**
         *   Listener do botão SELECIONAR IMAGEM para exibir a imagem da Galeria no ImageView
         *   Fonte: https://www.youtube.com/watch?v=AnNpUGyryiE
         * */
        Button selectImageToDisplayButton = findViewById(R.id.activity_formulario_link_select_image_button);

        selectImageToDisplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialogCamOrGallery();
            }
        });
    }
/*

    private void configuraBotaoSalvar() {
        */
/**
         *   Listener do botão SALVAR para salvar o link em dao e copiar imagem para pasta
         * *//*

        Button saveButton = findViewById(R.id.activity_formulario_link_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (campoNomeDoLink.getText().toString().equals("") ||
                        campoEnderecoDoLink.getText().toString().equals("") ||
                        selectedImageUri == null) {
                    Toast.makeText(FormularioLinkActivity.this,
                            "Preencha todos os campos e selecione uma imagem",
                            Toast.LENGTH_SHORT).show();
                } else {

                    finalizaFormulario();
                }

            }
        });
    }
*/

    private void finalizaFormulario() {
        Toast.makeText(this, "hasImage!", Toast.LENGTH_SHORT).show();
        if (hasImage(campoImagem)) {
            Log.i("hasImage", "finalizaFormulario: hasImage");
            Toast.makeText(this, "hasImage!", Toast.LENGTH_SHORT).show();
            copiarImagemDaGaleria();
        }

        /*
        *   Caso o usuário tenha editado a imagem por ele já selecionada no FormlarioLinkActivity
        *   Apaga a imagem anteriormente selecionada da pasta /LinksUteis/imagens
        *   Para não acumular lixo na pasta
        * */
        if (deveApagarImagemAnterior) {
            Toast.makeText(this, "deveApagarImagemAnteiror!", Toast.LENGTH_SHORT).show();
            apagaImagemAnterior();
        }

        preencheLink();

        if (link.temIdValido()) {
            dao.edita(link);
        } else {
            dao.salva(link);
            Toast.makeText(FormularioLinkActivity.this,
                    "Link salvo com sucesso!",
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
    *   Verifica se existe imagem na imageView. Caso não exista, não copia imagem inexistente
     *   para /LinksUteis/imagens/ evitando erro. Isto pode acontecer se o usuário apagar as pasta
     *   com suas imagens
     *   Fonte: https://stackoverflow.com/questions/9113895/how-to-check-if-an-imageview-is-attached-with-image-in-android
    * */
    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }

    /**
     *       Apaga a imagem anterior em /LinksUteis/imagens/
     * */
    private void apagaImagemAnterior() {

        File fdelete = new File(oldPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //System.out.println("file Deleted :" + oldPath);
                //Toast.makeText(FormularioLinkActivity.this, "Deleted!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(FormularioLinkActivity.this, "File doesn t exists!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void inicializacaoDosCampos() {
        campoNomeDoLink = findViewById(R.id.activity_formulario_link_nome_do_link);
        campoEnderecoDoLink = findViewById(R.id.activity_formulario_link_endereco_do_link);
        campoImagem = findViewById(R.id.activity_formulario_link_logo_imageview);
        picturePath = "";
        selectedImageUri = null;
    }

    private void salvaLink(Link link) {
        dao.salva(link);
        Toast.makeText(FormularioLinkActivity.this,
                "Link salvo com sucesso!",
                Toast.LENGTH_SHORT).show();
                    /*
                    Em vez de sempre criar uma nova activity, melhor finalizar esta, para
                    decrementar a pilha de activities

                    startActivity(new Intent(FormularioLinkActivity.this,
                            ListaLinksActivity.class));
                    */
        finish();
    }


    /**
    *       Preenche o link com informações atualizadas em FormularioLinkActivity
     *      Ou recebe novas informações para cadastro
    * */
    private void preencheLink() {
        String nomeDoLink = campoNomeDoLink.getText().toString();
        String enderecoDoLink = campoEnderecoDoLink.getText().toString();
        String imagePath = picturePath;
        String fotoDoLinkUri = selectedImageUri.toString();

        //return new Link(nomeDoLink, enderecoDoLink, fotoDoLinkUri, imagePath);
        link.setNome(nomeDoLink);
        link.setEndereco(enderecoDoLink);
        link.setCaminhoImagem(imagePath);
        link.setFotoUri(fotoDoLinkUri);
    }

    /**
     * Caixa de diálogo para usuário escolher câmera ou galeria
     */
    private void startDialogCamOrGallery() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(FormularioLinkActivity.this);

        myAlertDialog.setTitle("Logo do Link");
        myAlertDialog.setMessage("Selecione câmera ou galeria");

        myAlertDialog.setPositiveButton("Galeria",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent pictureActionIntent = null;

                        pictureActionIntent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(
                                pictureActionIntent,
                                GALERIA_IMAGENS);
                    }
                });

        myAlertDialog.setNegativeButton("Camera",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        startDialogLowOrHighDefinition();

                    }
                });

        myAlertDialog.create().show();

    }

    private void startDialogLowOrHighDefinition() {
        AlertDialog.Builder myAlertDialogImageDefinition = new AlertDialog.Builder(
                FormularioLinkActivity.this);
        myAlertDialogImageDefinition.setTitle("Definição da Foto");
        myAlertDialogImageDefinition.setMessage("Selecione o nível de definição da foto");

        myAlertDialogImageDefinition.setPositiveButton("Baixa Definição", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_LOW_DEFINITION);
                }

                //CropImage.startPickImageActivity(FormularioLinkActivity.this);

            }
        });

        /**
         *   Fonte: https://www.youtube.com/watch?v=CYRXXOM3aGI
         * */
        myAlertDialogImageDefinition.setNegativeButton("Alta Definição", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try {
                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentPhotoPath = imageFile.getAbsolutePath();
                    Uri imageUri = FileProvider.getUriForFile(FormularioLinkActivity.this,
                            "com.example.linksuteis.fileprovider", imageFile);

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    //takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);

                    // Mapeando onde será armazenada a imagem
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_HIGH_DEFINITION);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //CropImage.startPickImageActivity(FormularioLinkActivity.this);


            }
        });
        myAlertDialogImageDefinition.create().show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //checa a permissão!
        checkPermission();
    }

    /**
     * Método responsável por verificar se o app possui a permissão de escrita e leitura
     */
    private void checkPermission() {
        // Verifica necessidade de verificacao de permissao
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Verifica necessidade de explicar necessidade da permissao
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "É necessário a  de leitura e escrita!", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_PERMISSION);
            }
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CODE_PERMISSION);
            } else {
                // Solicita permissao
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        CODE_PERMISSION);
            }
        }
    }

    /**
     * Fonte: https://stackoverflow.com/questions/16433915/how-to-copy-file-from-one-location-to-another-location
     */
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         *   Código para usuário pegar imagem na galeria e exibir
         * */
        if (resultCode == RESULT_OK && requestCode == GALERIA_IMAGENS) {
            //  Pegando a Uri, que é um recurso para acessar a imagem
            selectedImageUri = data.getData();

            startCrop(selectedImageUri);

            //  Agora sim temos o caminho da imagem, String
/*            String caminhoGaleriaImagem = getRealPathFromURI(selectedImageUri);

            Bitmap imagemGaleria = (BitmapFactory.decodeFile(caminhoGaleriaImagem));
            campoImagem.setImageBitmap(imagemGaleria);*/
        }
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_LOW_DEFINITION) {
            Bundle extras = data.getExtras();
            Bitmap photoBitmap = (Bitmap) extras.get("data");

            selectedImageUri = getImageUri(getApplicationContext(), photoBitmap);

            startCrop(selectedImageUri);

            //campoImagem.setImageBitmap(photoBitmap);
        }
        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_HIGH_DEFINITION) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

            selectedImageUri = getImageUri(getApplicationContext(), bitmap);
            startCrop(selectedImageUri);

            //campoImagem.setImageBitmap(bitmap);
        }
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                selectedImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        0);
            } else {
                startCrop(imageUri);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                if (deveApagarImagemAnterior) {
                    oldUri = selectedImageUri;
                    oldPath = picturePath;
                }

                selectedImageUri = result.getUri();
                Toast.makeText(this, String.valueOf(selectedImageUri), Toast.LENGTH_SHORT).show();

                picturePath = result.getUri().getPath();
                campoImagem.setImageURI(selectedImageUri);

            }
        }
    }

    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);

    }

    private void copiarImagemDaGaleria() {
        // criamos um File com o diretório selecionado!
        //final File selecionada = new File(getRealPathFromURI(selectedImageUri));

        // picturePath obtida em onActivityResult, no cropImage
        final File selecionada = new File(picturePath);
        /*
         *  Caso não exista o novo diretório, vamos criar!
         */
        final File rootPath = new File(Environment.getExternalStorageDirectory() + DIRETORIO);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }


        /*
         * Criamos um file, como no DIRETORIO, com o mesmo nome da anterior
         */
        final File novaImagem = new File(rootPath, selecionada.getName());

        //Movemos o arquivo!
        try {
            copyFileUsingStream(selecionada, novaImagem);

            // Atualiza picturePath e selectedImageUri, agora na pasta LinksUteis
            picturePath = novaImagem.getPath();
            selectedImageUri = Uri.fromFile(novaImagem);
            //Toast.makeText(FormularioLinkActivity.this, String.valueOf(selectedImageUri), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSAO_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida. Pode continuar.
            } else {
                // A permissão foi negada. Precisa ver o que deve ser desabilitado.
            }
            return;
        }
    }

    /**
     * A partir do Uri extrai o caminho String do diretório válido, para carregarmos em um arquivo
     *
     * @param contentUri
     * @return
     */
    private String getRealPathFromURI(Uri contentUri) {

        //  Como um BD temos várias colunas com várias informações, onde queremos
        //  a coluna que possui o path do arquivo
        String[] colunas = {MediaStore.Images.Media.DATA};

        //  Aqui acessamos um ContentProvider como um BD através do ContentResolver
        //  que, por usa vez, faz a consulta(query) que queremos
        Cursor c = getContentResolver().query(contentUri,
                colunas, null, null, null);
        //  Com o Cursor percorremos o resultado de uma query que neste caso
        //  Só tem 1 resultado: a nossa imagem. Então movemos para a posição inicial
        c.moveToFirst();

        int columnIndex = c.getColumnIndex(colunas[0]);
        String caminho = c.getString(columnIndex);

        //  Aqui liberamos o recurso para não dar problema de memória
        c.close();
        return caminho;
    }
}
