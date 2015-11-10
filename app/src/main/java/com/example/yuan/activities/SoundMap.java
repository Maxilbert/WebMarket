package com.example.yuan.activities;

import com.example.yuan.views.RoundProgressBar;

import com.example.yuan.map4loud.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClients;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;


public class SoundMap extends FragmentActivity {

    String name = null;

    //http://128.235.40.185:8080/MyWebAppTest/ReturnData
    private GoogleMap mMap;
    //private MapView mMapView;
    private SupportMapFragment mMapFragment;
    private final int MIN_ZOOM = 11;
    private final int MAX_ZOOM = 17;

    private boolean showTwoHour = false;
    private boolean showLden = true;

    private boolean uploadFlag = false;

    private CheckBox mCheckLden;
    private CheckBox mCheckTwoHour;
    private CheckBox mCheckEvent;
    private CheckBox mCheckSource;
    private Button mBtnExpand;
    private Button mBtnCollapse;
    private SlidingUpPanelLayout mSlidingLayout;
    private RoundProgressBar mRoundProgressBar;
    //
    //private HashMap<LatLng,Float> gridDataCache = new HashMap<LatLng,Float>();
    private HashSet<LatLng> gridDataCache = new HashSet<LatLng>();
    private HashMap<LatLng, Float> gridData = new HashMap<LatLng, Float>();
    private double east, west, south, north;

    //
    private final int GRID_DATA_CACHE_LIMIT = 2000;
    private final LatLng SOUTHWEST = new LatLng(40.65, -74.25);
    private final LatLng SOUTHEAST = new LatLng(40.65, -73.85);
    private final LatLng NORTHEAST = new LatLng(40.95, -73.85);
    private final LatLng NORTHWEST = new LatLng(40.95, -74.25);

