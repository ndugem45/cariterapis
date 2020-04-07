package com.septian.cariterapis;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private TabLayout tb;
    private ViewPager vp;
    private GetMyLocation myLoc;
    private Button infoBtn,warBtn;
    private Dialog myD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myLoc = new GetMyLocation(this);

        tb = (TabLayout) findViewById(R.id.tabView);
        vp = (ViewPager) findViewById(R.id.pagerView);
        infoBtn = findViewById(R.id.infoBtn);
        warBtn = findViewById(R.id.buttonWarning);
        myD = new Dialog(this);

        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        tb.setupWithViewPager(vp);


        tb.setSelectedTabIndicatorColor(Color.parseColor("#654e8b"));
        tb.getTabAt(0).setIcon(R.drawable.maps);
        tb.getTabAt(1).setIcon(R.drawable.lokasi);
        tb.getTabAt(2).setIcon(R.drawable.kategori);
        tb.getTabAt(3).setIcon(R.drawable.doctor);

        tb.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vp.setCurrentItem(tab.getPosition());
                if(tab.getPosition() == 0){
                    infoBtn.setVisibility(View.VISIBLE);
                    warBtn.setVisibility(View.VISIBLE);
                }else{
                    infoBtn.setVisibility(View.GONE);
                    warBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, infoActivity.class);
                startActivity(in);
            }
        });

        warBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button ok;

                myD.setContentView(R.layout.custom_popup);
                myD.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myD.show();

                ok = myD.findViewById(R.id.okBtn);

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myD.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Apa kamu yakin ingin keluar ?");

        builder.setPositiveButton("Ya",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        myLoc.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myLoc.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myLoc.onDestroy();
    }

    class pagerAdapter extends FragmentPagerAdapter {

//        String data[] = {"home","location","category","doctor"};

        public pagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0){
                return new MapFragment();
            }
            if (position == 1){
                return new LocationFragment();
            }
            if (position == 2){
                return new CategoryFragment();
            }
            if (position == 3){
                return new DoctorFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;//data.length;
        }

//        @Nullable
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return data[position];
//        }
    }
}
