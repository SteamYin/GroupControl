package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import utils.ADBUtils;
import utils.CommonUtils;

public class TextAction extends Action {
    private Point point = null;
    private String text = "";

    public TextAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.TEXT;
        this.tag = "text";

        if(param.length < 3) return;
        point = new Point(param[1]);
        for(int i = 2; i< param.length; i++){
            if(!CommonUtils.isNull(text)) text += " ";
            text += param[i];
        }
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);
        if(device == null || device.task == null) return "";

        String _text = text;
        if(text.substring(0,1).equals("[")
                && text.substring(text.length()-1).equals("]")){
            _text = device.task.getText(text.substring(1, text.length()-1));
        }
        String __text = _text;
        String [] ss = _text.split("\\|");
        if(ss.length > 1){
            int index = CommonUtils.getRandomNum(0, ss.length - 1);
            __text = ss[index];
        }
        ADBUtils.textInput(device.serialnumber, point.getX(), point.getY(), __text);
        return "";
    }

}
