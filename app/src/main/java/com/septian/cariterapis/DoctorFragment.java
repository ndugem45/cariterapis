package com.septian.cariterapis;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v4.app.Fragment;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class DoctorFragment extends Fragment {

    private RecyclerView rv;
    private RecyclerView.Adapter mA;
    private RecyclerView.LayoutManager mLM;
    private ArrayList<Doctor> myData = new ArrayList<>();
    private ArrayList<Doctor> originalData = new ArrayList<>();
    private SearchView sv;
    private TextView er;

    public DoctorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doctor, container, false);

        rv = (RecyclerView) view.findViewById(R.id.recycleDoctor);
        rv.setHasFixedSize(true);

        mLM = new LinearLayoutManager(getActivity().getBaseContext());
        rv.setLayoutManager(mLM);


        mA = new DoctorFragment.MyAdapterHorizontal(getActivity().getBaseContext(),myData);
        rv.setAdapter(mA);

        er = view.findViewById(R.id.error);

        sv = view.findViewById(R.id.searchDoctor);
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


        getDoctor(getActivity().getBaseContext());

        return view;
    }


    public void getDoctor(Context c){

        RequestQueue queue = Volley.newRequestQueue(c);
        String url = "https://"+getString(R.string.ip)+"/terapis.php/";

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

                if(lengt > 0){
                    for (int i=0; i<lengt; i++){
                        Doctor l = new Doctor();
                        try {
                            JSONObject re = null;
                            re = new JSONObject(terapis.getJSONArray("data").get(i).toString());

                            l.setId( re.has("id") ? re.getString("id") : "" );
                            l.setName( re.has("terapis") ? re.getString("terapis") : "" );
                            l.setSpesialis( re.has("spesialis") ? re.getString("spesialis") : "" );

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

            for (Doctor row : originalData) {

                if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                    myData.add(row);
                }
            }

        }

        mA.notifyDataSetChanged();
    }


    public class MyAdapterHorizontal extends RecyclerView.Adapter<DoctorFragment.MyAdapterHorizontal.MyViewHolder> {

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
        public DoctorFragment.MyAdapterHorizontal.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item, parent, false);
            return new DoctorFragment.MyAdapterHorizontal.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DoctorFragment.MyAdapterHorizontal.MyViewHolder holder, final int position) {
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
