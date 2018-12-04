package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 跳转到某个流程的某个状态，流程id不填表示跳转到本流程的指定状态
 */
public class GotoAction extends Action {
    private String tostepname;  // 跳转到的步骤名称，步骤名称格式"[type:]name"
    public GotoAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.GOTO;
        this.tag = "goto";

        if(param.length < 2) return;
        tostepname = param[1];
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
        return this.tostepname;
    }

}
