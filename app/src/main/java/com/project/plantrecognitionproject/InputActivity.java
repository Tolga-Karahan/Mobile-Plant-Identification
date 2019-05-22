package com.project.plantrecognitionproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class InputActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Get input image and start InterpreterActivity to make prediction
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap inputImage =  Bitmap.createScaledBitmap(imageBitmap, 500, 500, true);
            startInterpreterActivity(inputImage);
        }

        else if(requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK){
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                BufferedInputStream rawImage = new BufferedInputStream(inputStream);
                Bitmap imageBitmap = BitmapFactory.decodeStream(rawImage);
                Bitmap inputImage = Bitmap.createScaledBitmap(imageBitmap, 500, 500, true);
                startInterpreterActivity(inputImage);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    public void takeImage(View clickedButton){
        // Delegate to camera app
        dispatchTakePictureIntent();
    }

    public void chooseImage(View clickedButton){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
    }

    public void showAbout(View clickedButon){
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        startActivity(aboutIntent);
    }

    public void exit(View clickedButton){
        finishAndRemoveTask();
    }

    private void startInterpreterActivity(Bitmap inputImage){

        // Pass input image to activity
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        inputImage.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] byteArray = bStream.toByteArray();

        Intent interpreterIntent = new Intent(this, InterpreterActivity.class);
        interpreterIntent.putExtra("Image", byteArray);

        // Start activity to show results
        startActivity(interpreterIntent);
    }
}