package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import utils.CommonUtils;

public class SleepAction extends Action {
    private int time_begin;
    private int time_end;


    public SleepAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.SLEEP;
        this.tag = "sleep";
        time_begin = 0;
        time_end = 0;
        if(param.length<2) return;
        String[] ss = param[1].split("-");
        time_begin = Integer.parseInt(ss[0]);
        if(ss.length > 1) time_end = Integer.parseInt(ss[1]);
    }

    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(device == null || device.task == null) return "";
        if(time_begin == 0) return "";

        int time = time_end == 0 ? time_begin : CommonUtils.getRandomNum(time_begin, time_end);
        try{
            Thread.sleep(time);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "";
    }
}
