package common;

public class Const {
    public static String version = "1.1.7-0927";
    public static String propfile = "./application.properties";
    public static String path_script = "./script/";
    public static String path_log = "./logs/";
    public static String path_screen = "./screens/";
    public static String path_temp = "./temp/";

    public static int base_port = 1056;     // 起始的端口号
//    public static String virtualapp_pkg = "io.virtualapp";
//    public static String virtualapp_activity = "io.virtualapp.splash.SplashActivity";

    public static String qtt_pkg = "com.jifen.qukan";
    public static String qtt_activity = "com.jifen.qkbase.start.JumpActivity";

    public static String dftt_pkg = "com.songheng.eastnews";
    public static String dftt_activity = "com.oa.eastfirst.activity.WelcomeActivity";

    public static int flow_type_clear = 100;    // 清除缓存的流程id

    public class DeviceStatus{
        public static final int OFFLINE = 0;
        public static final int CONNECTTION = 1;    // 链接上了，但是不能收发信息
        public static final int FREE = 2;
        public static final int BUSY = 3;
    }


    public class ActionType{
        public static final int LAUNCH = 1;       // 启动APP
        public static final int SLEEP = 2;
        public static final int TAP = 3;
        public static final int SWIPE = 4;
        public static final int TEXT = 5;
        public static final int KEYEVENT = 6;
        public static final int SCREENCAP = 7;
        public static final int GOTO = 8;
        public static final int OCR = 9;
        public static final int RGB = 10;
        public static final int POSITION = 11;
        public static final int DIVIDE = 12;
        public static final int ASSIGN = 13;
        public static final int COMPARE = 14;

        // 以下是业务动作，即将业务逻辑写死在动作里
        public static final int _FINDGROUP = 101;
    }
//    public class TaskStatus{
//        public static final int STOP = 0;        // 停止
//        public static final int RUNNING = 1;     // 运行状态
//        public static final int PAUSE = 2;       // 暂停
////        public static final int FINISH = 3;      // 完成
//        public static String getString(int status){
//            if(status == TaskStatus.STOP) return "停止";
//            else if(status == TaskStatus.RUNNING) return "运行";
//            return "未知";
//        }
//    }

    public enum TaskStatus{
        STOP(0, "停止"),
        RUNNING(1,"运行");
        private int code;
        private String name;
        TaskStatus(int code, String name){
            this.code = code;
            this.name = name;
        }
        public int getCode() {
            return code;
        }
        public String toString(){
            return name;
        }
        public static String getStringByCode(int code) {
            for (TaskStatus status : TaskStatus.values()) {
                if (status.getCode() == code) {
                    return status.toString();
                }
            }
            return "未知";
        }
    }

//    public enum FlowType {
//        SIGN_UP_ONE_DAY_FIRST(1, "步数达标赛首次报名", "signup_1day_first.txt", false),
//        SIGN_UP_ONE_DAY_CONTINUE(2, "步数达标赛连续报名", "signup_1day_continue.txt", false),
//        SIGN_UP_SEVEN_DAY_FIRST(3, "7日耐力赛首次报名", "signup_7day_first.txt", false),
//        SIGN_UP_SEVEN_DAY_CONTINUE(4, "7日耐力赛连续报名", "signup_7day_continue.txt", false) ,
//        REAL_NAME(5, "实名制", "realname.txt", true) ,
//        TEST(99, "测试业务", "test.txt", true);
//
//        private int code;
//        private String name;
//        private String script;
//        private boolean needdata;
//
//        FlowType(int code, String name, String script, boolean needdata)
//        {
//            this.code = code;
//            this.name = name;
//            this.script = script;
//            this.needdata = needdata;
//        }
//
//        public int getCode() {
//            return code;
//        }
//        public String getScript() {return script;}
//        public boolean isNeeddata() {
//            return needdata;
//        }
//
//        public String toString(){return name;};
//
//        public static String getStringByCode(int code) {
//            for (FlowType flowType : FlowType.values()) {
//                if (flowType.getCode() == code) {
//                    return flowType.toString();
//                }
//            }
//            return null;
//        }
//    }
}
