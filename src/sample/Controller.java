package sample;

import common.Const;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.*;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    @FXML
    private Button btnSwitchTask;
    @FXML
    private ListView lvDevice;
    @FXML
    private ListView lvLog;
    @FXML
    private Label labelAlias;
    @FXML
    private Label labelModel;
    @FXML
    private Label labelVersion;
    @FXML
    private Label labelScreen;
    @FXML
    private Label labelStatus;
    @FXML
    private Label labelAppCount;
    @FXML
    private Label labelTaskName;
    @FXML
    private Label labelTaskProcess;
    @FXML
    private Label labelFlowProcess;
    @FXML
    private Label labelTaskStatus;
    @FXML
    private Label labelAppRange;
    @FXML
    private Label labelAppId;
    @FXML
    private ImageView imageView;
    @FXML
    private Label labelStartTime;
    @FXML
    private Label labelEndTime;

    private Dictionary<String, Device> dicDevice = null;
    private Dictionary<String, Flow> dicFlow = null;
    private String currentSerialNumber = "";
    private Timer timer = new Timer();
    private Dictionary<Integer, FlowType> dicFlowType = null;
    private String imagePath = "";
    private Timer timerImage = new Timer();
    private ArrayDeque<String> arrayImagePath = new ArrayDeque<>();

    private Logger logger = LogManager.getLogger(Controller.class);

    public void init() {
        // 先加载流程类型
        loadFlowType();
        // 加载流程配置
        loadFlow();

        // 初次打开，需要从配置中获取设备信息
        if (dicDevice == null) {
            dicDevice = new Hashtable<>();
            String devices = ConfigUtils.getConfigValue("Devices");
            if (devices != null && !devices.equals("")) {
                String[] ss = devices.split(",");
                for (String sn : ss) {
                    Device dev = new Device(sn, true, this);
                    dicDevice.put(sn, dev);
                }
                updateDeviceListView();
                updateDeviceShow();
            }
        }

        // 每10秒执行一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkDevices()) {
                    updateDeviceListView();
                    updateDeviceShow();
                }
            }
        }, 2000, 10000);


        // 图片展示
        timerImage.schedule(new TimerTask() {
            @Override
            public void run() {
               // if (!imageChanged) return;

//               // if (CommonUtils.isNull(imagePath)) {
//                    imageView.setImage(null);
//                }
//                else {
                if(!arrayImagePath.isEmpty()){
                    try {
                        String path = arrayImagePath.getFirst();
                        if (!CommonUtils.isExists(path)) return;
                        arrayImagePath.removeFirst();
                        File file = new File(path);
                        // don"t load in the background
                        Image localImage = new Image(file.toURI().toURL().toString(), false);
                        imageView.setImage(localImage);
//                        System.out.println("加载图片："+imagePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    imageChanged = false;
                }
            }
        }, 200, 500);

        lvDevice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                currentSerialNumber = newValue;
                updateDeviceShow();
                //  System.out.println("Window Size Change:" + oldValue + "," + newValue);
            }
        });
    }

    public void uninit(){
        // 1、停止timer
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        if(timerImage != null){
            timerImage.cancel();
            timerImage = null;
        }

        // 2、关闭socket
//        Enumeration<String > keys = dicDevice.keys();
//        while(keys.hasMoreElements()){
//            String key = keys.nextElement();
//            Device dev = dicDevice.get(key);
//            if(dev != null && dev.client != null) {
//                dev.client.close();
//            }
//            if(dev != null && dev.task != null) {
//                dev.task.close();
//            }
//        }

        // 3、保存配置
        saveConfig();
    }

    public void setImagePath(String image){
//        imagePath = image;
//        imageChanged = true;
        if(!CommonUtils.isNull(image)) {
            if(imagePath.equals(image)) return;
            imagePath = image;

            arrayImagePath.addLast(image);
            logger.info(Thread.currentThread().getId()+"路径："+image+"加入图片显示队列");
        }
    }
    @FXML
    public void onButtonClick(ActionEvent event) {
        ObservableList<String> strList = FXCollections.observableArrayList();
        Enumeration<String > keys = dicDevice.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            strList.add(key);
        }
