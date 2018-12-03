package utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class OcrUtils {
    //private static String tesseract_exe = "c:/Program Files (x86)/Tesseract-OCR/tesseract.exe";

    private static String tesseract_exe = "./tesseract-Win64/tesseract.exe";
    private static String temp_path = "./temp/";

    private static Logger logger = LogManager.getLogger(OcrUtils.class);
    /**
     * 图像识别一行文字
     * @param serialnumber
     * @param image
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public static String doSingleLineOrc(String serialnumber, String image, int x, int y, int w, int h, boolean isdigits){
        try {
            String cutImage = temp_path + serialnumber+".png";
            String result = temp_path + serialnumber;

            // 切图
            ImageUtils.cutImage(image, cutImage, x, y, w, h);
            Process  pro = Runtime.getRuntime()
                    .exec(new String[]{tesseract_exe,
                            cutImage,
                            result,
                            "-l",
                            "chi_sim",
                            "-psm","7",
                            isdigits ? "digits" : ""   // 3.0支持白名单，4.0就不支持了
                    });
            pro.waitFor();

            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(result+".txt")),"UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String s=br.readLine();
            if(CommonUtils.isNull(s)) return "";
            // 从结果文件中读出识别结果
            s =s.replace("\n\f", "");
            s = s.replace(" ", "");
            reader.close();
            return s;
        } catch (IOException e) {
//            e.printStackTrace();
            logger.warn(e.getMessage());
        }
        catch (InterruptedException e){
//            e.printStackTrace();
            logger.warn(e.getMessage());
        }
        catch ( Exception e){
//            e.printStackTrace();

            logger.warn(e.getMessage());
        }
        return  "";
    }

    public static String doSingleLineOrc(String serialnumber, String image, int x, int y, int w, int h){
        return doSingleLineOrc(serialnumber, image, x, y, w, h, false);
    }
    /**
     * 图像识别多行文字，一个文字一行，包含坐标，坐标原点是图片的左下角
     * @param serialnumber
     * @param image
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public static String doMultiLineOrc(String serialnumber, String image, int x, int y, int w, int h)
    {
        return doMultiLineOrc(serialnumber, image, x, y, w, h, false);
    }

    /**
     * 图像识别多行文字，一个文字一行，包含坐标，坐标原点是图片的左下角
     * @param serialnumber
     * @param image
     * @param x
     * @param y
     * @param w
     * @param h
     * @param isdigits
     * @return
     */
    public static String doMultiLineOrc(String serialnumber, String image, int x, int y, int w, int h, boolean isdigits){
        try {
            String cutImage = temp_path + serialnumber+".png";
            String destImage = temp_path + serialnumber+"_dest.png";
            String result = temp_path + serialnumber;

            // 切图
            ImageUtils.cutImage(image, cutImage, x, y, w, h);
            ImageUtils.gray(cutImage, destImage);

            Process  pro = Runtime.getRuntime()
                    .exec(new String[]{tesseract_exe,
                            destImage,
                            result,
                            "-l","chi_sim",
                            "makebox",
                            isdigits ? "digits" : ""   // 3.0支持白名单，4.0就不支持了
                    });
            pro.waitFor();

            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(result+".box")),"UTF-8");
            BufferedReader br = new BufferedReader(reader);

            String s = "", line;
            while((line=br.readLine())!=null){
                if(!CommonUtils.isNull(s)) s += "\n";
                s += line;
            }
            reader.close();
            return s;
        } catch (IOException e) {
//            e.printStackTrace();
            logger.warn(e.getMessage());
        }
        catch (InterruptedException e){
//            e.printStackTrace();
            logger.warn(e.getMessage());
        }
        catch ( Exception e){
//            e.printStackTrace();
            logger.warn(e.getMessage());
        }
        return  "";
    }
}
