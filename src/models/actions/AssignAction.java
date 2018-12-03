package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Words;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;
import utils.OcrUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssignAction extends Action {
    private int flag;   // 1:最大；2：最小
    private String key1;
    private String key2;
    private String step1;
    private String step2;
    private static Logger logger = LogManager.getLogger(AssignAction.class);
//    #从[xxx]中获取1最大；2最小值
//    assign 1 [xxx] [ddd] step1 step2
    public AssignAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.ASSIGN;
        this.tag = "assign";

        if(param.length < 5) return;
        flag = Integer.parseInt(param[1]);

        String s = param[2];
        key1 = s.substring(1, s.length() - 1);
        s = param[3];
        key2 = s.substring(1, s.length() - 1);

        step1 = param[4];
        if(param.length < 6)
            step2 = "";
        else
            step2 = param[5];
        logger.info("====flag: "+flag+"   key1:"+key1+"     key2:"+key2+"   step1:"+step1+"   step2:"+step2);
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);

        if (device.task == null) return "";

        Object o = device.task.getKeyValue(key1);
        if(o == null) {
            logger.info("-----null");
            return "";
        }
        if(o instanceof ArrayList){
            logger.info("-----size() == "+((ArrayList)o).size());
            if(((ArrayList)o).size() == 0){
                return "";
            }
            if(((ArrayList)o).get(0) instanceof Words){
                Words w = null;
                for (Words _w: (ArrayList<Words>)o) {
                    logger.info("------  _w="+_w.toString());
                    if(w == null) w = _w;
                    else if(flag == 1 && _w.isGreater(w)) w = _w;
                    else if(flag == 2 && w.isGreater(_w)) w = _w;
                }
                if(w != null) {
                    device.task.addKeyValue(key2, w);

                    logger.info("找到值:"+w.toString());
                    return step1;
                }
                return step2;
            }
        }
        return "";
    }

}
