package gm.tieba.tabswitch.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IO {
    public static void copyFileFromAssets(Context context, String assetsFilePath, String targetFileFullPath) {
        try {
            InputStream assetsFileInputStream = context.getAssets().open(assetsFilePath);
            copyFileFromStream(assetsFileInputStream, targetFileFullPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFileFromStream(InputStream in, String targetPath) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(targetPath));
            byte[] buffer = new byte[8192];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1)
                fos.write(buffer, 0, byteCount);
            fos.flush();
            in.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(InputStream in, FileOutputStream fos) {
        try {
            byte[] buffer = new byte[8192];
            int byteCount = 0;
            while ((byteCount = in.read(buffer)) != -1)
                fos.write(buffer, 0, byteCount);
            fos.flush();
            in.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    deleteFiles(f);
                }
            }
        }
        file.delete();
    }
}