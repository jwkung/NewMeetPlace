package tw.com.example.ben.sharehouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import tw.com.example.ben.sharehouse.CHAT.dataModel.House;
import tw.com.example.ben.sharehouse.CHAT.dataModel.MyUser;
import tw.com.example.ben.sharehouse.lib.TinyDB;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;
    String account;        //帳號
    Intent mainActivity;   //主畫面
    private String userUID;//紀錄使用者ID
    TinyDB tinydb;         //小型資料庫  放置使用者資料
    private Firebase mFirebaseRef;
    private int flag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //主畫面初始化
        mainActivity = new Intent(this,MainActivity.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始資料庫  用於紀錄登入狀態   1為登陸  0為未登陸
        tinydb = new TinyDB(this);
        //先判斷用戶是否登陸
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged( @NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null) {
                    Log.d("onAuthStateChanged", "登入:"+ user.getUid()+"@@");
                    userUID =  user.getUid();
                    //紀錄登陸狀態
                    tinydb.putInt("loginState",1);
                    //updateContact();
                    //pushFriend("Jack");
                }else{
                    Log.d("onAuthStateChanged", "已登出");
                    tinydb.putInt("loginState",0);
                }
            }

        };
        // 如果是已經登陸過  跳過登陸畫面 結束登陸畫面
        if(tinydb.getInt("loginState")==1)
        {
            startActivity(mainActivity);
            finish();
        }
        mFirebaseRef =new Firebase("https://sharehousetest.firebaseio.com/users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //登陸監聽器
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authListener!=null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    //登錄   登錄鍵函式
    public void login(View v){
        final String email = ((EditText)findViewById(R.id.email))
                .getText().toString();
        final String password = ((EditText)findViewById(R.id.password))
                .getText().toString();
        Log.d("AUTH_LOGIN: ", email+"/"+password);
        //登錄 查看狀態 並跳轉頁面
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("onComplete", "onComplete");
                        //登入失敗
                        if (!task.isSuccessful()){

                            Log.d("onComplete", "登入失敗");
                            Query mRef = mFirebaseRef.orderByChild("account").equalTo(email);
                            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        new AlertDialog.Builder(LoginActivity.this)
                                                .setTitle("登入問題")
                                                .setMessage("密碼錯誤")
                                                .setPositiveButton("確定", null)
                                                .show();
                                    }
                                    else{
                                        new AlertDialog.Builder(LoginActivity.this)
                                                .setTitle("登入問題")
                                                .setMessage("無此帳號，是否要以此帳號與密碼註冊?")
                                                .setPositiveButton("註冊", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        createUser(email, password);
                                                    }
                                                })
                                                .setNeutralButton("取消", null)
                                                .show();
                                    }
                                }
                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                        }
                        else
                        {
                            //帳號預設為信箱
                            account = email;
                            addContact();
                            //登陸成功
                            startActivity(mainActivity);
                            //登陸畫面結束
                            finish();
                        }
                    }
                });

    }
    //創建使用者
    private void createUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                String message =
                                        task.isComplete() ? "註冊成功" : "註冊失敗";
                                new AlertDialog.Builder(LoginActivity.this)
                                        .setMessage(message)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
    }
    //將UID增加到資料庫中
    private void addContact(){
        House house = new House();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = db.getReference("users");
        //新增使用者資料到  遠端使用者資料庫中 增加帳號、暱稱、房子列表，暱稱預設為UID
        DatabaseReference Ref = db.getReference("userHouseTables").child(userUID);
        String houseTable = Ref.toString();
        MyUser myUser = new MyUser(userUID,account,houseTable);
        usersRef.child(userUID).setValue(myUser);
        //新增使用者資料增加到本地資料庫中
        TinyDB tinydb = new TinyDB(this);
        tinydb.putObject("MyUser",myUser);
    }

  /*  private void updateContact(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = db.getReference("users");
        Map<String, Object> data = new HashMap<>();
        data.put("nickname", "Hank123");
        usersRef.child(userUID).updateChildren(data,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        if (databaseError!=null){
                            //正確完成
                        }else{
                            //發生錯誤
                        }
                    }
                });
    }

    private void pushFriend(String name){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = db.getReference("users");
        DatabaseReference friendsRef = usersRef.child(userUID).child("friends").push();
        Map<String, Object> friend = new HashMap<>();
        friend.put("name", name);
        friend.put("phone", "22334455");
        friendsRef.setValue(friend);
        String friendId = friendsRef.getKey();
        Log.d("FRIEND", friendId+"");
    }
   */

}

