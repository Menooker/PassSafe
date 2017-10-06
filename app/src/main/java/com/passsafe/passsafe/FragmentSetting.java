package com.passsafe.passsafe;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

/**
 * Created by Menooker on 2017/9/23.
 */


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import static android.content.Context.MODE_PRIVATE;

public class FragmentSetting extends Fragment {

    //make a toast notification on the main thread
    //len - the toast time length
    void toast(final String str,final int len)
    {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity().getApplicationContext(), str,
                        len).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.layout_setting, container, false);
        Button butlogout=(Button)view.findViewById(R.id.but_logout);
        butlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //On log out
                Log.d("PASS","HAHAHAHA");
                //First clear the local data
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Login",MODE_PRIVATE).edit();
                editor.putString("name","");
                editor.putString("pass","");
                editor.putString("faceid", "0");
                editor.commit();
                MainActivity main=(MainActivity)getActivity();
                //drop the local DB
                main.fragmentMain.drop();
                //goto LoginActivity
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        Button butpull=(Button)view.findViewById(R.id.but_pull);
        butpull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<Void,Void,Boolean> task=new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        //get data from server
                        String response= Client.sendGet(Client.URL+"accounts/?username=" +
                                Client.encode(LoginActivity.mName));
                        MainActivity main=(MainActivity)getActivity();
                        //if data not empty, update local DB and refresh
                        if(response!=null)
                            main.fragmentMain.batchupdate(response);
                        toast("Accounts fetched from Cloud.",Toast.LENGTH_SHORT);
                        return null;
                    }
                };
                task.execute();
            }
        });


        Button butpush = (Button) view.findViewById(R.id.but_push);
        butpush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity main=(MainActivity)getActivity();
                //construct JSON from local DB data
                JSONArray arr=new JSONArray();
                for(FragmentMain.ItemData item: main.fragmentMain.data_list)
                {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("username", item.name);
                        obj.put("password", item.pass);
                        obj.put("title", item.site);
                        arr.put(obj);
                    }
                    catch (Exception e)
                    {
                        Client.ShowError(e);
                    }

                }
                final String data=Client.encode(arr.toString());
                //send the encoded JSON to server
                AsyncTask<Void,Void,Boolean> task=new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        String response= Client.sendGet(Client.URL+"updateaccounts/?username=" +
                                Client.encode(LoginActivity.mName)+"&accounts="+ data);
                        try {
                            JSONObject retobj=new JSONObject(response);
                            if(retobj.has("success") && retobj.getInt("success")==1 )
                            {
                                toast("Accounts pushed to Cloud.",Toast.LENGTH_SHORT);
                            }
                            else
                            {
                                toast("Upload Failed!",Toast.LENGTH_SHORT);
                            }
                        } catch (JSONException e) {
                            Client.ShowError(e);
                        }

                        return null;
                    }
                };
                task.execute();

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

    }
}
