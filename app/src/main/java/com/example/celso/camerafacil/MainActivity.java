package com.example.celso.camerafacil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    //constantes
    private static final int PERMISSAO_REQUEST = 2;
    private ImageView imagem; //declarando a imagem
    private final int GALERIA_IMAGENS = 1;
    private final int CAMERA = 3; // constante da galeria
    private final int TIRARFOTO = 4; // constante da galeria

    //converter bitmap
    private File diretorio;
    private String nomeDiretorio;
    private String diretorioApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //permissões para poder salvar no cartao de memoria do celular
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
            }
        }
        //permissões para poder salvar e na galria no cartao de memoria do celular
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
            }
        }

        imagem = findViewById(R.id.imageView); //referencias
        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() { //ação de clicar
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //intent da camera


                //com o parametro de volta da content realiza o star da activie da camera

                startActivityForResult(intent, GALERIA_IMAGENS);

            }
        });


        //botao de chamar a camera
        Button button1 = findViewById(R.id.button2);

        //função de chamar a camera
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //intenção de captura de imagem
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        //tenta criar o arquivo
                        arquivoFoto = criarArquivo();
                    } catch (IOException ex) {
                        // Manipulação em caso de falha de criação do arquivo
                    }
                    if (arquivoFoto != null) {
                        //caso exista o arquivo, ele vai criar um arquivo URI
                        Uri photoURI = FileProvider.getUriForFile(getBaseContext(),getBaseContext().getApplicationContext().getPackageName() + ".provider", arquivoFoto);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TIRARFOTO);
                    }
                }
            }
        });


    }

    private File arquivoFoto = null;//arquivo da foto recebe vazio
    //método só p salvar aquivo
    //método criando araquivo BOTANDO O NOME DA HORA E DATA PARA NAO SOBRESCREVER, CASO DESEJE SOBRESCRER BASTA BOTAR O MEMSO NOME
    private File criarArquivo() throws IOException {
        //
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //npme do arquivo vai com a data e a hora que a foto foi tirada
        File pasta = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //salvando na galeria, para salvar em um diretorio privado do app bastta alterar o metodo para "getExternalFilesDir()"
        File imagem = new File(pasta.getPath() + File.separator + "JPG_" + timeStamp + ".jpg");
        return imagem; // A IMAGEM ESTA AQUI DENTRO, BASTA TRABALHAR COM ELA

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALERIA_IMAGENS) {
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
            imagem.setImageBitmap(thumbnail);

        }
        if (requestCode == CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data"); // a foto esta na variavel imageBItmap
            //fução de glcm"classificador" pode vir aqui

            imagem.setImageBitmap(imageBitmap);//mostrando na imagem view declarado la em cima
        }
        //RETORNO DA FUNÇÃO. APENAS MOSTRANDO A IMAGEM
        if (requestCode == TIRARFOTO && resultCode == RESULT_OK) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(arquivoFoto))
            );
            exibirImagem();
        }
    }
    private void exibirImagem() {
                //DECODIFICANDO A IMAGEM PARA CABER NA TELA DO CELULAR
        int targetW= imagem.getWidth();
        int targetH= imagem.getHeight();
        BitmapFactory.Options bmOptions= new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds= true;
        BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath(), bmOptions);
        int photoW= bmOptions.outWidth;
        int photoH= bmOptions.outHeight;
        int scaleFactor= Math.min(photoW/targetW, photoH/targetH);
        bmOptions.inJustDecodeBounds= false;
        bmOptions.inSampleSize= scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath(), bmOptions);
        imagem.setImageBitmap(bitmap);

        //tentantivas de botar a imagme dentro do Drawable

        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        imagem.setImageDrawable(drawable);
    }





        //permissoes
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSAO_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {

            }
            return;
        }
    }



}