//        ListView<String> listView = new ListView<>(strList);
        lvDevice.setItems(strList);
//        lvDevice.setPrefSize(400, 200);
        lvDevice.setCellFactory(e -> new ColorCell());
        lvDevice.setEditable(true);

//        Label label = new Label("...");
//        label.textProperty().bind(lvDevice.getSelectionModel().selectedItemProperty());
//        label.setLayoutY(200);
//        root.getChildren().add(label);

    }

    class ColorCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if(item == null || dicDevice.get(item) == null) return;

            VBox box = new VBox();
            if(box != null){
                Device device = dicDevice.get(item);

                // model
                Label lableModel = new Label();
                lableModel.setText(device.alias == null || device.alias.equals("") ? device.model : device.alias);
                Font  font = lableModel.getFont();
                lableModel.setFont(Font.font(font.getFamily(), FontWeight.BOLD, 15));
                lableModel.setTextFill(device.status == Const.DeviceStatus.OFFLINE ? Color.GRAY : Color.BLACK);
                box.getChildren().add(lableModel);

                // version
                Label lableVersion = new Label();
                lableVersion.setText("Android: "+device.version);
                lableVersion.setTextFill(device.status == Const.DeviceStatus.OFFLINE ? Color.GRAY : Color.BLACK);
                box.getChildren().add(lableVersion);

                // screen
                Label lableScreen = new Label();
                lableScreen.setText(device.screen.width+"x"+device.screen.height);
                lableScreen.setTextFill(device.status == Const.DeviceStatus.OFFLINE ? Color.GRAY : Color.BLACK);
                box.getChildren().add(lableScreen);

                box.setUserData(device);

                setGraphic(box);
            }
            else{
                setGraphic(null);
            }

