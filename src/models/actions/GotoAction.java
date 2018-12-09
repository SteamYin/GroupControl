package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

import javax.swing.plaf.TextUI;
import java.util.ArrayList;
import java.util.List;

/**
 * 跳转到某个流程的某个状态，流程id不填表示跳转到本流程的指定状态
 */
public class GotoAction extends Action {
    private String key;
    private String tostepname;  // 跳转到的步骤名称，步骤名称格式"[type:]name"
    public GotoAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.GOTO;
        this.tag = "goto";

        if(param.length < 2) return;
        String s = param[1];
        if(s.startsWith("[") && s.endsWith("]")){
            this.key = s.replace("[","").replace("]","");;
        }
        else{
            tostepname = param[1];
        }
    }

    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(CommonUtils.isNull(key)) return this.tostepname;
        else return device.task.getKeyValueString(key);
    }

}
