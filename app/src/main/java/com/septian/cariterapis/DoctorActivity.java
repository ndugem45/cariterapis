package com.septian.cariterapis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DoctorActivity extends AppCompatActivity {

    private RecyclerView rv;
    private SearchView sv;
    private RecyclerView.Adapter mA;
    private RecyclerView.LayoutManager mLM;
    private ArrayList<Doctor> myData = new ArrayList<>();
    private ArrayList<Doctor> originalData = new ArrayList<>();
    private TextView er;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        rv = findViewById(R.id.recycleDoctorActivity);
        sv = findViewById(R.id.searchDoctorActivity);

        rv.setHasFixedSize(true);

        mLM = new LinearLayoutManager(this);
        rv.setLayoutManager(mLM);


        mA= new DoctorActivity.MyAdapterHorizontal(this,myData);
        rv.setAdapter(mA);

        er = findViewById(R.id.error);

        sv.setActivated(false);
        sv.setIconifiedByDefault(false);
        sv.setQueryHint("Cari terapis ...");

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

        String id = getIntent().getExtras().getString("id");
        String from = getIntent().getExtras().getString("from");
        getDoctorData(this, id, from);
    }

    public void search(String text){
        String charString = text;
        if (charString.length() <= 0) {
            myData.clear();
            myData.addAll(originalData);
        } else {
            myData.clear();

            for (Doctor row : originalData) {

                if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                    myData.add(row);
                }
            }

        }

        mA.notifyDataSetChanged();
    }


    public void getDoctorData(Context c, String id, String from){

        RequestQueue queue = Volley.newRequestQueue(c);

        String url = "";

        if(from.equalsIgnoreCase("kategori")){
            url = "https://"+getString(R.string.ip)+"/terapis.php?cat="+id;
        }else if(from.equalsIgnoreCase("lokasi")){
            url = "https://"+getString(R.string.ip)+"/lokasi.php?lok="+id;
        }

        Log.e("url === ",url);

        StringRequest st = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("res === ",response);
                er.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);

                myData.clear();
                originalData.clear();


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

                if(lengt > 0) {
                    for (int i = 0; i < lengt; i++) {
                        Doctor l = new Doctor();
                        try {
                            JSONObject re = null;
                            re = new JSONObject(terapis.getJSONArray("data").get(i).toString());


                            l.setId(re.has("id") ? re.getString("id") : "");
                            l.setName(re.has("terapis") ? re.getString("terapis") : "");
                            l.setSpesialis(re.has("spesialis") ? re.getString("spesialis") : "");

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


    public class MyAdapterHorizontal extends RecyclerView.Adapter<DoctorActivity.MyAdapterHorizontal.MyViewHolder> {

        Context mContext;
        ArrayList<Doctor> myData;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView docName,titleDoc;
            private ImageView imageDoc;

            public MyViewHolder(View view) {
                super(view);
                docName = view.findViewById(R.id.docName);
                titleDoc = view.findViewById(R.id.titleDoc);
                imageDoc = view.findViewById(R.id.imageDoc);
            }
        }


        public MyAdapterHorizontal(Context mContext, ArrayList<Doctor> data) {
            this.mContext = mContext;
            this.myData = data;
        }

        @Override
        public DoctorActivity.MyAdapterHorizontal.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item, parent, false);
            return new DoctorActivity.MyAdapterHorizontal.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DoctorActivity.MyAdapterHorizontal.MyViewHolder holder, final int position) {
            final Doctor data = myData.get(position);

            holder.docName.setText(data.getName());
            holder.titleDoc.setText(data.getSpesialis());
            holder.imageDoc.setImageResource(R.drawable.doctor);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(mContext, detailDoctorActivity.class);
                    in.putExtra("id",data.getId());
                    in.putExtra("name",data.getName());
                    in.putExtra("spesialis",data.getSpesialis());
                    startActivity(in);
                }
            });

        }

        @Override
        public int getItemCount() {
            return (myData == null) ? 0 : myData.size();
        }
    }
}
