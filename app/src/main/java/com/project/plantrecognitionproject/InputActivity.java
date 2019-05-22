package com.project.plantrecognitionproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class InputActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;

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

        // Get input image and start InterpreterActivity in order to get prediction
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap inputImage =  Bitmap.createScaledBitmap(imageBitmap, 500, 500, true);
            startInterpreterActivity(inputImage);
        }
    }

    public void takeImage(View clickedButton){
        // Delegate to camera app
        dispatchTakePictureIntent();
    }

    public void chooseImage(View clickedButton){

    }

    public void getHelp(View clickedButton){

    }

    public void showAbout(View clickedButon){

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