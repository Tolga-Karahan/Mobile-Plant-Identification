package com.project.plantrecognitionproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;

public class ModelInterpreter {

    public static final int CLASS_NUM = 76;
    public static final int IMG_SIZE = 224;
    public static final int CHANNELS = 3;

    private FirebaseModelOptions modelOptions = null;
    private FirebaseModelInputOutputOptions inputOutputOptions = null;
    private FirebaseModelInterpreter modelInterpreter = null;
    private static ModelInterpreter interpreterSingleton = null;

    private ModelInterpreter(){}

    public static ModelInterpreter getInstance(){

        if(interpreterSingleton == null){
            interpreterSingleton = new ModelInterpreter();
            interpreterSingleton.configureModel();
        }

        return interpreterSingleton;
    }

    private void configureModel(){
        try {
            modelOptions = createOptions();
            inputOutputOptions = createInputOutputOptions();
            modelInterpreter = FirebaseModelInterpreter.getInstance(modelOptions);
        }catch(FirebaseMLException e){
            e.printStackTrace();
        }
    }

    private FirebaseModelOptions createOptions(){

        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }

        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        // Build a remote model source object by specifying the name you assigned the model
        // when you uploaded it in the Firebase console.
        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("plant_recognition_model")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();

        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);

        // Configuring local model
        FirebaseLocalModel localSource = new FirebaseLocalModel.Builder("plant_recognition_model_local")
                .setAssetFilePath("plant_recognition_model_aug.tflite")
                .build();

        FirebaseModelManager.getInstance().registerLocalModel(localSource);

        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setRemoteModelName("plant_recognition_model")
                .setLocalModelName("plant_recognition_model_local")
                .build();

        return options;
    }

    private FirebaseModelInputOutputOptions createInputOutputOptions(){
        FirebaseModelInputOutputOptions inputOutputOptions = null;
        try{
            inputOutputOptions =  new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, IMG_SIZE, IMG_SIZE, CHANNELS})
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, CLASS_NUM})
                    .build();
        }catch(FirebaseMLException e){
            e.printStackTrace();
        }

        return inputOutputOptions;
    }

    public FirebaseModelInputs getModelInput(Bitmap inputImage){

        FirebaseModelInputs modelInputs = null;

        try {
            // Cropping image
            int height = inputImage.getHeight();
            int width = inputImage.getWidth();
            int new_dim = height < width ? height : width;

            Bitmap croppedImage = Bitmap.createBitmap(inputImage,
                    width / 2 - new_dim / 2,
                    height / 2 - new_dim / 2,
                    new_dim,
                    new_dim);

            // Resizing image
            Bitmap finalImage = Bitmap.createScaledBitmap(croppedImage, IMG_SIZE, IMG_SIZE, true);

            int batchNum = 0;
            float[][][][] input = new float[1][IMG_SIZE][IMG_SIZE][CHANNELS];

            for (int i = 0; i < IMG_SIZE; i++) {
                for (int j = 0; j < IMG_SIZE; j++) {
                    int pixel = finalImage.getPixel(i, j);

                    // Normalize channel values
                    float redPixel = Color.red(pixel);
                    redPixel /= 255;
                    redPixel -= 0.5;
                    redPixel *= 2;

                    float greenPixel = Color.green(pixel);
                    greenPixel /= 255;
                    greenPixel -= 0.5;
                    greenPixel *= 2;

                    float bluePixel = Color.blue(pixel);
                    bluePixel /= 255;
                    bluePixel -= 0.5;
                    bluePixel *= 2;

                    input[batchNum][i][j][0] = redPixel;
                    input[batchNum][i][j][1] = greenPixel;
                    input[batchNum][i][j][2] = bluePixel;
                }
            }

            modelInputs = new FirebaseModelInputs.Builder()
                    .add(input)
                    .build();
        }catch(FirebaseMLException e){
            e.printStackTrace();
        }

        return modelInputs;
    }

    public FirebaseModelInterpreter getModelInterpreter(){
        return modelInterpreter;
    }

    public FirebaseModelInputOutputOptions getModelInputOutputOptions(){
        return inputOutputOptions;
    }
}
