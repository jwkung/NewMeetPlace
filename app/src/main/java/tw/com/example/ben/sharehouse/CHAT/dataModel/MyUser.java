package tw.com.example.ben.sharehouse.CHAT.dataModel;

/**
 * Created by Ben on 16/8/16.
 */
public class MyUser {
    private String nickname;
    private String account;
    private String houseTable;
    private String truenickname;
    //##必要建構子 勿刪
    public MyUser()
    {

    }
    public MyUser(String nickname,String account,String houseTable,String name) {
        this.nickname=nickname;
        this.account=account;
        this.houseTable=houseTable;
        this.truenickname=name;
    }
    public String getNickname()
    {
        return nickname;
    }
    public String getHouseTable()
    {
        return houseTable;
    }
    public String getAccount()
    {
        return account;
    }
    public void setTruenickname(String name) { this.truenickname=nickname;}
    public String getTruenickname() { return truenickname;}

}
