package tw.com.example.ben.sharehouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tw.com.example.ben.sharehouse.CHAT.dataModel.House;
import tw.com.example.ben.sharehouse.CHAT.dataModel.MyUser;
import tw.com.example.ben.sharehouse.lib.TinyDB;

public class edit_house extends AppCompatActivity {
    House house;
    private DatabaseReference mDatabase;
    TinyDB tinydb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_house);
        Intent it = getIntent();
        house =new House();
    }
    public void finish(View view)
    {
        final EditText edt= (EditText) findViewById(R.id.name);
        final String housename = edt.getText().toString();
        //Log.e("edt","edt"+housename);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tinydb = new TinyDB(this);
        MyUser myUser= (MyUser) tinydb.getObject("MyUser", MyUser.class);
        String HouseTableName = myUser.getNickname();
        //Log.e("dataSnapshot","dataSnapshot"+ HouseTableName);
        mDatabase.child("userHouseTables").child(HouseTableName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for ( DataSnapshot snapshot :dataSnapshot.getChildren() ){
                    String name = snapshot.child("url").getValue().toString();
                    Log.e("dataSnapshot","dataSnapshot"+ name );
                    new Firebase(name).child("name").setValue(housename);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {   }
        });
    }


}
