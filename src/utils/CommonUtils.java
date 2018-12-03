package utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class CommonUtils {
    /**
     * 判断字符串是否为空
     * @param s
     * @return
     */
    public static boolean isNull(String s){
        return s == null || s.trim().equals("");
    }

    /**
     * 生成一个指定范围的随机数
     * @param min
     * @param max
     * @return
     */
    public static int getRandomNum(int min, int max){
        return min+(int)(Math.random()*(max-min+1));
    }

    /**
     * 时间转字符串
     *
     * @param date   时间
     * @param dateFormat 时间格式
     * @return
     */
    public static String date2String(Date date, String dateFormat) {
        try {
            if(date == null) return "";
            return new SimpleDateFormat(dateFormat).format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Date string2Date(String date, String timeformat){
        try {
            if(isNull(date)) return null;
            return new SimpleDateFormat(timeformat).parse(date);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static int string2Int(String number){
        try{
            if(isNull(number)) return 0;
            return Integer.parseInt(number);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static String cutString(String str, int length){
        if(isNull(str)) return str;
        if(str.length() <= length) return str;
        return str.substring(0, length)+"...";
    }

    /**
     * 判断字符串是否为数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str){
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断文件是否存在
     * @param file
     * @return
     */
    public static Boolean isExists(String file){
        File f=new File(file);

        return f.exists();
    }

    public static void deleteFile(String file){
        File f = new File(file);
        if(f != null && f.exists()) f.delete();
    }
    /**
     * 将文件拷贝到指定目录
     * @param source 文件所在目录(文件的全路径)
     * @param dest 指定目录(包含复制文件的全名称)
     * @throws Exception
     */
    public static void copyFile(String source, String dest) throws Exception {
        InputStream input = null;
        OutputStream output = null;

        // 如果原来的文件存在，先删除
        CommonUtils.deleteFile(dest);

        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }

//        FileInputStream input=new FileInputStream(oldAddress);
//        FileOutputStream output=new FileOutputStream(newAddress);//注意：newAddress必须包含文件名字，比如说将D:/AAA文件夹下的文件"a.xml"复制到D:\test目录下，则newAddress必须为D:\test\a.xml
////oldAddress必须是a.xml文件的全路径，即D:\AAA\a.xml,否则就会报IO异常的错误
//        int in=input.read();
//        while(in!=-1){
//            output.write(in);
//            in=input.read();
//        }
//        input.close();
//        output.close();
    }
}
