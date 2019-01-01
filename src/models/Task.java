package models;

import common.Const;
import models.actions.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sample.Controller;
import utils.CommonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Task implements Cloneable {
    public Controller controller;
    public String name;
    public int type;

    public String datafile;     // 任务所需要数据文件所保存路径
    public TaskData taskdata;   // 任务所需要的数据
    public int status;      // 任务状态
    public Date starttime;  // 任务启动时刻，用于定时启动任务
    public Date endtime;  // 任务启动时刻，用于定时启动任务

    // 流程执行参数
    public Device device;   // 作用于哪个设备

    public int stepid;      // 执行到第几步，从1开始

    public String capscreenname;    // 截屏保存的文件路径，用于图像识别

    public boolean running = true;

    public int try_times = 1;  // 运行次数

    private Dictionary<String, Object> dicKeyValue = null;

    private static Logger logger = LogManager.getLogger(Task.class);

    public Task(String name, Device device, int flowtype,
                String starttime, String endtime, String datafile, Controller controller){
        this.name = name;
        this.device = device;
        //this.flow = flow;
        this.type = flowtype;
        if(CommonUtils.isNull(starttime)) this.starttime = null;
        else this.starttime = CommonUtils.string2Date(starttime, "yyyy-MM-dd HH:mm:ss");
        if(CommonUtils.isNull(endtime)) this.endtime = null;
        else this.endtime = CommonUtils.string2Date(endtime, "yyyy-MM-dd HH:mm:ss");


        this.datafile = datafile;
        if(!CommonUtils.isNull(datafile)) taskdata = new TaskData(datafile);
        this.controller = controller;
        setStatus(Const.TaskStatus.STOP.getCode());

        this.running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                    if(!runTask()) {
                        try {
                            Thread.sleep(100);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private List<Integer> parseAppids(String appids){
        String [] ss = appids.split(",");
        if(ss == null || ss.length == 0) return null;
        List<Integer> list = new ArrayList<>();
        for (String s: ss){
            if(CommonUtils.isNull(s)) continue;
            String[] sss = s.split("-");
            if(sss == null) continue;
            else if(sss.length == 1) list.add(Integer.parseInt(s));
            else if(sss.length == 2){
                for(int i = Integer.parseInt(sss[0]); i<= Integer.parseInt(sss[1]); i++){
                    list.add(i);
                }
            }
        }
        return list;
    }

    private Flow getFlow(){
        return controller != null ?  controller.getFlow(type, device == null || device.screen == null ? "" : device.screen.toString()) : null;
    }
    private Flow getClearCacheFlow(){
        return controller != null ?  controller.getFlow(Const.flow_type_clear, device == null || device.screen == null ? "" : device.screen.toString()) : null;
    }


    public void reset() {
        this.try_times = 1;
    }
    public void setStatus(int status){
        // 用于重启virtualapp
        Flow flow = getFlow();
        if (status != Const.TaskStatus.RUNNING.getCode() && flow != null) {
        }
        if(status == Const.TaskStatus.RUNNING.getCode()) {
            device.setStatus(Const.DeviceStatus.BUSY);
        }
        else if(status == Const.TaskStatus.STOP.getCode() && device.status == Const.DeviceStatus.BUSY)
            device.setStatus(Const.DeviceStatus.FREE);

//        this.isrun = false;
        this.status = status;
    }

    public boolean isFinish(){
        return this.status == Const.TaskStatus.STOP.getCode();
    }


    public boolean runTask(){
        if(this.status != Const.TaskStatus.RUNNING.getCode()
                || device == null) return false;
        if(this.starttime != null && this.starttime.after(new Date())) return false;
        if(this.endtime != null && this.endtime.before(new Date())) return false;
        Flow flow = getFlow();
        if(flow == null || flow.listAction == null) return false;
//        int appid = getAppid();
//        if(appid <= 0) return false;

//        if(run_count >= flow.restart){
//            run_count = 0;
//            logMesg("清除缓存");
//            Flow flowClearCache = getClearCacheFlow();
//            if(flowClearCache != null){
//                Action action = flowClearCache.getNextAction(0);
//                while(action != null && this.status == Const.TaskStatus.RUNNING.getCode()){
//                    String stepName = action.doAction(device, appid);
//
//                    if(action.type == Const.ActionType.OCR
//                            || action.type == Const.ActionType.GOTO){
//                        action = flowClearCache.getActionByName(stepName);
//                    }
//                    else
//                        action = flowClearCache.getNextAction(action.stepid);
//                }
//            }
//
//            logMesg("重启多开应用...");
//            device.restartApp();
//            return false;
//        }
        if(device.status == Const.DeviceStatus.CONNECTTION) return false;
        else if(device.status == Const.DeviceStatus.FREE) device.setStatus(Const.DeviceStatus.BUSY);

//        if(appid <= device.appcount) {
//            device.client.unlockScreen();
            // 执行流程
            logMesg("应用流程[" + flow.memo + "]开始执行...");
            long start = System.currentTimeMillis() / 1000;
//            flow.resetTryTimes();
            dicKeyValue = null;    // 用于保存流程变量
            this.capscreenname = "";
            Action action = flow.getNextAction(0);
//            boolean runFail = false;
            while(action != null){
                if(this.status != Const.TaskStatus.RUNNING.getCode()) {
                    logMesg("应用流程[" + flow.memo + "]执行被暂停");
                    return true;
                }

//                if(action.trytimes >= flow.maxtrytimes) {
//                    logMesg("应用流程[" + flow.memo + "]执行超过最大尝试次数，停止执行！");
//
//                    action = flow.getActionByName("last");
////                    appidlist_fail.add(appid);
//                    runFail = true;
//                }
//                else
                if(flow.timeout > 0 && (System.currentTimeMillis() / 1000 - start) > flow.timeout){
                    logMesg("应用流程[" + flow.memo + "]执行超时，停止执行！");
                    //break;
                    start = System.currentTimeMillis() / 1000;
                    action = flow.getActionByName("timeout");
//                    runFail = true;
                }

                if(action == null) break;
                if (action.type != Const.ActionType.SLEEP)
                    logMesg("执行流程中第" + action.stepid + "步["+action.tag+"]" + (CommonUtils.isNull(action.memo)?"":(":"+action.memo)));
                stepid = action.stepid;

                long startAction = System.currentTimeMillis() / 1000;
                String stepName = action.doAction(device);
                if(action.type == Const.ActionType._READ_QTT
                    || action.type == Const.ActionType._PLAY_DFTT
                    || action.type == Const.ActionType._READ_DFTT
                    || action.type == Const.ActionType._READ_HTT
                        || action.type == Const.ActionType._READ_ZQKD
                        || action.type == Const.ActionType._READ_SHZX){
                    long endAction = System.currentTimeMillis() / 1000;
                    device.addFlowTimeLen(flow.type, endAction - startAction);
                }

                if (controller != null) controller.updateTaskShow(device.serialnumber, false);

//                if(runFail) break;

//                if(action.type == Const.ActionType.OCR
//                        || action.type == Const.ActionType.POSITION
//                        || action.type == Const.ActionType.GOTO
//                        || action.type == Const.ActionType.RGB
//                        || action.type == Const.ActionType.DIVIDE
//                        || action.type == Const.ActionType.ASSIGN
//                        || action.type == Const.ActionType.COMPARE
//                        || action.type == Const.ActionType._FINDGROUP){
                if(CommonUtils.isNull(stepName)) action = flow.getNextAction(action.stepid);
                else if(stepName.equals("__SUCCESS__")){
                    logMesg("应用流程执行成功!");
                    action = flow.getActionByName(stepName);
                    if(action == null) action = flow.getActionByName("last");
                }
                else if(stepName.equals("__FAIL__")){
//                        action = flow.getNextAction(action.stepid);
                    logMesg("应用流程执行失败!");
                    action = flow.getActionByName(stepName);
                    if(action == null) action = flow.getActionByName("last");
                }
//                else if(stepName.equals("__FAIL_LAST__")){
////                        action = flow.getNextAction(action.stepid);
//                    logMesg("应用流程执行失败!");
//                    action = flow.getActionByName(stepName);
//                    if(action == null) action = flow.getActionByName("last");
//                }
                else {
                    String[] fs = stepName.split("-");
                    if(fs.length == 1) action = flow.getActionByName(stepName);
                    else{
                        // 跳转到其他流程的指定状态下
                        int _type = Integer.parseInt(fs[0]);
                        if(_type != type) {

                            if(_type == 1 &&
                                    (type == 3 || type == 6 || type == 7)){
                                long endAction = System.currentTimeMillis() / 1000;
                                device.addFlowTimeLen(type, endAction - start);
                            }

                            type = _type;
                            // 流程变了，重新开始计时
                            logMesg("跳转到流程["+type+"]!");
                            flow = getFlow();
                            if (flow == null || flow.listAction == null) return false;

                            start = System.currentTimeMillis() / 1000;
                        }
                        action = flow.getActionByName(fs[1]);
                    }
                }
//                }
//                else
//                    action = flow.getNextAction(action.stepid);

            }
            logMesg("应用流程[" + flow.memo + "]执行结束，耗时" + (System.currentTimeMillis() / 1000 - start) + "秒");
//        }
//        if(this.appid_id > this.appidlist.size()){ // 执行结束
//            // 判断失败列表是否为空
//            if(appidlist_fail == null || appidlist_fail.size() == 0
//                || try_times > 3){
//                setStatus(Const.TaskStatus.STOP.getCode());
//                logMesg("任务["+name+"]执行结束");
//
//                // 写成功失败日志
//                saveRunResult();
//            }
//            else{
//                // 将失败的放入appidlist重做
//                appidlist.clear();
//                appidlist.addAll(appidlist_fail);
//                appidlist_fail.clear();
//                appid_id = 1;
//                try_times++;
//                run_count = flow.restart;
//            }
//        }
        if(controller != null) {
            controller.updateTaskShow(device.serialnumber, false);
            controller.saveConfig();
        }
        return true;
    }

    public void close(){
        this.running = false;
    }

    public boolean isRunning(){
        return this.running;
    }
    /**
     * 用于保存到配置
     * @return
     */
    public String toString(){
        Flow flow = getFlow();
        if(device == null
                || flow == null || flow.listAction == null) return "";

        return flow.type
                +"|"+this.name
                +"|"+CommonUtils.date2String(this.starttime, "yyyy-MM-dd HH:mm:ss")
                +"|"+CommonUtils.date2String(this.endtime, "yyyy-MM-dd HH:mm:ss")
                +"|"+datafile;
    }

    public String getText( String field){
        if(taskdata == null) return null;
        return taskdata.getData(field);
    }

    public void addKeyValue(String key, Object value){
        if(dicKeyValue == null) dicKeyValue = new Hashtable<>();
        String [] k = key.split("@");
        Boolean append = false;
        if(k.length == 2 && (k[1].equals("a"))) append = true;
        String _key = k[0];

        if(!append){
            dicKeyValue.put(_key, value);
        }
        else {
            Object _value = getKeyValue(_key);
            if (_value == null) dicKeyValue.put(_key, value);
            else if (_value instanceof String) {
                String __value = _value.toString();
                __value += value.toString();
                dicKeyValue.put(_key, __value);
            }
            else if(_value instanceof Words){
                Words __value = (Words)_value;
                __value.addWords((Words)value);
                dicKeyValue.put(_key, __value);
            } else if (_value instanceof ArrayList) {
                if (((List) _value).size() == 0) {
                    dicKeyValue.put(_key, value);
                } else if (((List) _value).get(0) instanceof Words) {
                    List<Words> __value = (List<Words>) _value;
                    __value.addAll((List<Words>) value);
                    dicKeyValue.put(_key, __value);
                } else {
                    dicKeyValue.put(_key, value);
                }
            }
        }
    }

    public Object getKeyValue(String key){
        if(dicKeyValue == null || dicKeyValue.isEmpty()) {
            return null;
        }
        try{
            return dicKeyValue.get(key);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public String getKeyValueString(String key){
        Object o = getKeyValue(key);
        if(o instanceof String) return o.toString();
        else if(o instanceof Words){
            Words w = (Words)o;
            Point point = new Point(w.point1, w.point2);
            return point.toString();
        } else if(o instanceof Point) {
            Point point = (Point)o;
            return point.toString();
        }
        return "";
    }

    public int getFlowStep(){
        Flow flow = getFlow();
        return flow == null || flow.listAction == null ? 0 : flow.listAction.size();
    }
    public int getFinishStep(){return stepid;}
    private void logMesg(String message){
//        if(controller != null) controller.logMesg(device.getDeviceName()+":"+message);
        logger.info("["+device.getDeviceName()+"] "+message);
    }

//    private void saveRunResult(){
//        final String path =  Const.path_log + CommonUtils.date2String(new Date(), "yyyyMMdd") + "/";
//
//        File file = new File(path);
//        file.mkdirs();
//
//        try {
//            FileOutputStream
//                    out = new FileOutputStream(new File( path+ device.getDeviceName()+"_"+this.name + ".txt"));
//            out.write(("设备："+device.getDeviceName()+"\r\n").getBytes());
//            out.write(("任务名称："+name+"\r\n").getBytes());
//            out.write(("任务类型："+controller.getFlowName(type)+"\r\n").getBytes());
//            if(appidlist_back != null && appidlist_back.size() > 0){
//                String success = "";
//                for(Integer appid: appidlist_back){
//                    if(!CommonUtils.isNull(success)) success += ",";
//                    success += appid;
//                }
//                out.write(("执行appid列表："+success+"\r\n").getBytes());
//            }
//            if(appidlist_success != null && appidlist_success.size() > 0){
//                String success = "";
//                for(Integer appid: appidlist_success){
//                    if(!CommonUtils.isNull(success)) success += ",";
//                    success += appid;
//                }
//                out.write(("成功appid列表："+success+"\r\n").getBytes());
//            }
//            String fail = "";
//            if(appidlist_fail != null && appidlist_fail.size() > 0){
//                for(Integer appid: appidlist_fail){
//                    if(!CommonUtils.isNull(fail)) fail += ",";
//                    fail += appid;
//                }
//            }
//            if(appidlist_fail_last != null && appidlist_fail_last.size()>0){
//                for(Integer appid: appidlist_fail_last){
//                    if(!CommonUtils.isNull(fail)) fail += ",";
//                    fail += appid;
//                }
//            }
//            if(!CommonUtils.isNull(fail)) out.write(("失败appid列表："+fail+"\r\n").getBytes());
//            out.close();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return (Task)super.clone();
    }
}
