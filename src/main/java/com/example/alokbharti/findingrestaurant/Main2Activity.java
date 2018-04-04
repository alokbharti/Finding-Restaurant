package com.example.alokbharti.findingrestaurant;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Main2Activity extends AppCompatActivity {
    private GeoDataClient mGeoDataClient;
    int stuff;
    TextView textView;
    TextView textView2;
    TextView textView3;

    double lat,lon;
    private static final int MY_PERMISSION_REQUEST_LOCATION=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        stuff = bundle.getInt("AdapterClicked");


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(Main2Activity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)){
                ActivityCompat.requestPermissions(Main2Activity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION);
            }
            else {
                ActivityCompat.requestPermissions(Main2Activity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION);
            }

        }
        else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                lat = location.getLatitude();
                lon = location.getLongitude();
                Log.d("Getting data", "Lon and lat");

                String url = getUrl(lat, lon);
                //Loading Hotels Data..
                loadHotelsInfo(url);

            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(MainActivity.this,"Turn on your location",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder dialog = new AlertDialog.Builder(Main2Activity.this);
                dialog.setTitle("Not seeing hotels info....");
                dialog.setMessage("Please make sure your Data and Location turn on");
                dialog.setNegativeButton("Ok", null);
                final AlertDialog alert = dialog.create();
                alert.show();

            }
        }

    }

    public void loadHotelsInfo(String url){

        textView3= (TextView) findViewById(R.id.hotel_phone);
        final String[] phone = {""};
        final ProgressDialog progressDialog = new ProgressDialog(Main2Activity.this);
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
                            JSONObject object = jsonArray.getJSONObject(stuff);
                            String add = object.getString("vicinity");
                            Log.d("address",add);
                            String name = object.getString("name");

                            //getting id of hotel
                            String placeId= object.getString("place_id");
                            Log.d("id",placeId);


                            //getting details of hotels
                            mGeoDataClient = Places.getGeoDataClient(Main2Activity.this, null);
                            mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                                @Override
                                public void onComplete(Task<PlaceBufferResponse> task) {
                                    if (task.isSuccessful()) {
                                        PlaceBufferResponse places = task.getResult();
                                        Place myPlace = places.get(0);
                                        phone[0] = myPlace.getPhoneNumber().toString();
                                        Log.d("Phone found: " , myPlace.getPhoneNumber()+"");
                                        places.release();

                                        textView3.setText(phone[0]);
                                    } else {
                                        Log.d("Phone not found.","not found");

                                        textView3.setText("Phone Number not found");
                                    }
                                }
                            });




                            //for finding latitude and longitude of hotels
                            JSONObject object2 = object.getJSONObject("geometry").getJSONObject("location");
                            String latitude = object2.getString("lat");
                            String longitude = object2.getString("lng");

                            Log.d("lat",latitude+"");
                            Log.d("lat",longitude+"");
                            final Double lati = Double.parseDouble(latitude);
                            final Double longi = Double.parseDouble(longitude);


                            ///for floating button
                            FloatingActionButton fab = findViewById(R.id.fab);
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                    /**
                                     Uri gmmIntentUri = Uri.parse("google.streetview:cbll:23.177092, 80.015666"+ Uri.encode("Dumna Treat Restaurant"));
                                     Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                     mapIntent.setPackage("com.google.android.apps.maps");
                                     if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                     startActivity(mapIntent);
                                     }
                                     **/
                                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                            Uri.parse("http://maps.google.com/maps?saddr="+String.valueOf(lat)+","
                                                    +String.valueOf(lon)+"&daddr="+String.valueOf(lati)+","+String.valueOf(longi)));
                                    startActivity(intent);
                                }
                            });

                            //for finding phone number of hotel
                            //String phone = GetPhoneNumber(lati,longi);

                            textView2 = (TextView) findViewById(R.id.hotel_name);
                            textView2.setText(name);
                            textView = (TextView)findViewById(R.id.hotel_details);
                            textView.setText(add);

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

    public String GetPhoneNumber(double lat, double lon){
        String hotelNumber = "";
        Geocoder geocoder = new Geocoder(Main2Activity.this, Locale.getDefault());
        List<Address> addressList;
        try{
            addressList = geocoder.getFromLocation(lat,lon,1);
            if(addressList.size()>0){
                hotelNumber = addressList.get(0).getPhone();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return hotelNumber;
    }

}
