public class Initialization{
    
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
}
