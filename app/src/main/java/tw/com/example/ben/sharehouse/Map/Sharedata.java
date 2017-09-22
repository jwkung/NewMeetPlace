package tw.com.example.ben.sharehouse.Map;

/**
 * Created by Nightingale on 2017/9/22.
 */

public class Sharedata {
    public String Markertype;
    public String key;

    public Sharedata(){}

    public Sharedata(String type,String key){
        this.key = key;
        this.Markertype = type;
    }
}
