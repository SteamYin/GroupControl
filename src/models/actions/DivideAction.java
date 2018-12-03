package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Words;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;
import utils.OcrUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DivideAction extends Action {
    private int direction;   // 1垂直切割；2：水平切割
    private Point point; // 起始块左上角坐标
    private int w;
    private int h;
    private int d;
    private int c;
    private String regex;
    private String key;
    private String step1;
    private String step2;
//    private String points;
    private boolean isdigits;
    private static Logger logger = LogManager.getLogger(DivideAction.class);
    //#切图识别，识别率更高 1表示垂直；2表示水平 x1,y1表示切图第一块的左上角，w表示每一小块的宽度,h表示小块高度，d表示间距；c表示块的个数
    // divide 1 x1,y1 w h d c "reg" [xxx@a] step1 step2
    public DivideAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType.DIVIDE;
        this.tag = "divide";

        if(param.length < 11) return;
        isdigits = param[0].endsWith("digits");
        direction = Integer.parseInt(param[1]);
           point= new Point(param[2]);
        w = Integer.parseInt(param[3]);
        h = Integer.parseInt(param[4]);
        d = Integer.parseInt(param[5]);
        c = Integer.parseInt(param[6]);
        String s = "";
        int i;
        for(i = 7; i< param.length; i++){
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

logger.info("====key:"+key+"   step1:"+step1+"   step2:"+step2);
    }
    @Override
    public String doAction(Device device) {
        super.doAction(device);

        if (device.task == null || CommonUtils.isNull(device.task.capscreenname)) return "";

        List<Words> list = new ArrayList<Words>();
        for(int i = 0; i < c; i ++){
            int x1 = point.getX()+(direction == 1 ? 0 : (i * d));
            int y1 = point.getY()+(direction == 1 ?  (i * d) : 0);

            String s = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, x1, y1, w, h, isdigits);
            if(CommonUtils.isNull(s)) continue;

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(s);
            if(m.matches()) {
                int x2 = x1 + w;
                int y2 = y1 + h;

                Words word = new Words(s, new Point(x1, y1), new Point(x2, y2));

                list.add(word);
                logger.info("匹配串：" + word.toString());
            }
        }
        if(list.size() > 0) {
            device.task.addKeyValue(key, list);

            logger.info("----------step1:"+step1);
            return step1;
        }
        else return step2;
    }

}
