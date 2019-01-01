package sample;

import common.Const;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.Device;
import models.Point;
import models.Rgb;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.OcrUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Date;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 800, 600));
//        primaryStage.show();

        URL location = getClass().getResource("sample.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root = fxmlLoader.load();
        //如果使用 Parent root = FXMLLoader.load(...) 静态读取方法，无法获取到Controller的实例对象
        primaryStage.setTitle("群控app v"+Const.version);
        Scene scene = new Scene(root, 1300, 700);
        //加载css样式
        //scene.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());
        primaryStage.setScene(scene);
        Controller controller = fxmlLoader.getController();   //获取Controller的实例对象
        //Controller中写的初始化方法
        controller.init();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.print("监听到窗口关闭");
                controller.uninit();
            }
        });
        primaryStage.show();
    }


    public static void main(String[] args) {
       // OcrUtils.doOrc("2222","d:/10.png",172,687,187,100);
//        checkProxy("101.37.14.151",3128);
//        checkProxy("182.112.202.250",8998);

//        File file = new File("D:\\qtt\\GroupControl\\screens\\20181126\\002\\a\\115131_672.png");
//        File file = new File("D:\\qtt\\GroupControl\\screens\\20181126\\002\\a\\105019_060.png");
//        Point theFirstPoint = findLine(file, 1781, 700);
//        System.out.println(theFirstPoint != null ? ("找到1 "+theFirstPoint.toString()):"未找到1");
//        if(theFirstPoint != null) {
//            Point theSecondPoint = findLine(file, theFirstPoint.getY() - 250, 450);
//            System.out.println(theFirstPoint != null ? ("找到1 "+theFirstPoint.toString()):"未找到1");
//            System.out.println(theSecondPoint != null ? ("找到2 "+theSecondPoint.toString()):"未找到2");
//            if(isAd(file, theFirstPoint.getY())){
//                System.out.println("广告");
//            }else{
//                System.out.println("非广告");
//            }
//
//        }
        launch(args);
    }


    private static boolean isAd(File file, int y){
        // x = 980, y = [30,60]
        Rgb rgbWhite = new Rgb(-1,-1,-1);
        int x = 980;
        int oldY = y;
        y -= 30;
        while(oldY - y < 60){
            Rgb rgb = getPixelRgb(file, x, y);
            if(rgb == null){
                y -= 4; continue;}
            System.out.println("("+x+","+y+"):"+rgb.toString());
            y -= 4;
            if(!rgb.equals(rgbWhite)) return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
//    private static Point findNextPage(File file){
//        Point point1 = findLine(file, 1770, 700);
//        if(point1 == null) return null;
//        System.out.println("找到分割线1："+point1.getY());
//        // 找上面一条线
//        while(true) {
//            Point point2 = findLine(file, point1.getY()-230);
//            if (point2 == null) return null;
//
//            System.out.println("找到分割线2："+point2.getY());
//            if (point1.getY() - point2.getY() > 270
//                    || point1.getY() - point2.getY() < 230){
//                // 正常应该是255-265
//                point1 = point2;
//                System.out.println("分割线范围不对："+(point1.getY() - point2.getY()));
//                continue;
//            }
//            // 判断是不是广告，是广告，继续往下找
//            Rgb rgb = getPixelRgb(file, 970, point1.getY() - 60);
//            if(rgb.equals(new Rgb(141,141,141))){
//                point1 = point2;
//                System.out.println("内容为广告："+rgb.toString());
//                continue;
//            }
////            String text = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 970, point1.getY() - 90, 66, 40, false);
////            // 两行文字
////            if(text.equals("广告")) {
////                // 正常应该是255左右
////                point1 = point2;
////                continue;
////            }
////            text = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 970, point1.getY() - 80, 66, 40, false);
////            if(text.equals("广告")) {
////                // 正常应该是255左右
////                point1 = point2;
////                continue;
////            }
//            return new Point(point1.getX(), (point1.getY()+point2.getY())/2);
//        }
//    }

    /**
     * 判断是否为分割线
     * @param file
     * @param y
     * @return 0：不是；1：是；-1：白色像素点
     */
    private static int isLine(File file, int x, int y){
        Rgb rgbLine1 = new Rgb(230, 230, 230);
        Rgb rgbLine2 = new Rgb(248, 248, 248);
        Rgb rgbWhite = new Rgb(-1,-1,-1);
        Rgb rgb = getPixelRgb(file, x, y);
        if(rgb == null) return 0;
        System.out.println("("+x+","+y+"):"+rgb.toString());
        if(!rgb.equals(rgbLine1) && !rgb.equals(rgbLine2)) return rgb.equals(rgbWhite) ? -1 : 0;
        // 边上的要是白色
        rgb = getPixelRgb(file, 5, y);
        System.out.println("(5,"+y+"):"+rgb.toString());
        if(!rgb.equals(rgbWhite)) return 0;

        // 再横向找2个点，如果都是一样的话，则表示找到了
        rgb = getPixelRgb( file, 540, y);
        if(rgb == null || (!rgb.equals(rgbLine1) && !rgb.equals(rgbLine2))) return 0;
        rgb = getPixelRgb( file, x+400, y);
        if(rgb == null || (!rgb.equals(rgbLine1) && !rgb.equals(rgbLine2))) return 0;
        return 1;
    }

    private static Point findLine(File file, int y, int rang){
        int oldY = y;
        // x范围：380-400
        int x = 380;
        int not_white = 0;
        while (oldY - y <= rang){
            if(x++ > 400) x = 380;

            int flag = isLine(file,x, y);
            if(flag == 1) return new Point(400, y);
            else if(flag == -1){
                not_white = 0;
                y-=2;
            }else{
                not_white++;
                y-= (not_white >= 2 ? 40 : 2);
            }
        }
        return null;
    }

//    private static Point findSecondLine(File file, int y){
//        // 在范围250-270找第二条线
//        int oldY = y;
//        y -= 250;
////        while(oldY - y < 270){
////
////
////            y -= 2;
////        }
//
//        int x = 380;
//        int not_white = 0;
//        while (oldY - y <= 700){
//            if(x++ > 400) x = 380;
//
//            int flag = isLine(file,x, y);
//            if(flag == 1) return new Point(400, y);
//            else if(flag == -1){
//                not_white = 0;
//                y-=2;
//            }else{
//                not_white++;
//                y-= (not_white >= 2 ? 40 : 2);
//            }
//        }
//        return null;
//    }

//    /**
//     * 从下向上开始查找分割线
//     * @param file
//     * @param y
//     * @return
//     */
//    private static Point findLine(File file, int y){
//        int x = 400;
//        Rgb rgb = new Rgb(230, 230, 230);
////        Rgb rgbWhit = new Rgb(250, 250, 250);
////        y+=10;
//        int n = 0;
//        while(n++<265){
//            y-= 1;
//            Rgb _rgb = getPixelRgb( file, x, y);
//            if(_rgb == null) continue;
////            System.out.println(n+": "+y+": "+_rgb.toString());
//            if(!_rgb.equals(rgb)) continue;
//
//            // 再横向找2个点，如果都是一样的话，则表示找到了
//            _rgb = getPixelRgb( file, x+200, y);
//            if(_rgb == null) continue;
//            if(!_rgb.equals(rgb)) continue;
//            _rgb = getPixelRgb( file, x+400, y);
//            if(_rgb == null) continue;
//            if(!_rgb.equals(rgb)) continue;
//
//            return new Point(x, y);
//        }
//        return null;
//    }

    public static Rgb getPixelRgb(File file, int x, int y){
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
            return new Rgb(bi.getRGB(x, y));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean checkProxy(String ip, Integer port) {
        try {
            Date timeStart = new Date();
            //http://1212.ip138.com/ic.asp 可以换成任何比较快的网页
//            Jsoup.connect("https://sign.breff.cn/v1/user/14311")
            Document doc = Jsoup.connect("https://sign.breff.cn/v1/user/14311")
//            Document doc = Jsoup.connect("https://im.qq.com/")
                    .timeout(4 * 1000).ignoreContentType(true)
                    .proxy(ip, port)
                    .get();
            System.out.println("ip " + ip + " is ok");
//            System.out.println(doc.body());
            System.out.println("耗时："+(new Date().getTime() - timeStart.getTime())+"毫秒");
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("----ip " + ip + " is not aviable");//异常IP
            return false;
        }

    }
    public static void createIPAddress(String ip,int port) {
        URL url = null;
        try {
            url = new URL("https://sign.breff.cn/v1/user/14311");
        } catch (MalformedURLException e) {
            System.out.println("url invalidate");
        }
//        InetSocketAddress addr = null;
//        addr = new InetSocketAddress(ip, port);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr); // http proxy
        System.setProperty("http.proxyHost", ip);
        System.setProperty("http.proxyHost", ip);
        System.setProperty("http.proxyPort", port+"");
        InputStream in = null;
        try {
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(1000);
            in = conn.getInputStream();
        } catch (Exception e) {
            System.out.println("ip " + ip + " is not aviable");//异常IP
        }
        String s = convertStreamToString(in);
        System.out.println(s);
        // System.out.println(s);
        if (s.indexOf("baidu") > 0) {//有效IP
            System.out.println(ip + ":"+port+ " is ok");
        }
    }
    public static String convertStreamToString(InputStream is) {
        if (is == null)
            return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();

    }
}
