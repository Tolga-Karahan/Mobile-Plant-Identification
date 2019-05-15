package com.project.plantrecognitionproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int CLASS_NUM = 76;
    public static final int IMG_SIZE = 224;
    public static final int CHANNELS = 3;

    private static ModelInterpreter modelInterpreter = null;
    private static ImageView imageView = null;
    private static TextView view1 = null;
    private static TextView view2 = null;
    private static TextView view3 = null;
    private static TextView view4 = null;
    private static  TextView view5 = null;
    private static HashMap<String, HashMap<String, String>> plant_info =
            new HashMap<String, HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imgView);
        view1 = (TextView)findViewById(R.id.view1);
        view2 = (TextView)findViewById(R.id.view2);
        view3 = (TextView)findViewById(R.id.view3);
        view4 = (TextView)findViewById(R.id.view4);
        view5 = (TextView)findViewById(R.id.view5);

        // Creating and configuring interpreter
        modelInterpreter = new ModelInterpreter();
        modelInterpreter.configureModel();

        // Parsing JSON which holds info about plants
        String json = loadJSON("flowers_info.json");
        parseJSON(json);

        // Getting picture from camera
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap inputImage =  Bitmap.createScaledBitmap(imageBitmap, 500, 500, true);
            imageView.setImageBitmap(inputImage);
            try {
                // Get prediction from model interpreter
                FirebaseModelInputs modelInputs = modelInterpreter.getModelInput(inputImage);
                FirebaseModelInputOutputOptions inputOutputOptions = modelInterpreter.getModelInputOutputOptions();
                FirebaseModelInterpreter firebaseInterpreter = modelInterpreter.getModelInterpreter();

                firebaseInterpreter.run(modelInputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {
                                        System.out.println("Task succeeded!");
                                        float[][] output = result.getOutput(0);
                                        float[] probabilities = output[0];

                                        int argmax = 0;
                                        //find predicted label
                                        for (int i = 1; i < probabilities.length; i++) {
                                            if (probabilities[i] > probabilities[argmax])
                                                argmax = i;
                                        }
                                        showInfo(argmax);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        view1.setText("Prediction is failed!");
                                    }
                                });
            }catch(FirebaseMLException e){
                e.printStackTrace();
            }
        }
    }

    private String loadJSON(String fileName){
        String json = null;

        try{
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }catch(IOException e){
            e.printStackTrace();
        }

        return json;
    }

    private void parseJSON(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            HashMap<String, String> child = null;

            for(int i = 0; i < CLASS_NUM; i++){
                JSONObject parent = jsonObject.getJSONObject(""+i);
                child = new HashMap<String, String>();
                child.put("isim", parent.getString("isim"));
                child.put("familya", parent.getString("familya"));
                child.put("cins", parent.getString("cins"));
                child.put("anavatan", parent.getString("anavatan"));
                child.put("bilgi", parent.getString("bilgi"));
                plant_info.put(""+i, child);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showInfo(int id){
        String plantName = plant_info.get(""+id).get("isim");
        String familyName = plant_info.get(""+id).get("familya");
        String genusName = plant_info.get(""+id).get("cins");
        String origin = plant_info.get(""+id).get("anavatan");
        String info = plant_info.get(""+id).get("bilgi");

        view1.setText("İsim: " + plantName);
        view2.setText("Familya: " + familyName);
        view3.setText("Cins: " + genusName);
        view4.setText("Anavatan: " + origin);
        view5.setText("Bilgi " + info);
    }
}