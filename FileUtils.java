package com.videotrimmer.library.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

    public static final String app_name = "Your App Name";
    public static final File mSdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());//0
    private static final File APP_DIRECTORY = new File(mSdCard, app_name);//0/Add Audio To Video
    private static final File TEMP_DIRECTORY_AUDIO = new File(APP_DIRECTORY, ".temp_audio");
    private static final File TEMP_DIRECTORY_VIDEO= new File(APP_DIRECTORY, ".temp_video");

    //------------SKT--------------

    public static String getTempAudioFilePath(Context context, String title,String extension) {
        String parentdir = getTempAudioDirectory(context).getAbsolutePath();

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + "/" + filename + i + extension;
            else
                testPath = parentdir + "/" + filename + extension;

            try {
                RandomAccessFile f = new RandomAccessFile(
                        new File(testPath), "r");
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }
        return path;
    }


    public static File getTempAudioDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File dir=  new File(getAppDirectory(context, Environment.DIRECTORY_MUSIC), ".temp_audio");//
            if (!dir.exists())
                dir.mkdirs();
            return dir;
        }
        else {
            if(!TEMP_DIRECTORY_AUDIO.exists())
                TEMP_DIRECTORY_AUDIO.mkdirs();
            return TEMP_DIRECTORY_AUDIO;
        }
    }


    public static String getTempVideoFilePath(Context context, String title,String extension) {
        String parentdir = getTempVideoDirectory(context).getAbsolutePath();

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + "/" + filename + i + extension;
            else
                testPath = parentdir + "/" + filename + extension;

            try {
                RandomAccessFile f = new RandomAccessFile(
                        new File(testPath), "r");
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }
        return path;
    }


    public static File getTempVideoDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File dir=  new File(getAppDirectory(context, Environment.DIRECTORY_MOVIES), ".temp_video");//
            if (!dir.exists())
                dir.mkdirs();
            return dir;
        }
        else {
            if(!TEMP_DIRECTORY_VIDEO.exists())
                TEMP_DIRECTORY_VIDEO.mkdirs();

            return TEMP_DIRECTORY_VIDEO;
        }
    }


    public static File getAppDirectory(Context context,String type){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            File dir= new File(getExternalStorageDir(context,type), app_name);
            if (!dir.exists())
                dir.mkdirs();
            return dir;
        }

        else
            return APP_DIRECTORY;

    }


    /**
     *
     * @param context required if sdk greater than {@link Build.VERSION_CODES#Q}, otherwise can be null
     * @return absolute path of external storage directory.
     */
    public static String getExternalStorageDir(Context context, String type){
        String externalRootDir;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            externalRootDir=context.getExternalFilesDir(type).getAbsolutePath();
        }else{
            externalRootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        return externalRootDir;
    }

    /**
     * This function works only below SDK version 29
     * @return
     */

    public static String getOutputDir(){

        String externalRootDir = Environment.getExternalStorageDirectory().getPath();

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }

        String parentdir = externalRootDir + app_name;
        File dir = new File (parentdir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return parentdir;
    }

    public static String getOutputDir(Context context){

        String externalRootDir = getExternalStorageDir(context,Environment.DIRECTORY_MOVIES);

        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }

        String parentdir = externalRootDir;
        File dir = new File (parentdir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return parentdir;
    }

    public static String getOutputFileName(String mVideoPath){
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String extension=mVideoPath.substring(mVideoPath.lastIndexOf("."));
        if(extension==null || extension.isEmpty())
            extension=".mp4";
        return "MIXED_" + timestamp+extension;
    }
}
