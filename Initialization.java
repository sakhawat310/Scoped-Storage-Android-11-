public class Initialization{ 
    public static final String app_name = "Your App Name";
    public static final File mSdCard = new File(Environment.getExternalStorageDirectory().getAbsolutePath());//0
    private static final File APP_DIRECTORY = new File(mSdCard, app_name);
    private static final File TEMP_DIRECTORY_AUDIO = new File(APP_DIRECTORY, ".temp_audio");
    private static final File TEMP_DIRECTORY_VIDEO= new File(APP_DIRECTORY, ".temp_video");
}
