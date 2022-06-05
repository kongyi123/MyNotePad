package com.example.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class FirebaseSheetPost {
    public String id;
    public String name;
    public String content;
    public String textSize;

    public FirebaseSheetPost() {

    }

    public FirebaseSheetPost(String arg1, String arg2, String arg3, String arg4) {
        this.id = arg1;
        this.name = arg2;
        this.content = arg3;
        this.textSize = arg4;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("content", content);
        result.put("textSize", textSize);
        return result;
    }
}

