package com.passsafe.passsafe;

import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.Fragment;

/**
 * Created by Menooker on 2017/9/23.
 */


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FragmentMain extends Fragment {

    MyDatabaseHelper helper;
    ListView listview;
    List<ItemData> data_list=new ArrayList<ItemData>();
    MyItemAdapter adapter;
    List<ItemData> output_data()
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("password",null,null,null,null,null,null);
        List<ItemData> lst=data_list;
        data_list.clear();
        if(cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String pass = cursor.getString(cursor.getColumnIndex("pass"));
                String site = cursor.getString(cursor.getColumnIndex("site"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                lst.add(new ItemData(id,site,name,pass));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return lst;
    }

    void del(int id)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("password","id= "+id,null);
    }
    void insert(String site, String name,String pass)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("site",site);
        values.put("pass",pass);
        db.insert("password",null,values);
    }

    void update(int id,String site, String name,String pass)
    {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name",name);
        values.put("site",site);
        values.put("pass",pass);
        db.update("password",values,"id= "+id ,null);
    }


    public class ItemData {
        public String site, name, pass;
        public int id;
        public ItemData(int id,String site, String name,String pass) {
            this.name = name;
            this.site=site;
            this.pass=pass;
            this.id = id;
        }
    }

    public class MyItemAdapter extends ArrayAdapter<ItemData> {

        private int resourceId;
        public MyItemAdapter(Context context, int textViewResourceId,
                            List<ItemData> objects) {
            super(context, textViewResourceId, objects);
            //拿取到子项布局ID
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemData data = getItem(position);
            //为子项动态加载布局
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            TextView tname = (TextView) view.findViewById(R.id.txt_name);
            TextView tpass = (TextView) view.findViewById(R.id.txt_pass);
            TextView tsite = (TextView) view.findViewById(R.id.txt_site);
            tname.setText("User Name: "+data.name);
            //tpass.setText(data.pass);
            tsite.setText(data.site);
            return view;
        }

    }

    void refresh()
    {
        output_data();
        adapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        helper = new MyDatabaseHelper(getActivity(),"password.db",null,1);
        helper.getWritableDatabase();

        View view= inflater.inflate(R.layout.layout_pass, container, false);
        listview=(ListView)view.findViewById(R.id.lst_pass);

        adapter = new MyItemAdapter(getActivity(), R.layout.layout_list, data_list);
        listview.setAdapter(adapter);
        refresh();

        Button butadd=(Button)view.findViewById(R.id.but_add);
        butadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity act=(MainActivity)getActivity();
                act.SwitchToAdd();
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                DeleteMessageBox("Are you sure to delete this item?",i);
                return false;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            long timestamp=0;
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TextView tpass = (TextView) view.findViewById(R.id.txt_pass);
                if(tpass.getText().equals(""))
                    tpass.setText(data_list.get(i).pass);
                else
                    tpass.setText("");
                long currenttime=System.currentTimeMillis();
                if(currenttime-timestamp<300)
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Password has been copied to clipboard!",
                            Toast.LENGTH_SHORT).show();
                    ClipboardManager cmb = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(data_list.get(i).pass.trim());

                }
                timestamp=currenttime;
            }
        });

        return view;
    }



    void DeleteMessageBox(String prompt,int idx)
    {
        final int i=idx;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        int id = data_list.get(i).id;
                        del(id);
                        refresh();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(prompt).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public class MyDatabaseHelper extends SQLiteOpenHelper {
        public static final String CREATE_DB = "create table password ( "
                + " id integer primary key autoincrement,"
                + "site text,"
                + "name text,"
                + "pass text)";
        private Context context;
        public MyDatabaseHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }


}