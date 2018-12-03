package models.actions;

import common.Const;
import models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;
import utils.OcrUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionAction extends Action {
    private Point point1;
    private Point point2;
    private String regex;
    private String key;
    private String step1;
    private String step2;
    private String points;
    private boolean isdigits;
    private static Logger logger = LogManager.getLogger(PositionAction.class);
    //#图像识别，对当前图片进行识别，截取指定区域文字识别，进行正则匹配,匹配成功进入step1,否则进入step2，需要匹配到所有满足条件的区域
    // position x1,y1 x2,y2 "reg" [xxx] step1 step2
    public PositionAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.POSITION;
        this.tag = "position";

        if(param.length < 6) return;
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
        // 除去[]
        key = s.substring(1, s.length() - 1);
        i++;
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

        points = "";
        String result = OcrUtils.doMultiLineOrc(device.serialnumber, device.task.capscreenname, x1,y1,w, h, isdigits);
        logger.info("===="+isdigits);
        String [] ss = result.split("\n");
        Words _wo = null;
        for(String wo: ss){
            Words words = new Words(wo);
            if(_wo == null) _wo = words;
            else{
                if(_wo.isInLine(words, true)) _wo.addWords(words);
                else{
//                    listWords.add(_wo);
                    // 得到完整的一行_wo
                    parseWord(_wo, x1, y1, h);

                    _wo = words;
                }
            }
        }
        parseWord(_wo, x1, y1, h);

        device.task.addKeyValue(key, points);
        logger.info("----解析出坐标列表："+points);
        return CommonUtils.isNull(points) ? step2 : step1;
    }


    private boolean parseWord(Words word, int x, int y, int h){
        if(word == null || CommonUtils.isNull(word.str)) return false;
        logger.info("----识别结果:"+word.str);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(word.str);
        if(m.matches()) {
            Point point = new Point(word.point1, word.point2);
            // 将坐标原点从左下转换成左上
            point.zeroPointDown2Up(h);

            // 转换成原图坐标
            point.addPoint(new Point(x, y));

            // 将位置结果放入任务字典里
            //task.addKeyValue(key, point.toString());
            if(CommonUtils.isNull(points)) points = point.toString();
            else{
                points += " ";
                points += point.toString();
            }
            return true;
        }
        return false;
    }

}
