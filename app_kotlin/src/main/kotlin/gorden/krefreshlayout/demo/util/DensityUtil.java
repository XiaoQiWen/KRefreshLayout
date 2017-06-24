package gorden.krefreshlayout.demo.util;

import android.content.res.Resources;

/**
 * document
 * Created by Gordn on 2017/6/19.
 */

public class DensityUtil {
    private static float density = Resources.getSystem().getDisplayMetrics().density;

    public static int dip2px(int dp){
        return (int) (dp*density);
    }
    public static int dip2px(float dp){
        return (int) (dp*density);
    }
    public static int appWidth(){
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }
    public static int appHeight(){
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
