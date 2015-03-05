package nl.senseos.mytimeatsense.util;

/**
 * Created by ronald on 4-3-15.
 */
public class Clock {

    private int days;
    private int hours;
    private int minutes;
    private int seconds;

    public Clock(int d, int h, int m, int s){
        days = d;
        hours = h;
        minutes = m;
        seconds = s;
    }

    public Clock(long totalSeconds){

        days = (int) totalSeconds / (24 * 60 * 60);
        hours = (int) (totalSeconds - 24 * 60 * 60 * days) / (60 * 60);
        minutes = (int) (totalSeconds - days * (24 * 60 * 60) - hours
                * (60 * 60)) / (60);
        seconds = (int) (totalSeconds - days * (24 * 60 * 60) - hours
                * (60 * 60) - minutes * 60);

    }

    public String getDays(){
        return Integer.toString(days);
    }

    public String getHours(){
        return  String.format("%02d", hours);
    }

    public String getMinutes(){
        return  String.format("%02d", minutes);
    }

    public String getSeconds(){
        return  String.format("%02d", seconds);
    }
}
