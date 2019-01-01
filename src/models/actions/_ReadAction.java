package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Rgb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ADBUtils;
import utils.CommonUtils;
import utils.OcrUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

public class _ReadAction extends Action {
    private String key;      // 保存下一页的位置
    private String step1;   // 读完并找到下一页的位置
    private String step2;   // 广告或者读完，没找到下一页，需返回到首页


    private Date lastMoveTime = new Date();

    protected int min_line_color = 200;     // 直线像素上的颜色最小值
    protected int max_line_color = 245;     // 直线像素上的颜色最大值

    protected int min_content_heigth = 280;   // 列表内容的最小高度,用于确定查找范围
    protected int max_content_heigth = 450;   // 列表内容的最大高度

    protected int y_step_length = 2;          // 分隔线的高度，即几个像素
    protected int x_step_length = 80;         // 取3个点的横向间隔

    protected int page_size = 10;           // 最多翻几页
    protected int check_page_no = 4;        // 从第几页开始检查

    protected int max_check_y = 1770;       // 检测范围的最大y坐标，即最下方
    protected int min_check_y = 1000;       // 检测范围的最小y坐标

    protected int min_check_x = 380;        // 检测范围的第一个点最小x坐标
    protected int max_check_x = 400;        // 检测范围的第一个点最小x坐标
    protected int white_x = 5;              // 间隔线不能顶到屏幕的左右两边，所以得检测坐标一个点是不是白色

    protected int time_out_length = 300;    // 超时时长，秒为单位

    protected int line_content_heigth = 40; // 线与文字等内容之间的最小间距，为了优化查找功能

    protected int sleep_length = 3000;



    private static Logger logger = LogManager.getLogger(_ReadAction.class);
    public _ReadAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = 0;
        this.tag = "read";

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
        long start = System.currentTimeMillis() / 1000;

        device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,device.getDeviceName(),
                device.task.name, false);
        logger.info("截屏："+device.task.capscreenname);

        // 1、判断是文章页
        if(!isView()){
            logger.info("非文章页，进入"+step2);
            return step2;
        }

        int n = 0;

        while(n++ < page_size){
            // 是否暂停
            if(!device.task.isRunning()) return step2;
            long now = System.currentTimeMillis() / 1000;
            // 超时检查
            if(now - start >= time_out_length) {
                logger.info("超时，进入"+step2);
                return step2;
            }
            sleep(sleep_length);
            if(!device.task.isRunning()) return step2;
            // 翻页
            ADBUtils.swipeInput(device.serialnumber, 600, 1770, 620, 590, 2000);
            if(n > check_page_no) {
                logger.info("进入第"+n+"屏");
                // 截屏
                device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,device.getDeviceName(),
                        device.task.name, false);
                // 找下一个内容链接第坐标
                Point point = findNextPage(device);
                if(point != null){
                    device.task.addKeyValue(key, point);
                    return step1;
                }
            }
        }
        return step2;
    }

    public boolean isView(){
        return true;
    }

//    public Point findNextPage(Device device){
//        return null;
//    }
    /**
     *
     * @param device
     * @return
     */
    private Point findNextPage(Device device){
        File file = new File(device.task.capscreenname);
        Point point1 = findLine(device, file, max_check_y,0, max_content_heigth);
        if(point1 == null) return null;
        logger.info("找到分割线1："+point1.getY());
        // 找上面一条线
        while(true) {
            if(!device.task.isRunning()) return null;
            Point point2 = findLine(device, file, point1.getY(), min_content_heigth, max_content_heigth);
            if (point2 == null) return null;

            logger.info("找到分割线2："+point2.getY());

            // 判断是不是广告，是广告，继续往下找
            if(isAd(device, file, point1.getY(), point2.getY())){
                point1 = point2;
                logger.info("内容为广告，继续找");
                continue;
            }
            return new Point(point1.getX(), (point1.getY()+point2.getY())/2);
        }
    }

    /**
     * 判断是否为分割线
     * @param file
     * @param y
     * @return 0：不是；1：是；-1：白色像素点
     */
    protected int isLine(File file, int y) {
        int x = CommonUtils.getRandomNum(min_check_x, max_check_x);
        Rgb rgb = getPixelRgb(file, x, y);
        if (rgb == null) return 0;
        int r = rgb.isLineColor();
        if (r == 0) return 0;
        if (r != 255)
            System.out.println("(" + x + "," + y + "):" + rgb.toString());

        // 边上的要是白色
        if (r < min_line_color || r > max_line_color) return r == 255 ? -1 : 0;
        rgb = getPixelRgb(file, white_x, y);
        r = rgb.isLineColor();
        if (r != 255) return 0;

        // 再横向找2个点，如果都是一样的话，则表示找到了
        x += x_step_length;
        rgb = getPixelRgb(file, x, y);
        if (rgb == null) return 0;
        r = rgb.isLineColor();
        if (r < min_line_color || r > max_line_color) return 0;

        x += x_step_length;
        rgb = getPixelRgb(file, x, y);

        if (rgb == null) return 0;
        r = rgb.isLineColor();
        if (r < min_line_color || r > max_line_color) return 0;
        return 1;
    }


    /**
     * 在y坐标上面[min_heigth,max_heigth]指找分隔线，从下往上找
     * @param device
     * @param file
     * @param y
     * @param min_heigth
     * @param max_heigth
     * @return
     */
    private Point findLine(Device device, File file, int y, int min_heigth, int max_heigth){
        if(y < min_check_y) return null;
        int current_y = y - min_heigth;
        while (y - current_y <= max_heigth){
            moveScreen(device);
            int flag = isLine(file, current_y);
            if(flag == 1) return new Point(min_check_x, current_y);
            else if(flag == -1){  // 白色
                current_y -= y_step_length;
            }else{ // 非白色
                current_y -= line_content_heigth;
            }
        }
        return null;
    }

    /**
     * 获得图像上某点的RGB
     * @param file
     * @param x
     * @param y
     * @return
     */
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

    /**
     * 上下移动图片，避免app积分停止
     * @param device
     */
    private void moveScreen(Device device){
        Date now = new Date();
        if(now.getTime() - lastMoveTime.getTime() < 5000) return;

        ADBUtils.swipeInput(device.serialnumber, 600, 1070, 620, 590, 800);
        ADBUtils.swipeInput(device.serialnumber, 600, 590, 620, 1070, 800);
        lastMoveTime = new Date();
    }

    /**
     * 判断是否为广告，各子类自己定义
     * @param device
     * @param file
     * @return
     */
    protected boolean isAd(Device device, File file, int bottom, int top){
        return false;
    }

}
