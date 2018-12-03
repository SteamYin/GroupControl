package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class TaskData {
    Hashtable<String, Integer> htFieldIndex = null;
    Hashtable<Integer, String[]> htData = null;

    public TaskData(String filename){
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(filename)), "GBK");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
            while ((s = br.readLine()) != null) {
                s = s.trim();
                if(s.equals("") || s.indexOf("#") == 0) continue;
                String []ss = s.split(":");
                if(ss.length != 2) continue;

                if(ss[0].equals("appid")){
                    // 字段索引
                    String[] sss = ss[1].split(",");
                    htFieldIndex = new Hashtable<>();
                    for(int i = 0; i < sss.length; i++){
                        htFieldIndex.put(sss[i], i);
                    }
                }
                else{
                    if(htFieldIndex == null) continue;
                    if(htData == null) htData = new Hashtable<>();
                    int appid = Integer.parseInt(ss[0]);
                    String [] fields = ss[1].split(",");
                    if(fields.length < htFieldIndex.size()) continue;
                    htData.put(appid, fields);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getData(String fieldname){
//        if(htData == null || htFieldIndex == null) return null;
//        if(!htData.containsKey(appid)) return null;
//        if(!htFieldIndex.containsKey(fieldname)) return null;
//        int index = htFieldIndex.get(fieldname);
//        return htData.get(appid)[index];
        return null;
    }

}
