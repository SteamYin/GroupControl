package models;

public class FlowTimeLen {  private int type;
    private long timelen;
    private long maxtime;

//    public FlowTimeLen(){
//        super();
//    }
    public FlowTimeLen(int type, long timelen, long maxtime){
        this.maxtime = maxtime;
        this.timelen = timelen;
        this.type = type;
    }

    public long getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(long maxtime) {
        this.maxtime = maxtime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimelen() {
        return timelen;
    }

    public void setTimelen(long timelen) {
        this.timelen = timelen;
    }

}
