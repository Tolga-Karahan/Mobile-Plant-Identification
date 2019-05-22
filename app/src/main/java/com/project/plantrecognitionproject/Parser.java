package com.project.plantrecognitionproject;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

// A singleton for parsing info
public class Parser{

    public static int CLASS_NUM = 76;
    private static Parser jsonParser = null;
    private static HashMap<String, HashMap<String, String>> plant_info = null;

    private Parser(){}

    public static Parser getInstance(InputStream is){

        if(jsonParser == null){
            plant_info = new HashMap<>();
            jsonParser = new Parser();
            jsonParser.parseJSON(is);
        }

        return jsonParser;
    }

    private String loadJSON(InputStream is){
        String json = null;

        try{
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

    private void parseJSON(InputStream is){
        String json = loadJSON(is);

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

    public String getItem(int id, String key){
        return plant_info.get(""+id).get(key);
    }
}