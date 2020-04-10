package aicare.net.cn.sdk.ailinksdkdemoandroid;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * xing<br>
 * 2019/5/25<br>
 * java类作用描述
 */
public class TimeUtils {

    public static String getTime(){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return format.format(System.currentTimeMillis())+":\n";
    }

}
