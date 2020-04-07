package com.septian.cariterapis;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
public class CategoryFragment extends Fragment {

    private RecyclerView rv;
    private RecyclerView.Adapter mA;
    private RecyclerView.LayoutManager mLM;
    private ArrayList<Category> myData = new ArrayList<>();
    private ArrayList<Category> originalData = new ArrayList<>();
    private SearchView sv;
    private TextView er;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        rv = (RecyclerView) view.findViewById(R.id.recycleCategory);
        rv.setHasFixedSize(true);

        mLM = new LinearLayoutManager(getActivity().getBaseContext());
        rv.setLayoutManager(mLM);


        mA = new CategoryFragment.MyAdapterHorizontal(getActivity().getBaseContext(),myData);
        rv.setAdapter(mA);

        er = view.findViewById(R.id.error);

        sv = view.findViewById(R.id.searchCategory);
        sv.setActivated(false);
        sv.setIconifiedByDefault(false);
        sv.setQueryHint("Cari kategori terapi ...");

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


        getCategory(getActivity().getBaseContext());

        return  view;
    }


    public void getCategory(Context c){

        RequestQueue queue = Volley.newRequestQueue(c);
        String url = "https://"+getString(R.string.ip)+"/kategori.php/";

        StringRequest st = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("res === ",response);
                er.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);

                myData.clear();
                originalData.clear();


                JSONObject kategori = null;
                try {
                    kategori = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int lengt = 0;
                try {
                    lengt = kategori.getJSONArray("data").length();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(lengt > 0){
                    for (int i=0; i<lengt; i++){
                        Category l = new Category();
                        try {
                            JSONObject re = null;
                            re = new JSONObject(kategori.getJSONArray("data").get(i).toString());

                         l.setId( re.has("id") ? re.getString("id") : "" );
                         l.setName( re.has("name") ? re.getString("name") : "" );

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

            for (Category row : originalData) {

                if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                    myData.add(row);
                }
            }

        }

        mA.notifyDataSetChanged();
    }

    public class MyAdapterHorizontal extends RecyclerView.Adapter<CategoryFragment.MyAdapterHorizontal.MyViewHolder> {

        Context mContext;
        ArrayList<Category> myData;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView CatName;
            private ImageView imageCat;

            public MyViewHolder(View view) {
                super(view);

                CatName = view.findViewById(R.id.catName);
                imageCat = view.findViewById(R.id.imageCat);

            }
        }


        public MyAdapterHorizontal(Context mContext, ArrayList<Category> data) {
            this.mContext = mContext;
            this.myData = data;
        }

        @Override
        public CategoryFragment.MyAdapterHorizontal.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
            return new CategoryFragment.MyAdapterHorizontal.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final CategoryFragment.MyAdapterHorizontal.MyViewHolder holder, final int position) {
            final Category data = myData.get(position);

            holder.CatName.setText(data.getName());
            holder.imageCat.setImageResource(R.drawable.kategori);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(mContext, DoctorActivity.class);
                    Log.e("id nya kategori === ",data.getId());
                    in.putExtra("id",data.getId());
                    in.putExtra("from","kategori");
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