    private LocationManager locationManager;
    private String locationProvider;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        init();
    }

    private void init() {

        //Get current username
        Intent intent = getIntent();
        name = intent.getStringExtra("username");

        //
        //Get location manager provider
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Initial checkboxes and button
        mCheckLden = (CheckBox) findViewById(R.id.checkLden);
        mCheckTwoHour = (CheckBox) findViewById(R.id.checkTwoHour);
        mCheckEvent = (CheckBox) findViewById(R.id.checkEvent);
        mCheckSource = (CheckBox) findViewById(R.id.checkSource);
        mBtnExpand = (Button) findViewById(R.id.btnRecord);
        mBtnCollapse =  (Button) findViewById(R.id.btnCollapse);
        mRoundProgressBar = (RoundProgressBar) findViewById(R.id.roundProgressBar);

        //Set default camera view of Google Map through programmtically generated mapView
        //GoogleMapOptions options = new GoogleMapOptions();
        //options.mapType(GoogleMap.MAP_TYPE_NORMAL);
        //options.camera(new CameraPosition(new LatLng(40.80, -74.05), 14, 0, (float) 0.0));
        //mMapView = new MapView(this, options);
        //mMapView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //mMapView.onCreate(savedInstanceState);
        //setContentView(mMapView);

        //Set default camera view of Google Map through xml generated mapView
        //mMapView = (MapView) findViewById(R.id.mapView);
        //mMapView.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        mMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapView);
        //setContentView(mMapView);
        //mMap = mMapView.getMap();
        mMap = mMapFragment.getMap();
         double currentLatLon [] = getCurrentLatLon();
        if (40.65 < currentLatLon [0] && currentLatLon[0] < 40.95 && -74.25 < currentLatLon[1] && currentLatLon[1] < -73.85) {
            mMap.setMyLocationEnabled(true);
            CameraPosition newPosition = new CameraPosition(new LatLng(currentLatLon[0], currentLatLon[1]), 13, 0, (float) 0.0);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(newPosition);
            mMap.moveCamera(update);
            uploadFlag = true;
        }
        else {
            Toast.makeText(SoundMap.this, "You cannot upload noise data since your current location", Toast.LENGTH_LONG).show();
            CameraPosition newPosition = new CameraPosition(new LatLng(40.80, -74.05), 13, 0, (float) 0.0);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(newPosition);
            mMap.moveCamera(update);
        }
        //Set default UI of Google Map
        UiSettings us = mMap.getUiSettings();
        us.setZoomControlsEnabled(true);
        us.setZoomGesturesEnabled(true);
        us.setScrollGesturesEnabled(true);
        us.setCompassEnabled(true);
        us.setRotateGesturesEnabled(false);
        us.setTiltGesturesEnabled(false);

        //initial the bounds of sound data query;
        east = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude;
        north = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude;
        west = east;
        south = north;

        //Draw the BOUNDS of monitoring area
        drawBounds();

        //Set listener for camera view change
        mMap.setOnCameraChangeListener(onCameraChangeListener);

        //Set listener for map mode choice
        mCheckLden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCheckLden.isChecked()) {
                    mCheckTwoHour.setChecked(false);
                    showLden = true;
                    showTwoHour = false;
                    Toast.makeText(SoundMap.this,
                            "Zoom in to see Lden map",
                            Toast.LENGTH_SHORT).show();
                } else if (!mCheckLden.isChecked()) {
                    showLden = false;
                }
            }
        });
        mCheckTwoHour.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCheckTwoHour.isChecked()) {
                    mCheckLden.setChecked(false);
                    showLden = false;
                    showTwoHour = true;
                    Toast.makeText(SoundMap.this,
                            "Zoom in to see two-hour dBA map",
                            Toast.LENGTH_SHORT).show();
                } else if (!mCheckTwoHour.isChecked()) {
                    showTwoHour = false;
                }
            }
        });

        //Sliding Up panel and its Listener
        mSlidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingLayout.setAnchorPoint(0.45f);
        mSlidingLayout.setTouchEnabled(false);
        final String TAG = "onPanelSlide";
        mSlidingLayout.setPanelSlideListener(
                new SlidingUpPanelLayout.PanelSlideListener() {
                    @Override
                    public void onPanelSlide(View panel, float slideOffset) {
//                        Log.i(TAG, "onPanelSlide, offset " + slideOffset);
//                        Log.i(TAG, "main height=" + mSlidingLayout.findViewById(R.id.main).getHeight());
                    }

                    @Override
                    public void onPanelExpanded(View panel) {
                        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    }

                    @Override
                    public void onPanelCollapsed(View panel) {
                        //timer.cancel();
                    }

                    @Override
                    public void onPanelAnchored(View panel) {
                        //mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    }

                    @Override
                    public void onPanelHidden(View panel) {
                        //timer.cancel();
                    }
                }
        );

        //Collapse Button Listener
        mBtnCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                mRoundProgressBar.setProgress(0);
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });

        //Expand Button Listener
        mBtnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double lat, lon;
                double currentLatLon[] = getCurrentLatLon();
                lat = currentLatLon[0];
                lon = currentLatLon[1];
                if (40.65 < lat && lat < 40.95 && -74.25 < lon && lon < -73.85) {
                    mMap.setMyLocationEnabled(true);
                    uploadFlag = true;
                } else {
                    Toast.makeText(SoundMap.this,
                            "You cannot upload noise data since your current location",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(SoundMap.this, "Lat: " + lat + ", Lon: " + lon, Toast.LENGTH_SHORT).show();
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                //Start to record and analyze sound
                mRoundProgressBar.setVisibility(View.VISIBLE);
                if(timer!=null){
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    int progress = 0;
                    public void run() {
                        if(progress <= 100) {
                            mRoundProgressBar.setProgress(progress);
                            progress++;
                        } else {
                            //mRoundProgressBar.setVisibility(View.INVISIBLE);
                            timer.cancel();
                        }
                    }
                }, 0, 1 * 100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                    }
                }).start();
                //Start to send data to server

            }
        });
    }



    /**
     * Camera View Change Listener:
     * 1.When Zoom is greater or equal than 17, draw sound map masking for map;
     * 2. When Zoom is less or equal than 13, clear the sound map mask from view;
     * 3. Constrain area and zoom;
     */
    GoogleMap.OnCameraChangeListener onCameraChangeListener =
            new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    boolean requestFlag = false;
                    //Limit area and zoom
                    limitZoneAndZoom();
                    LatLngBounds visibleBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    //if zoom level is either 17 or 18;
                    //if zoom level is 14 or smaller, clear sound mask;
                    if (mMap.getCameraPosition().zoom >= 13) {
                        if (visibleBounds.southwest.latitude < south || visibleBounds.southwest.longitude < west) {
                            requestFlag = true;
                        }
                        if (visibleBounds.northeast.latitude > north || visibleBounds.northeast.longitude > east) {
                            requestFlag = true;
                        }
                    } else if(mMap.getCameraPosition().zoom < 13) {
                        clearSoundMask();
                    }
                    if(gridDataCache.size() > GRID_DATA_CACHE_LIMIT){
                        clearSoundMask();
                    }
                    if (requestFlag){
                        south = visibleBounds.southwest.latitude - 0.0009;//the boundry of sound mask
                        north = visibleBounds.northeast.latitude + 0.0009;//the boundry of sound mask
                        west = visibleBounds.southwest.longitude - 0.0012;//the boundry of sound mask
                        east = visibleBounds.northeast.longitude + 0.0012;//the boundry of sound mask
                        new Thread(RequestDataThread).start();
                        //System.gc();
                    }
                }
            };


    /**
     *
     */
    private void drawBounds() {
        PolylineOptions rectOptions = new PolylineOptions().add(
                SOUTHWEST,
                SOUTHEAST,
                NORTHEAST,
                NORTHWEST,
                SOUTHWEST);
        Polyline polyline = mMap.addPolyline(rectOptions
                .color(Color.BLUE)
                .width(3));
        polyline.setVisible(true);
    }


    /**
     * Add sound mask polygons
     */
    private void drawSoundMask(){
        double d1=0.0004f, d2=0.0003f;
        Iterator iter = gridData.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            LatLng key = (LatLng) entry.getKey();
            float val = (float) entry.getValue();
            PolygonOptions rectOptions = new PolygonOptions().add(
                    new LatLng((2*key.latitude - d2)/2, (2*key.longitude - d1)/2),
                    new LatLng((2*key.latitude + d2)/2, (2*key.longitude - d1)/2),
                    new LatLng((2*key.latitude + d2)/2, (2*key.longitude + d1)/2),
                    new LatLng((2*key.latitude - d2)/2, (2*key.longitude + d1)/2),
                    new LatLng((2*key.latitude - d2)/2, (2*key.longitude - d1)/2));
            if(val > 50) {
                if (val <= 100) {
                    Polygon polygon = mMap.addPolygon(rectOptions
                            .fillColor(Color.argb((int) val, (int) ((val - 50.0f) * 5.1f), (int) (255.0f - (val - 50.0f) * 5.1f), 0))
                            .strokeColor(Color.argb(0, 255, 255, 255))
                            .strokeWidth(0));
                    polygon.setVisible(true);
                } else {
                    Polygon polygon = mMap.addPolygon(rectOptions
                            .fillColor(Color.argb((int) val, 255, 0, 0))
                            .strokeColor(Color.argb(0, 255, 255, 255))
                            .strokeWidth(0));
                    polygon.setVisible(true);
                }
            } else {
                // If sound is smaller than 50 db, do not draw its polygon out.
            }
        }
    }


    /**
     * clear the sound mask (called when zoom in)
     */
    private void clearSoundMask(){
        mMap.clear();
        gridDataCache.clear();
        gridData.clear();
        east = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude;
        north = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude;
        west = east;
        south = north;
        drawBounds();
        System.gc();
    }


    /**
     * Limit the area shown in app to a rectangular area including New York City;
     */
    private void limitZoneAndZoom(){
        CameraPosition position = mMap.getCameraPosition();
        VisibleRegion region = mMap.getProjection().getVisibleRegion();
        float zoom = 0;
        if(position.zoom < MIN_ZOOM) zoom = MIN_ZOOM;
        if(position.zoom > MAX_ZOOM) zoom = MAX_ZOOM;
        LatLng correction = getLatLngCorrection(region.latLngBounds);
        if(zoom != 0 || correction.latitude != 0 || correction.longitude != 0) {
            zoom = (zoom==0) ? position.zoom : zoom;
            double lat = position.target.latitude + correction.latitude;
            double lon = position.target.longitude + correction.longitude;
            CameraPosition newPosition = new CameraPosition(new LatLng(lat,lon), zoom, position.tilt, position.bearing);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(newPosition);
            mMap.moveCamera(update);
        }
    }


    /**
     * Returns the correction for Lat and Lng if camera is trying to get outside of visible map
     * @param cameraBounds Current camera bounds
     * @return Latitude and Longitude corrections to get back into bounds.
     */
    private LatLng getLatLngCorrection(LatLngBounds cameraBounds) {
        double latitude=0, longitude=0;
        if(cameraBounds.southwest.latitude < SOUTHWEST.latitude) {
            latitude =SOUTHWEST.latitude - cameraBounds.southwest.latitude + 0.0002;
        }
        if(cameraBounds.southwest.longitude < SOUTHWEST.longitude) {
            longitude = SOUTHWEST.longitude - cameraBounds.southwest.longitude + 0.0002;
        }
        if(cameraBounds.northeast.latitude > NORTHEAST.latitude) {
            latitude = NORTHEAST.latitude - cameraBounds.northeast.latitude - 0.0002;
        }
        if(cameraBounds.northeast.longitude > NORTHEAST.longitude) {
            longitude = NORTHEAST.longitude - cameraBounds.northeast.longitude - 0.0002;
        }
        return new LatLng(latitude, longitude);
    }


    /**
     *
     */
    Handler httpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String jsonArrayString = data.getString("soundData");
            gridData.clear();
            try {
                JSONArray jsonArray = new JSONArray(jsonArrayString);
                String [] stringArray = new String [3];
                for (int index = 0; index < jsonArray.length(); index++){
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    Iterator<?> iterator = jsonObject.keys();
                    String key = null;
                    String value = null;
                    while (iterator.hasNext()) {
                        key = (String) iterator.next();
                        value = jsonObject.getString(key);
                        if (key.equals("db")){
                            stringArray[0] = value;
                        } else if (key.equals("lon")){
                            stringArray[1] = value;
                        } else if (key.equals("lat")){
                            stringArray[2] = value;
                        }
                    }
                    LatLng temp = new LatLng(Float.parseFloat(stringArray[2]), Float.parseFloat(stringArray[1]));
                    if (!gridDataCache.contains(temp) && Float.parseFloat(stringArray[0]) > 50) {
                        gridData.put(temp, Float.parseFloat(stringArray[0]));
                        gridDataCache.add(temp);
                    }
                }
                //datas.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SoundMap.this, "Error: cannot connect server.",
                        Toast.LENGTH_LONG).show();
            }
            //Start process UI
            drawSoundMask();
        }
    };


    /**
     * Get the sound map data within a rectangular zone
     * from servlet through Http post connection
     */
    Runnable RequestDataThread = new Runnable(){
        @Override
        public void run() {
            // TODO: http post.
            String result = "0";
            //Get the instance of ClosealbeHttpClient
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //The url of servlet
            //String url = "http://128.235.40.185:8080/MyWebAppTest/ReturnData";
            String url = "https://web.njit.edu/~yl768/webapps7/ReturnData";
            //New HTTP Post request
            HttpPost httpPost = new HttpPost(url);
            //Add Name Value Pairs to HTTP request
            NameValuePair pair1 = new BasicNameValuePair("east", "" + east);
            NameValuePair pair2 = new BasicNameValuePair("west", "" + west);
            NameValuePair pair3 = new BasicNameValuePair("south", "" + south);
            NameValuePair pair4 = new BasicNameValuePair("north", "" + north);
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(pair1);
            pairs.add(pair2);
            pairs.add(pair3);
            pairs.add(pair4);
            //Send Http post request
            try {
                HttpEntity requestEntity = new UrlEncodedFormEntity(pairs);
                httpPost.setEntity(requestEntity);
                CloseableHttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(entity.getContent()));
                    result = reader.readLine();
                    //Log.d("HTTP", "POST:" + result);
                } else {
                    result = "" + response.getStatusLine().getStatusCode();
                    //Log.d("HTTP", "ERROR:" + result);
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("soundData", result);
            msg.setData(data);
            httpHandler.sendMessage(msg);
        }
    };


    /**
     *
     * @return
     */
    private double[] getCurrentLatLon (){
        double currentLatLon [] = {0, 0};
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //If is GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //If is Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "No location provider", Toast.LENGTH_SHORT).show();
            currentLatLon[0] = 91; //91 is illegal for latitude
            currentLatLon[1] = 181;//181 is illegal for longitude
            return currentLatLon;
        }
        //
        //获取Location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SoundMap.this, "Don't have permission to access location ", Toast.LENGTH_SHORT).show();
                return currentLatLon;
            }
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        currentLatLon[0] = location.getLatitude();
        currentLatLon[1] = location.getLongitude();
        return currentLatLon;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mMapView.onDestroy();
    }

}