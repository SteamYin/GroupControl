package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Rgb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ADBUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class _PlayDFTTAction extends Action {
    private String key;      // 保存下一页的位置
    private String step1;   // 读完并找到下一页的位置
    private String step2;   // 广告或者读完，没找到下一页，需返回到首页

    private static Logger logger = LogManager.getLogger(_ReadDFTTAction.class);
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
        // 记录开始时间
        long start = System.currentTimeMillis() / 1000;
        long pos = 0;       // 进度到1/10时的时刻
        while(true){
            if(!device.task.isRunning()) return step2;
            long now = System.currentTimeMillis() / 1000;
            if(now - start >= 600) return step2;    // 超出10分钟，需要返回到主流程
            sleep(3000);
            if(pos > 0) {
                if((now - start) >= (pos - start - 1) * 10){ // 播放结束
                    device.task.addKeyValue(key, new Point(540,1780));
                    return step1;
                }
            }else {
                device.task.capscreenname = ADBUtils.screenCap(device.serialnumber, device.getDeviceName(),
                        device.task.name, false);
                File file = new File(device.task.capscreenname);
                Rgb rgb = getPixelRgb(file, 108, 660);
                if(rgb.equals(new Rgb(244,75,80))){
                    pos = now;
                }
            }
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

}
