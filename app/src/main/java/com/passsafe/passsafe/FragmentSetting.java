package com.passsafe.passsafe;

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

import static android.content.Context.MODE_PRIVATE;

public class FragmentSetting extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.layout_setting, container, false);
        Button butlogout=(Button)view.findViewById(R.id.but_logout);
        butlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("PASS","HAHAHAHA");
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("Login",MODE_PRIVATE).edit();
                editor.putString("name","");
                editor.putString("pass","");
                editor.commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

    }
}
