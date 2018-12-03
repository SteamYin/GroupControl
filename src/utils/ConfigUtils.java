package utils;

import common.Const;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * 配置管理
 */
public class ConfigUtils {
    private static Properties prop = new Properties();;

    private static Logger logger = LogManager.getLogger(ConfigUtils.class);

    private static boolean readProperties() {
        if(prop.size() > 0) return true;
        try {
            //读取属性文件a.properties
            InputStream in = new BufferedInputStream(new FileInputStream(Const.propfile));
           // prop.load(in);     ///加载属性列表

            prop.load(new InputStreamReader(in, "UTF-8"));
//            Iterator<String> it = prop.stringPropertyNames().iterator();
//            while (it.hasNext()) {
//                String key = it.next();
//                System.out.println(key + ":" + prop.getProperty(key));
//            }
            in.close();
        } catch (Exception e) {
//            System.out.println(e);
            logger.warn(e.getMessage());
        }
        return prop.size() > 0;
    }

    private static void saveProperties() {
        if(prop.size() == 0) return;
        try {
            ///保存属性到b.properties文件
            FileOutputStream oFile = new FileOutputStream(Const.propfile, false);//true表示追加打开
            //prop.store(oFile);
            prop.store(oFile, "AndroidRobot配置文件");
            oFile.close();
        } catch (Exception e) {
//            System.out.println(e);
            logger.warn(e.getMessage());
        }
    }

    public static String getConfigValue(String key){
        if(!readProperties()) return null;
        return prop.getProperty(key);
    }

    public static void setConfigValue(String key, String value, boolean save){
        if(!readProperties()) return;
        prop.setProperty(key, value);
        if(save) saveProperties();
    }

    public static void setConfigValue(String key, String value){
        setConfigValue(key, value, false);
    }

    public static void save(){
        saveProperties();
    }
}
