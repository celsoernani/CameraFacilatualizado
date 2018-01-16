package com.embrapii.it2b.app_ml;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSAO_REQUEST = 2;
    private ImageView imagem;
    private final int GALERIA_IMAGENS = 1;
    private final int CAMERA = 3;
    private final int TIRARFOTO = 4;
    public Bitmap imageGrayCamera = null;
    public Bitmap imageGrayGaleria = null;

    public MainActivity() throws IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permissões
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSAO_REQUEST);
            }
        }

        imagem = findViewById(R.id.imageView);
        // Botão de chamar a galeria
        Button button = findViewById(R.id.button);
        // Botão de chamar a câmera
        Button button1 = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); //intent da camera
                // Com o parâmetro de volta da content realiza o start da activity da camera
                startActivityForResult(intent, GALERIA_IMAGENS);
            }
        });

        // Função de chamar a câmera
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intenção de captura de imagem
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        arquivoFoto = criarArquivo();
                    } catch (IOException e) {
                        // Print em caso de falha de criação do arquivo
                        e.printStackTrace();
                    }
                    if (arquivoFoto != null) {
                        // Caso exista o arquivo, ele vai criar um arquivo URI
                        Uri photoURI = FileProvider.getUriForFile(getBaseContext(),getBaseContext().getApplicationContext().getPackageName() + ".provider", arquivoFoto);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TIRARFOTO);
                    }
                }
            }
        });

    }

    private File arquivoFoto = null;
    // Método cria imagem com o nome APP_ML usando persistencia interna do Android
    public File criarArquivo() throws IOException {
        // Salvando em um diretorio privado do app usando o método getExternalFilesDir()
        File pasta = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagem = new File(pasta.getPath() + File.separator + "APP_ML" + ".jpg");
        return imagem;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Condição que leva o usuário para a galeria
        if (resultCode == RESULT_OK && requestCode == GALERIA_IMAGENS) {
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
            imageGrayGaleria = doGreyscale(thumbnail);
            //imagem.setImageBitmap(thumbnail);
            imagem.setImageBitmap(imageGrayGaleria);
        }
        // Mostrando na ImageView declarada globalmente
        // Condição que faz o usuário salvar a foto para poder aparecer na tela principal
        if (requestCode == CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imagem.setImageBitmap(imageBitmap);
        }
        // Retorno do método apenas mostrando a imagem
        // Condição que leva o usuário para a câmera e mostra a imagem capturada
        if (requestCode == TIRARFOTO && resultCode == RESULT_OK) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(arquivoFoto))
            );
            exibirImagem();
        }
    }

    // Esse método exibe a imagem na tela principal do App depois que é tirada a foto
    private void exibirImagem() {
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
        imageGrayCamera = doGreyscale(bitmap);
        //imagem.setImageBitmap(bitmap);
        imagem.setImageBitmap(imageGrayCamera);
    }

    public static Bitmap doGreyscale(Bitmap src) {
        // constant factors
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        // create output bitmap
        Bitmap imageGray = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // Informações dos pixels
        int A, R, G, B;
        int pixel;

        // Pega o tamanho da imagem
        int width = src.getWidth();
        int height = src.getHeight();

        // Varre cada pixel da imagem
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // Consegue o valor do pixel
                pixel = src.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                // set new pixel color to output bitmap
                imageGray.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // Retorna a imagem em escala de cinza
        return imageGray;
    }

    // Permissão master método obrigatório para Apps que usam permissão
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

