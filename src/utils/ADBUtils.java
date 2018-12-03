package utils;

import common.Const;
import models.FileSystem;
import models.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sample.Controller;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ADBUtils {
    private static String getPreCmd(String serialnumber) {
        return "adb -s " + serialnumber + " shell ";
    }

    //    private static Logger logger = LogManager.getLogger(Controller.class);
    public static List<String> getDevices() {
        String cmd = "adb devices";
        List<String> result = CMDUtils.runCMD(cmd);
        if (result == null || result.size() == 0) return null;

        List<String> listDevice = new ArrayList<>();
        for (String s : result) {
            if (s.equals("List of devices attached") || s.equals("")) continue;
            String[] ss = s.split("\t");
            //    System.out.println(ss[0]);
            listDevice.add(ss[0]);
        }
        return listDevice;
    }

//    public static void forwardDevice(String serialnumber, int port) {
//        String cmd = "adb -s " + serialnumber + " forward tcp:" + port + " tcp:8997";
//        //  System.out.println(cmd);
//        CMDUtils.runCMD(cmd);
//    }

    /**
     * 启动app
     *
     * @param serialnumber
     * @param pkg
     * @param activity
     */
    public static void startApp(String serialnumber, String pkg, String activity) {
        // io.virtualapp/io.virtualapp.splash.SplashActivity

        String cmd = getPreCmd(serialnumber) + "am start " + pkg + "/" + activity;
        CMDUtils.runCMD(cmd);
    }

    /**
     * 强制关闭app
     *
     * @param serialnumber
     * @param pkg
     */
    public static void stopApp(String serialnumber, String pkg) {
        String cmd = getPreCmd(serialnumber) + "am force-stop " + pkg;
        CMDUtils.runCMD(cmd);
    }

    public static String getModel(String serialnumber) {
        String cmd = getPreCmd(serialnumber) + "getprop ro.product.model";
        List<String> result = CMDUtils.runCMD(cmd);
        if (result == null || result.size() == 0) return null;
        return result.get(0);
    }

    public static String getVersion(String serialnumber) {
        String cmd = getPreCmd(serialnumber) + "getprop ro.build.version.release";
        List<String> result = CMDUtils.runCMD(cmd);
        if (result == null || result.size() == 0) return null;
        return result.get(0);
    }

    public static String getBrand(String serialnumber) {
        String cmd = getPreCmd(serialnumber) + "getprop ro.product.brand";
        List<String> result = CMDUtils.runCMD(cmd);
        if (result == null || result.size() == 0) return null;
        return result.get(0);
    }

    private static FileSystem getFileSystem(String serialnumber) {
        /*      魅族手机： adb shell df /mnt/shell/emulated
                其他： adb shell df /data
                获取sdcard存储信息： adb shell df /storage/sdcard
         */

        // 返回时不同手机结果不一样
        /*
        Filesystem               Size     Used     Free   Blksize
        /data                   10.3G     4.1G     6.2G   4096
         */
        /*
        Filesystem      1K-blocks     Used Available Use% Mounted on
        /dev/block/dm-1  56368440 32053608  24167376  58% /data
         */
//        String cmd = getPreCmd(serialnumber) + "df /data";
//        List<String> result = CMDUtils.runCMD(cmd);
//        if (result == null || result.size() == 0) return null;
//        //for(String s : result){
//        String s = result.get(1);
//        if(s.indexOf("Filesystem") != -1 || s.indexOf("No such file") !=-1) return null;
//
//        String[] ss = s.split("\t");
//        if(ss.length != 5) return null;
//        return new FileSystem(getMSize(ss[1]), getMSize(ss[2]),getMSize(ss[3]));
        return null;
    }

    public static Screen getScreen(String serialnumber) {
        /*
            mUnrestrictedScreen=(0,0) 1080x1920
         */
        String cmd = getPreCmd(serialnumber) + "\"dumpsys window | grep mRestrictedOverscanScreen\"";
        List<String> result = CMDUtils.runCMD(cmd);
        if (result == null || result.size() == 0) return null;
        //for(String s : result){
        String s = result.get(0).trim();
        if (s.indexOf("error") != -1) return null;
        String[] ss = s.split(" ");
        if (ss == null || ss.length != 2) return null;
        String[] wh = ss[1].split("x");
        if (wh == null || wh.length != 2) return null;
        return new Screen(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));

    }

    private static long getMSize(String size) {
        size = size.toLowerCase();
        if (size.indexOf("g") != -1) {
            String temp = size.replace("g", "");
            return Long.parseLong(Float.parseFloat(temp) * 1024 + "");
        } else if (size.indexOf("m") != -1) {
            String temp = size.replace("m", "");
            return Long.parseLong(temp);

        } else if (size.indexOf("k") != -1) {
            String temp = size.replace("k", "");
            return Long.parseLong(Float.parseFloat(temp) / 1024 + "");

        } else
            return Long.parseLong(Float.parseFloat(size) / 1024 / 1024 + "");
    }

    /**
     * 点击指定位置
     *
     * @param serialnumber
     * @param x
     * @param y
     */
    public static void tapInput(String serialnumber, int x, int y) {
        String cmd = getPreCmd(serialnumber) + "input tap " + x + " " + y;
        CMDUtils.runCMD(cmd);
    }

    public static void swipeInput(String serialnumber, int x1, int y1, int x2, int y2, int duration) {
        String cmd = getPreCmd(serialnumber) + "input swipe " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + duration;

        // System.out.println(cmd);
        CMDUtils.runCMD(cmd);
    }

    /**
     * 在指定位置输入字符串
     *
     * @param serialnumber 设备sn
     * @param x            位置x坐标
     * @param y            位置y坐标
     * @param text         输入字符串，包含中文，中文需要手机端支持（安装ADBKeyBoard.apk输入法，并设置成缺省输入法）
     */
    public static void textInput(String serialnumber, int x, int y, String text) {
        if (CommonUtils.isNull(text)) return;
        tapInput(serialnumber, x, y);
        String cmd = getPreCmd(serialnumber);
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(text);
        if (m.find()) {
            // 包含中文输入方式
            cmd += "am broadcast -a ADB_INPUT_TEXT --es msg  \"" + text + "\"";
        } else {
            cmd += "input text \"" + text + "\"";
        }
        CMDUtils.runCMD(cmd);
    }

    public static void keyeventInput(String serialnumber, int keyevent) {
        String cmd = getPreCmd(serialnumber) + "input keyevent " + keyevent;
        CMDUtils.runCMD(cmd);
    }

    private static void _screenCap(String serialnumber, String name, String path) {
        try {
            File file = new File(path);
            file.mkdirs();

            String cmd = "adb -s " + serialnumber + " shell screencap -p /sdcard/" + name + ".png";
            CMDUtils.runCMD(cmd);
            Thread.sleep(50);

            cmd = "adb -s " + serialnumber + " pull /sdcard/" + name + ".png " + path;
            CMDUtils.runCMD(cmd);

            cmd = "adb -s " + serialnumber + " shell rm /sdcard/" + name + ".png";
            CMDUtils.runCMD(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 截屏
     *
     * @param serialnumber 设备序列号
     * @param task         任务名称
     * @param inthread     是否新启一个线程
     */
    public static String screenCap(String serialnumber, String devicename, String task, boolean inthread) {
        // 日期\设备名\任务名\appid.png
        /*
        $ adb shell screencap -p /sdcard/screen.png
        $ adb pull /sdcard/screen.png ./
        $ adb shell rm /sdcard/screen.png
         */
        final String path = (Const.path_screen  + CommonUtils.date2String(new Date(), "yyyyMMdd") + "/" + devicename + "/" + task + "/").replace("/", File.separator).replace("\\", File.separator);
        final String _name = CommonUtils.date2String(new Date(), "HHmmss_SSS");

        if (inthread) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    _screenCap(serialnumber, _name, path);
                }
            }).start();
        } else {
            _screenCap(serialnumber, _name, path);
            // System.out.println("非线程执行。");
        }
        return path + _name + ".png";
    }

    public static String screenCap(String serialnumber, String devicename, String task) {
        return screenCap(serialnumber, devicename, task, true);
    }

//    public static void copyScreenCap(String src, String dest){
//        final String cmd = ("xcopy \"" + src + "\" \"" + dest+"\"").replace("\\", File.separator).replace("/", File.separator);
//        CMDUtils.runCMD(cmd);
//    }

    public static void copyScreenCap(String src, String serialnumber, String devicename, String task) {
        final String path = (Const.path_screen  + CommonUtils.date2String(new Date(), "yyyyMMdd") + "/" + devicename + "/" + task + "/").replace("/", File.separator).replace("\\", File.separator);
        final String _name = CommonUtils.date2String(new Date(), "HHmmss_SSS");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int n = 0;
                    while(true){
                        if(CommonUtils.isExists(src)){
//                            System.out.println("文件"+ src+"存在！");
                            Thread.sleep(1000);
                            //System.out.println(CMDUtils.runCMD(cmd));
                            CommonUtils.copyFile(src, path+_name);
                            break;
                        }
                        else{
//                            System.out.println("文件"+ src+"不存在！");
                            if(n++ > 5) break;
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
