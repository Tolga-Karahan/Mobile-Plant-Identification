package com.project.plantrecognitionproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class InfoActivity extends AppCompatActivity {

    private Parser jsonParser = null;

    // Views
    private ImageView imageView = null;
    private TextView textArea = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        findViews();
        createParser();
        showInfo();
    }

    private void findViews(){
        imageView = findViewById(R.id.imgView);
        textArea = findViewById(R.id.textArea);
    }

    private void createParser(){
        try{
            jsonParser = Parser.getInstance(getAssets().open("flowers_info.json"));
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private Bitmap getInputImage() {
        byte[] raw = getIntent().getByteArrayExtra("Image");
        Bitmap inputImage = BitmapFactory.decodeByteArray(raw, 0, raw.length);
        return inputImage;
    }

    private void showInfo(){

        // Get predicted class id
        int id = getIntent().getIntExtra("predictedClass", -1);

        String name   = jsonParser.getItem(id, "isim");
        String family = jsonParser.getItem(id, "familya");
        String genus  = jsonParser.getItem(id, "cins");
        String origin = jsonParser.getItem(id, "anavatan");

        String flowerInfo = String.format(getString(R.string.info_message),
                name,
                family,
                genus,
                origin);

        imageView.setImageBitmap(getInputImage());;
        textArea.setText(flowerInfo);
    }

    public void returnMethod(View clickedView){
        finish();
    }
}