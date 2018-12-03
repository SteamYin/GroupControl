package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import utils.ADBUtils;
import utils.CommonUtils;

public class Action {
    public String name;      // 动作名称，缺省为空
    public int type;        // 动作类型
    public String tag;
    public String memo;     // 动作描述，备注
    public int stepid;      // 第几步
    public int trytimes;    // 执行次数
    protected Flow flow;

    public Action(String name, int stepid, String memo, Flow flow) {
        this.name = name;
        this.stepid = stepid;
        this.memo = memo;
        this.flow = flow;
        this.trytimes = 0;
    }

    public void resetTryTimes(){
        this.trytimes = 0;
    }

    public String doAction(Device device) {
        if (device == null || device.task == null) return "";
        // sleep之后截屏内容会改变
        if (this.type == Const.ActionType.SLEEP) device.task.capscreenname = "";

        trytimes++;

        if (this.type == Const.ActionType.SLEEP
                || this.type == Const.ActionType.LAUNCH
                || this.type == Const.ActionType.ASSIGN
                || this.type == Const.ActionType.COMPARE
  //              || this.type == Const.ActionType.SCREENCAP
                || this.type == Const.ActionType.GOTO)
            return "";

        // 只有截屏内容发生改变才进行截屏
        if (CommonUtils.isNull(device.task.capscreenname)
                && (flow.capscreen
                || this.type == Const.ActionType.OCR
                || this.type == Const.ActionType.POSITION
                || this.type == Const.ActionType.DIVIDE
                || this.type == Const.ActionType.RGB))
            device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,
                    device.getDeviceName(),
                    device.task.name,
                    this.type != Const.ActionType.OCR
                            && this.type != Const.ActionType.RGB
                            && this.type != Const.ActionType.POSITION
                            && this.type != Const.ActionType.DIVIDE);
        return "";
    }

    public void sleep(long len){
        try{
            Thread.sleep(len);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
