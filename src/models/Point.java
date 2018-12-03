package models;

import utils.CommonUtils;

public class Point {
    private String key;
    private int x_begin;
    private int y_begin;

    private int x_end;
    private int y_end;

    public Point(Point point1, Point point2){
        this.x_begin = point1.getX() < point2.getX() ? point1.getX() : point2.getX();
        this.x_end = point1.getX() > point2.getX() ? point1.getX() : point2.getX();
        this.y_begin = point1.getY() < point2.getY() ? point1.getY() : point2.getY();
        this.y_end = point1.getY() > point2.getY() ? point1.getY() : point2.getY();
    }

    public Point(int x, int y){
        this.x_begin = x;
        this.x_end = x;
        this.y_begin = y;
        this.y_end = y;
    }

    public Point(String xy){
        if(xy.startsWith("[") || xy.startsWith("]")){
            xy = xy.replace("[","").replace("]","");
            this.key = xy;
        }
        else {
            xy = xy.replace("(", "").replace(")", "");
            String[] ss = xy.split(",");
            if (ss == null || ss.length != 2) return;
            String[] xx = ss[0].split("-");
            this.x_begin = Integer.parseInt(xx[0]);
            this.x_end = xx.length == 2 ? Integer.parseInt(xx[1]) : this.x_begin;

            String[] yy = ss[1].split("-");
            this.y_begin = Integer.parseInt(yy[0]);
            this.y_end = yy.length == 2 ? Integer.parseInt(yy[1]) : this.y_begin;
        }
    }

    public int getX(){
        return x_begin == x_end ? x_begin : CommonUtils.getRandomNum(x_begin, x_end);
    }

    public  int getY(){
        return y_begin == y_end ? y_begin : CommonUtils.getRandomNum(y_begin, y_end);
    }

    public int getX_begin() {return x_begin;}
    public int getX_end() {return x_end;}
    public int getY_begin() {return y_begin;}
    public int getY_end() {return y_end;}

    public void set(int x, int y){
        this.x_begin = x;
        this.x_end = x;
        this.y_begin = y;
        this.y_end = y;
    }

    public boolean isVariable(){ return !CommonUtils.isNull(key);}
    public String getKey() { return key;}

    public String toString(){
        String s = ""+x_begin;
        if(x_begin != x_end) s += ("-"+x_end);
        s += (","+y_begin);
        if(y_begin != y_end) s += ("-"+y_end);
        return s;
    }

    /**
     * 坐标原点为左下方，转换成左上方
     * @param h 高度
     */
    public void zeroPointDown2Up(int h){
        int y = this.y_begin;
        this.y_begin = h - y_end;
        this.y_end = h - y;
    }

    /**
     * 用于计算切图上的某点在原图上的坐标
     * @param point
     */
    public void addPoint(Point point){
        int x = point.getX();
        this.x_begin += x;
        this.x_end += x;
        int y = point.getY();
        this.y_begin += y;
        this.y_end += y;
    }
    public static Point ConvertPoint(Point point, Screen src, Screen dst){
        if(point == null
                || src == null || src.height == 0 || src.width == 0
                || dst == null || dst.height == 0 || dst.width == 0)
            return null;
        return new Point(point.getX() * dst.width / src.width, point.getY() * dst.height / src.height);
    }

}
