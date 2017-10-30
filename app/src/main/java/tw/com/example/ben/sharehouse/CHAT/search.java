package tw.com.example.ben.sharehouse.CHAT;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import tw.com.example.ben.sharehouse.CHAT.dataModel.Friend;
import tw.com.example.ben.sharehouse.CHAT.dataModel.House;
import tw.com.example.ben.sharehouse.CHAT.dataModel.MyUser;
import tw.com.example.ben.sharehouse.R;
import tw.com.example.ben.sharehouse.lib.TinyDB;

public class search extends AppCompatActivity implements AdapterView.OnItemClickListener{
    TextView textView;
    ListView listView;
    private Firebase mFirebaseRef,connect;
    TinyDB tinyDB ;
    search_list_adapter adapter;
    EditText editText,edt_housename;
    Button button;
    ChatApplication GV;
    Boolean flag;
    public ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        tinyDB =new TinyDB(this);
        MyUser user= (MyUser) tinyDB.getObject("MyUser", MyUser.class);
        String table = user.getHouseTable();
        setTitle("搜尋帳號");
        listView = (ListView) findViewById(R.id.list);
        mFirebaseRef =new Firebase("https://sharehousetest.firebaseio.com/users");

        editText = (EditText) findViewById(R.id.text);
        edt_housename = (EditText) findViewById(R.id.edt_housename);

        listView.setOnItemClickListener(this);
        adapter= new search_list_adapter(this,mFirebaseRef);
        listView.setAdapter(adapter);

        GV = (ChatApplication) this.getApplicationContext();
        flag = false;
        mAdapter = new ArrayAdapter<String>(this,R.layout.friend_check_item,R.id.textfriend);
    }
    public void search(View view)
    {
        String searchWord =  editText.getText().toString();
        //Log.v("searchWord:",searchWord);
        adapter= new search_list_adapter(this, mFirebaseRef.orderByChild("account").equalTo(searchWord));
        listView.setAdapter(adapter);

    }
    @Override
    public void onStart() {
        super.onStart();
        String searchWord =  editText.getText().toString();
        adapter= new search_list_adapter(this, mFirebaseRef.orderByChild("account").equalTo(searchWord));
        listView.setAdapter(adapter);

       // Log.v("onstart","%%%%");
    }
     public void add(View v)
     {
         String rr=  tinyDB.getString("friend");
         String friendtable=  tinyDB.getString("friendtable");
         connect= new Firebase(friendtable);
         House house= (House) tinyDB.getObject("newchatroom",House.class);
         connect.push().setValue(house);
         Log.v("444",friendtable);
         String name =  editText.getText().toString();
         String subname = name.substring(0,name.indexOf("@"));
         flag = true;

         Toast.makeText(this,subname+"已加入聊天室", Toast.LENGTH_SHORT).show();
     }

     public void friend_pick(View v)
     {
         mAdapter.clear();
         MyUser myUser= (MyUser) tinyDB.getObject("MyUser", MyUser.class);
         String UserKey = myUser.getNickname();


         FirebaseDatabase db = FirebaseDatabase.getInstance();
         DatabaseReference Ref = db.getReference("friends").child(UserKey);

         Ref.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 Log.v("friendPick",dataSnapshot.getValue(Friend.class).getName());
                 mAdapter.add(dataSnapshot.getValue(Friend.class).getName());
             }
             @Override
             public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

             @Override
             public void onChildRemoved(DataSnapshot dataSnapshot) {}

             @Override
             public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

             @Override
             public void onCancelled(DatabaseError databaseError) {}
         });

         showfriends();
     }

    private void showfriends()
    {
        final AlertDialog.Builder friendAlert = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View convertView = (View) inflater.inflate(R.layout.friend_check,null);
        ListView lv = (ListView) convertView.findViewById(R.id.friendlist);
        lv.setAdapter(mAdapter);
        lv.setItemsCanFocus(true);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        friendAlert.setTitle("好友列表");
        friendAlert.setView(convertView);
        friendAlert.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText( getApplicationContext() ,"朋友"+mAdapter.getItem(position)+"已加入聊天室", Toast.LENGTH_SHORT).show();
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                final DatabaseReference Ref= db.getReference();
                final Query AddQuery = Ref.child("users").orderByChild("account").equalTo(mAdapter.getItem(position).toString());
                AddQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.exists()){
                            MyUser user = dataSnapshot.getValue(MyUser.class);
                            String housekey = user.getHouseTable();
                            House house= (House) tinyDB.getObject("newchatroom",House.class);
                            new Firebase(housekey).push().setValue(house);
                        }else{
                            AddQuery.removeEventListener(this);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {  }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
        });
    }

    public void friend_add(View v)
     {
         MyUser myUser= (MyUser) tinyDB.getObject("MyUser", MyUser.class);
         String UserKey = myUser.getNickname();

         FirebaseDatabase db = FirebaseDatabase.getInstance();
         DatabaseReference Ref = db.getReference();

         if( editText.getText().toString().length() != 0 ){
             final Query friendaddQuery = Ref.child("friends").child(UserKey).orderByChild("name").equalTo(editText.getText().toString());
             friendaddQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(DataSnapshot dataSnapshot) {
                     if(dataSnapshot.exists()){
                        friendaddQuery.removeEventListener(this);
                     }else{
                         Friend friend = new Friend(editText.getText().toString());
                         dataSnapshot.getRef().push().setValue(friend);
                     }
                 }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {

                 }
             });
         }

     }

     public void searchcheck(View v)
     {
         if(edt_housename.getText().toString().length() != 0 && flag != false){
             new Firebase(GV.getSearch_House_Name()).child("name").setValue(edt_housename.getText().toString());
         }
         if(flag == false){

             MyUser myUser= (MyUser) tinyDB.getObject("MyUser", MyUser.class);
             String UserKey = myUser.getNickname();

             FirebaseDatabase db = FirebaseDatabase.getInstance();
             final DatabaseReference Ref= db.getReference();
             final Query deleteQuery = Ref.child("userHouseTables").child(UserKey).orderByChild("url").equalTo(GV.getSearch_House_Name());
             deleteQuery.addChildEventListener(new ChildEventListener() {
                 @Override
                 public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                     if(dataSnapshot.exists()){
                         Log.v("deletehouse",dataSnapshot.getRef().toString());
                         dataSnapshot.getRef().setValue(null);
                         deleteQuery.removeEventListener(this);
                     }
                 }

                 @Override
                 public void onChildChanged(DataSnapshot dataSnapshot, String s) {  }

                 @Override
                 public void onChildRemoved(DataSnapshot dataSnapshot) {  }

                 @Override
                 public void onChildMoved(DataSnapshot dataSnapshot, String s) {   }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {   }
             });
             new Firebase(GV.getSearch_House_Name()).removeValue();
         }

         finish();
     }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v("444","444444444");
    }
}
