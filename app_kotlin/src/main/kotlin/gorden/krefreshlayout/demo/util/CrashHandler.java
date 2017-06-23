package gorden.krefreshlayout.demo.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 错误日志
 * Created by Gorden on 2015/11/23.
 */
@SuppressWarnings("unused")
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "crash_log";
    private Context mContext;
    private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().toString();
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "Acrash";
    private static final String FILE_NAME = "crash";
    //log文件的后缀名
    private static final String FILE_NAME_SUFFIX = ".log";
    private static CrashHandler mInstance = new CrashHandler();
    //系统默认的异常处理（默认情况下，系统会终止当前的异常程序）
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private boolean DEBUG = false;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return mInstance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
//        try {
//            //导出异常信息到SD卡中
//            saveExceptionToSDCard(ex);
//            //这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug
//            uploadExceptionToServer(ex);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //打印出当前调用栈信息
        if (DEBUG) {
            XLog.e(TAG, "开始打印错误日志");
            StringWriter mStringWriter = new StringWriter();
            PrintWriter mPrintWriter = new PrintWriter(mStringWriter);
            ex.printStackTrace(mPrintWriter);
            mPrintWriter.close();
            XLog.e(TAG, mStringWriter.toString());
        }
        //如果系统提供了默认的异常处理器，则交给系统去结束我们的程序，否则就由我们自己结束自己
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
//            ActivityStack.getInstance().finishAllActivity();
            Process.killProcess(Process.myPid());
        }
    }

    /**
     * 为我们的应用程序设置自定义Crash处理
     */
    public void init(Context context, boolean isDebug) {
        mContext = context.getApplicationContext();
        DEBUG = isDebug;
        //获取系统默认的异常处理器
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将当前实例设为系统默认的异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void saveExceptionToSDCard(Throwable ex) throws IOException {
        if (!DEBUG) return;
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "sdcard unmounted,skip dump exception");
            return;
        }
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //以当前时间创建log文件
        File file = new File(PATH + File.separator + FILE_NAME + time + FILE_NAME_SUFFIX);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //导出发生异常的时间
            pw.println(time);

            //导出手机信息
            dumpPhoneInfo(pw);

            pw.println();
            //导出异常的调用栈信息
            ex.printStackTrace(pw);

            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump crash info failed");
        }

    }

    private void uploadExceptionToServer(Throwable ex) {
        //TODO Upload Exception Message To Your Web Server
    }


    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        //应用的版本名称和版本号
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        //android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);

        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //cpu架构
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
    }
}
