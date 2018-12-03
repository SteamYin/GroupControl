package models;

public class Screen {
    public int width;
    public int height;

    public Screen(int width, int height){
        this.width = width;
        this.height = height;
    }

    public Screen(String wh){
        String[] ss = wh.split("x");
        if(ss == null || ss.length != 2) return;
        this.width = Integer.parseInt(ss[0]);
        this.height = Integer.parseInt(ss[1]);
    }

    public String toString(){
        return this.width+"x"+this.height;
    }
}