//            Rectangle rect = new Rectangle(100, 100);
//            if (item != null) {
//                rect.setFill(Color.web(item));
//                setGraphic(rect);
//            } else {
//                setGraphic(null);
//            }
        }
    }

    public synchronized void logMesg(String message){
        logger.info(message);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ObservableList<String> strList = lvLog.getItems();
                if (strList.size() >= 100) strList.remove(0);
                strList.add(CommonUtils.date2String(new Date(), "MM-dd HH:mm:ss")+"  "+message);
                lvLog.setItems(strList);
                lvLog.scrollTo(strList.size() - 1);
            }
        });
    }

    /**
     * 用于定期检查设备列表
     * @return true:表示发生改变; false: 未发生改变
     */
    private boolean checkDevices(){
        boolean changed = false;
        boolean saveconfig = false;
        // 1、初次打开，需要从配置中获取
        if(dicDevice == null){
            dicDevice = new Hashtable<>();
            String devices = ConfigUtils.getConfigValue("Devices");
            if(devices != null && !devices.equals("")){
                String [] ss = devices.split(",");
                for (String sn: ss) {
                    Device dev = new Device(sn, true, this);
                    dicDevice.put(sn, dev);
                }
                changed = true;
            }
        }

        // 2、获取在线设备
        List<String> listSN = ADBUtils.getDevices();
        if(listSN != null && listSN.size() > 0){
            for(String sn: listSN){
                Device dev = dicDevice.get(sn);
                if(dev == null){
                    dev = new Device(sn, false, this);
                    dicDevice.put(sn, dev);
                    changed = true;
                    saveconfig = true;
                }
                else{
                    if(dev.status == Const.DeviceStatus.OFFLINE){
                        dev.setStatus(Const.DeviceStatus.CONNECTTION);
                        changed = true;
                    }
//                    if(dev.appcount == 0 && dev.client != null) dev.client.sendData("GETAPPCOUNT");
                }
            }
        }

        // 3、检查是否存在线设备掉线
        Enumeration<String > keys = dicDevice.keys();
        while(keys.hasMoreElements()) {
            String sn = keys.nextElement();
            if(currentSerialNumber == null || currentSerialNumber.equals("")) currentSerialNumber = sn;
            Device dev = dicDevice.get(sn);
            if(dev == null || dev.status == Const.DeviceStatus.OFFLINE) continue;
            // 字典里的设备是在线状态的
            if(listSN.contains(sn)) continue;
            // sn掉线了
            dev.setStatus(Const.DeviceStatus.OFFLINE);
            changed = true;
            if(dev.task != null) dev.task.close();
        }

        // 保存到配置文件
        if(saveconfig) saveConfig();

        return changed;
    }

    private void updateDeviceListView(){
        List<Device> list = new ArrayList<>();
        Enumeration<String > keys = dicDevice.keys();
        while(keys.hasMoreElements()){
            String key = keys.nextElement();
            list.add(dicDevice.get(key));
        }
        Collections.sort(list);
        ObservableList<String> strList = FXCollections.observableArrayList();

        for(Device device: list){
            strList.add(device.serialnumber);
        }
//        ListView<String> listView = new ListView<>(strList);
        lvDevice.setItems(strList);
//        lvDevice.setPrefSize(400, 200);
        lvDevice.setCellFactory(e -> new ColorCell());
    }

    public void updateDeviceShow(String sn){
        if(sn.equals(currentSerialNumber)) updateDeviceShow();
    }

    private void updateDeviceShow() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Device dev = dicDevice.get(currentSerialNumber);
                if(dev == null) return;
                labelAlias.setText(dev.alias);
                labelModel.setText(dev.model);
                labelVersion.setText("Android:" + dev.version);
                labelScreen.setText(dev.screen.width + "x" + dev.screen.height);
                labelStatus.setText(dev.getStatusName());
                labelAppCount.setText(dev.appcount + "");
            };
        });
        updateTaskShow(currentSerialNumber, true);
    }

    public void updateTaskShow(String sn, Boolean changed){
        if(!sn.equals(currentSerialNumber)) return;
        try {
            Device dev = dicDevice.get(sn);
            if(dev == null || dev.task == null) return;

            final Task task = (Task)dev.task.clone();
//
//            System.out.println(dev.task.+"=============");
//            System.out.println(task+"=============");
            if (task == null) return;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    labelTaskName.setText(CommonUtils.cutString(task.name + "[" + dicFlowType.get(task.type).toString() + "]", 20));
//                    labelTaskProcess.setText(CommonUtils.cutString(task.getFinishCount() + "/" + task.getAPPCount(), 20));
                    labelFlowProcess.setText(CommonUtils.cutString(task.getFinishStep() + "/" + task.getFlowStep(), 20));
                    labelTaskStatus.setText(CommonUtils.cutString(Const.TaskStatus.getStringByCode(task.status), 20));
                    labelStatus.setText(CommonUtils.cutString(dev.getStatusName(), 20));
//                    labelAppRange.setText(CommonUtils.cutString(task.appids, 20));
                    labelStartTime.setText(CommonUtils.cutString(CommonUtils.date2String(task.starttime, "yyyy-MM-dd HH:mm:ss"), 20));
                    labelEndTime.setText(CommonUtils.cutString(CommonUtils.date2String(task.endtime, "yyyy-MM-dd HH:mm:ss"), 20));
                    //labelAppId.setText((task.appid > task.appid_end ? task.appid_end : task.appid)+"");
//                    labelAppId.setText(CommonUtils.cutString(task.getAppid() + "", 20));

                    btnSwitchTask.setText(CommonUtils.cutString(task.status == Const.TaskStatus.STOP.getCode() ? "启动任务" : "暂停任务", 20));
                    if (changed) {
                        arrayImagePath.clear();
                        imageView.setImage(null);
                    }
                    if (!CommonUtils.isNull(task.capscreenname))
                        setImagePath(task.capscreenname);
                }

            });
        }catch (Exception e){

        }
    }

    public String getFlowName(int type){
        FlowType ft = dicFlowType.get(type);

        return ft == null ? "" : ft.toString();
    }
    public void saveConfig() {
        Enumeration<String> keys = dicDevice.keys();
        String devices = "";
        while (keys.hasMoreElements()) {
            String sn = keys.nextElement();
            Device dev = dicDevice.get(sn);
            if (dev == null) continue;

            dev.saveDevice();
            if (!devices.equals("")) devices += ",";
            devices += sn;
        }
        ConfigUtils.setConfigValue("Devices", devices, true);
    }

    public void loadFlowType(){
        int flowTypeCount = Integer.parseInt(ConfigUtils.getConfigValue("FlowCount"));
        if(flowTypeCount == 0) return;
        dicFlowType =  new Hashtable<>();
        for(int i = 1; i <= flowTypeCount; i++){
            String flowType = ConfigUtils.getConfigValue("Flow_"+i);
            if(CommonUtils.isNull(flowType)) continue;
            FlowType ft = new FlowType(flowType);
            if(ft.getCode() == 0) continue;
            if(ft.getCode() == Const.flow_type_clear){
                logMesg("流程id["+Const.flow_type_clear+"]为系统保留id，不能使用");
                continue;
            }
            dicFlowType.put(ft.getCode(), ft);
        }
        // 清除缓存系统默认
        dicFlowType.put(Const.flow_type_clear, new FlowType(Const.flow_type_clear, "缓存清除", "clear_cache.txt", false));
    }

    public void loadFlow(){
        List<String> listScreenSize = new ArrayList<>();
        File file = new File(Const.path_script);
        File[] fs = file.listFiles();
        for(File f: fs){
            if(!f.isDirectory()) continue;
            listScreenSize.add(f.toString().replace(Const.path_script, "").replace("/", ""));
        }

        dicFlow = new Hashtable<>();
        Enumeration<Integer> keys = dicFlowType.keys();
        while(keys.hasMoreElements()){
            int key = keys.nextElement();
            FlowType flowType = dicFlowType.get(key);

        //for (Const.FlowType flowType : Const.FlowType.values()) {
            // 基础的流程
            dicFlow.put(flowType.getCode()+"", new Flow(flowType.getCode(), flowType.getScript(), null));

            // 指定屏幕尺寸的流程
            if(listScreenSize.size() == 0) continue;
            for(String screen: listScreenSize){
                Flow flow = new Flow(flowType.getCode(), flowType.getScript(), screen);
                if(flow == null || flow.listAction == null || flow.listAction.size() == 0) continue;
                dicFlow.put(flowType.getCode()+"_"+screen, flow);
            }

            logMesg("加载["+flowType.toString()+"]流程脚本成功");
        }
    }

    public Flow getFlow(int type, String screensize){
        if(dicFlow == null) return null;
        String key = type +"";
        if(!CommonUtils.isNull(screensize)) {
            Flow flow = dicFlow.get(key+"_"+screensize);
            if(flow != null && flow.listAction != null && flow.listAction.size() > 0) return flow;
        }
        return dicFlow.get(key);
    }

    @FXML
    public void onModifyAliasClick(ActionEvent event){
        if(currentSerialNumber == null || currentSerialNumber.equals("")) return;
        Device dev = dicDevice.get(currentSerialNumber);
        TextInputDialog dialog = new TextInputDialog(dev.alias);
        dialog.setTitle("输入终端别名");
       // dialog.setHeaderText("设置终端别名，便于记忆");
        dialog.setHeaderText(null);
        dialog.setContentText("终端别名:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            dev.alias = result.get();
            updateDeviceListView();
            updateDeviceShow();
            saveConfig();
        }

// The Java 8 way to get the response value (with lambda expression).
        //result.ifPresent(name -> System.out.println("Your name: " + name));
    }


    @FXML
    public void onAddTaskClick(ActionEvent event) {
        if(CommonUtils.isNull(currentSerialNumber)) return;

        Device device = dicDevice.get(currentSerialNumber);
        Task task = device.task;
        if(task != null && task.status == Const.TaskStatus.RUNNING.getCode()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("控制器");
            alert.setHeaderText(null);
            alert.setContentText("有任务正在执行中，请先停止!");
            alert.showAndWait();
            return;
        }else if(task != null && !task.isFinish()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("控制器");
            alert.setHeaderText("新建任务会覆盖当前任务！！");
            alert.setContentText("有任务没有执行结束，确定要新建任务吗？");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                // ... user chose OK
            } else {
                return;
            }
        }
        // Create the custom dialog.
        //        Dialog<Pair<String, String>> dialog = new Dialog<>();
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("任务配置");
        dialog.setHeaderText(null);

        // Set the icon (must be included in the project).
        //        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 50, 10, 50));

        TextField taskname = new TextField();
        taskname.setPromptText("任务名称");
        TextField app_range = new TextField();
        app_range.setPromptText("终端范围");
