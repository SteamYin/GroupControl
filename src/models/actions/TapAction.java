package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class TapAction extends Action {
    private int flag = 0;   // 类型 1表示精准坐标，2表示从后面几个随机一个坐标
    private List<Point> points ;
    public TapAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.TAP;
        this.tag = "tap";

        if(param.length < 3) return;
        flag = Integer.parseInt(param[1]);
        points = new ArrayList<>();
        for(int i = 2; i < param.length; i++){
            points.add(new Point(param[i]));
        }
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(device == null
                || flag<=0 || flag >= 3
                || points == null || points.size() == 0) return "";
        switch (flag){
            case 1: {
                Point point = points.get(0);
                if(point.isVariable()){
                    List<Point> list = getPoints(device.task.getKeyValueString(point.getKey()));
                    if(list == null) return "";
                    point = list.get(0);
                }
                point = Point.ConvertPoint(point, flow.screen, device.screen);
                if(point == null) return "";
                ADBUtils.tapInput(device.serialnumber, point.getX(), point.getY());
            }
            break;
            case 2:{
                if(points == null || points.size() == 0) return "";
                Point _point = points.get(0);
                List<Point> list = points;
                if(_point.isVariable()){
                    list = getPoints(device.task.getKeyValueString(_point.getKey()));
                    if(list == null) return "";
                }
                int index = CommonUtils.getRandomNum(0, list.size() - 1);
                Point point = Point.ConvertPoint(list.get(index), flow.screen, device.screen);
                if(point == null) return "";

                ADBUtils.tapInput(device.serialnumber, point.getX(), point.getY());
            }
            break;
            default:
                break;
        }
        return "";
    }

    private List<Point> getPoints(String points){
        if(CommonUtils.isNull(points)) return null;
        List<Point> list = new ArrayList<>();
        String[] arr = points.split(" ");
        for(int i = 0; i < arr.length; i++){
            list.add(new Point(arr[i]));
        }
        return list;
    }

}
