package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.OcrUtils;

import java.io.File;

public class _ReadQKTXAction extends _ReadAction {

    private static Logger logger = LogManager.getLogger(_ReadQKTXAction.class);
    public _ReadQKTXAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow, param);
        this.type = Const.ActionType._READ_QKTX;
        this.tag = "readqktx";

        this.min_line_color = 220;     // 直线像素上的颜色最小值
        this.max_line_color = 250;     // 直线像素上的颜色最大值

        this.min_content_heigth = 280;   // 列表内容的最小高度,用于确定查找范围
        this.max_content_heigth = 320;   // 列表内容的最大高度

        this.y_step_length = 2;          // 分隔线的高度，即几个像素
//        protected int x_step_length = 80;         // 取3个点的横向间隔

        this.page_size = 7;           // 最多翻几页
        this.check_page_no = 2;        // 从第几页开始检查
//
//        protected int max_check_y = 1770;       // 检测范围的最大y坐标，即最下方
//        protected int min_check_y = 1000;       // 检测范围的最小y坐标
//
        this.min_check_x = 500;        // 检测范围的第一个点最小x坐标
        this.max_check_x = 600;        // 检测范围的第一个点最小x坐标
//        protected int white_x = 5;              // 间隔线不能顶到屏幕的左右两边，所以得检测坐标一个点是不是白色
//
//        protected int time_out_length = 300;    // 超时时长，秒为单位

        this.line_content_heigth = 50; // 线与文字等内容之间的最小间距，为了优化查找功能
    }

    @Override
    public String doAction(Device device) {
        return super.doAction(device);
    }

    @Override
    protected boolean isAd(Device device, File file, int bottom, int top){
        String text = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 954, bottom - 85, 85, 40, false);
        logger.info("广告判断识别结果："+text);
        if(text.indexOf("广告") != -1 && text.indexOf("厂告") != -1) return true;
        text = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 954, bottom - 56, 60, 40, false);
        logger.info("广告判断识别结果："+text);
        if(text.indexOf("广告") != -1 && text.indexOf("厂告") != -1) return true;
        return false;
    }
}
