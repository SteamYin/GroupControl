package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

public class SwipeAction extends Action {
    private Point from = null;
    private Point to = null;
    private int duration_begin;
    private int duration_end;
    public SwipeAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.SWIPE;
        this.tag = "swipe";

        if(param.length < 4) return;
        from = new Point(param[1]);
        to = new Point(param[2]);
        duration_begin = Integer.parseInt(param[3]);
        if(param.length >= 5) duration_end = Integer.parseInt(param[4]);
        else  duration_end = 0;
    }

    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(device == null || device.task == null) return "";
        Point pointFrom = Point.ConvertPoint(from, flow.screen, device.screen);
        Point pointTo = Point.ConvertPoint(to, flow.screen, device.screen);
        if(pointFrom == null || pointTo == null) return "";
        int duration = duration_end == 0 ? duration_begin : CommonUtils.getRandomNum(duration_begin, duration_end);
        ADBUtils.swipeInput(device.serialnumber, pointFrom.getX(), pointFrom.getY(), pointTo.getX(), pointTo.getY(), duration);
        return "";
    }
}
