package com.bingqi.urbanapplication;

import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;

public class CsvUtils {
    private static final String TAG = "CsvUtils";

    public static void saveToCsv(String fileName, ArrayList<String> content) {
        try {
            String folderName = null;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                if (path != null) {
                    folderName = path +"/CSV/";
                }
            }

            File fileDir = new File(folderName);
            Log.e(TAG, "dataDir: " + folderName);
            if(!fileDir.exists()) {
                fileDir.mkdirs();
            }

            String savedPath = folderName + fileName;
            Log.e(TAG, "DataPath: " + savedPath);
            File file = new File(savedPath);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < content.size(); ++ i) {
                sb.append(content.get(i));
                sb.append("\n");
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(sb.toString());
            bw.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
