package com.project.plantrecognitionproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.ByteArrayOutputStream;

public class InterpreterActivity extends AppCompatActivity {

    private ModelInterpreter modelInterpreter = null;
    private Bitmap inputImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getInputImage();
        createModelInterpreter();
        makeInference();
    }

    private void getInputImage() {
        byte[] raw = getIntent().getByteArrayExtra("Image");
        inputImage = BitmapFactory.decodeByteArray(raw, 0, raw.length);
    }

    private void createModelInterpreter(){
        // Creating and configuring interpreter
        modelInterpreter = ModelInterpreter.getInstance();
    }

    private void makeInference(){

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
                                    startInfoActivity(argmax);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Prediction is failed!");
                                }
                            });
        }catch(FirebaseMLException e){
            e.printStackTrace();
        }
    }

    private void startInfoActivity(int predictedClass){

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        inputImage.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] byteArray = bStream.toByteArray();

        Intent infoIntent = new Intent(this, InfoActivity.class);
        infoIntent.putExtra("Image", byteArray);
        infoIntent.putExtra("predictedClass", predictedClass);
        finish();
        startActivity(infoIntent);
    }
}
