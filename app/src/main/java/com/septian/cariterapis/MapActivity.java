package com.septian.cariterapis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity {

    private MapView mv;
    private ConstraintLayout cl;
    private TextView detail, name;
    private String id;
    private GoogleMap maps;
    private SharedPreferences sp = null;
    private ImageView wa,map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sp =  this.getSharedPreferences("data",MODE_PRIVATE);

        mv = findViewById(R.id.mapViewDetail);
        mv.onCreate(savedInstanceState);

        detail = findViewById(R.id.distanceDetailMap);
        name = findViewById(R.id.nameDetailMap);
        cl = findViewById(R.id.infoLayout);
        wa = findViewById(R.id.waBtn);
        map = findViewById(R.id.mapBtn);


        id = getIntent().getExtras().getString("id");
        name.setText(getIntent().getExtras().getString("name"));
        detail.setText(getIntent().getExtras().getString("distance"));

        final Double lat = getIntent().getExtras().getDouble("lat");
        final Double lng = getIntent().getExtras().getDouble("lng");

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mv.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                maps = mMap;
                LatLng center = new LatLng( lat, lng );


                maps.getUiSettings().setMyLocationButtonEnabled(false);
                maps.getUiSettings().setMapToolbarEnabled(false);
                maps.getUiSettings().setAllGesturesEnabled(false);

                maps.addMarker(new MarkerOptions().position( center ));

                maps.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return true;
                    }
                });

                CameraPosition cameraPosition = new CameraPosition.Builder().target(center).zoom(15).build();
                maps.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }


        });

        wa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = getIntent().getExtras().getString("phone");//"+628562780004";

                if(number.equalsIgnoreCase("")){
                    noPhone();
                }else {
                    number = number.replace(" ", "").replace("+", "");
                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                    sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
                    sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net");
                    try {
                        startActivity(sendIntent);
                    } catch (android.content.ActivityNotFoundException ex) {
                        showError();
                    }
                }
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat.toString()+","+lng.toString());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    showError();
                }
            }
        });

    }

    private void showError(){
        Toast.makeText(this,"Aplikasi belum terinstall",Toast.LENGTH_SHORT).show();
    }

    private void noPhone(){
        Toast.makeText(this,"Maaf, belum ada nomor telepon",Toast.LENGTH_SHORT).show();
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
}
