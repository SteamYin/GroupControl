package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Rgb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ADBUtils;
import utils.OcrUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

public class _PlayDFTTAction extends Action {
    private String key;      // 保存下一页的位置
    private String step1;   // 读完并找到下一页的位置
    private String step2;   // 广告或者读完，没找到下一页，需返回到首页

    private static Logger logger = LogManager.getLogger(_PlayDFTTAction.class);
    public _PlayDFTTAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType._PLAY_DFTT;
        this.tag = "playdftt";

        if(param.length < 3) return;
        String s = param[1];
        // 除去[]
        key = s.substring(1, s.length() - 1);
        step1 = param[2];
        if(param.length < 3)
            step2 = "";
        else
            step2 = param[3];
    }

    @Override
    public String doAction(Device device) {
        super.doAction(device);
        // 超过5分钟就会提醒阅读这篇文章时间太长

        // 先判断是否为广告
        device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,device.getDeviceName(),
                device.task.name, false);
        logger.info("截屏："+device.task.capscreenname);
        String text = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 140, 100, 90, 50, false);
        if(text.equals("关闭")) return step2;

        // 非广告，开始阅读
        int n = 0;

        while(n++ < 20){
            if(!device.task.isRunning()) return step2;
            sleep(3000);
            if(!device.task.isRunning()) return step2;
//            ADBUtils.swipeInput(device.serialnumber, 600, 1770, 620, 190, 2000);
            ADBUtils.swipeInput(device.serialnumber, 600, 1770, 620, 590, 2000);
            if(n > 2) {
                // 截屏
                device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,device.getDeviceName(),
                        device.task.name, false);
                Point point = findNextPage(device);
                if(point != null){
                    device.task.addKeyValue(key, point);
                    return step1;
                }
            }
        }
        return step2;
    }

    /**
     *
     * @param device
     * @return
     */
    private Point findNextPage(Device device){
        File file = new File( device.task.capscreenname);
        Point point1 = findLine(device, file, 1780,700);
        if(point1 == null) return null;
        logger.info("找到分割线1："+point1.getY());
        // 找上面一条线
        while(true) {
            if(!device.task.isRunning()) return null;
            Point point2 = findLine(device, file, point1.getY()- 260, 450);
            if (point2 == null) return null;

            logger.info("找到分割线2："+point2.getY());
            if (point1.getY() - point2.getY() > 280){
                logger.info("分割线范围不对："+(point1.getY() - point2.getY()));
                // 正常应该是255-265
                point1 = point2;
                continue;
            }
            // 判断是不是广告，是广告，继续往下找
            if(isAd(device, file, point1.getY())){
                point1 = point2;
                logger.info("内容为广告");
                continue;
            }
            return new Point(point1.getX(), (point1.getY()+point2.getY())/2);
        }
    }

    public Rgb getPixelRgb(File file, int x, int y){
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
            return new Rgb(bi.getRGB(x, y));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    Date lastMoveTime = new Date();
    private void moveScreen(Device device){
        Date now = new Date();
        if(now.getTime() - lastMoveTime.getTime() < 5000) return;

        ADBUtils.swipeInput(device.serialnumber, 600, 1070, 620, 590, 800);
        ADBUtils.swipeInput(device.serialnumber, 600, 590, 620, 1070, 800);
        lastMoveTime = new Date();
    }
    private boolean isAd(Device device, File file, int y){
        // x = 980, y = [30,60]
        Rgb rgbWhite = new Rgb(-1,-1,-1);
        int x = 980;
        int oldY = y;
        y -= 30;
        while(oldY - y < 60){
            moveScreen(device);
            Rgb rgb = getPixelRgb(file, x, y);
            if(rgb == null){
                y -= 4; continue;}
//            System.out.println("("+x+","+y+"):"+rgb.toString());
            y -= 4;
            if(!rgb.equals(rgbWhite)) return true;
        }
        return false;
    }

    /**
     * 判断是否为分割线
     * @param file
     * @param y
     * @return 0：不是；1：是；-1：白色像素点
     */
    private int isLine(File file, int x, int y){
        Rgb rgbLine1 = new Rgb(235, 235, 235);
        Rgb rgbLine2 = new Rgb(244, 244, 244);
        Rgb rgbWhite = new Rgb(-1,-1,-1);
        Rgb rgb = getPixelRgb(file, x, y);
        if(rgb == null) return 0;
        System.out.println("("+x+","+y+"):"+rgb.toString());
        if(!rgb.equals(rgbLine1) && !rgb.equals(rgbLine2)) return rgb.equals(rgbWhite) ? -1 : 0;
        // 边上的要是白色
        rgb = getPixelRgb(file, 5, y);
//        System.out.println("(5,"+y+"):"+rgb.toString());
        if(!rgb.equals(rgbWhite)) return 0;

        // 再横向找2个点，如果都是一样的话，则表示找到了
        rgb = getPixelRgb( file, 540, y);
        if(rgb == null || (!rgb.equals(rgbLine1) && !rgb.equals(rgbLine2))) return 0;
        rgb = getPixelRgb( file, x+400, y);
        if(rgb == null || (!rgb.equals(rgbLine1) && !rgb.equals(rgbLine2))) return 0;
        return 1;
    }


    private Point findLine(Device device, File file, int y, int rang){
        int oldY = y;
        // x范围：380-400
        int x = 380;
        int not_white = 0;
        while (oldY - y <= rang){
            if(x++ > 400) x = 380;
            moveScreen(device);
            int flag = isLine(file,x, y);
            if(flag == 1) return new Point(400, y);
            else if(flag == -1){
                not_white = 0;
                y-=3;
            }else{
                not_white++;
                y-= (not_white >= 2 ? 40 : 3);
            }
        }
        return null;
    }
}
