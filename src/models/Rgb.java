package models;

import utils.CommonUtils;

public class Rgb {

    private int r = -1;
    private int g= -1;
    private int b= -1;


    public Rgb(int r, int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Rgb(int pixel){
        r = (pixel & 0xff0000) >> 16;
        g = (pixel & 0xff00) >> 8;
        b = (pixel & 0xff);
    }
    public Rgb(String rgb){
        rgb = rgb.replace("(","").replace(")","");
        String[] ss = rgb.split(",");
        if(ss == null || ss.length != 3) return;
        this.r = Integer.parseInt(ss[0]);
        this.g = Integer.parseInt(ss[1]);
        this.b = Integer.parseInt(ss[2]);
    }

    public int isLineColor(){
        return (Math.abs(this.r - this.b) < 2 && Math.abs(this.r - this.g) < 2) ? this.r : 0;
    }
    public boolean equals(Rgb rgb){
//        if(this.r == -1 || rgb.r == -1
//                || this.g == -1 || rgb.g == -1
//                || this.b == -1 || rgb.b == -1) return false;

        return Math.abs(this.r - rgb.r) <= 10
                && Math.abs(this.g - rgb.g) <= 10
                && Math.abs(this.b - rgb.b) <= 10;
    }

    public String toString(){
        return "RGB("+this.r+","+this.g+","+this.b+")";
    }
}
