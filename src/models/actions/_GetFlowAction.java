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

public class _GetFlowAction extends Action {
    private String key;      // 保存跳转流程id以及状态名
    private String step1;   // 读完并找到下一页的位置
    private String step2;   // 广告或者读完，没找到下一页，需返回到首页

    private static Logger logger = LogManager.getLogger(_GetFlowAction.class);
    public _GetFlowAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType._GET_FLOW;
        this.tag = "getflow";

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

        int type = device.getFlowType();
        if(type > 0){
            device.task.addKeyValue(key, type+"-read");
            return step1;
        }
        else return step2;
    }

}
