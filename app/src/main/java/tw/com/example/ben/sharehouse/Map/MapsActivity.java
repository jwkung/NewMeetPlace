package tw.com.example.ben.sharehouse.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import tw.com.example.ben.sharehouse.CHAT.ChatApplication;
import tw.com.example.ben.sharehouse.CHAT.FirebaseListAdapter;
import tw.com.example.ben.sharehouse.CHAT.dataModel.Chat;
import tw.com.example.ben.sharehouse.CHAT.dataModel.House;
import tw.com.example.ben.sharehouse.CHAT.dataModel.MyUser;
import tw.com.example.ben.sharehouse.LoginActivity;
import tw.com.example.ben.sharehouse.R;
import tw.com.example.ben.sharehouse.lib.TinyDB;
import tw.com.example.ben.sharehouse.ui.IconSmallerOnTouchListener;

import static android.os.SystemClock.sleep;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,GoogleMap.OnMarkerDragListener,GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        GoogleMap.OnInfoWindowClickListener,GoogleMap.OnCameraIdleListener,GoogleMap.OnInfoWindowLongClickListener,PlaceSelectionListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;//地圖相機位置
    private Double currentCameraLat,currentCameraLon,currentCameraZoom;
    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);//預設位置，偵測不到使用者位置時使用。
    private static final int DEFAULT_ZOOM = 15;//預設地圖Scale
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;//使用者目前位置
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private FirebaseAuth mAuth;
    private DatabaseReference mLocReference;//資料庫存取路徑-使用者位置
    private DatabaseReference mUMkrReference;//資料庫存取路徑-自訂標記
    private DatabaseReference NearplaceReference;//資料庫存取路徑-附近地點結果資料
    private DatabaseReference CenterpointReference;//資料庫存取路徑-中心點位置
    private DatabaseReference SearchmkrReference;//資料存取路徑-搜尋標記位置
    private DatabaseReference ManagerCameraReference;//資料庫存取路徑-聊天室管理者相機位置資料
    private DatabaseReference ManagerMarkerReference;//資料庫存取路徑-聊天室管理者同步畫面資料
    private DatabaseReference ManagerPolylineReference;//資料庫存取路徑-聊天室管理者Flag
    private DatabaseReference ManagerFlagReference;//資料庫存取路徑-聊天室管理者Flag
    private DatabaseReference FinalPlaceReference;//final place
    private ChildEventListener mChildEventListener,mUMkraddChildEventListener ;//資料庫變動監聽事件(使用者位置/自訂標記)
    private ChildEventListener mNearPlaceChildEventListener;//資料庫變動監聽事件(附近地點結果資料)
    private ChildEventListener mFinalPlaceEventListener;
    private ChildEventListener mManagerPolylineChildEventListener;
    private ValueEventListener mManagerValueEventListener;//資料庫變動監聽事件(聊天室管理者資料)
    private ValueEventListener mCenterPointListener;//資料庫變動監聽事件(中心點位置資料)
    private ValueEventListener mSearchmkrListener;//資料庫變動監聽事件(搜尋標記資料)
    private ValueEventListener mFollowMarkerValueEventListener;
    private ValueEventListener mManagerFlagEventListener;

    public List<Marker> markersList,UmarkerList;//(使用者位置/自訂標記)List
    public List<String> chatmember;//聊天室成員List
    public List<String> placetype;//搜尋地點類型List
    public int UMkrNumLimit;
    public int MAX_PLACES = 60;
    private int nearradius;
    private int NVrmenupage;
    private static final int RC_PHOTO_PICKER = 2 ;
    private int[] TollBarTitle = {R.string.app_name,R.string.RealtimeLoc_Switch,R.string.about};
    public String lastkey,lastemail;
    public String Chatroom_Key;
    private String mUsername;
    private String finplacetype;
    public String nxtstring;
    public LocationRequest mLocationRequest;
    private DrawerLayout DL;
    private FrameLayout FL;
    protected NavigationView NV,NP_NV;
    Toolbar toolbar;
    FirebaseUser currentUser;
    int locreqflag;//是否同步自身位置
    private List<Marker> placeMarkers,naviMarkers;
    private MarkerOptions[] places,naviplaces;
    int page;
    LatLng centerpoint;
    private Marker centermarker;
    private Marker searchmarker;
    private FirebaseDatabase MapDatabase;
    private ImageView send ,addpic ,chat;
    private NavigationView NVr;
    private ListView LV;
    private boolean flag_chatlist;
    private Firebase mFirebaseRef;
    private FirebaseListAdapter mChatListAdapter;
    private EditText mMessageEditText;
    public Boolean isManager,isfollowmode;
    private StorageReference mChatPhotosStorageReferenece;
    private FirebaseStorage mFirebaseStorage;
    private boolean issearch,searchexec;
    private Menu toolbarmenu;
    private TextView toolbartxv;
    private ScrollView bar_scrollvw;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 111;
    FloatingActionButton fabtest;
    private boolean managerflag;
    ChatApplication GV;  // Global Variable
    private Marker choicefinalplacemkr,finalplacemkr;
    private final double EARTH_RADIUS = 6378137.0;  // Distance paremeter
    private double Distance; // Distance
    private boolean isnavi,issnap;
    private int navimarkernum;
    private List <Polyline> navipolyline;
    private String[] polylinecolor={"#afff0000","#afff8000","#afffc800","#af80ff00","#af00ff00","#af00ff80","#af00ffff","#af0080ff","#af0000ff","#af8000ff","#afff00ff"};
    private boolean isnavi_srchshop,isnavi_showallmemeberroutes;
    private List<String> start_type_list,start_place_list,end_type_list,end_place_list,str_navipolyline;
    private Spinner start_type_spin,start_place_spin,end_type_spin,end_place_spin;
    private TinyDB tinydbb;
    EditText edt;


    // TODO: OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();


        //地圖資料庫路徑
        FirebaseApp app = FirebaseApp.getInstance("MapRtDb");
        MapDatabase = FirebaseDatabase.getInstance(app);
        //聊天室資料庫-使用者資料
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //取得房間號碼及相關設定
        Intent it = getIntent();
        String FIREBASE_URL = it.getStringExtra("URL");
        Chatroom_Key=it.getStringExtra("roomkey");
        Log.i("key: ",Chatroom_Key);
        flag_chatlist = false;
        LV= (ListView)findViewById(R.id.chatlist);
        setupUsername();
        mFirebaseRef = new Firebase(FIREBASE_URL);
        mMessageEditText = (EditText) findViewById( R.id.messageInput) ;
        isfollowmode = false;
        isManager = false;
        issearch = false;
        searchexec =false;
        managerflag = false;
        isnavi =false;
        issnap =false;
        toolbartxv = (TextView) findViewById(R.id.toolbar_txv);
        bar_scrollvw=(ScrollView)findViewById(R.id.bar_scrollvw);
        //檢查使用者是否存在(有無登入)
        if(currentUser==null ){
            Intent intent = new Intent();
            intent.setClass(MapsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            //使用者位置標記路徑及儲存LIST
            tinydbb = new TinyDB(this);
            mLocReference = MapDatabase.getReference().child(Chatroom_Key).child("users");
            markersList = new ArrayList<>();
            //使用者自訂標記路徑、儲存LIST及個人自訂標記數量上限
            mUMkrReference = MapDatabase.getReference().child(Chatroom_Key).child("Umarker");
            UmarkerList = new ArrayList<>();
            UMkrNumLimit = 1;

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_rightdrawer);
            FloatingActionButton fab_addusrmkr = (FloatingActionButton) findViewById(R.id.addusermkr);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DL.openDrawer(GravityCompat.END);
                }

            });
            fab_addusrmkr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UMkrNumLimit > 0) {
                        // [START_EXCLUDE]
                        final double[] CameraLat = new double[1];
                        final double[] CameraLon = new double[1];
                        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                CameraLat[0] = mMap.getCameraPosition().target.latitude;
                                CameraLon[0] = mMap.getCameraPosition().target.longitude;
                                submitUmkrlocation(Double.toString(CameraLat[0]), Double.toString(CameraLon[0]));
                            }
                        });

                    }
                    else{
                        Toast.makeText(MapsActivity.this, "標記已達上限，無法再增加!", Toast.LENGTH_LONG).show();
                    }
                }

            });
            setUpToolBar();//上方工具列功能實作
            locreqflag = 1;//同步自身位置旗標
            //附近位置儲存陣列及初始搜索半徑
            placeMarkers = new ArrayList<Marker>();
            navipolyline = new ArrayList<Polyline>();
            naviMarkers = new ArrayList<Marker>();
            str_navipolyline = new ArrayList<String>();
            nearradius = 250;
            //設定相關功能資料庫路徑
            NearplaceReference = MapDatabase.getReference().child(Chatroom_Key).child("Nearplace");
            ManagerCameraReference = MapDatabase.getReference().child(Chatroom_Key).child("Manager").child("Camera");
            ManagerMarkerReference = MapDatabase.getReference().child(Chatroom_Key).child("Manager").child("ClickedMarker");
            ManagerFlagReference   = MapDatabase.getReference().child(Chatroom_Key).child("Manager").child("Flag");
            ManagerPolylineReference =  MapDatabase.getReference().child(Chatroom_Key).child("Manager").child("Polyline");
            CenterpointReference = MapDatabase.getReference().child(Chatroom_Key).child("CenterPoint");
            SearchmkrReference = MapDatabase.getReference().child(Chatroom_Key).child("SearchMarker");
            mFirebaseStorage = FirebaseStorage.getInstance();
            mChatPhotosStorageReferenece = mFirebaseStorage.getReference().child("chat_photos");
            FinalPlaceReference= MapDatabase.getReference().child(Chatroom_Key).child("FinalPlace");
            check_manager();//檢查誰是管理員
            set_map_member_loc();//使用者位置標記監聽實作
            set_map_customize_mkr();//使用者自訂標記監聽實作
            set_manager();//管理員的地圖同步資料監聽實作
            set_usersetting();//使用者設定-是否開啟同步位置
            set_nearplace_mkr();//附近地點標記監聽實作-追隨模式
            set_center_mkr();//中心點位置監聽實作-追隨模式
            set_search_mkr();//搜尋標記監聽實作-追隨模式
            set_followmkr();//標記同步顯示-追隨模式
            set_manageflag();
            set_finalplace();
            set_polyline();



            fabtest = (FloatingActionButton) findViewById( R.id.fab);
            fabtest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isManager&&choicefinalplacemkr!=null){
                        //finalplacemkr = choicefinalplacemkr;
                        FinalPlace fp = new FinalPlace(choicefinalplacemkr.getTitle(),String.valueOf(choicefinalplacemkr.getPosition().latitude),
                                String.valueOf(choicefinalplacemkr.getPosition().longitude));

                        FinalPlaceReference.child("fp").setValue(fp);
                        Toast.makeText(getApplicationContext(),"已設定最終目的地", Toast.LENGTH_SHORT).show();

                    }
                    else if(!isManager){
                       if(finalplacemkr!=null){
                           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                   new LatLng(finalplacemkr.getPosition().latitude,
                                           finalplacemkr.getPosition().longitude), DEFAULT_ZOOM));
                       }
                    }
                }
            });
            fabtest.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if(isManager){
                        if(!managerflag){
                            String s ="true";
                            ManagerFlagReference.setValue(s);
                            Toast.makeText(getApplicationContext(),"開啟Fab標記給其他成員", Toast.LENGTH_SHORT).show();

                        }
                        else{
                            String s ="false";
                            ManagerFlagReference.setValue(s);
                            Toast.makeText(getApplicationContext(),"關閉Fab標記給其他成員", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });
        }
        //下方工作列按鈕及各項監聽實作
        send = (ImageView) findViewById(R.id.sendButton);
        addpic = (ImageView) findViewById( R.id.pic);
        chat = (ImageView) findViewById( R.id.chatB);

        addpic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        addpic.setOnTouchListener(new IconSmallerOnTouchListener());

        chat.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(!flag_chatlist){
                    View mapv = getSupportFragmentManager().findFragmentById(R.id.map).getView();
                    if (mapv != null) {
                        mapv.setAlpha(0.3f);
                    }
                    //LV.setVisibility(View.VISIBLE);
                    Log.i("onclick","y");
                    flag_chatlist = true;
                }
                else{
                    View mapv = getSupportFragmentManager().findFragmentById(R.id.map).getView();
                    if (mapv != null) {
                        mapv.setAlpha(1.0f);
                    }
                    // LV.setVisibility(View.INVISIBLE);
                    Log.i("onclick","n");
                    flag_chatlist = false;
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                String time;
                if ( minute < 10 ){
                    time = hour +":0";
                }
                else{
                    time = hour +":"+minute;
                }
                //sendMessage();
                // TODO: Send messages on click
                Chat friendlyMessage = new Chat(mMessageEditText.getText().toString(), mUsername, null,time);
                // Clear input box
                mFirebaseRef.push().setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });
        send.setOnTouchListener(new IconSmallerOnTouchListener());

        //右側導覽列、列表頁面號碼、聊天室成員列表LIST及附近地點類型LIST
        NVr = (NavigationView) findViewById(R.id.Right_Navigation);
        NVrmenupage = 0;
        chatmember = new ArrayList<>();
        placetype = new ArrayList<>();

        //聊天紀錄
        mChatListAdapter = new FirebaseListAdapter(mFirebaseRef, this, mUsername,R.layout.chat_user1_item);
        //綁定adapter
        LV.setAdapter(mChatListAdapter);
        //如果資料有變 焦點到listview最下方

        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //移到listview最下方
                LV.setSelection(mChatListAdapter.getCount() - 1);
            }
        });

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //0725
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    // TODO: OnMapReady
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker marker)
            {
                View infoWindow;
               if(marker.getTag() == "nearplace"|| marker.getTag() == "searchmarker"||marker.getTag() == "naviplace"){
                   infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                           (FrameLayout)findViewById(R.id.map), false);
               }
               else{
                   infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents2,
                           (FrameLayout)findViewById(R.id.map), false);
               }


                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;

            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                return null;
            }

        });

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        //reloaddata
        reloaddata();
        //0730
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        mMap.setOnCameraIdleListener(this);
        //mMap.setOnCameraMoveStartedListener(this);
        //mMap.setOnCameraMoveListener(this);
        //mMap.setOnCameraMoveCanceledListener(this);

    }
    //資訊視窗點擊事件
    @Override
    public void onInfoWindowClick(final Marker marker) {
        if(isfollowmode)//追隨模式中不可使用附近地點搜尋
            return;
        if(marker.getTag() == "nearplace")//附近地點不可再進行一次附近地點搜尋
            return;
        centerpoint = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);//將選取的標記設為中心點
        finplacetype = null;//reset finplacetype
        placetype.clear();//reset placetype
        getPlaceTypeSelected("np",null,null);//進行附近地點搜尋
    }

    //資訊視窗長按事件
    @Override
    public void onInfoWindowLongClick(final Marker marker) {
        if(isfollowmode)//追隨模式中不可使用相關功能
            return;
        //判斷該標記是不是自訂標記，不是的話不做任何動作
        int size=UmarkerList.size();
        int flag=0;
        for(int m=0 ; m < size; ++m) {
            Marker mkr;
            mkr = UmarkerList.get(m);
            String tag = (String) mkr.getTag();
            String tagg = (String) marker.getTag();
            if(tag!=null &&tagg!=null)
                if (tag.equals(tagg)) {
                    flag=1;
                }
        }
        if(flag == 0){
            return ;
        }
        //自訂標記的相關功能-修改刪除
        String t = marker.getTitle();
        final LatLng L = marker.getPosition();
        final View item = LayoutInflater.from(MapsActivity.this).inflate(R.layout.modify_mkrtitle, null);
        new AlertDialog.Builder(MapsActivity.this,R.style.MyDialogTheme)
                .setTitle(t)
                .setCancelable(true)
                .setMessage(R.string.Latitude + L.latitude + "\n" + R.string.Longtitude + L.longitude)
                .setPositiveButton(R.string.Change_Title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(MapsActivity.this,R.style.MyDialogTheme)
                                .setTitle("修改自訂標記名稱")
                                .setView(item)
                                .setCancelable(true)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EditText e = (EditText) item.findViewById(R.id.edit_text);
                                        String s = e.getText().toString();
                                        if (TextUtils.isEmpty(s)) {
                                            e.setError("Name can not be empty");
                                        } else {
                                            marker.setTitle(e.getText().toString());
                                            final LatLng L = marker.getPosition();
                                            final String key = (String) marker.getTag();
                                            ValueEventListener postListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    UserMkr usermkr = dataSnapshot.getValue(UserMkr.class);
                                                    UserMkr umkr ;
                                                    if (usermkr != null) {
                                                        umkr = new UserMkr(Double.toString(L.latitude), Double.toString(L.longitude), marker.getTitle(), usermkr.Email);
                                                        mUMkrReference.child(key).setValue(umkr);
                                                        Toast.makeText(getApplicationContext(), R.string.success_modify, Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    // Getting Post failed, log a message
                                                    // ...
                                                }
                                            };
                                            mUMkrReference.child(key).addListenerForSingleValueEvent(postListener);

                                        }


                                    }
                                })
                                .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();

                    }
                })
                .setNegativeButton(R.string.del_mkr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = (String) marker.getTag();
                        ValueEventListener postListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                UserMkr usermkr = dataSnapshot.getValue(UserMkr.class);
                                if (usermkr != null) {
                                    lastemail = usermkr.Email;
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                // ...
                            }
                        };
                        mUMkrReference.child(key).addListenerForSingleValueEvent(postListener);
                        mUMkrReference.child(key).removeValue();
                        lastkey=key;


                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // TODO: GetDeviceLocation
    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            submitlocation(mLastKnownLocation);
        }

        // Set the map's camera position to the current location of the device.
        TinyDB tinydb;
        tinydb = new TinyDB(this);
        User_MapSettings Ums = (User_MapSettings) tinydb.getObject("UserMapSettings",User_MapSettings.class);
        String lat = Ums.getCurrentCameraLat();
        String lon = Ums.getCurrentCameraLon();
        String zoom = Ums.getCurrentCameraZoom();
        if(lat != null && lon != null && zoom != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Double.parseDouble(lat),
                                Double.parseDouble(lon)), Float.parseFloat(zoom)));

        }else if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mLastKnownLocation = null;
        }
    }
    //將自身位置傳送至資料庫
    public void submitlocation(Location L) {
        if(currentUser!=null){
            String userId = getUid();
            String useremail = getEmail();
            User user = new User(useremail,Double.toString(L.getLatitude()),Double.toString(L.getLongitude()) );
            mLocReference.child(userId).setValue(user);
        }
    }
    //使用者的UID
    public String getUid() {
        return currentUser.getUid();
    }
    //使用者的信箱
    public String getEmail() {
        return currentUser.getEmail();
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    public void onDestroy(){
        super.onDestroy();
        //移除所有的監聽事件及清空相關LIST
        if (mChildEventListener != null) {
            mLocReference.removeEventListener(mChildEventListener);
        }
        if(markersList!=null){
            for(int pm=0; pm<markersList.size(); pm++){
                if(markersList.get(pm) !=null)
                    markersList.get(pm).remove();
            }
        }
        if(mUMkraddChildEventListener!=null){
            mUMkrReference.removeEventListener(mUMkraddChildEventListener);
        }
        if(UmarkerList!=null){
            for(int pm=0; pm<UmarkerList.size(); pm++){
                if(UmarkerList.get(pm) !=null)
                    UmarkerList.get(pm).remove();
            }
        }
        if(chatmember!=null){
            for(int pm=0; pm<chatmember.size(); pm++){
                if(chatmember.get(pm) !=null){
                    chatmember.remove(pm);
                }
            }
        }
        if(mManagerValueEventListener!=null){
            ManagerCameraReference.removeEventListener(mManagerValueEventListener);
        }
        if(mNearPlaceChildEventListener != null){
            NearplaceReference.removeEventListener(mNearPlaceChildEventListener);
        }
        if(mCenterPointListener!=null){
            CenterpointReference.removeEventListener(mCenterPointListener);
        }
        if(mFollowMarkerValueEventListener != null){
            ManagerMarkerReference.removeEventListener(mFollowMarkerValueEventListener);
        }
        if(mSearchmkrListener != null){
            SearchmkrReference.removeEventListener(mSearchmkrListener);
        }
        if(mManagerFlagEventListener !=null){
            ManagerFlagReference.removeEventListener(mManagerFlagEventListener);
        }
        if(mFinalPlaceEventListener !=null){
            FinalPlaceReference.removeEventListener(mFinalPlaceEventListener);
        }
        if(finalplacemkr !=null){
            finalplacemkr.remove();
        }
        if( mManagerPolylineChildEventListener != null) {
            ManagerPolylineReference.removeEventListener(mManagerPolylineChildEventListener);
        }

    }

    public void onStop() {
        super.onStop();
        TinyDB tinydb;
        tinydb = new TinyDB(this);
        User_MapSettings Ums = (User_MapSettings) tinydb.getObject("UserMapSettings",User_MapSettings.class);
        String locreq = Ums.getIslocreq();
        if(currentCameraLat != null && currentCameraLon != null && currentCameraZoom != null) {
            User_MapSettings UserMapSettings = new User_MapSettings(getEmail(),locreq,String.valueOf(currentCameraLat),
                    String.valueOf(currentCameraLon),String.valueOf(currentCameraZoom));
            tinydb.putObject("UserMapSettings", UserMapSettings);
        }
        if(placeMarkers.size() > 0){
            ArrayList<Nearplace> np = new ArrayList<>();
            for(int i = 0;i<placeMarkers.size();i++){
                Nearplace n = new Nearplace(placeMarkers.get(i).getTitle(), placeMarkers.get(i).getSnippet()
                        , String.valueOf(placeMarkers.get(i).getPosition().latitude), String.valueOf(placeMarkers.get(i).getPosition().longitude));
                np.add(i,n);
            }
            tinydb.putListNearplace(Chatroom_Key+"Nearplace",np);
            Log.i("savenearplace : ",String.valueOf(placeMarkers.size()));
        }
        else{
            tinydb.remove(Chatroom_Key+"Nearplace");
        }

        if(naviMarkers.size() > 0){
            ArrayList<Nearplace> np = new ArrayList<>();
            for(int i = 0;i<naviMarkers.size();i++){
                Nearplace n = new Nearplace(naviMarkers.get(i).getTitle(), naviMarkers.get(i).getSnippet()
                        , String.valueOf(naviMarkers.get(i).getPosition().latitude), String.valueOf(naviMarkers.get(i).getPosition().longitude));
                np.add(i,n);
            }
            tinydb.putListNearplace(Chatroom_Key+"Naviplace",np);
            Log.i("savenaviplace : ",String.valueOf(naviMarkers.size()));
        }
        else{
            tinydb.remove(Chatroom_Key+"Naviplace");
        }

    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onRestart(){
        super.onRestart();
    }

    @Override
    public void onLocationChanged(Location l) {
        if(locreqflag == 1){
            submitlocation(l);
        }
    }
    //傳送自訂標記資料到資料庫
    private void submitUmkrlocation(String Le , String Lo ){
        String t = "Drag Marker";
        String e = getEmail();
        UserMkr umkr = new UserMkr(Le,Lo,t,e);
        mUMkrReference.push().setValue(umkr);
    }
    // TODO: Marker Drag
    //標記拖曳的三個監聽事件
    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        final LatLng L =marker.getPosition();
        final Marker m =marker;
        final String key = (String) marker.getTag();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMkr usermkr = dataSnapshot.getValue(UserMkr.class);
                UserMkr umkr ;
                if (usermkr != null && key != null) {
                    umkr = new UserMkr(Double.toString(L.latitude),Double.toString(L.longitude),m.getTitle(), usermkr.Email);
                    mUMkrReference.child(key).setValue(umkr);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        if (key != null) {
            mUMkrReference.child(key).addListenerForSingleValueEvent(postListener);
        }

    }
    //標記點擊事件
    @Override
    public boolean onMarkerClick(final Marker marker) {
        //如果是聊天室管理者，傳送自己點擊的標記，讓開啟追隨模式的成員自動同步點擊該標記
        if(isManager){
            choicefinalplacemkr = marker;
            if(Objects.equals(marker.getTag(), "nearplace")){
                for (int i = 0 ; i<placeMarkers.size();i++) {
                    if (Objects.equals(marker.getTitle(), placeMarkers.get(i).getTitle())) {
                        Sharedata sd = new Sharedata("nearplace",Integer.toString(i));
                        ManagerMarkerReference.setValue(sd);
                        return false;
                    }
                }
            }
            else if(Objects.equals(marker.getTag(), "Centermarker")){
                Sharedata sd = new Sharedata("Centermarker","NULL");
                ManagerMarkerReference.setValue(sd);
            }
            else if(Objects.equals(marker.getTag(), "searchmarker")){
                Sharedata sd = new Sharedata("searchmarker","NULL");
                ManagerMarkerReference.setValue(sd);
            }
            else{
                for(int i =0;i<markersList.size();i++){
                    if(Objects.equals(marker.getTag(),markersList.get(i).getTag())){
                        Sharedata sd = new Sharedata("usermkr",String.valueOf(markersList.get(i).getTag()));
                        ManagerMarkerReference.setValue(sd);
                        return false;
                    }
                }
                for(int i =0;i<UmarkerList.size();i++){
                    if(Objects.equals(marker.getTag(),UmarkerList.get(i).getTag())){
                        Sharedata sd = new Sharedata("Customizemkr",String.valueOf(UmarkerList.get(i).getTag()));
                        ManagerMarkerReference.setValue(sd);
                        return false;
                    }
                }
            }

            }


        return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuu, menu);
        toolbarmenu=menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btn_rnvdrawer) {
            DL.openDrawer(GravityCompat.END);
            return true;
        }
        else if(id == R.id.addusermkr){
            if(issearch){
                toolbarmenu.getItem(1).setIcon(getResources().getDrawable(R.drawable.placeholder));
                toolbarmenu.getItem(2).setVisible(true);
                bar_scrollvw.setVisibility(View.GONE);
                issearch = false;
            }
            else {
                if (UMkrNumLimit > 0) {
                    // [START_EXCLUDE]
                    final double[] CameraLat = new double[1];
                    final double[] CameraLon = new double[1];
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            CameraLat[0] = mMap.getCameraPosition().target.latitude;
                            CameraLon[0] = mMap.getCameraPosition().target.longitude;
                            submitUmkrlocation(Double.toString(CameraLat[0]), Double.toString(CameraLon[0]));
                        }
                    });

                } else {
                    Toast.makeText(MapsActivity.this, "標記已達上限，無法再增加!", Toast.LENGTH_LONG).show();
                }
            }
            return true;
        }
        else if (id == R.id.search_place)
        {
            if(!issearch){
                toolbarmenu.getItem(1).setIcon(getResources().getDrawable(R.drawable.cross));
                toolbarmenu.getItem(2).setVisible(false);
                bar_scrollvw.setVisibility(View.VISIBLE);
                issearch = true;
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(new LatLngBounds(
                                    new LatLng(21.9041171,120.85067879999997),
                                    new LatLng(25.299229,121.53658900000005)))
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    //  error.
                }
            }
            else{
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // error.
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }

    //0807
    public void setContentView(@LayoutRes int layoutResID) {
        DL = (DrawerLayout) getLayoutInflater().inflate(R.layout.activtiy_nav_drawer,null);
        FL = (FrameLayout) DL.findViewById(R.id.content_frame);
        NV = (NavigationView)DL.findViewById(R.id.Left_Navigation);
        NV.setItemIconTintList(null);
        NP_NV = (NavigationView)DL.findViewById(R.id.Right_Navigation);
        NP_NV.setItemIconTintList(null);
        LV = (ListView)DL.findViewById(R.id.chatlist);
        getLayoutInflater().inflate(layoutResID, FL, true);
        super.setContentView(DL);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setUpNavigation();
        setUpRightNavigation();
    }
    // TODO: nv_menu setting
    //左側導覽列實作
    private void setUpNavigation() {
        // Set navigation item selected listener
        NV.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.map_add_friend:
                        LayoutInflater inflater = getLayoutInflater();
                        final View convertView = (View) inflater.inflate(R.layout.nickname_edittext,null);
                        edt = (EditText) convertView.findViewById(R.id.edt_nickname);

                        final FirebaseDatabase db = FirebaseDatabase.getInstance();
                        final DatabaseReference Ref = db.getReference("users");

                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("輸入成員帳號")
                                .setView(convertView)
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if( edt.getText().toString().length() != 0 ){
                                            final Query MapAddFriend = Ref.orderByChild("account").equalTo(edt.getText().toString());
                                            MapAddFriend.addChildEventListener(new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                    if(dataSnapshot.exists()){

                                                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                                                        final DatabaseReference Refa= db.getReference();

                                                        final MyUser user = dataSnapshot.getValue(MyUser.class);
                                                        final String housekey = GV.getSearch_House_Name();
                                                        Log.v("houseKey",housekey+"123");
                                                        final House house= (House) tinydbb.getObject("newchatroom",House.class);
                                                        final String name = user.getNickname();
                                                        final Query friendcheckQuery = Refa.child("userHouseTables").child(name).orderByChild("url").equalTo(housekey);
                                                        friendcheckQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.exists()){
                                                                    Toast.makeText( getApplicationContext() ,"朋友"+edt.getText().toString()+"已經在聊天室中", Toast.LENGTH_SHORT).show();
                                                                    friendcheckQuery.removeEventListener(this);
                                                                }else{
                                                                    Toast.makeText( getApplicationContext() ,"朋友"+edt.getText().toString()+"加入聊天室", Toast.LENGTH_SHORT).show();
                                                                    Firebase MapAddFriend = new Firebase(user.getHouseTable());
                                                                    MapAddFriend.push().setValue(house);
                                                                }
                                                            }
                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {}
                                                        });
                                                    }else{
                                                        MapAddFriend.removeEventListener(this);

                                                    }
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
                                        }
                                    }
                                })
                                .setNeutralButton("取消", null)
                                .show();
                        break;
                    case R.id.getcenterpoint:
                        getcenterpoint();
                        DL.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.hidenearplace:
                        del_nearplace_mkr();
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navRealtimeLoc_Switch:
                        if(locreqflag == 1){
                            locreqflag = -1;
                            change_usersetting(false);
                            Menu m = NV.getMenu();
                            MenuItem item = m.findItem(R.id.navRealtimeLoc_Switch);
                            item.setTitle(R.string.open_realtime_access);
                            Log.i("locreqflag",String.valueOf(locreqflag));

                        }
                        else{
                            locreqflag = 1;
                            change_usersetting(true);
                            Menu m = NV.getMenu();
                            MenuItem item = m.findItem(R.id.navRealtimeLoc_Switch);
                            item.setTitle(R.string.close_realtime_access);
                            Log.i("locreqflag",String.valueOf(locreqflag));
                        }
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.followmode:
                        if(!isfollowmode){
                            isfollowmode = true;
                            Menu m = NV.getMenu();
                            MenuItem item = m.findItem(R.id.followmode);
                            MenuItem item2 = m.findItem(R.id.getcenterpoint);
                            MenuItem item3 = m.findItem(R.id.hidenearplace);
                            item.setTitle(R.string.closefollowmode);
                            item2.setVisible(false);
                            item3.setVisible(false);
                        }
                        else{
                            isfollowmode = false;
                            Menu m = NV.getMenu();
                            MenuItem item = m.findItem(R.id.followmode);
                            MenuItem item2 = m.findItem(R.id.getcenterpoint);
                            MenuItem item3 = m.findItem(R.id.hidenearplace);
                            item.setTitle(R.string.followmode);
                            item2.setVisible(true);
                            item3.setVisible(true);
                        }
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navi_ori:
                        setnavi("ori");
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navi_all:
                        setnavi("all");
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navi_customize:
                        setnavi("customize");
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navi_cleanroute:
                        del_polyline();
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navi_cleanstore:
                        del_navimkr();
                        DL.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navItemAbout:
                           /* Intent intent2 = new Intent();
                            intent2.setClass(Navigation_BaseActivity.this, About.class);
                            startActivity(intent2);
                            overridePendingTransition(0, 0);
                            finish();*/
                        break;

                    case R.id.navItemLogout:
                       /* new AlertDialog.Builder(MapsActivity.this)
                                .setTitle("Logout")
                                .setMessage("Are you sure you want to Logout?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent();
                                        intent.setClass(MapsActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();*/
                        break;
                }
                return false;

            }
        });

    }
    //上方ToolBar實作
    public void setUpToolBar() {
        //setSupportActionBar(toolbar);
        GV = (ChatApplication) this.getApplicationContext();
        String name = GV.getChatName();
        //setSupportActionBar(toolbar);
        //toolbar.setLogo(R.mipmap.ic_launcher);//设置app logo
        toolbar.setTitle(name);
        //toolbar.setLogo(R.mipmap.ic_launcher);//设置app logo
        //toolbar.setTitle(TollBarTitle[0]);//设置主標题
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DL.openDrawer(GravityCompat.START);
            }
        });
        setSupportActionBar(toolbar);
        //設定當使用者點擊ToolBar中的Navigation Icon時，Icon會隨著轉動
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle( this, DL, toolbar,R.string.open_navigation,R.string.close_navigation){
            @Override
            public void onDrawerClosed(View drawerView) {
                super .onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super .onDrawerOpened(drawerView);
                TextView Txv =(TextView)findViewById(R.id.navigation_header_userID);
                TextView Txv2 =(TextView)findViewById(R.id.nv_contact_name);
                String e = getEmail();
                Txv.setText(e);
                setchatmembermenu();
                try {
                    if (NVrmenupage == 0) {
                        Txv2.setText(R.string.chat_member_list);
                    } else if (NVrmenupage == 1) {
                        Txv2.setText(R.string.nearplacestore);
                    } else if (NVrmenupage == 2){
                        Txv2.setText(R.string.naviplacestore);
                    }
                }
                catch (Exception ex){
                    Log.i("SetText fail","Textview is outside ");
                }

            }
        };
        DL.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
    // TODO: Nearplace setting
    //查詢附近地點URL設定
    private String getDirectionsUrl(LatLng centerpoint,String radius){

        //Log.i("placetype",finplacetype);
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
                "json?location="+centerpoint.latitude+","+centerpoint.longitude+
                "&radius="+radius+"&sensor=true" +
                "&types="+finplacetype+
                "&language=zh-TW"+
                "&key=AIzaSyDa5rahbohkWotgck3IDv6votMlBeMWzS8";

    }
    //附近地點搜尋結果中的下一頁URL
    private  String nextpage(String nxt){

        return  "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                "pagetoken="+nxt+
                "&key=AIzaSyDa5rahbohkWotgck3IDv6votMlBeMWzS8";
    }

    //地圖相機監聽事件實作
    @Override
    public void onCameraIdle() {

        final double[] CameraLat = new double[1];
        final double[] CameraLon = new double[1];
        final double[] CameraZoom = new double[1];
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    CameraLat[0] = mMap.getCameraPosition().target.latitude;
                    CameraLon[0] = mMap.getCameraPosition().target.longitude;
                    CameraZoom[0] = mMap.getCameraPosition().zoom;
                    currentCameraLat = CameraLat[0];
                    currentCameraLon = CameraLon[0];
                    currentCameraZoom = CameraZoom[0];
                    if(isManager){
                        Manager m = new Manager(getEmail(),Double.toString(CameraLat[0]),Double.toString(CameraLon[0]),Double.toString(CameraZoom[0]));
                        ManagerCameraReference.setValue(m);
                    }
                }
        });

    }

    @Override
    public void onPlaceSelected(Place place) {
    }
    @Override
    public void onError(Status status) {

    }


    //附近地點查詢相關實作
    private class TransTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while(line!=null){
                    //Log.d("HTTP", line);
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("JSON", s);
            if(isnavi){
                parsenaviJSON(s);
                if (naviplaces != null ) {
                    for (int p =navimarkernum ; p < naviplaces.length+navimarkernum; p++) {
                        //will be null if a value was missing
                        if (naviplaces[p-navimarkernum] != null) {
                            naviMarkers.add(p, mMap.addMarker(naviplaces[p - navimarkernum]));
                            naviMarkers.get(p).setTag("naviplace");
                            NavigationView nv = (NavigationView) findViewById(R.id.Right_Navigation);
                        }
                    }
                    navimarkernum += naviplaces.length;
                }

            }
            else if(!searchexec) {
                parseJSON(s);
                if (nearradius >= 1000 && page == 1 && places.length < 10) {
                    nearradius += 1000;
                    String url = getDirectionsUrl(centerpoint, Integer.toString(nearradius));
                    new TransTask().execute(url);
                    return;
                /*if(places.length < 1){
                    Toast.makeText(MapsActivity.this, "中心點附近無相關地點，請重新查詢", Toast.LENGTH_LONG).show();
                }*/

                } else if (page == 1 && places.length < 10) {
                    nearradius += 250;
                    String url = getDirectionsUrl(centerpoint, Integer.toString(nearradius));
                    new TransTask().execute(url);
                    return;
                }
                if (places != null) {
                    Log.i("len", Integer.toString(places.length));
                    int p;
                    int pagen = (page - 1) * 20;
                    for (p = pagen; p < places.length + pagen; p++) {
                        //will be null if a value was missing
                        if (places[p - pagen] != null) {
                            placeMarkers.add(p,mMap.addMarker(places[p - pagen]));
                            placeMarkers.get(p).setTag("nearplace");
                            if (isManager) {
                                Nearplace n = new Nearplace(places[p - pagen].getTitle(), places[p - pagen].getSnippet()
                                        , String.valueOf(places[p - pagen].getPosition().latitude), String.valueOf(places[p - pagen].getPosition().longitude));
                                NearplaceReference.child(Integer.toString(p)).setValue(n);
                            }

                        }
                    }
                }
                while (nxtstring != null) {
                    page++;
                    sleep(2000);
                    Log.i("Start next page", "123");
                    String nxtpgeurl = nextpage(nxtstring);
                    nxtstring = null;
                    new TransTask().execute(nxtpgeurl);
                }
                Log.i("Radius", Integer.toString(nearradius));
                if (nearradius <= 750)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            centerpoint, DEFAULT_ZOOM));
                else if (nearradius == 1000)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            centerpoint, DEFAULT_ZOOM - 1));
                else if (nearradius <= 3000)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            centerpoint, DEFAULT_ZOOM - 2));
                else if (nearradius <= 6000)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            centerpoint, DEFAULT_ZOOM - 3));
                else
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            centerpoint, DEFAULT_ZOOM - 4));
            }
            else{
               MarkerOptions mkrop=parsesearchplaceJSON(s);
                if (mkrop != null){
                    toolbartxv.setText(mkrop.getTitle());
                    searchexec = false;
                    if(searchmarker == null){
                        searchmarker = mMap.addMarker(mkrop);
                        searchmarker.setTag("searchmarker");
                        searchmarker.setIcon(BitmapDescriptorFactory.defaultMarker(200));
                    }
                    else {
                        searchmarker.setPosition(mkrop.getPosition());
                        searchmarker.setTitle(mkrop.getTitle());
                        searchmarker.setSnippet(mkrop.getSnippet());
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(searchmarker.getPosition().latitude,
                                    searchmarker.getPosition().longitude), DEFAULT_ZOOM)
                    );
                    searchmarker.showInfoWindow();
                    if (isManager) {
                        Nearplace n = new Nearplace(searchmarker.getTitle(), searchmarker.getSnippet()
                                , String.valueOf(searchmarker.getPosition().latitude), String.valueOf(searchmarker.getPosition().longitude));
                       SearchmkrReference.setValue(n);
                    }
                }
            }



        }
        private MarkerOptions parsesearchplaceJSON(String s){
            try{
                JSONObject resultObject = new JSONObject(s);
                JSONObject result = resultObject.getJSONObject("result");
                    //parse each place
                    boolean missingValue;
                    LatLng placeLL=null;
                    String placeName="";
                    String vicinity="";
                    String rating="";
                    try{
                        //attempt to retrieve place data values
                        missingValue=false;
                        JSONObject loc = result.getJSONObject("geometry").getJSONObject("location");
                        placeLL = new LatLng(
                                Double.valueOf(loc.getString("lat")),
                                Double.valueOf(loc.getString("lng")));
                        vicinity = result.getString("formatted_address");
                        placeName = result.getString("name");
                        try{
                            rating = result.getString("rating");
                        }
                        catch(Exception e){
                            rating = "";
                        }

                    }
                    catch(JSONException jse){
                        missingValue=true;
                        jse.printStackTrace();
                    }
                    if(missingValue)
                        return null;

                    else{
                        return new MarkerOptions()
                                .position(placeLL)
                                .title(placeName)
                                .snippet("地址: "+vicinity+"\n"+"評分: "+rating);
                    }


            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void parsenaviJSON(String s){
            try {
                //parse JSON
                JSONObject resultObject = new JSONObject(s);
                JSONArray placesArray = resultObject.getJSONArray("results");
                naviplaces = new MarkerOptions[placesArray.length()];
                if(placesArray.length() < 1 )
                    return;
                for (int p=0; p<placesArray.length(); p++) {
                    //parse each place
                    boolean missingValue;
                    LatLng placeLL=null;
                    String placeName="";
                    String vicinity="";
                    String rating="";
                    try{
                        //attempt to retrieve place data values
                        missingValue=false;
                        JSONObject placeObject = placesArray.getJSONObject(p);
                        JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                        placeLL = new LatLng(
                                Double.valueOf(loc.getString("lat")),
                                Double.valueOf(loc.getString("lng")));
                        vicinity = placeObject.getString("vicinity");
                        placeName = placeObject.getString("name");
                        try{
                            rating = placeObject.getString("rating");
                        }
                        catch(Exception e){
                            rating = "";
                        }

                    }
                    catch(JSONException jse){
                        missingValue=true;
                        jse.printStackTrace();
                    }
                    if(missingValue)
                        naviplaces[p]=null;
                    else{
                        naviplaces[p]=new MarkerOptions()
                                .position(placeLL)
                                .title(placeName)
                                .snippet("地址: "+vicinity+"\n"+"評分: "+rating);
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private void parseJSON(String s) {
            try {
                //parse JSON
                JSONObject resultObject = new JSONObject(s);
                JSONArray placesArray = resultObject.getJSONArray("results");

                try{
                    nxtstring = resultObject.getString("next_page_token");
                }
                catch(Exception e)
                {
                    String i="NuLL";
                    Log.i("No next!",i);
                }
                // Log.i("nxtstring: ",nxtstring);
                places = new MarkerOptions[placesArray.length()];
                if(placesArray.length() < 10 )
                    return;
                for (int p=0; p<placesArray.length(); p++) {
                    //parse each place
                    boolean missingValue;
                    LatLng placeLL=null;
                    String placeName="";
                    String vicinity="";
                    String rating="";
                    try{
                        //attempt to retrieve place data values
                        missingValue=false;
                        JSONObject placeObject = placesArray.getJSONObject(p);
                        JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                        placeLL = new LatLng(
                                Double.valueOf(loc.getString("lat")),
                                Double.valueOf(loc.getString("lng")));
                        JSONArray types = placeObject.getJSONArray("types");
                        for(int t=0; t<types.length(); t++){
                            String thisType=types.get(t).toString();
                            //what type is it
                            if(thisType.contains("food")){
                                //currIcon = foodIcon;
                                break;
                            }
                            else if(thisType.contains("bar")){
                                //currIcon = drinkIcon;
                                break;
                            }
                            else if(thisType.contains("store")){
                                //currIcon = shopIcon;
                                break;
                            }
                        }
                        vicinity = placeObject.getString("vicinity");
                        placeName = placeObject.getString("name");
                        try{
                            rating = placeObject.getString("rating");
                        }
                        catch(Exception e){
                            rating = "";
                        }

                    }
                    catch(JSONException jse){
                        missingValue=true;
                        jse.printStackTrace();
                    }
                    if(missingValue)
                        places[p]=null;

                    else{
                        JSONObject placeObject = placesArray.getJSONObject(p);
                        JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                        Double mkr_lat = Double.valueOf(loc.getString("lat"));
                        Double mkr_lng = Double.valueOf(loc.getString("lng"));
                        Double user_lat = 0.0 ;
                        Double user_lng = 0.0 ;
                        int size=markersList.size();
                        for(int m=0 ; m < size; ++m) {
                            Marker mkr;
                            mkr = markersList.get(m);
                            String key = (String) mkr.getTitle();
                            if(key != null && key.equals(getEmail())){
                                user_lat = mkr.getPosition().latitude;
                                user_lng = mkr.getPosition().longitude;
                            }

                        }
                        Double Distance = DisConculate(user_lat,user_lng,mkr_lat,mkr_lng);

                        if( Distance < 999.99 ){
                            places[p]=new MarkerOptions()
                                    .position(placeLL)
                                    .title(placeName)
                                    .snippet("地址: "+vicinity+"\n"+"評分: "+rating+"\n距離: "+Distance+"M");
                        }else{
                            Distance = Distance /1000;
                            places[p]=new MarkerOptions()
                                    .position(placeLL)
                                    .title(placeName)
                                    .snippet("地址: "+vicinity+"\n"+"評分: "+rating+"\n距離: "+Distance+"KM");
                        }


                    }

                }



            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    //取得中心點的實作
    public void getcenterpoint(){
        //final LatLng[] centerpoint = new LatLng[1];
        final double[] lat = {0.0};
        final double[] lon = {0.0};
        final double[] num = {0.0};

        ValueEventListener getcenterpointListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Log.v(TAG,""+ postSnapshot.getKey());
                    User user = postSnapshot.getValue(User.class);
                    if(user!= null){
                        lat[0] +=  Double.parseDouble(user.Lat);
                        Log.i("lat",user.Lat);
                        lon[0] +=  Double.parseDouble(user.Lon);
                        num[0] +=  1.0;
                    }
                }
                lat[0]/=num[0];
                lon[0]/=num[0];
                Log.i("Lat",Double.toString(lat[0]));
                centerpoint = new LatLng(lat[0],lon[0]);
                if(centermarker != null){
                    centermarker.remove();
                }
                centermarker = mMap.addMarker(new MarkerOptions().position(centerpoint)
                        .title("Center Point")
                        .icon(BitmapDescriptorFactory.defaultMarker(100))
                        .draggable(false));
                centermarker.setTag("Centermarker");
                CenterPoint cp = new CenterPoint(Double.toString(lat[0]),Double.toString(lon[0]));
                if(isManager){
                    CenterpointReference.setValue(cp);
                }
                finplacetype = null;//reset finplacetype
                placetype.clear();//reset placetype
                getPlaceTypeSelected("np",null,null);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(centerpoint.latitude,
                                centerpoint.longitude), DEFAULT_ZOOM)
                );
                centermarker.showInfoWindow();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        mLocReference.addListenerForSingleValueEvent(getcenterpointListener);


    }
    // TODO: NV_r menu setting
    //右側導覽列實作
    private void setUpRightNavigation(){

        NP_NV.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //String s = (String) menuItem.getTitle();
                    int cp = menuItem.getGroupId();
                    if(cp == 0){
                        TextView Txv2 =(TextView) NVr.findViewById(R.id.nv_contact_name);
                        NVrmenupage=(NVrmenupage+1)%3;
                        setchatmembermenu();
                        try{
                            if(NVrmenupage == 0)
                                Txv2.setText(R.string.chat_member_list);
                            else if(NVrmenupage == 1)
                                Txv2.setText(R.string.nearplacestore);
                            else
                                Txv2.setText(R.string.naviplacestore);
                        }
                        catch (Exception e){
                            Log.i("Err","");
                        }
                    }
                    else{
                        if(cp == 1){
                            int itemid = menuItem.getItemId();
                            Marker mkr = markersList.get(itemid);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mkr.getPosition().latitude,
                                            mkr.getPosition().longitude), DEFAULT_ZOOM)
                            );
                            mkr.showInfoWindow();

                        }
                        else if(cp == 2){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(centerpoint.latitude,
                                            centerpoint.longitude), DEFAULT_ZOOM)
                            );
                            centermarker.showInfoWindow();

                        }
                        else if (cp == 3){
                            int itemid = menuItem.getItemId();
                            Marker mkr = UmarkerList.get(itemid);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mkr.getPosition().latitude,
                                            mkr.getPosition().longitude),DEFAULT_ZOOM)
                            );
                            mkr.showInfoWindow();
                        }
                        else if (cp == 4){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(searchmarker.getPosition().latitude,
                                            searchmarker.getPosition().longitude),DEFAULT_ZOOM)
                            );
                            searchmarker.showInfoWindow();
                        }
                        else if(cp == 5){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(finalplacemkr.getPosition().latitude,
                                            finalplacemkr.getPosition().longitude),DEFAULT_ZOOM)
                            );
                            finalplacemkr.showInfoWindow();
                        }
                        else if (cp == 6) {
                            for (int p = 0; p < placeMarkers.size(); p++) {
                                //will be null if a value was missing
                                if (placeMarkers.get(p) != null) {
                                    placeMarkers.get(p).setIcon(BitmapDescriptorFactory.defaultMarker(0));
                                    placeMarkers.get(p).setAlpha(0.6f);
                                }
                            }
                            int id = menuItem.getItemId();
                            placeMarkers.get(id).setIcon(BitmapDescriptorFactory.defaultMarker(20));
                            placeMarkers.get(id).setAlpha(1.0f);
                            placeMarkers.get(id).showInfoWindow();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(placeMarkers.get(id).getPosition().latitude,
                                            placeMarkers.get(id).getPosition().longitude), DEFAULT_ZOOM)
                            );
                        }
                        else if( cp == 7){
                            for (int p = 0; p < naviMarkers.size(); p++) {
                                //will be null if a value was missing
                                if (naviMarkers.get(p) != null) {
                                    naviMarkers.get(p).setIcon(BitmapDescriptorFactory.defaultMarker(0));
                                    naviMarkers.get(p).setAlpha(0.6f);
                                }
                            }
                            int id = menuItem.getItemId();
                            naviMarkers.get(id).setIcon(BitmapDescriptorFactory.defaultMarker(20));
                            naviMarkers.get(id).setAlpha(1.0f);
                            naviMarkers.get(id).showInfoWindow();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(naviMarkers.get(id).getPosition().latitude,
                                            naviMarkers.get(id).getPosition().longitude), DEFAULT_ZOOM)
                            );
                        }
                        DL.closeDrawer(Gravity.END);
                    }
                return false;
            }
        });
    }

    //右側導覽列-聊天室成員列表實作
    private void setchatmembermenu(){
        Menu menu = NVr.getMenu();
        menu.clear();
        menu.add(0,0,Menu.NONE,"切換頁面");
        if(NVrmenupage == 0) {
            for (int i = 0; i < chatmember.size(); ++i) {
                menu.add(1, i, Menu.NONE, chatmember.get(i));
            }
            if (centermarker != null) {
                menu.add(2, 0, Menu.NONE, "中心點");
            }
            if (UmarkerList.size() > 0) {
                for (int i = 0; i < UmarkerList.size(); ++i) {
                    Marker mkr = UmarkerList.get(i);
                    menu.add(3, i, Menu.NONE, mkr.getTitle());
                }
            }
            if (searchmarker != null) {
                menu.add(4, 0, Menu.NONE, searchmarker.getTitle());
            }
            if (finalplacemkr != null) {
                menu.add(5, 0, Menu.NONE, finalplacemkr.getTitle() + "(fin)");
            }
        }
        if(placeMarkers.size() > 0 && NVrmenupage == 1){
            for(int i = 0 ; i < placeMarkers.size();++i){
                Marker mkr = placeMarkers.get(i);
                menu.add(6,i,Menu.NONE,mkr.getTitle());
            }
        }
        if(naviMarkers.size() > 0 && NVrmenupage == 2){
            for(int i = 0 ; i < naviMarkers.size();++i){
                Marker mkr =naviMarkers.get(i);
                menu.add(7,i,Menu.NONE,mkr.getTitle());
            }
        }
    }

    public void set_map_member_loc(){
        //0724
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildADD:" + dataSnapshot.getKey());
                User user = dataSnapshot.getValue(User.class);
                if(user!=null){
                    String userKey = dataSnapshot.getKey();
                    // [START_EXCLUDE]
                    LatLng latLng = new LatLng(Double.parseDouble(user.Lat), Double.parseDouble(user.Lon));
                    //Log.i("Lat",user.Lat);
                    Marker marker=mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(user.email)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                    marker.setTag(userKey);
                    markersList.add(marker);
                    chatmember.add(user.email);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                String userKey = dataSnapshot.getKey();
                LatLng latLng ;
                if (user != null) {
                    latLng = new LatLng(Double.parseDouble(user.Lat), Double.parseDouble(user.Lon));
                    int size=markersList.size();
                    // Log.i("size:",String.valueOf(size));
                    for(int m=0 ; m < size; ++m) {
                        Marker mkr;
                        mkr = markersList.get(m);
                        String tag= String.valueOf(mkr.getTag());
                        Log.i("tag:",tag);
                        if(tag.equals(userKey))
                        {
                            mkr.setPosition(latLng);
                            //Log.i("size:",String.valueOf(size));
                        }
                    }
                }
                // [START_EXCLUDE]

                //Log.i("Lat",user.Lat);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mLocReference.addChildEventListener(childEventListener);
        mChildEventListener = childEventListener;
    }

    public void set_map_customize_mkr(){
        //0730
        ChildEventListener UmarkerchildEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                String key = dataSnapshot.getKey();
                UserMkr usermkr = dataSnapshot.getValue(UserMkr.class);
                String email=getEmail();
                // [START_EXCLUDE]
                LatLng latLng ;
                if (usermkr != null) {
                    latLng = new LatLng(Double.parseDouble(usermkr.Lat), Double.parseDouble(usermkr.Lon));
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(usermkr.Title)
                            .icon(BitmapDescriptorFactory.defaultMarker(60))
                            .draggable(true));
                    marker.setTag(key);
                    UmarkerList.add(marker);
                    if(email.equals(usermkr.Email)){
                        UMkrNumLimit--;
                    }
                }

                // [END_EXCLUDE]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                UserMkr usermkr = dataSnapshot.getValue(UserMkr.class);
                String key = dataSnapshot.getKey();
                LatLng latLng ;
                if (usermkr != null) {
                    latLng = new LatLng(Double.parseDouble(usermkr.Lat), Double.parseDouble(usermkr.Lon));
                    if(usermkr.Email.equals(getEmail())){
                        Log.i("NUM: ",Integer.toString(UMkrNumLimit));
                    }
                    // [START_EXCLUDE]
                    int size=UmarkerList.size();
                    // Log.i("size:",String.valueOf(size));
                    for(int m=0 ; m < size; ++m) {
                        Marker mkr;
                        mkr = UmarkerList.get(m);
                        String tag= String.valueOf(mkr.getTag());
                        if (tag.equals(key)) {
                            mkr.setPosition(latLng);
                            mkr.setTitle(usermkr.Title);
                        }

                    }
                }
                //Log.i("Lat",user.Lat);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                UserMkr usermkr = dataSnapshot.getValue(UserMkr.class);
                String key = dataSnapshot.getKey();
                String email;
                String e =getEmail();
                if (usermkr != null) {
                    email = usermkr.Email;
                    int size=UmarkerList.size();
                    for (int m = 0; m < size; ++m) {
                        Marker mkr;
                        mkr = UmarkerList.get(m);
                        if (key.equals(mkr.getTag())) {
                            mkr.remove();
                            UmarkerList.remove(m);
                        }
                    }
                    if (e.equals(email)) {
                        UMkrNumLimit += 1;
                    }
                }




            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mUMkrReference.addChildEventListener(UmarkerchildEventListener);
        mUMkraddChildEventListener = UmarkerchildEventListener;

    }

    private void setupUsername() {
        TinyDB tinydb;
        tinydb = new TinyDB(this);
        MyUser myUser = (MyUser) tinydb.getObject("MyUser",MyUser.class);
        if(myUser.getTruenickname() == null){
            mUsername=myUser.getAccount();
        }else{
            mUsername=myUser.getTruenickname();
        }
    }

    private void getPlaceTypeSelected(final String mode, final LatLng start, final LatLng end ){
        final View item = LayoutInflater.from(MapsActivity.this).inflate(R.layout.placetypeselected, null);
        for(int i = 0;i<4;i++){
            placetype.add("null");
        }
        new AlertDialog.Builder(MapsActivity.this,R.style.MyDialogTheme)
                .setTitle("選擇地點類型")
                .setView(item)
                .setCancelable(true)
                .setPositiveButton("確認", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0 ; i<placetype.size();++i){
                            if(!Objects.equals(placetype.get(i), "null") && placetype.get(i) != null) {
                                if (finplacetype != null) {
                                    finplacetype = finplacetype + "|";
                                    finplacetype = finplacetype + placetype.get(i);
                                }
                                else{
                                    finplacetype = placetype.get(i);
                                }

                            }

                        }
                        if(mode.equals("np")) {
                            if(isManager) {
                                NearplaceReference.removeValue();
                            }
                            del_nearplace_mkr();//Clear exist nearplace Marker
                            NVr.getMenu().clear();//Clear right menu list
                            setchatmembermenu();
                            page = 1;//reset result page
                            nearradius = 250; //default search radius
                            String url = getDirectionsUrl(centerpoint, "250");//get search url
                            isnavi = false;
                            new TransTask().execute(url);//search nearplace
                        }
                        else if(mode.equals("nv")){
                            del_navimkr();
                            NVr.getMenu().clear();//Clear right menu list
                            setchatmembermenu();
                            navilib(start,end,true,"customize",0);
                        }
                    }

                })
                .setNegativeButton("取消", null)
                .show();

        CheckBox c1 = (CheckBox)item.findViewById(R.id.cb_restaurant);
        CheckBox c2 = (CheckBox)item.findViewById(R.id.cb_coffee);
        CheckBox c3 = (CheckBox)item.findViewById(R.id.cb_shoppingmall);
        CheckBox c4 = (CheckBox)item.findViewById(R.id.cb_convenience_store);
        c1.setOnCheckedChangeListener(mOnCheckedChangeListener);
        c2.setOnCheckedChangeListener(mOnCheckedChangeListener);
        c3.setOnCheckedChangeListener(mOnCheckedChangeListener);
        c4.setOnCheckedChangeListener(mOnCheckedChangeListener);




    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.cb_restaurant:
                    if(isChecked) {
                        placetype.add(0,"restaurant");
                    } else{
                        placetype.set(0,"null");
                    }
                    break;
                case R.id.cb_coffee:
                    if(isChecked) {
                        placetype.add(1,"cafe");
                    } else{
                        placetype.set(1,"null");
                    }
                    break;
                case R.id.cb_shoppingmall:
                    if(isChecked) {
                        placetype.add(2,"shopping_mall");

                    } else{
                        placetype.set(2,"null");
                    }
                    break;
                case R.id.cb_convenience_store:
                    if(isChecked) {
                        placetype.add(3,"convenience_store");

                    } else{
                        placetype.set(3,"null");
                    }
                    break;


            }
        }

    };


    private void del_nearplace_mkr() {
        if (placeMarkers.size() > 0) {
            for(int i = 0 ; i<placeMarkers.size();i++)
                placeMarkers.get(i).remove();
        }
        placeMarkers = new ArrayList<Marker>();
    }

    private void del_navimkr(){
        if (naviMarkers.size() > 0) {
            for (int i = 0;i<naviMarkers.size();i++)
                naviMarkers.get(i).remove();
        }
        naviMarkers = new ArrayList<Marker>();
    }

    private void  del_polyline(){
        if (navipolyline.size() > 0) {
            for (int i = 0 ; i < navipolyline.size() ; i++) {
                navipolyline.get(i).remove();

            }
        }
        if(str_navipolyline.size() > 0){
            for(int i = 0;i<str_navipolyline.size() ; i++){
                str_navipolyline.remove(i);
            }
        }
        tinydbb.remove(Chatroom_Key+"Polyline");
        str_navipolyline = new ArrayList<>();
        navipolyline = new ArrayList<Polyline>();
    }

    private void set_manager(){

        ValueEventListener ManagerEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Manager m = dataSnapshot.getValue(Manager.class);
                if(!isManager && isfollowmode && m!=null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(Double.parseDouble(m.Camerapos_lat),
                                    Double.parseDouble(m.Camerapos_lon)), Float.parseFloat(m.Camerapos_zoom)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        ManagerCameraReference.addValueEventListener(ManagerEventListener);
        mManagerValueEventListener = ManagerEventListener;


    }

    private void check_manager(){
        ValueEventListener ManagerValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Manager m = dataSnapshot.getValue(Manager.class);
                if (m == null){
                    m = new Manager(getEmail(),"0.0","0.0","15");
                    ManagerCameraReference.setValue(m);
                    isManager = true;
                    Menu menu = NV.getMenu();
                    MenuItem item = menu.findItem(R.id.followmode);
                    item.setVisible(false);
                }
                else {
                    if( getEmail().equals(m.manager_email)) {
                        setfabvisible(true);
                        isManager = true;
                        ManagerFlagReference.setValue("false");
                        Toast.makeText(getApplicationContext(),"Manger", Toast.LENGTH_SHORT).show();
                        Menu menu = NV.getMenu();
                        MenuItem item = menu.findItem(R.id.followmode);
                        item.setVisible(false);

                    }
                    else{
                        isManager = false;
                        setfabvisible(false);
                        Toast.makeText(getApplicationContext(),"Not Manger", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        ManagerCameraReference.addListenerForSingleValueEvent(ManagerValueEventListener);

    }

    public  void  set_usersetting(){
        TinyDB tinydb;
        tinydb = new TinyDB(this);
        try{
            User_MapSettings UserMapSettings = (User_MapSettings) tinydb.getObject("UserMapSettings",User_MapSettings.class);
            String locreq =UserMapSettings.getIslocreq();
            if(locreq !=null){
                if (Objects.equals(locreq, "true")){
                    locreqflag = 1;
                    Menu m = NV.getMenu();
                    MenuItem item = m.findItem(R.id.navRealtimeLoc_Switch);
                    item.setTitle(R.string.close_realtime_access);
                    Log.i("set locreqflag",String.valueOf(locreqflag));
                }
                else {
                    locreqflag = -1;
                    Menu m = NV.getMenu();
                    MenuItem item = m.findItem(R.id.navRealtimeLoc_Switch);
                    item.setTitle(R.string.open_realtime_access);
                    Log.i("set locreqflag",String.valueOf(locreqflag));
                }

            }
        }
        catch (Exception e){
            User_MapSettings UserMapSettings = new User_MapSettings(getEmail(),"false");
            tinydb.putObject("UserMapSettings",UserMapSettings);
        }

    }

    public void change_usersetting(boolean b){
        TinyDB tinydb;
        tinydb = new TinyDB(this);
        User_MapSettings Ums = (User_MapSettings) tinydb.getObject("UserMapSettings",User_MapSettings.class);
        String lat = Ums.getCurrentCameraLat();
        String lon = Ums.getCurrentCameraLon();
        String zoom = Ums.getCurrentCameraZoom();
        if(lat == null || lon == null || zoom == null) {
            if (b) {
                User_MapSettings UserMapSettings = new User_MapSettings(getEmail(), "true");
                tinydb.putObject("UserMapSettings", UserMapSettings);
            } else {
                User_MapSettings UserMapSettings = new User_MapSettings(getEmail(), "false");
                tinydb.putObject("UserMapSettings", UserMapSettings);
            }
        }
        else{
            if (b) {
                User_MapSettings UserMapSettings = new User_MapSettings(getEmail(), "true",lat,lon,zoom);
                tinydb.putObject("UserMapSettings", UserMapSettings);
            } else {
                User_MapSettings UserMapSettings = new User_MapSettings(getEmail(), "false",lat,lon,zoom);
                tinydb.putObject("UserMapSettings", UserMapSettings);
            }
        }
    }

    private void set_nearplace_mkr() {
        ChildEventListener NearplaceEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(isfollowmode){
                    String key = dataSnapshot.getKey();
                    Nearplace np = dataSnapshot.getValue(Nearplace.class);
                    if(np!=null){
                         LatLng l = new LatLng(Double.parseDouble(np.lat),Double.parseDouble(np.lon));
                        int ky =Integer.parseInt(key);
                        placeMarkers.add(ky, mMap.addMarker(new MarkerOptions().position(l)
                                .title(np.title)
                                .snippet(np.vicinity)
                                .icon(BitmapDescriptorFactory.defaultMarker(0))
                                .draggable(false)));
                        placeMarkers.get(ky).setTag("nearplace");
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                del_nearplace_mkr();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        NearplaceReference.addChildEventListener(NearplaceEventListener);
        mNearPlaceChildEventListener = NearplaceEventListener;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK ){
            Uri selectedImageUri = data.getData();
            StorageReference photoRef = mChatPhotosStorageReferenece.child(selectedImageUri.getLastPathSegment());

            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);
                    String time;
                    if ( minute < 10 ){
                        time = hour +":0"+minute;
                    }
                    else{
                        time = hour +":"+minute;
                    }
                    Chat friendlyMessage = null;
                    if (downloadUrl != null) {
                        friendlyMessage = new Chat(null, mUsername , downloadUrl.toString(),time);
                    }
                    mFirebaseRef.push().setValue(friendlyMessage);
                }
            });
        }
        else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String searchplaceuri =set_search_place_url(place.getId());
                searchexec = true;
                isnavi = false ;
                new TransTask().execute(searchplaceuri);
                //toolbartxv.setText(place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG,"cancel");
            }
        }
    }

    private String set_search_place_url(String placeid) {
                return "https://maps.googleapis.com/maps/api/place/details/json?" +
                        "placeid="+placeid+
                        "&language=zh-TW"+
                        "&key=AIzaSyDa5rahbohkWotgck3IDv6votMlBeMWzS8";

    }

    private void set_center_mkr(){
        ValueEventListener CenterpointEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isfollowmode) {
                    CenterPoint cp = dataSnapshot.getValue(CenterPoint.class);
                    if (cp != null) {
                        if (centermarker != null) {
                            centermarker.remove();
                        }
                        centerpoint = new LatLng(Double.parseDouble(cp.lat),Double.parseDouble(cp.lon));
                        centermarker = mMap.addMarker(new MarkerOptions().position(centerpoint)
                                .title("Center Point ")
                                .icon(BitmapDescriptorFactory.defaultMarker(100))
                                .draggable(false));
                        centermarker.setTag("Centermarker");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        CenterpointReference.addValueEventListener(CenterpointEventListener);
        mCenterPointListener = CenterpointEventListener;

    }

    private void set_followmkr(){
        ValueEventListener FollowMarkerEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Sharedata sd = dataSnapshot.getValue(Sharedata.class);
                if(sd!=null&&isfollowmode){
                    if(Objects.equals(sd.Markertype, "nearplace")){
                        placeMarkers.get(Integer.parseInt(sd.key)).showInfoWindow();
                    }
                    if(Objects.equals(sd.Markertype, "usermkr")){
                        for(int i=0 ; i<markersList.size() ; i++){
                            if(Objects.equals(sd.key,markersList.get(i).getTag())){
                                markersList.get(i).showInfoWindow();
                            }
                        }
                    }
                    if(Objects.equals(sd.Markertype, "Customizemkr")){
                        for(int i=0 ; i<UmarkerList.size() ; i++){
                            if(Objects.equals(sd.key,UmarkerList.get(i).getTag())){
                                UmarkerList.get(i).showInfoWindow();
                            }
                        }
                    }
                    if(Objects.equals(sd.Markertype, "Centermarker")){
                        centermarker.showInfoWindow();
                    }
                    if(Objects.equals(sd.Markertype, "searchmarker")){
                        searchmarker.showInfoWindow();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        ManagerMarkerReference.addValueEventListener(FollowMarkerEventListener);
        mFollowMarkerValueEventListener=FollowMarkerEventListener;


    }

    private void set_search_mkr(){
        ValueEventListener SearchMarkerEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isfollowmode) {
                    Nearplace searchmkr = dataSnapshot.getValue(Nearplace.class);
                    if (searchmkr != null) {
                        if (searchmarker != null) {
                            searchmarker.remove();
                        }
                        searchmarker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(searchmkr.lat),Double.parseDouble(searchmkr.lon)))
                                .title(searchmkr.title)
                                .snippet(searchmkr.vicinity)
                                .icon(BitmapDescriptorFactory.defaultMarker(200))
                                .draggable(false));
                        searchmarker.setTag("searchmarker");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
         SearchmkrReference.addValueEventListener(SearchMarkerEventListener);
         mSearchmkrListener= SearchMarkerEventListener;
    }

    private void set_manageflag() {
        ValueEventListener ManagerFlagEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String s = (String) dataSnapshot.getValue();
                if (Objects.equals(s, "true")) {
                    if(isManager){
                        managerflag = true;
                        return;
                    }
                    setfabvisible(true);
                }
                else {
                    if(isManager){
                        managerflag = false;
                    }
                    else {
                        setfabvisible(false);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ManagerFlagReference.addValueEventListener(ManagerFlagEventListener);
        mManagerFlagEventListener = ManagerFlagEventListener;
    }

    private void setfabvisible(Boolean b){
        if (b){
            fabtest.show();
        }
        else{
            fabtest.hide();
        }

    }

    private double DisConculate(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    //導航實作
    private void setnavi(final String mode){
        int size = markersList.size();
        final LatLng[] origin = {null};
        final LatLng[] customize_start = new LatLng[1];
        final LatLng[] customize_end = new LatLng[1];
        switch (mode){
            case "ori":
                if(finalplacemkr == null){
                    return;
                }
                for(int n = 0;n<size;++n){
                    Marker mkr;
                    mkr = markersList.get(n);
                    if(Objects.equals(mkr.getTitle(), getEmail())){
                        origin[0] = mkr.getPosition();
                    }
                }
                if (origin[0] == null){
                    Log.i("err","origin == null");
                    return;
                }
                del_polyline();
                navilib(origin[0],finalplacemkr.getPosition(),false,mode,0);
                break;
            case "all":
                if(finalplacemkr == null){
                    return;
                }
                del_polyline();
                for(int n = 0;n<size;n++){
                    Marker mkr;
                    mkr = markersList.get(n);
                    LatLng ori = mkr.getPosition();
                    if (ori == null){
                        Log.i("err","origin == null");
                    }
                    else{
                        Log.i("err",String.valueOf(n));
                        navilib(ori,finalplacemkr.getPosition(),false,mode,n);
                    }
                }


                break;

            case "customize":
                final View item = LayoutInflater.from(MapsActivity.this).inflate(R.layout.navi_customize, null);
                final String[] type = {null};
                final String[] edtype={null};
                start_place_spin = (Spinner)item.findViewById(R.id.start_place_spinner);
                start_type_spin = (Spinner)item.findViewById(R.id.start_type_spinner) ;
                end_place_spin = (Spinner)item.findViewById(R.id.end_place_spinner);
                end_type_spin = (Spinner)item.findViewById(R.id.end_type_spinner);
                start_place_list = new ArrayList<>();
                start_type_list = new ArrayList<>();
                end_place_list = new ArrayList<>();
                end_type_list = new ArrayList<>();
                isnavi_srchshop = false;
                isnavi_showallmemeberroutes = false;
                new AlertDialog.Builder(MapsActivity.this,R.style.MyDialogTheme)
                        .setTitle("進階路徑規劃")
                        .setView(item)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(customize_start[0].equals(customize_end[0])){
                                    Toast.makeText(MapsActivity.this,"起終點不可以相同，請重新選擇",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    del_polyline();
                                    if(isnavi_showallmemeberroutes){
                                        int size = markersList.size();
                                        for(int n = 0;n<size;++n){
                                            Marker mkr;
                                            mkr = markersList.get(n);
                                            origin[0] = mkr.getPosition();
                                            if (origin[0] == null){
                                                Log.i("err","origin == null");
                                            }
                                            else{
                                                navilib(origin[0],customize_end[0],false,"all",n+1);
                                            }
                                        }

                                    }
                                    if(isnavi_srchshop){
                                        finplacetype = null;//reset finplacetype
                                        placetype.clear();//reset placetype
                                        getPlaceTypeSelected("nv",customize_start[0],customize_end[0]);
                                    }
                                    else{
                                        navilib(customize_start[0],customize_end[0],false,mode,0);
                                    }
                                }
                            }
                        })
                        .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                if(markersList!=null&&markersList.size()!=0) {
                    start_type_list.add("聊天室成員");
                    end_type_list.add("聊天室成員");
                }
                if(UmarkerList!=null&&UmarkerList.size()!=0) {
                    start_type_list.add("自訂標記");
                    end_type_list.add("自訂標記");
                }
                if(placeMarkers.size()> 0) {
                    start_type_list.add("附近地點標記");
                    end_type_list.add("附近地點標記");
                }
                if(searchmarker!=null) {
                    start_type_list.add("搜尋標記");
                    end_type_list.add("搜尋標記");
                }
                if(finalplacemkr!=null){
                    start_type_list.add("最終決定地點");
                    end_type_list.add("最終決定地點");
                }
                ArrayAdapter<String> adapter_st_type = new ArrayAdapter<>(MapsActivity.this,android.R.layout.simple_spinner_item, start_type_list);
                start_type_spin.setAdapter(adapter_st_type);
                start_type_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        start_place_list.clear();
                        type[0] = start_type_list.get(position);
                        if(start_type_list.get(position).equals("聊天室成員")){
                            for(int i =0;i<markersList.size();i++){
                                start_place_list.add(markersList.get(i).getTitle());
                            }
                        }
                        else if(start_type_list.get(position).equals("自訂標記")){
                            for(int i =0;i<UmarkerList.size();i++){
                                start_place_list.add(UmarkerList.get(i).getTitle());
                            }
                        }
                        else if(start_type_list.get(position).equals("附近地點標記")){
                            for (int i = 0;i< placeMarkers.size();i++) {
                                    start_place_list.add(placeMarkers.get(i).getTitle());
                            }
                        }
                        else if(start_type_list.get(position).equals("搜尋標記")){
                            start_place_list.add(searchmarker.getTitle());
                        }
                        else if(start_type_list.get(position).equals("最終決定地點")){
                            start_place_list.add(finalplacemkr.getTitle());
                        }

                        ArrayAdapter<String> adapter_st_place = new ArrayAdapter<>(MapsActivity.this,android.R.layout.simple_spinner_item, start_place_list);
                        start_place_spin.setAdapter(adapter_st_place);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                start_place_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(type[0].equals("聊天室成員")){
                                customize_start[0] = markersList.get(position).getPosition();
                        }
                        else if(type[0].equals("自訂標記")){
                                customize_start[0]= UmarkerList.get(position).getPosition();
                        }
                        else if(type[0].equals("附近地點標記")){
                                customize_start[0] = placeMarkers.get(position).getPosition();
                        }
                        else if(type[0].equals("搜尋標記")){
                            customize_start[0]=searchmarker.getPosition();
                        }
                        else if(type[0].equals("最終決定地點")){
                            customize_start[0]= finalplacemkr.getPosition();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });



                ArrayAdapter<String> adapter_ed = new ArrayAdapter<>(MapsActivity.this,android.R.layout.simple_spinner_item, end_type_list);
                end_type_spin.setAdapter(adapter_ed);
                end_type_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        end_place_list.clear();
                        edtype[0] = end_type_list.get(position);
                        if(end_type_list.get(position).equals("聊天室成員")){
                            for(int i =0;i<markersList.size();i++){
                                end_place_list.add(markersList.get(i).getTitle());
                            }
                        }
                        else if(end_type_list.get(position).equals("自訂標記")){
                            for(int i =0;i<UmarkerList.size();i++){
                                end_place_list.add(UmarkerList.get(i).getTitle());
                            }
                        }
                        else if(end_type_list.get(position).equals("附近地點標記")){
                            for (int i = 0;i< placeMarkers.size();i++) {
                                end_place_list.add(placeMarkers.get(i).getTitle());
                            }
                        }
                        else if(end_type_list.get(position).equals("搜尋標記")){
                            end_place_list.add(searchmarker.getTitle());
                        }
                        else if(end_type_list.get(position).equals("最終決定地點")){
                            end_place_list.add(finalplacemkr.getTitle());
                        }

                        ArrayAdapter<String> adapter_st_place = new ArrayAdapter<>(MapsActivity.this,android.R.layout.simple_spinner_item, end_place_list);
                        end_place_spin.setAdapter(adapter_st_place);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                end_place_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(edtype[0].equals("聊天室成員")){
                        customize_end[0] = markersList.get(position).getPosition();
                    }
                    else if(edtype[0].equals("自訂標記")){
                        customize_end[0]= UmarkerList.get(position).getPosition();
                    }
                    else if(edtype[0].equals("附近地點標記")){
                        customize_end[0] = placeMarkers.get(position).getPosition();
                    }
                    else if(edtype[0].equals("搜尋標記")){
                        customize_end[0]=searchmarker.getPosition();
                    }
                    else if(edtype[0].equals("最終決定地點")){
                        customize_end[0]= finalplacemkr.getPosition();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
                });

                break;

        }

    }

    public void navi_srchshop(View v){
        CheckBox checkBox = (CheckBox) v;
        if(checkBox.isChecked()){
            isnavi_srchshop = true;
        }
        else{
            isnavi_srchshop = false;
        }
    }

    public void navi_show_all_member_route(View v){
        CheckBox checkBox = (CheckBox) v;
        if(checkBox.isChecked()){
            isnavi_showallmemeberroutes = true;
        }
        else{
            isnavi_showallmemeberroutes = false;
        }
    }

    private void set_navi_url(String lat,String lon){
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
                "json?location="+lat+","+lon+
                "&radius=30&sensor=true" +
                "&types="+finplacetype+"&language=zh-TW"+
                "&key=AIzaSyDa5rahbohkWotgck3IDv6votMlBeMWzS8";
        isnavi = true ;
        new TransTask().execute(url);
    }

    private void set_finalplace(){

        ChildEventListener FinalPlaceChildListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FinalPlace fp = dataSnapshot.getValue(FinalPlace.class);
                if(fp!=null){
                    LatLng l = new LatLng(Double.parseDouble(fp.lat),Double.parseDouble(fp.lon));
                    finalplacemkr = mMap.addMarker(new MarkerOptions().position(l)
                            .title(fp.title)
                            .icon(BitmapDescriptorFactory.defaultMarker(150)));
                    finalplacemkr.setTag("FinalPlace");

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                FinalPlace fp = dataSnapshot.getValue(FinalPlace.class);
                if(fp!=null){
                    LatLng l = new LatLng(Double.parseDouble(fp.lat),Double.parseDouble(fp.lon));
                    finalplacemkr.setPosition(l);
                    Log.i("change","");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FinalPlaceReference.addChildEventListener(FinalPlaceChildListener);
        mFinalPlaceEventListener= FinalPlaceChildListener;


    }

    private void navilib(LatLng ori, LatLng dest, final Boolean searchplace,final String mode,final int num){
        String key = "AIzaSyDa5rahbohkWotgck3IDv6votMlBeMWzS8";
        GoogleDirection.withServerKey(key)
                .from(ori)
                .to(dest)
                .execute(new DirectionCallback(){
            @Override
            public void onDirectionSuccess(Direction direction, String rawBody) {
                // Do something here
                if (direction.isOK()) {
                    //mMap.addMarker(new MarkerOptions().position(ori));
                    //mMap.addMarker(new MarkerOptions().position(dest));
                    ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                    Leg leg = direction.getRouteList().get(0).getLegList().get(0);
                    Info distanceInfo = leg.getDistance();
                    String route_dis = distanceInfo.getText();
                    String[] AfterSplit = route_dis.split(" ");
                    Double routedis =Double.parseDouble(AfterSplit[0]);
                    List<LatLng> tmpline;
                    String tmps;
                    switch (mode) {
                        case "ori":
                            navipolyline.add(0, mMap.addPolyline(DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.RED)));
                            tmpline = navipolyline.get(0).getPoints();
                            tmps =PolyUtil.encode(tmpline);
                            str_navipolyline.add(0,tmps);
                            if(isManager){
                                ManagerPolylineReference.child(String.valueOf(num)).setValue(tmps);
                            }
                            break;
                        case "all":
                            try{
                            navipolyline.add(num,mMap.addPolyline(DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5,
                                    Color.parseColor(polylinecolor[num % 11]))));
                            tmpline = navipolyline.get(num).getPoints();
                            tmps = PolyUtil.encode(tmpline);
                            str_navipolyline.add(num,tmps);
                                if(isManager){
                                    ManagerPolylineReference.child(String.valueOf(num)).setValue(tmps);
                                }
                            }
                            catch(Exception e){
                                Log.i("indexerr","");
                            }
                            break;
                        case "customize":
                            navipolyline.add(0, mMap.addPolyline(DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.RED)));
                            tmpline = navipolyline.get(0).getPoints();
                            tmps =PolyUtil.encode(tmpline);
                            str_navipolyline.add(0,tmps);
                            if(isManager){
                                ManagerPolylineReference.child(String.valueOf(num)).setValue(tmps);
                            }
                            break;
                    }
                    if(routedis <= 5.0 && searchplace) {
                        navimarkernum = 0;
                        LatLng lastpoint = directionPositionList.get(0);

                        for (int i = 1; i < directionPositionList.size(); i++) {
                            Double dis = DisConculate(lastpoint.latitude, lastpoint.longitude, directionPositionList.get(i).latitude, directionPositionList.get(i).longitude);
                            if (dis >= 60 && dis <= 120) {
                                set_navi_url(String.valueOf(directionPositionList.get(i).latitude),String.valueOf(directionPositionList.get(i).longitude));
                                lastpoint = directionPositionList.get(i);
                            } else if (dis > 120) {
                                int weight = (int) (dis / 60);
                                LatLng ll;
                                for (int j = 1; j < weight; j++) {
                                    ll = new LatLng(lastpoint.latitude + (directionPositionList.get(i).latitude - lastpoint.latitude) * j / weight,
                                            lastpoint.longitude + (directionPositionList.get(i).longitude - lastpoint.longitude) * j / weight);
                                    set_navi_url(String.valueOf(ll.latitude),String.valueOf(ll.longitude));
                                }
                                set_navi_url(String.valueOf(directionPositionList.get(i).latitude),String.valueOf(directionPositionList.get(i).longitude));
                                lastpoint = directionPositionList.get(i);

                            }
                        }
                    }
                    if(navipolyline.size() > 0){
                        ArrayList<String> s = (ArrayList<String>) str_navipolyline;
                        tinydbb.remove(Chatroom_Key+"Polyline");
                        tinydbb.putListString(Chatroom_Key+"Polyline", s);
                        Log.i("save","polylinesave");
                    }
                    /*naviMarkers[p] = mMap.addMarker(naviplaces[p]);
                        naviMarkers[p].setIcon(BitmapDescriptorFactory.defaultMarker(50));
                        naviMarkers[p].setTag("naviplace");*/

                }
            }

            @Override
            public void onDirectionFailure(Throwable t) {
                // Do something here
            }
        });
    }

    private void set_polyline() {
        ChildEventListener ManagerPolylineEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                String line = (String) dataSnapshot.getValue();
                List<LatLng> polyline = PolyUtil.decode(line);
                //mMap.addPolyline(new PolylineOptions().addAll(polyline));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ManagerPolylineReference.addChildEventListener(ManagerPolylineEventListener);
        mManagerPolylineChildEventListener = ManagerPolylineEventListener;
    }

    private void reloaddata(){
        TinyDB tinydb;
        tinydb = new TinyDB(this);
        ArrayList<Nearplace> temp,temp2;
        ArrayList<String>temp3;
        temp = tinydb.getListNearplace(Chatroom_Key+"Nearplace");
        temp2 = tinydb.getListNearplace(Chatroom_Key+"Naviplace");
        temp3 = tinydb.getListString(Chatroom_Key+"Polyline");
        if(temp.size()!=0){
            for(int i = 0 ; i <temp.size();i++ ){
                LatLng l =new LatLng (Double.parseDouble(temp.get(i).lat),Double.parseDouble(temp.get(i).lon));
                try{
                    MarkerOptions option = new MarkerOptions().position(l).title(temp.get(i).title).snippet(temp.get(i).vicinity);
                    placeMarkers.add(i,mMap.addMarker(option));
                    placeMarkers.get(i).setTag("nearplace");
                }
                catch (Exception e){
                    Log.i("err:",String.valueOf(i));
                }
            }
        }

        if(temp2.size()!=0){
            for(int i = 0 ; i <temp2.size();i++ ){
                LatLng l =new LatLng (Double.parseDouble(temp2.get(i).lat),Double.parseDouble(temp2.get(i).lon));
                try{
                    MarkerOptions option = new MarkerOptions().position(l).title(temp2.get(i).title).snippet(temp2.get(i).vicinity);
                    naviMarkers.add(i,mMap.addMarker(option));
                    naviMarkers.get(i).setTag("naviplace");
                }
                catch (Exception e){
                    Log.i("err:",String.valueOf(i));
                }
            }
        }
        if(temp3.size() > 0){
            String tmpline;
            List<LatLng> polyline;
            for(int i = 0 ; i <temp3.size();i++ ){
                try{
                   str_navipolyline.add(i,temp3.get(i));
                   polyline = PolyUtil.decode(str_navipolyline.get(i));
                   navipolyline.add(i,mMap.addPolyline(new PolylineOptions().addAll(polyline).width(5).color(Color.parseColor(polylinecolor[i % 11]))));
                }
                catch (Exception e){
                    Log.i("err:",String.valueOf(i));
                }
            }

        }

    }
}



