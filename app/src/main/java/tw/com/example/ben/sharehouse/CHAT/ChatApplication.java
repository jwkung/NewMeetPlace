package tw.com.example.ben.sharehouse.CHAT;

import com.firebase.client.Firebase;

/**
 * @author Jenny Tong (mimming)
 * @since 12/5/14
 *
 * Initialize Firebase with the application context. This must happen before the client is used.
 */
public class ChatApplication extends android.app.Application {

    private int Flag_D;
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

    public void setFlag_D(int flag){
        this.Flag_D=flag;
    }

    public int getFlag_D(){
        return this.Flag_D;
    }
}
