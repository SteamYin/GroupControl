package models.actions;

import common.Const;
import models.Device;
import models.Flow;
import models.Point;
import models.Words;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.ADBUtils;
import utils.CommonUtils;
import utils.OcrUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class _FindGroupAction extends Action {
    private String key;
    private String step1;
    private String step2;

    private String day = "";
    private Dictionary<String, String> dicCityGroups = null;
    private static Logger logger = LogManager.getLogger(_FindGroupAction.class);
    public _FindGroupAction(String name, int stepid, String memo, Flow flow, String[] param) {
        super(name, stepid, memo, flow);
        this.type = Const.ActionType._FINDGROUP;
        this.tag = "findgroup";

        if(param.length < 3) return;
        String s = param[1];
        // 除去[]
        key = s.substring(1, s.length() - 1);
        step1 = param[2];
        if(param.length < 3)
            step2 = "";
        else
            step2 = param[3];

        // 从文件中读取当天的城市组报名情况
        readCityGroup();
    }

    private void readCityGroup(){
        day = CommonUtils.date2String(new Date(), "yyyyMMdd");
        String file = Const.path_temp + day + ".txt";
        File f=new File(file);
        if(!f.exists()){
            logger.warn("流程脚步文件"+file+"不存在");
            day = "";
            return;
        }

        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(f), "GBK");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
            while ((s = br.readLine()) != null) {
                String [] ss = s.split(":");
                if(ss.length != 2) continue;

                if(dicCityGroups == null) dicCityGroups = new Hashtable<>();
                dicCityGroups.put(ss[0], ss[1]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeCityGroup(){
        if(CommonUtils.isNull(day)) return;
        if(dicCityGroups == null) return;
        if(!day.equals(CommonUtils.date2String(new Date(), "yyyyMMdd"))) return;

        String file = Const.path_temp + day + ".txt";
        try {
            File f=new File(file);

            FileOutputStream
                    out = new FileOutputStream(f);

            Enumeration<String > keys = dicCityGroups.keys();
            while(keys.hasMoreElements()) {
                String city = keys.nextElement();

                out.write((city+":"+dicCityGroups.get(city)+"\r\n").getBytes());
            }

            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public String doAction(Device device) {
        super.doAction(device);

//        device.task.addKeyValue("city", "");
        String city = "";
        // 最多只能滚5次
        for(int i = 0; i < 5; i++){
            // 1、滚动屏幕
            if(i > 0){
                logger.info("下一屏");
                if(device.screen.height == 1794)
                    ADBUtils.swipeInput(device.serialnumber, 600, 1725, 620, 157, 1000);
                else
                    ADBUtils.swipeInput(device.serialnumber, 600, 2000, 620, 184, 1000);
            }
            else{
                ADBUtils.swipeInput(device.serialnumber, 600,1480, 620,1222, 500);
            }
            sleep(300);
            // 2、截屏
            device.task.capscreenname = ADBUtils.screenCap(device.serialnumber,device.getDeviceName(),
                    device.task.name, false);

            logger.info("截屏："+device.task.capscreenname);
            device.task.controller.updateTaskShow(device.serialnumber, false);

            // 3、如果城市名为空，读取城市名称
            if(CommonUtils.isNull(city)){
                logger.info("开始识别城市名");
                city = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 800, 110, 995-800, 170-110, false);

                if(CommonUtils.isNull(city)){
                    return step2;
                }
                logger.info("识别出城市："+city);
            }

            // 4、定位第一行"本期报名"坐标
            Point _point = getPoint(device);
            if(_point == null) continue;
            logger.info("定位第一行\"本期报名\"坐标："+_point.toString());

            // 5、读取分组人数，找到满足条件的组
            Point point = new Point(315,_point.getY_begin() - 85);   // 报名人数的坐标
            int w = 110;
            int h = 72;
            int d = 201;
            //List<Words> list = new ArrayList<Words>();
            for(int j = 0; j < 9; j ++){
                int x1 = point.getX();
                int y1 = point.getY()+(j * d);
                if(y1 > device.screen.height) continue;

                String s = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, x1, y1, w, h, true);
                if (CommonUtils.isNull(s)) continue;
                logger.info("识别出报名人数：" + s);
                int count = CommonUtils.string2Int(s);
                if (count >= 20 || count == 0) continue;

                // 5、读取分组号，如果分组没有报名则返回分组坐标，否则继续找
                String ss = OcrUtils.doSingleLineOrc(device.serialnumber, device.task.capscreenname, 10, y1 + 85, city.length() == 2 ? 150 : 130, 45, false);
                if (CommonUtils.isNull(ss)) continue;
                logger.info("识别出组号：" + ss);
                ss = ss.replace("组", "");
                int groupno = CommonUtils.string2Int(ss);
                if(groupno == 0) continue;
                if(!checkCityGroup(city, groupno)) continue;

                int x2 = x1 + w;
                int y2 = y1 + h;

                Words word = new Words(s, new Point(x1, y1), new Point(x2, y2));
                device.task.addKeyValue(key, word);

                return step1;
            }
        }

        return step2;
    }


    private boolean checkCityGroup(String city, int group){
        if(CommonUtils.isNull(day)) day = CommonUtils.date2String(new Date(), "yyyyMMdd");
        if(dicCityGroups == null) dicCityGroups = new Hashtable<>();
        String groups = null;
        try{
            groups = dicCityGroups.get(city);

            if(!CommonUtils.isNull(groups)){
                String[] ss=groups.split(",");
                for(String g : ss){
                    if(Integer.parseInt(g) == group ) return false;
                }
                groups+= (","+group);
            }
            else            groups = group+"";

        }
        catch (Exception e){
            groups = group+"";
        }

        dicCityGroups.put(city, groups);
        writeCityGroup();
        return true;
    }


    /**
     * 获取本期得分左上角坐标
     * @param device
     * @return
     */
    private Point getPoint(Device device){
        int x1 = 280;
        int y1 = 220;
        int w = 465 - x1;
        int h = 500 - y1;
        String result = OcrUtils.doMultiLineOrc(device.serialnumber, device.task.capscreenname, x1,y1,w, h, false);
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
                    if(parseWord(_wo, x1, y1, h)){
                        return _wo.point1;
                    }

                    _wo = words;
                }
            }
        }
        if(parseWord(_wo, x1, y1, h)) return _wo.point1;
        return null;
    }

    private boolean parseWord(Words word, int x, int y, int h){
        if(word == null || CommonUtils.isNull(word.str)) return false;
        logger.info("----识别结果:"+word.str);
        Pattern p = Pattern.compile("本\\S{1,8}名");
        Matcher m = p.matcher(word.str);
        if(m.matches()) {
            Point point = new Point(word.point1, word.point2);
            // 将坐标原点从左下转换成左上
            point.zeroPointDown2Up(h);

            // 转换成原图坐标
            point.addPoint(new Point(x, y));

            word.point1 = new Point(point.getX_begin(), point.getY_begin());
            word.point2 = new Point(point.getX_end(), point.getY_end());
            return true;
        }
        return false;
    }
}
