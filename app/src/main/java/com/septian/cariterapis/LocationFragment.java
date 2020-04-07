package com.septian.cariterapis;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class LocationFragment extends Fragment {

    private RecyclerView rv;
    private RecyclerView.Adapter mA;
    private RecyclerView.LayoutManager mLM;
    private ArrayList<Location> myData = new ArrayList<>();
    private ArrayList<Location> originalData = new ArrayList<>();
    private SearchView sv;
    private TextView er;
    private SharedPreferences sp = null;

    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        sp =  getActivity().getBaseContext().getSharedPreferences("data",MODE_PRIVATE);

        rv = (RecyclerView) view.findViewById(R.id.recycleLocation);
        rv.setHasFixedSize(true);

        mLM = new LinearLayoutManager(getActivity().getBaseContext());
        rv.setLayoutManager(mLM);


        mA = new LocationFragment.MyAdapterHorizontal(getActivity().getBaseContext(),myData);
        rv.setAdapter(mA);

        er = view.findViewById(R.id.error);

        sv = view.findViewById(R.id.searchLocation);
        sv.setActivated(false);
        sv.setIconifiedByDefault(false);
        sv.setQueryHint("Cari tempat terapi ...");

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }

        });

        getLocationData(getActivity().getBaseContext());

        return view;
    }





    public void getLocationData(Context c){

        RequestQueue queue = Volley.newRequestQueue(c);
        String url = "https://"+getString(R.string.ip)+"/lokasi.php/";


        StringRequest st = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("res === ",response);
                er.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);

                myData.clear();
                originalData.clear();


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
                        Location l = new Location();
                        try {
                            JSONObject re = null;
                            re = new JSONObject(lokasi.getJSONArray("data").get(i).toString());

    //                        Log.e("===",re.toString());

                            l.setId( re.has("id") ? re.getString("id") : "" );
                            l.setName( re.has("name") ? re.getString("name") : ""  );

                            l.setLoc( re.has("location") ? re.getString("location") : "" );
                            l.setLat( re.has("lat") ? Double.parseDouble(re.getString("lat")) : 0 );
                            l.setLng( re.has("lng") ? Double.parseDouble(re.getString("lng")) : 0 );
                            l.setPhone( re.has("phone") ? re.getString("phone") : "" );

                            LatLng pos1 = new LatLng(Double.parseDouble(sp.getString("lat","0")),Double.parseDouble(sp.getString("lng","0")));
                            LatLng pos2 = new LatLng(l.getLat(),l.getLng());

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

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        myData.add(l);
                    }

                    originalData.addAll(myData);
                    mA.notifyDataSetChanged();
                }else{
                    er.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("er === ",error.toString());
                er.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
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


    public void search(String text){
        String charString = text;
        if (charString.length() <= 0) {
            myData.clear();
            myData.addAll(originalData);
        } else {
            myData.clear();

            for (Location row : originalData) {

                if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                    myData.add(row);
                }
            }

        }

        mA.notifyDataSetChanged();
    }

    public class MyAdapterHorizontal extends RecyclerView.Adapter<MyAdapterHorizontal.MyViewHolder> {

        Context mContext;
        ArrayList<Location> myData;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView locationName;
            private TextView locationDistance;
            private TextView locationAddress;
            private Button map,wa;

            public MyViewHolder(View view) {
                super(view);

                locationName = view.findViewById(R.id.locationName);
                locationAddress = view.findViewById(R.id.locationAddress);
                locationDistance = view.findViewById(R.id.locationDistance);
                wa = view.findViewById(R.id.waDetailBtn);
                map = view.findViewById(R.id.mapDetailBtn);
            }
        }


        public MyAdapterHorizontal(Context mContext, ArrayList<Location> data) {
            this.mContext = mContext;
            this.myData = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
            return new LocationFragment.MyAdapterHorizontal.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final Location data = myData.get(position);


            holder.locationName.setText(data.getName());
            holder.locationDistance.setText(data.getDistance());
            holder.locationAddress.setText(data.getLoc());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(mContext, DoctorActivity.class);
                    Log.e("id nya lokasi === ",data.getId());
                    in.putExtra("id",data.getId());
                    in.putExtra("from","lokasi");
                    startActivity(in);
                }
            });

            holder.wa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String number = data.getPhone();//"+628562780004";

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

            holder.map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+data.getLat().toString()+","+data.getLng().toString());
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

        @Override
        public int getItemCount() {
            return (myData == null) ? 0 : myData.size();
        }
    }

    private void showError(){
        Toast.makeText(getActivity().getBaseContext(),"Aplikasi belum terinstall",Toast.LENGTH_SHORT).show();
    }

    private void noPhone(){
        Toast.makeText(getActivity().getBaseContext(),"Maaf, belum ada nomor telepon",Toast.LENGTH_SHORT).show();
    }

}
