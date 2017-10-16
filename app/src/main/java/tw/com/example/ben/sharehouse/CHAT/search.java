package tw.com.example.ben.sharehouse.CHAT;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
