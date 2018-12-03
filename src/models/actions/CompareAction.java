package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Words;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;

import java.util.ArrayList;

public class CompareAction extends Action {
    private int flag;   // 1:最大；2：最小
    private String key1;
    private String value1;
    private String key2;
    private String value2;

    private String step1;
    private String step2;
    private static Logger logger = LogManager.getLogger(CompareAction.class);
//    #比较大小,[xxx]大于等于xxx，进入step1，否则进入step2
//    compare [xxx] xxx step1 step2
    public CompareAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.COMPARE;
        this.tag = "compare";

        if(param.length < 4) return;

        String s = param[1];
        if(s.startsWith("[") && s.endsWith("]")) {
            key1 = s.substring(1, s.length() - 1);
            value1 = null;
        }
        else {
            key1 = null;
            value1 = s;
        }
        s = param[2];
        if(s.startsWith("[") && s.endsWith("]")){
            key2 = s.substring(1, s.length() - 1);
            value2 = null;
        }
        else {
            key2 = null;
            value2 = s;
        }
        step1 = param[3];
        if(param.length < 5)
            step2 = "";
        else
            step2 = param[4];
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);

        if (device.task == null) return "";

        String s1 = key1 == null ? value1 : device.task.getKeyValueString(key1);
        String s2 = key2 == null ? value2 : device.task.getKeyValueString(key2);

        if(CommonUtils.isNumber(s1) && CommonUtils.isNumber(s2)) return Integer.parseInt(s1) >= Integer.parseInt(s2) ? step1 : step2;
        return s1.compareTo(s2) >= 0 ? step1 : step2;
    }

}
