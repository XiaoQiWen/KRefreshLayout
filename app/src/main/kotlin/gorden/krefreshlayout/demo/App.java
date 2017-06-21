package gorden.krefreshlayout.demo;

import android.app.Application;
import android.content.Intent;

import gorden.krefreshlayout.demo.ui.SampleActivity;
import gorden.krefreshlayout.demo.util.CrashHandler;

/**
 * Created by Gorden on 2017/6/20.
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this,true);
    }
}
