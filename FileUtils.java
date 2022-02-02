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

    public static final String app_name = "Add Audio To Video";
    public static final File mSdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());//0
    private static final File APP_DIRECTORY = new File(mSdCard, app_name);//0/Add Audio To Video
    private static final File TEMP_DIRECTORY_AUDIO = new File(APP_DIRECTORY, ".temp_audio");
    private static final File TEMP_DIRECTORY_VIDEO= new File(APP_DIRECTORY, ".temp_video");

    /**
     * > 28 /0/package_name/Movies/Add Audio To Video/
     * @param context
     * @param uri
     * @return
     */

    @SuppressLint("NewApi")
    public static String getPath(Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        if (isKitKat ) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                String fullPath = getPathFromExtSD(split);
                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }
            if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    }
                    finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));


                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                //In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }
                    }
                }
                else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    Uri contentUri=null;
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }
            }
            if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;

                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};


                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }

            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(context,uri);
            }

            if(isWhatsAppFile(uri)){
                return getFilePathForWhatsApp(context,uri);
            }


            if ("content".equalsIgnoreCase(uri.getScheme())) {

                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }
                if (isGoogleDriveUri(uri)) {
                    return getDriveFilePath(context,uri);
                }
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {

                    // return getFilePathFromURI(context,uri);
                    return copyFileToInternalStorage(context,uri,"userfiles");
                    // return getRealPathFromURI(context,uri);
                }
                else
                {
                    return getDataColumn(context, uri, null, null);
                }

            }
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        else {

            if(isWhatsAppFile(uri)){
                return getFilePathForWhatsApp(context,uri);
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {
                        MediaStore.Images.Media.DATA
                };
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver()
                            .query(uri, projection, selection, selectionArgs, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }


    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }
        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static String getDriveFilePath(Context context,Uri uri) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private static String copyFileToInternalStorage(Context context,Uri uri,String newDirName) {
        Uri returnUri = uri;

        Cursor returnCursor = context.getContentResolver().query(returnUri, new String[]{
                OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE
        }, null, null, null);


        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if(!newDirName.equals("")) {
            File dir = new File(context.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(context.getFilesDir() + "/" + newDirName + "/" + name);
        }
        else{
            output = new File(context.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();

        }
        catch (Exception e) {

            Log.e("Exception", e.getMessage());
        }

        return output.getPath();
    }

    private static String getFilePathForWhatsApp(Context context,Uri uri){
        return  copyFileToInternalStorage(context,uri,"whatsapp");
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isWhatsAppFile(Uri uri){
        return "com.whatsapp.provider.media".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    /**
     * get absolute file path from content uri
     * @param resolver content resolver
     * @param contentUri content uri
     * @return file path
     */
    public static String getContentPath(ContentResolver resolver, Uri contentUri){
        final String column = "_data";
        final String[] projection = {
                column
        };

        Cursor cursor = resolver.query(contentUri, projection, null, null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            final int column_index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(column_index);
        }

        return null;
    }

    /**
     *
     * @return
     */

    public static String getDateAsFileName(){
        return String.valueOf(new Date().getTime());

    }

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
