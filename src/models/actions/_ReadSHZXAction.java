package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Rgb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;
import utils.OcrUtils;

import java.io.File;

public class _ReadSHZXAction extends _ReadAction {

    private static Logger logger = LogManager.getLogger(_ReadSHZXAction.class);
    public _ReadSHZXAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow, param);
        this.type = Const.ActionType._READ_SHZX;
        this.tag = "readshzx";

        this.min_line_color = 220;     // 直线像素上的颜色最小值
        this.max_line_color = 250;     // 直线像素上的颜色最大值

        this.min_content_heigth = 280;   // 列表内容的最小高度,用于确定查找范围
        this.max_content_heigth = 450;   // 列表内容的最大高度

        this.y_step_length = 1;          // 分隔线的高度，即几个像素        值越小效率越低
//        protected int x_step_length = 80;         // 取3个点的横向间隔

//        protected int page_size = 10;           // 最多翻几页
//        protected int check_page_no = 4;        // 从第几页开始检查
//
//        protected int max_check_y = 1770;       // 检测范围的最大y坐标，即最下方
//        protected int min_check_y = 1000;       // 检测范围的最小y坐标
//
//        protected int min_check_x = 380;        // 检测范围的第一个点最小x坐标
//        protected int max_check_x = 400;        // 检测范围的第一个点最小x坐标
//        protected int white_x = 5;              // 间隔线不能顶到屏幕的左右两边，所以得检测坐标一个点是不是白色
//
//        this.time_out_length = 300;    // 超时时长，秒为单位

        this.line_content_heigth = 50; // 线与文字等内容之间的最小间距，为了优化查找功能
        this.sleep_length = 2000;
    }

    @Override
    public String doAction(Device device) {
        return super.doAction(device);
    }

    @Override
    protected boolean isAd(Device device, File file, int bottom, int top){
        String text = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 50, bottom - 81, 65, 33, false);
        logger.info("广告判断识别结果："+text);
        if(text.indexOf("广告") != -1 && text.indexOf("厂告") != -1) return true;
        return false;
    }

    /**
     * 判断是否为分割线
     * @param file
     * @param y
     * @return 0：不是；1：是；-1：白色像素点
     */
    @Override
    protected int isLine(File file, int y) {
        Rgb lineRgb = new Rgb(237,238,240);
        Rgb whiteRgb = new Rgb(255,255,255);
        int x = CommonUtils.getRandomNum(min_check_x, max_check_x);
        Rgb rgb = getPixelRgb(file, x, y);
        if (rgb == null) return 0;

        if(!rgb.equals(lineRgb)) return rgb.equals(whiteRgb) ? -1 : 0;
//        int r = rgb.isLineColor();
//        if (r == 0) return 0;
//        if (r != 255)
//            System.out.println("(" + x + "," + y + "):" + rgb.toString());

        // 边上的要是白色
        rgb = getPixelRgb(file, white_x, y);
        if(!rgb.equals(whiteRgb)) return 0;


        // 再横向找2个点，如果都是一样的话，则表示找到了
        x += x_step_length;
        rgb = getPixelRgb(file, x, y);
        if (rgb == null) return 0;
        if(!rgb.equals(lineRgb)) return 0;

        x += x_step_length;
        rgb = getPixelRgb(file, x, y);

        if (rgb == null) return 0;
        if(!rgb.equals(lineRgb)) return 0;
        return 1;
    }
}
