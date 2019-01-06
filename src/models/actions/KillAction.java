package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import utils.ADBUtils;

public class KillAction extends Action {

    private int app_type;
    private String package_name;
    public KillAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.LAUNCH;
        this.tag = "kill";
        app_type = 1;
        if(param.length <= 1) return;
        app_type = Integer.parseInt(param[1]);
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
//        if(device == null || device.client == null) return "";
//        device.client.sendData("LAUNCHAPP:"+appid);
        if(app_type == 1)
            ADBUtils.stopApp(device.serialnumber, Const.qtt_pkg);
        else if(app_type == 2)
            ADBUtils.stopApp(device.serialnumber, Const.dftt_pkg);
        else if(app_type == 3)
            ADBUtils.stopApp(device.serialnumber, Const.htt_pkg);
        else if(app_type == 4)
            ADBUtils.stopApp(device.serialnumber, Const.zqkd_pkg);
        else if(app_type == 5)
            ADBUtils.stopApp(device.serialnumber, Const.shzx_pkg);
        else if(app_type == 6)
            ADBUtils.stopApp(device.serialnumber, Const.qktx_pkg);
        else if(app_type == 7)
            ADBUtils.stopApp(device.serialnumber, Const.hstt_pkg);
        return "";
    }

}
