package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CMDUtils {
    private static Logger logger = LogManager.getLogger(CMDUtils.class);
    public static List<String> runCMD(String cmd){
        if(cmd.indexOf("devices") == -1)
            logger.info(cmd);
//            System.out.println(cmd);
        Process p;
        List<String> resultstr = new ArrayList<String>();
        try
        {
            //执行命令
            p = Runtime.getRuntime().exec(cmd);
            //取得命令结果的输出流
            InputStream fis=p.getInputStream();
            //用一个读输出流类去读
            //用缓冲器读行
            BufferedReader br=new BufferedReader( new InputStreamReader(fis,"GB2312"));
            String line=null;
            while((line=br.readLine())!=null)
            {
               resultstr.add(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return resultstr;
    }
}
