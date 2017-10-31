package tw.com.example.ben.sharehouse.CHAT;

import com.firebase.client.Firebase;

/**
 * @author Jenny Tong (mimming)
 * @since 12/5/14
 *
 * Initialize Firebase with the application context. This must happen before the client is used.
 */
public class ChatApplication extends android.app.Application {

    private String KEY_TWO;             // delete house
    private String ChatName;
    private String Change_House_Name;
    private String Search_House_Name;
    private int friendcount;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }


    public void setFriendcount(int friendcount){
        this.friendcount=friendcount;
    }
    public int getFriendcount(){ return this.friendcount; }
    public void addFriendcount(){ this.friendcount++;}

    public void setKEY_TWO(String KEY) { this.KEY_TWO = KEY; }
    public String getKEY_TWO(){ return this.KEY_TWO; }

    public void setChatName(String name) { this.ChatName = name; }
    public String getChatName(){ return this.ChatName ; }

    public void setChange_House_Name(String name) {this.Change_House_Name = name;}
    public String getChange_House_Name(){ return this.Change_House_Name;}

    public void setSearch_House_Name(String name) {this.Search_House_Name = name;}
    public String getSearch_House_Name(){ return this.Search_House_Name;}
}
