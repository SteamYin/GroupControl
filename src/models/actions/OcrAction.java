package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ADBUtils;
import utils.CommonUtils;
import utils.OcrUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrAction extends Action {
    private Point point1;
    private Point point2;
    private String regex;
    private String key;
    private String step1;
    private String step2;
    private boolean isdigits;
    private static Logger logger = LogManager.getLogger(OcrAction.class);
    //#图像识别，对当前图片进行识别，截取指定区域文字识别，进行正则匹配,匹配成功进入step1,否则进入step2，其中[key]可以为空
    //ocr x1,y1 x2,y2 "reg" [key] step1 step2
    public OcrAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.OCR;
        this.tag = "ocr";

        if(param.length < 5) return;
        isdigits = param[0].endsWith("digits");
           point1= new Point(param[1]);
        point2= new Point(param[2]);
        String s = "";
        int i;
        for(i = 3; i< param.length; i++){
            if(!CommonUtils.isNull(s)) s+= " ";
            s+= param[i];
            if(s.endsWith("\"")) break;
        }

        // 需去除前后的"
        regex = s.substring(1, s.length() - 1);
        i++;
        if(param.length < i+1) return;
        s = param[i];
        if(s.startsWith("[") && s.endsWith("]")){
            key = s.substring(1, s.length() - 1);
            i++;
        }
        if(param.length < i+1) return;
        step1 = param[i];
        if(param.length < i+2)
            step2 = "";
        else
            step2 = param[i+1];
    }

    @Override
    public String doAction(Device device) {
        super.doAction(device);

        if (device.task == null || CommonUtils.isNull(device.task.capscreenname)) return "";
        int x1 = point1.getX() < point2.getX() ? point1.getX() : point2.getX();
        int y1 = point1.getY() < point2.getY() ? point1.getY() : point2.getY();

        int x2 = point1.getX() < point2.getX() ? point2.getX() : point1.getX();
        int y2 = point1.getY() < point2.getY() ? point2.getY() : point1.getY();

        int w = x2 - x1;
        int h = y2 - y1;
        //String result = OcrUtils.doOrc(device.serialnumber+"_"+appid+"_"+x1, device.task.capscreenname, x1,y1,w, h);
        String result = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, x1,y1,w, h, isdigits);
        logger.info("识别结果：["+result+"] 正则表达式：\""+regex+"\"");
        if(CommonUtils.isNull(result)) return step2;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(result);
        //if(m.matches()){
        if(m.find()){
            if(!CommonUtils.isNull(key)) {
                logger.info("匹配结果："+ m.group());
                device.task.addKeyValue(key, m.group());
            }
            return step1;
        }
        else
            return step2;
    }

}
