package com.septian.cariterapis;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private GoogleMap maps;
    private MapView mv;
    private int permision;
    private SharedPreferences sp = null;
    private ArrayList<com.septian.cariterapis.Location> myData = new ArrayList<>();


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mv = view.findViewById(R.id.mapView);
        mv.onCreate(savedInstanceState);

        mv.onResume();

        sp =  getActivity().getBaseContext().getSharedPreferences("data",MODE_PRIVATE);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getLocationData(getActivity().getBaseContext());

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mv.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mv.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mv.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mv.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("request === ",String.valueOf(requestCode));
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    sukses
                    Log.e("res === ","sukses");
                } else {
//                    gagal
                    Log.e("res === ","gagal");
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void getLocationData(Context c){

        RequestQueue queue = Volley.newRequestQueue(c);
        String url = "https://"+getString(R.string.ip)+"/lokasi.php/";


        StringRequest st = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("res === ",response);

                myData.clear();


                JSONObject lokasi = null;
                try {
                    lokasi = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int lengt = 0;
                try {
                    lengt = lokasi.getJSONArray("data").length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(lengt > 0){
                    for (int i=0; i<lengt; i++){
                        com.septian.cariterapis.Location l = new com.septian.cariterapis.Location();
                        try {
                            JSONObject re = null;
                            re = new JSONObject(lokasi.getJSONArray("data").get(i).toString());

                            l.setId( re.has("id") ? re.getString("id") : "" );
                            l.setName( re.has("name") ? re.getString("name") : ""  );
//                            l.setDistance("0 KM");
                            l.setLoc( re.has("location") ? re.getString("location") : "" );
                            l.setLat( re.has("lat") ? Double.parseDouble(re.getString("lat")) : 0 );
                            l.setLng( re.has("lng") ? Double.parseDouble(re.getString("lng")) : 0 );

                            android.location.Location loc = new android.location.Location("myLoc");
                            loc.setLatitude(Double.parseDouble(sp.getString("lat","0")));
                            loc.setLongitude(Double.parseDouble(sp.getString("lng","0")));

                            android.location.Location loc2 = new android.location.Location("thisLoc");
                            loc2.setLatitude(l.getLat());
                            loc2.setLongitude(l.getLng());

                            float [] dis = new float[100];
                            android.location.Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), loc2.getLatitude(), loc2.getLongitude(), dis);

//                            Log.e("hasil === ", String.valueOf(dis[0]));

                            double distance = loc.distanceTo(loc2) * 0.001;
                            NumberFormat formatter = new DecimalFormat("#0.00");

                            l.setDistance(String.valueOf(formatter.format(distance))+" KM");

                            myData.add(l);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

                mv.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {
                        maps = mMap;
                        LatLng center;


                        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            maps.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);

                            center = new LatLng(Double.parseDouble(sp.getString("lat", "0")), Double.parseDouble(sp.getString("lng", "0")));
                        }else{
                            center = new LatLng(Double.parseDouble(getString(R.string.lat)), Double.parseDouble(getString(R.string.lng)));
                        }






                        Log.e("size data === ",String.valueOf(myData.size()));
                        for(int i=0; i<myData.size(); i++){
                            maps.addMarker(new MarkerOptions().position(new LatLng(myData.get(i).getLat(), myData.get(i).getLng())).title(myData.get(i).getName()).snippet(myData.get(i).getLoc()));
                            Log.e("nama === ",String.valueOf(myData.get(i).getName()));
                        }


                        CameraPosition cameraPosition = new CameraPosition.Builder().target(center).zoom(15).build();
                        maps.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }


                });


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("er === ",error.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");

                return headers;
            }
        };


        queue.add(st);
    }

}