//        TextField app_end = new TextField();
//        app_end.setPromptText("结束编号");
        ComboBox flowtype = new ComboBox();
       // ObservableList<FlowType> list = new ObservableList<FlowType>();
       // flowtype.setItems(FXCollections.observableArrayList(Const.FlowType.values()));
        List<FlowType> list = new ArrayList<>();
        Enumeration<Integer> keys = dicFlowType.keys();
        while(keys.hasMoreElements()) {
            int key = keys.nextElement();
            if(key == Const.flow_type_clear) continue;
            list.add(dicFlowType.get(key));
        }
        flowtype.setItems(FXCollections.observableArrayList(list));

        TextField starttime = new TextField();
        starttime.setPromptText("空表示立即启动");
        TextField endtime = new TextField();
        endtime.setPromptText("空表示没有结束时间");
        TextField taskfile = new TextField();
        taskfile.setPromptText("任务数据");


        grid.add(new Label("任务名称:"), 0, 0);
        grid.add(taskname, 1, 0);
        grid.add(new Label("终端范围:"), 0, 1);
        grid.add(app_range, 1, 1);
        grid.add(new Label("1,3-10,23,..."), 2, 1);
//        grid.add(new Label("结束编号:"), 0, 2);
//        grid.add(app_end, 1, 2);
        grid.add(new Label("业务类型:"), 0, 2);
        grid.add(flowtype, 1, 2);

        grid.add(new Label("启动时刻:"), 0, 3);
        grid.add(starttime, 1, 3);
        grid.add(new Label("yyyy-MM-dd HH:mm:ss"), 2, 3);

        grid.add(new Label("结束时刻:"), 0, 4);
        grid.add(endtime, 1, 4);
        grid.add(new Label("yyyy-MM-dd HH:mm:ss"), 2, 4);

        grid.add(new Label("任务数据:"), 0, 5);
        grid.add(taskfile, 1, 5);
        Button btnOpen = new Button("...");
        btnOpen.setOnAction(
                (final ActionEvent e) -> {
                    Stage stage = new Stage();
                    stage.setTitle("选择任务所需要的配置文件");
                    File file = new FileChooser().showOpenDialog(stage);
                    if (file != null) {
//                        openFile(file);
                        taskfile.setText(file.toString());
                    }
                });
        grid.add(btnOpen, 2, 5);

        btnOpen.setDisable(true);
        taskfile.setDisable(true);
        flowtype.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) ->{
                boolean needdata = ((FlowType)(flowtype.getValue())).isNeeddata();
                btnOpen.setDisable(!needdata);
                taskfile.setText("");
                taskfile.setDisable(!needdata);
            });

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        //        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        //        taskname.textProperty().addListener((observable, oldValue, newValue) -> {
        //            loginButton.setDisable(newValue.trim().isEmpty());
        //        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> taskname.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Task(taskname.getText(), device, ((FlowType)(flowtype.getValue())).getCode(),starttime.getText(), endtime.getText(), taskfile.getText(),this);
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(_task -> {
            if(device.task != null) device.task.close();
            device.addNewTask(_task);
            updateTaskShow(currentSerialNumber, false);
        });
    }

    @FXML
    public void onSwitchTaskClick(ActionEvent event) {
        if(CommonUtils.isNull(currentSerialNumber)) return;
        Device device = dicDevice.get(currentSerialNumber);
        if(device.status == Const.DeviceStatus.OFFLINE) return; // 掉线状态

        Task task = device.task;
        if(task == null || task.isFinish()) return;

        if(task.status == Const.TaskStatus.RUNNING.getCode()){
            task.setStatus(Const.TaskStatus.STOP.getCode());
            btnSwitchTask.setText("启动任务");
        }
        else{
            task.setStatus(Const.TaskStatus.RUNNING.getCode());
            btnSwitchTask.setText("暂停任务");
        }
    }

    @FXML
    public void onResetTaskClick(ActionEvent event)
    {
        if(CommonUtils.isNull(currentSerialNumber)) return;
        Device device = dicDevice.get(currentSerialNumber);
        if(device.status == Const.DeviceStatus.OFFLINE) return; // 掉线状态

        Task task = device.task;
        if(task == null) return;
        task.reset();
        task.setStatus(Const.TaskStatus.RUNNING.getCode());
    }

    @FXML
    public void onReloadFlowClick(ActionEvent event){
        loadFlowType();
        loadFlow();
    }

    @FXML
    public void onOpenScriptPathClick(ActionEvent event){
        try {
            Runtime.getRuntime().exec("cmd /c start explorer "+new File(Const.path_script).getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    public void onOpenScreenPathClick(ActionEvent event){
        try {
            File file = new File(Const.path_screen + CommonUtils.date2String(new Date(), "yyyyMMdd") + "/");
            if(file.exists()) Runtime.getRuntime().exec("cmd /c start explorer "+file.getAbsolutePath());
            else Runtime.getRuntime().exec("cmd /c start explorer "+new File(Const.path_screen).getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    public void onOpenLogPathClick(ActionEvent event){
        try {
            Runtime.getRuntime().exec("cmd /c start explorer "+new File(Const.path_log).getAbsolutePath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    public void onTestClick(ActionEvent event) {
        Pattern p=Pattern.compile("\\d+");
        Matcher m=p.matcher("22bb23");
        System.out.println(m.find()+m.group());//返回true
        Matcher m2=p.matcher("aa2223");
        System.out.println(m2.find()+m2.group());//返回true
        Matcher m3=p.matcher("8805氢");
        System.out.println(m3.find()+m3.group());//返回true
        Matcher m4=p.matcher("aabb");
        System.out.println(m4.find()+m4.group());//返回false
//        Pattern p = Pattern.compile("\\d+");
//        Matcher m = p.matcher("8805氢");
//        if(m.matches()){
//          System.out.println("true");
//        }
//        else
//            System.out.println("false");
//        System.out.println("hhhhh"+("石家庄".length()));

//        String regex = "自动[\\s\\S]{1,8}成功";
//        String result = "自动撮名3期设置成功";
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(result);
//        logMesg("+++"+ m.matches());
        //logMesg("========");

//        String result = OcrUtils.doOrc("eeee", "D:\\VirtualApp\\资料\\达标赛图像标本\\步数达标赛连续报名成功标本\\10.png",
//                450,1250, 180, 80);
//        String result = OcrUtils.doOrc("eeee", "C:\\bin\\AndroidRobot_jar\\screens\\20180828\\0017x_test\\3\\10.jpg",
//                410,1390, 262, 57);
//        logMesg(result);

//        int[] rgb = new int[3];
//        File file = new File("d:\\82_last.jpg");
//        BufferedImage bi = null;
//        try {
//            bi = ImageIO.read(file);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        int width = bi.getWidth();
//        int height = bi.getHeight();
//        int minx = bi.getMinX();
//        int miny = bi.getMinY();
//        System.out.println("width=" + width + ",height=" + height + ".");
//        System.out.println("minx=" + minx + ",miniy=" + miny + ".");
////        for (int i = minx; i < width; i++) {
////            for (int j = miny; j < height; j++) {
//                int pixel = bi.getRGB(988, 1160); // 下面三行代码将一个数字转换为RGB数字
//                rgb[0] = (pixel & 0xff0000) >> 16;
//                rgb[1] = (pixel & 0xff00) >> 8;
//                rgb[2] = (pixel & 0xff);
//                System.out.println("i=" + 988 + ",j=" + 1160 + ":(" + rgb[0] + ","
//                        + rgb[1] + "," + rgb[2] + ")"+pixel);
////            }
////        }

//        Rgb rgb = new Rgb(76,155,255);
//        int pixel = ImageUtils.getPixel( "d:\\82_last.jpg", 985, 1148);
//        Rgb _rgb = new Rgb(pixel);
//        System.out.println("====="+rgb.equals(_rgb));

//        Logger logger = LogManager.getRootLogger();

        //使用默认的配置信息，不需要写log4j.properties
//        BasicConfigurator.configure();
        //设置日志输出级别为info，这将覆盖配置文件中设置的级别
//        logger.setLevel(Level.INFO);
        //下面的消息将被输出
        //logger.info("this is an info");
//        logger.warning("this is a warn");
//
        //logger.warn("this is a warn");
//        logger.error("this is an error");
//        logger.fatal("this is a fatal");

//
//        String s = OcrUtils.doMultiLineOrc("ffff", "d:\\52.png", 280, 480, 440-280, 1650-480, true);
//        System.out.println(s);
////
////
//        List<Words> listWords = new ArrayList<>();
//        String [] ss = s.split("\n");
//        Words _w = null;
//        for(String w: ss){
//            Words words = new Words(w);
//            if(_w == null) _w = words;
//            else{
//                if(_w.isInLine(words, true)) _w.addWords(words);
//                else{
//                    listWords.add(_w);
//                    System.out.println("------"+_w.str);
//                    _w = words;
//                }
//            }
//        }
//        if(_w != null) {listWords.add(_w);System.out.println("------"+_w.str);}
//        System.out.println(listWords.size());

//
//        Point point1 = new Point("250,664");
//        Point point2 = new Point("830,723");
//        int x1 = point1.getX() < point2.getX() ? point1.getX() : point2.getX();
//        int y1 = point1.getY() < point2.getY() ? point1.getY() : point2.getY();
//
//        int x2 = point1.getX() < point2.getX() ? point2.getX() : point1.getX();
//        int y2 = point1.getY() < point2.getY() ? point2.getY() : point1.getY();
//
//        int w = x2 - x1;
//        int h = y2 - y1;
//        //String result = OcrUtils.doOrc(device.serialnumber+"_"+appid+"_"+x1, device.task.capscreenname, x1,y1,w, h);
//        String result = OcrUtils.doSingleLineOrc("fdfdf", "d:\\52.png", x1,y1,w, h);
//        logger.info("识别结果："+result);
//        String regex = "自动[\\s\\S]{1,8}成功";
//        if(CommonUtils.isNull(result)) return;
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(result);

//        try {
//            ADBUtils.copyScreenCap("./screens/20180905/001/ee/1/4.png", "./screens/20180905/001/ee/4.png");
//            //CommonUtils.copyFile("./screens/20180905/001/ee/1/4.png", "./screens/20180905/001/ee/4.png");
//        }catch (Exception e){
//            e.printStackTrace();
//        }

//        try {
//            File file = new File(".\\screens\\20180906\\001\\ee\\tt_1.png");
//            String localUrl = file.toURI().toURL().toString();
//            // don"t load in the background
//            Image localImage = new Image(localUrl, false);
//           // Image image = new Image("D:\\VirtualApp\\AndroidRobot\\screens\\20180906\\001\\ee\\tt_1.png");
//
//            imageView.setImage(localImage);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        File directory = new File(Const.path_script);//设定为当前文件夹
//        try{
//            System.out.println(directory.getAbsolutePath());//获取绝对路径
//        }catch(Exception e){}
//        try {
//            Runtime.getRuntime().exec("cmd /c start explorer "+directory.getAbsolutePath());
//        }catch (Exception e){
//            e.printStackTrace();
//        }

//        String  str = "10";
//        Pattern p = Pattern.compile("^\\d{1,2}$");
//        Matcher m = p.matcher(str);
//        if(m.matches()) {
//            System.out.println("true");
//        }
//        else{
//            System.out.println("false");
//        }

//        Queue<String> queue = new ArrayList();
//
//        String path = "d:\\52.png";
//
//        int w = 110;
//        int h = 72;
//        int d = 201;
//        int c = 8;
//        int x = 315;
//        int y = 514;
//
//        for(int i = 0; i < 8;i ++){
//
//            int x1 = x;
//            int y1 = y + i * d;
//
//            int x2 = x1 +w;
//            int y2 = y1 + h;
//
//            String s = OcrUtils.doSingleLineOrc(i+"dd", path, x1, y1, w, h, true);
//            System.out.println("==="+s);
//        }

    }

}
