package models;


import common.Const;
import models.actions.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Flow {
    public int type;    // 流程类型
    public String name; // 流程名称

    public List<Action> listAction;
    private String mobile;   // 流程配置时所使用的手机型号
    public Screen screen;       // 配置手机屏幕宽度x高度
    public String memo;     // 流程说明
    private int restart;     // 表示每运行restart个app后重启多开应用
    public boolean capscreen = false;   // 是否每一步操作之前做一次截屏
    public int maxtrytimes = 3;     // 表示最大尝试次数，即每一步操作次数不能超过指定数
    public int timeout = 0;         // 超时时间，秒为单位
    private static Logger logger = LogManager.getLogger(Flow.class);

    public Flow(int type, String scriptfile, String screensize){
        this.type = type;
        try {
            String file = Const.path_script;
            if(!CommonUtils.isNull(screensize)) file += (screensize+"/");
            file += scriptfile;
            File f=new File(file);
            if(!f.exists()){
//                System.out.println("流程脚步文件"+file+"不存在");
                logger.warn("流程脚步文件"+file+"不存在");
                return;
            }

            InputStreamReader reader = new InputStreamReader(new FileInputStream(f), "GBK");
            BufferedReader br = new BufferedReader(reader);
            String s = "";
            String actionMemo = "";
            int stepid = 1;
            listAction = new ArrayList<>();

            while((s=br.readLine())!=null){
                s = s.trim();
                if(s.equals("")){
                    actionMemo = "";
                }
                else if(s.indexOf("#") == 0) {
                    actionMemo = s.substring(1);
                }
                else{
                    String name = "";
                    String [] sss = s.split(":");
                    if(sss.length == 1){
                        name = "";
                    }
                    else if(sss.length == 2){
                        name = sss[0];
                        s = sss[1];
                    }
                    String [] ss = s.split(" ");
                    if(ss[0].equals("memo")){
                        this.memo = s.substring(5);
                        actionMemo = "";
                    }
                    else if(ss[0].equals("restart")){
                        this.restart = Integer.parseInt(ss[1]);
                    }
                    else if(ss[0].equals("mobile")){
                        this.mobile =  s.substring(7);
                        actionMemo = "";
                    }
                    else if(ss[0].equals("screen")){
                        this.screen = new Screen(s.substring(7));
                        actionMemo = "";
                    }
                    else if(ss[0].equals("capscreen")){
                        this.capscreen = ss.length >= 2 && ss[1].equals("true");
                        actionMemo = "";
                    }
                    else if(ss[0].equals("maxtrytimes")){
                        this.maxtrytimes = ss.length >= 2 ? Integer.parseInt(ss[1]) : 3;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("timeout")){
                        this.timeout = ss.length >= 2 ? Integer.parseInt(ss[1]) : 0;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("launch")){
                        listAction.add(new LaunchAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("kill")){
                        listAction.add(new KillAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("sleep")){
                        listAction.add(new SleepAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("tap")){
                        listAction.add(new TapAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("swipe")){
                        listAction.add(new SwipeAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("text")){
                        listAction.add(new TextAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("screencap")){
                        listAction.add(new ScreenCapAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("keyevent")){
                        listAction.add(new KeyEventAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("goto")){
                        listAction.add(new GotoAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].startsWith("ocr")){
//                    else if(ss[0].equals("ocr")){
                        listAction.add(new OcrAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("rgb")){
                        listAction.add(new RgbAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
//                    else if(ss[0].equals("position")){
                    else if(ss[0].startsWith("position")){
                        listAction.add(new PositionAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("assign")){
                        listAction.add(new AssignAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("divide")){
                        listAction.add(new DivideAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }


                    else if(ss[0].equals("compare")){
                        listAction.add(new CompareAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("_readqtt")){
                        listAction.add(new _ReadQTTAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("_readdftt")){
                        listAction.add(new _ReadDFTTAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("_playqtt")){
                        listAction.add(new _PlayQTTAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else if(ss[0].equals("_playdftt")){
                        listAction.add(new _PlayDFTTAction(name, stepid, actionMemo,this, ss));
                        stepid++;
                        actionMemo = "";
                    }
                    else{
//                        System.out.println("解析不了："+s);
                        logger.warn("解析不了："+s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Action getNextAction(int stepid){
        if(listAction == null || listAction.size() == 0 || listAction.size() <= stepid) return null;
        if(stepid <= 0) return listAction.get(0);

        return listAction.get(stepid);
    }

    public Action getActionByName(String name){
        if(listAction == null || listAction.size() == 0 || CommonUtils.isNull(name)) return null;
        logger.info("进入状态名:"+name);
        for(Action action: listAction){
            if(CommonUtils.isNull(action.name)) continue;
            if(name.equals(action.name)) return action;
        }
        return null;
    }

//    public void resetTryTimes(){
//        for(Action action: listAction){
//            action.resetTryTimes();
//        }
//    }
}
