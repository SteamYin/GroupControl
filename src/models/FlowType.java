package models;

import common.Const;
import models.actions.*;
import utils.CommonUtils;
import utils.ConfigUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FlowType {
    private int code;
    private String name;
    private String script;
    private boolean needdata;

    public FlowType(String flowconfig) {
        try {
            String[] ss = flowconfig.split("\\|");
            // 1|步数达标赛首次报名|signup_1day_first.txt|false
            if (ss.length != 4) return;

            this.code = Integer.parseInt(ss[0]);
            this.name = ss[1];
            this.script = ss[2];
            this.needdata = ss[3].equals("true");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  FlowType(int code, String name, String script, boolean needdata) {
        this.code = code;
        this.name = name;
        this.script = script;
        this.needdata = needdata;
    }

    public int getCode() {
        return code;
    }

    public String getScript() {
        return script;
    }

    public boolean isNeeddata() {
        return needdata;
    }

    public String toString() {
        return name;
    }

}
