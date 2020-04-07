package com.septian.cariterapis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class detailDoctorActivity extends AppCompatActivity {

    private TextView name,spesialis,er,jadwal;
    private ImageView imageDoc;
    private RecyclerView rv;
    private RecyclerView.Adapter mA;
    private RecyclerView.LayoutManager mLM;
    private ArrayList<Location> myData = new ArrayList<>();
    private ArrayList<Location> originalData = new ArrayList<>();
    private SharedPreferences sp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_doctor);

        rv = findViewById(R.id.recycleDetailDoctor);
        name = findViewById(R.id.detailDocName);
        spesialis = findViewById(R.id.titleDetailDoc);
        imageDoc = findViewById(R.id.imageDetailDoc);

        rv.setHasFixedSize(true);

        mLM = new LinearLayoutManager(this);
        rv.setLayoutManager(mLM);

        sp =  this.getSharedPreferences("data",MODE_PRIVATE);


        mA= new detailDoctorActivity.MyAdapterHorizontal(this,myData);
        rv.setAdapter(mA);

        er = findViewById(R.id.error);


        String id = getIntent().getExtras().getString("id");
        String terapis = getIntent().getExtras().getString("name");
        String title = getIntent().getExtras().getString("spesialis");

        name.setText(terapis);
        spesialis.setText(title);
        imageDoc.setImageResource(R.drawable.doctor);

        getPraktekData(this, id);
    }

    public void getPraktekData(Context c, String id){

        RequestQueue queue = Volley.newRequestQueue(c);

        String url = "https://"+getString(R.string.ip)+"/praktek.php?dok="+id;


        StringRequest st = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("res === ",response);
                er.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);


                JSONObject terapis = null;
                try {
                    terapis = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int lengt = 0;
                try {
                    lengt = terapis.getJSONArray("data").length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(lengt > 0){
                    for (int i=0; i<lengt; i++){
                        Location l = new Location();
                        try {
                            JSONObject re = null;
                            re = new JSONObject(terapis.getJSONArray("data").get(i).toString());


                            l.setId( re.has("id_lokasi") ? re.getString("id_lokasi") : "" );
                            l.setLng( re.has("lng") ? Double.parseDouble(re.getString("lng")) : 0 );
                            l.setLat( re.has("lat") ? Double.parseDouble(re.getString("lat")) : 0 );
                            l.setHari( re.has("hari") ? re.getString("hari") : "" );
                            l.setJam( re.has("jam") ? re.getString("jam") : "" );
                            l.setName( re.has("lokasi") ? re.getString("lokasi") : "" );
                            l.setLoc( re.has("alamat") ? re.getString("alamat") : "" );
                            l.setPhone( re.has("phone") ? re.getString("phone") : "" );

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

    public class MyAdapterHorizontal extends RecyclerView.Adapter<detailDoctorActivity.MyAdapterHorizontal.MyViewHolder> {

        Context mContext;
        ArrayList<Location> myData;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView locationName;
            private TextView locationDistance;
            private TextView locationAddress;
            private TextView jadwal;
            private View line;
            private Button wa,map;

            public MyViewHolder(View view) {
                super(view);

                locationName = view.findViewById(R.id.locationName);
                locationAddress = view.findViewById(R.id.locationAddress);
                locationDistance = view.findViewById(R.id.locationDistance);
                jadwal = view.findViewById(R.id.jadwal);
                line = view.findViewById(R.id.bottomLine);
                map = view.findViewById(R.id.mapDetailBtn);
                wa = view.findViewById(R.id.waDetailBtn);
            }
        }


        public MyAdapterHorizontal(Context mContext, ArrayList<Location> data) {
            this.mContext = mContext;
            this.myData = data;
        }

        @Override
        public detailDoctorActivity.MyAdapterHorizontal.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
            return new detailDoctorActivity.MyAdapterHorizontal.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final detailDoctorActivity.MyAdapterHorizontal.MyViewHolder holder, final int position) {
            final Location data = myData.get(position);


            holder.locationName.setText(data.getName());
            holder.locationDistance.setText(data.getDistance());
            holder.locationAddress.setText(data.getLoc());
            holder.jadwal.setVisibility(View.VISIBLE);
            holder.jadwal.setText(data.getHari()+"\n"+data.getJam());

            float density = detailDoctorActivity.this.getResources().getDisplayMetrics().density;
            float dp = 20 / density;
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)holder.line.getLayoutParams();
            params.setMargins(0, (int)dp, 0, 0);
            holder.line.setLayoutParams(params);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(mContext, MapActivity.class);
                    in.putExtra("id", data.getId());
                    in.putExtra("name", data.getName());
                    in.putExtra("lat", data.getLat());
                    in.putExtra("lng", data.getLng());
                    in.putExtra("distance", data.getDistance());
                    in.putExtra("phone", data.getPhone());
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
        Toast.makeText(detailDoctorActivity.this,"Aplikasi belum terinstall",Toast.LENGTH_SHORT).show();
    }

    private void noPhone(){
        Toast.makeText(detailDoctorActivity.this,"Maaf, belum ada nomor telepon",Toast.LENGTH_SHORT).show();
    }
}
