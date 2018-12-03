package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

public class ScreenCapAction extends Action {
//    private String name = "";
//    private boolean last = false;

    private String flag = "";

    public ScreenCapAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.SCREENCAP;
        this.tag = "screencap";
        if(param.length < 2) return;
//        name = param[1];
        if(param.length == 2){
            flag = param[1];
        }
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(device == null || device.task == null) return "";
        if(!CommonUtils.isNull(device.task.capscreenname))
            ADBUtils.copyScreenCap(device.task.capscreenname, device.serialnumber, device.getDeviceName(),device.task.name);
        else
            device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,device.getDeviceName(),
                    device.task.name);
        return device.task.capscreenname;
    }

}
