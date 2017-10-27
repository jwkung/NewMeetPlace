package tw.com.example.ben.sharehouse.CHAT.dataModel;

/**
 * Created by Feng on 2017/10/27.
 */

public class Friend {
    private String name;

    private Friend(){

    }

    public Friend(String name){
        this.name = name;
    }

    public String getName(){ return this.name; }
    public void setName(String name){ this.name = name;}
}
