package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class KeyEventAction extends Action {
    private int keyevent = 0;
    public KeyEventAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.KEYEVENT;
        this.tag = "keyevent";

        if(param.length < 2) return;
        keyevent = Integer.parseInt(param[1]);
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(device == null || device.task == null) return "";
        ADBUtils.keyeventInput(device.serialnumber, keyevent);
        return "";
    }

}
