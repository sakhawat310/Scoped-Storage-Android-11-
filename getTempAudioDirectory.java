public class Initialization{
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
}
