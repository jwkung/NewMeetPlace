package tw.com.example.ben.sharehouse.Map;

/**
 * Created by Nightingale on 2017/9/18.
 */

public class User_MapSettings {
    private String islocreq ;
    private String useremail;
    private String currentCameraLat;
    private String currentCameraLon;
    private String currentCameraZoom;

    public User_MapSettings(){

    }
    public User_MapSettings(String useremail,String islocreq){
        this.islocreq = islocreq;
        this.useremail=useremail;
    }
    public User_MapSettings(String useremail,String islocreq,String currentCameraLat,String currentCameraLon,String currentCameraZoom){
        this.islocreq = islocreq;
        this.useremail=useremail;
        this.currentCameraLat=currentCameraLat;
        this.currentCameraLon=currentCameraLon;
        this.currentCameraZoom=currentCameraZoom;
    }
    public String getIslocreq() {return islocreq;}
    public String getUseremail(){return  useremail;}
    public String getCurrentCameraLat(){return  currentCameraLat;}
    public String getCurrentCameraLon(){return currentCameraLon;}
    public String getCurrentCameraZoom(){return currentCameraZoom;}



}
