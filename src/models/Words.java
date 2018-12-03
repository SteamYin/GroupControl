package models;

import utils.CommonUtils;

public class Words {
    public String str;
    public Point point1;
    public Point point2;
    private int count;  // 字数

    public Words(String s){
        if(CommonUtils.isNull(s)) return;
        String [] ss = s.split(" ");
        if(ss.length != 6) return;
        str = ss[0];
        point1 = new Point(Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
        point2 = new Point(Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
        count = 1;
    }

    public Words(String s, Point point1, Point point2){
        this.str = s;
        this.point1 = point1;
        this.point2 = point2;
    }

    /**
     * 合并
     * @param word
     */
    public void addWords(Words word){
        if(point1.getX() < word.point1.getX()) str += word.str;
        else str = word.str + str;
        int x1 =point1.getX() < word.point1.getX() ? point1.getX() : word.point1.getX();
        int x2 =point2.getX() > word.point2.getX() ? point2.getX() : word.point2.getX();
        int y1 =point1.getY() < word.point1.getY() ? point1.getY() : word.point1.getY();
        int y2 =point2.getY() > word.point2.getY() ? point2.getY() : word.point2.getY();
        point1.set(x1, y1);
        point2.set(x2, y2);
        count++;
    }

    /**
     * 判断是否在一行上
     * @param word
     * @param noempty 没有空格，如果为true，当新加字的左边减去原字符串右边的距离超过单个字的2倍，认为不在一行
     * @return
     */
    public boolean isInLine(Words word, boolean noempty){
        int yMiddle = (word.point1.getY() + word.point2.getY()) / 2;
        if(yMiddle > point1.getY() && yMiddle < point2.getY()){
            if(!noempty) return true;

            int width = Math.abs(point2.getX() - point1.getX()) / count;
            // 如果word的左边离this的右边距离超过单个字宽度的2倍，判断不在一行上
            return word.point1.getX() - this.point2.getX() < (2 * width);
        }
        else return false;
    }

    /**
     * 是否大于word
     * @param word
     * @return
     */
    public boolean isGreater(Words word){
        if(word == null) return true;
        if(CommonUtils.isNumber(this.str) && CommonUtils.isNumber(word.str)) return Integer.parseInt(str) > Integer.parseInt(word.str);
        return str.compareTo(word.str) > 0;
    }

    public String toString(){
        return str + " "+ point1.toString() + " " + point2.toString();
    }
}
