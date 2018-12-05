package models;

import com.alibaba.fastjson.JSONArray;
import common.Const;
import sample.Controller;
import utils.ADBUtils;
import utils.CommonUtils;
import utils.ConfigUtils;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class Device implements Comparable<Device> {
    public String serialnumber;
    public String alias;
    public String model;
    public String version;
    public String brand;
   // public FileSystem fileSystem;
    public Screen screen;

    public int status;      // 状态 0：掉线；1：正在对接终端；2：正常/空闲；3：执行任务
    public Controller controller;

    public int port = 0;
    public int appcount = 0;
    public Task task = null;

    private String day = "";

    private class FlowTimeLen{
        private int type;
        private int timelen;
        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getTimelen() {
            return timelen;
        }

        public void setTimelen(int timelen) {
            this.timelen = timelen;
        }
    }

    private List<FlowTimeLen> listFlowTimeLen = null;


    public Device(String serialnumber, boolean byconfig, Controller controller) {
        this.serialnumber = serialnumber;
        this.controller = controller;
        readDevice(byconfig);
    }

    /**
     * 获取设备信息
     * @param byconfig true: 从配置中读取; false: 通过指令从终端获取
     */
    private boolean readDevice(boolean byconfig) {
        if (byconfig) {
            String key = "Device_" + this.serialnumber;
            String value = ConfigUtils.getConfigValue(key);
            if (value == null) return false;
            String[] ss = value.split("\\|");
            if (ss == null || ss.length != 5) return false;
            this.alias = ss[0].equals("null") ? "" : ss[0];
            this.model = ss[1];
            this.version = ss[2];
            this.brand = ss[3];
            this.screen = new Screen(ss[4]);
            this.status = Const.DeviceStatus.OFFLINE;

            this.task = readTask();
            this.listFlowTimeLen = readFlowTimeLen();
            return true;
        } else {
            this.model = ADBUtils.getModel(serialnumber);
            this.version = ADBUtils.getVersion(serialnumber);
            this.brand = ADBUtils.getBrand(serialnumber);
            this.screen = ADBUtils.getScreen(serialnumber);
            setStatus(Const.DeviceStatus.CONNECTTION);
            this.listFlowTimeLen = readFlowTimeLen();
            return (this.model != null && this.version != null && this.brand != null);
        }
    }

    private List<FlowTimeLen> readFlowTimeLen(){
        return null;
    }

    private void writeFlowTimeLen(){

    }

    public int getFlowType() {
        return 0;
    }

    public void addFlowTimeLen(long time_lenght){
        
    }
    private Task readTask(){
        String key = "Task_" + this.serialnumber;
        String value = ConfigUtils.getConfigValue(key);
        if (value == null) return null;
        String[] ss = value.split("\\|");
        if(ss.length < 2) return null;
        /*
         flow.type
                +"|"+this.name
                +"|"+appids
                +"|"+appid_id
                +"|"+starttime
                +"|"+datafile;
         */
        return new Task(ss[1], this, Integer.parseInt(ss[0]) , ss.length>= 3 ? ss[2]:"", ss.length>= 4 ? ss[3]:"", ss.length>=5 ? ss[4]:"", controller);
    }

    public void setStatus(int status){
        this.status = status;

//        if(status == Const.DeviceStatus.CONNECTTION){
//            if(this.client != null) this.client.close();
//            if(port == 0) port = Const.base_port++;
//            this.client = new Client(port, this);
//        }
//        else if(status == Const.DeviceStatus.OFFLINE){
//            this.client = null;
//        }
    }

    public void setAppCount(int count){
        this.appcount = count;
        setStatus(Const.DeviceStatus.FREE);
        logMesg("连接成功！");
        if(controller != null){
            controller.updateDeviceShow(this.serialnumber);
        }
    }

    /**
     * 写设备信息到配置文件中
     */
    public void saveDevice(){
        // 设备的基本信息
        String value = (this.alias == null ? "" : this.alias)
                + "|" + (this.model == null ? "" : this.model)
                + "|" + (this.version == null ? "" : this.version)
                + "|" + (this.brand == null ? "" : this.brand)
                + "|" + (this.screen == null ? "0x0" : this.screen.width + "x" + this.screen.height);
        String key = "Device_" + this.serialnumber;
        ConfigUtils.setConfigValue(key, value);

        // 任务信息
        if(task == null) return;
        ConfigUtils.setConfigValue("Task_"+this.serialnumber, task.toString());
    }

    public String getStatusName(){
        switch (status){
            case Const.DeviceStatus.OFFLINE: return "离线";
            case Const.DeviceStatus.CONNECTTION: return "连接中...";
            case Const.DeviceStatus.FREE: return "空闲";
            case Const.DeviceStatus.BUSY: return "任务执行中";
        }
        return "未知";
    }

    public String getDeviceName(){
        return CommonUtils.isNull(alias) ? model : alias;
    }

    /**
     * 新增状态
     * @param task
     */
    public void addNewTask(Task task){
        this.task = task;
    }

    /**
     * 重启多开app
     */
    public void restartApp(){
        try {
//            ADBUtils.stopApp(serialnumber, Const.virtualapp_pkg);
//            Thread.sleep(1000);
            //ADBUtils.startApp(serialnumber, Const.virtualapp_pkg, Const.virtualapp_activity);
            //Thread.sleep(2000);
            this.appcount = 0;
//            setStatus(Const.DeviceStatus.CONNECTTION);

            setStatus(Const.DeviceStatus.FREE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void logMesg(String message){
        if(controller != null) controller.logMesg(getDeviceName()+":"+message);
    }
    @Override
    public int compareTo(Device s) {
        //自定义比较方法，如果认为此实体本身大则返回1，否则返回-1
        return this.getDeviceName().compareTo(s.getDeviceName());
    }
}
