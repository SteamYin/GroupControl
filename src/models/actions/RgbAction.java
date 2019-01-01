package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Rgb;
import utils.CommonUtils;
import utils.ImageUtils;

public class RgbAction extends Action {
    private Point point;
    private Rgb rgb;
    private String step1;
    private String step2;

    // rgb x,y r,g,b stepid1 stepid 2
    public RgbAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.RGB;
        this.tag = "rgb";

        if(param.length < 4) return;
        point= new Point(param[1]);
        rgb = new Rgb(param[2]);
        step1 = param[3];
        step2 = param.length > 4 ? param[4] : "";
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if (device.task == null || CommonUtils.isNull(device.task.capscreenname)) return "";
        int pixel = ImageUtils.getPixel( device.task.capscreenname, point.getX(), point.getY());
//        if(pixel == -1) return step2;
        Rgb _rgb = new Rgb(pixel);
        return this.rgb.equals(_rgb) ? step1 : step2;
    }

}
