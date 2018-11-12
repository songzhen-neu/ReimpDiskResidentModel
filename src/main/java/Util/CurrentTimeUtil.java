package Util;

public class CurrentTimeUtil {
    public static long startTime,endTime;
    public static long getExecuteTime(){
        return (endTime-startTime);
    }
    public static void setStartTime(){
        startTime=System.currentTimeMillis();
    }

    public static void setEndTime(){
        endTime=System.currentTimeMillis();
    }
}
