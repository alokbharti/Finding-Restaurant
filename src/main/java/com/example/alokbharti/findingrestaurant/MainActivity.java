package com.example.alokbharti.findingrestaurant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private List<ListItem> listItems;
    TextView textView;
    double lat,lon;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static final int MY_PERMISSION_REQUEST_LOCATION=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        textView = (TextView)findViewById(R.id.textview);

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Welcome !!");
        dialog.setMessage("Wanna see hotels at your Location");
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Uploading hotels of a random place say Patna
                String Url1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=25.611923,%2085.138727&radius=5000&type=restaurant&key=AIzaSyCfKpEfhwTAHNFoR9RQijZiM943AJM2z10";
                textView.setText("Name of Hotels at "+GetLocation(25.611923,85.136549));
                loadHotelsData(Url1);

            }
        });
        dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){

                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)){
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSION_REQUEST_LOCATION);
                    }
                    else {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSION_REQUEST_LOCATION);
                    }

                }else{
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    try {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        Log.d("Getting data","Lon and lat");

                        textView.setText("Name of Hotels at "+GetLocation(lat,lon));

                        String url = getUrl(lat,lon);
                        //Loading Hotels Data..
                        loadHotelsData(url);
                        //Toast.makeText(MainActivity.this,String.valueOf(lat),Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this,String.valueOf(lon),Toast.LENGTH_SHORT).show();
                       // textView.setText(GetLocation(location.getLatitude(), location.getLongitude()));


                    }catch (Exception e){
                        e.printStackTrace();
                        //Toast.makeText(MainActivity.this,"Turn on your location",Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle("Not seeing hotels....");
                        dialog.setMessage("Please make sure your Data and Location turn on");
                        dialog.setNegativeButton("Ok",null);
                        final AlertDialog alert = dialog.create();
                        alert.show();
                    }
                }

            }
        });
        final AlertDialog alert = dialog.create();
        alert.show();

        listItems = new ArrayList<>();
    }
/**
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSION_REQUEST_LOCATION:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED){

                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        try {
                            Log.d("Getting data","Lon and lat");
                            loadHotelsData(lat,lon);
                           // textView.setText(GetLocation(location.getLatitude(), location.getLongitude()));
                        }catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                        }

                    }
                    else {
                        Toast.makeText(this,"No Permission",Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

**/

    public String GetLocation(double lat, double lon){
        String CurCity = "";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addressList;
        try{
            addressList = geocoder.getFromLocation(lat,lon,1);
            if(addressList.size()>0){
                CurCity = addressList.get(0).getAddressLine(0);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return CurCity;
    }



    public void loadHotelsData(String url){
        //url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=23.176107,%2080.026518&radius=5000&type=restaurant&key=AIzaSyCfKpEfhwTAHNFoR9RQijZiM943AJM2z10";
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Hotels data....");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            Log.d("in lodeHotelsData","Reached");
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            for(int i =0;i<jsonArray.length();i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                ListItem item = new ListItem(object.getString("name"));
                                listItems.add(item);

                            }
                            mAdapter = new MyAdapter(listItems,getApplicationContext());
                            mRecyclerView.setAdapter(mAdapter);
                            Log.d("in lodeHotelsData","after adapter");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public String getUrl(double lat,double lon){
        String url;
        url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+String.valueOf(lat)+","+String.valueOf(lon)+
                "&radius=5000&type=restaurant&key=AIzaSyCfKpEfhwTAHNFoR9RQijZiM943AJM2z10";
        Log.d("url",url);
        return url;
    }
}
