package com.bingqi.urbanapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataUtils {

    private static final String TAG = "DataUtils";
    private DatabaseReference mDatabase;

    public DataUtils(String path) {
        mDatabase = FirebaseDatabase.getInstance().getReference(path);
    }

    public void writeData(String pathString, Object obj) {

        mDatabase.child(pathString).setValue(obj);
    }
}
