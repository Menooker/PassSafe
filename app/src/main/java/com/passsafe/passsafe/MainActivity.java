package com.passsafe.passsafe;

import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ViewPager mainPager;
    BottomNavigationView navigation;
    public FragmentMain fragmentMain;
    public class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> viewList;

        public MyPagerAdapter(List<Fragment> viewList) {
            super(getSupportFragmentManager());
            this.viewList = viewList;
        }
        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return viewList.get(position);
        }



    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mainPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_dashboard:
                    mainPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_notifications:
                    mainPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mainPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater inflater = getLayoutInflater();
        ArrayList<Fragment> viewList = new ArrayList<Fragment>();
        fragmentMain=new FragmentMain();
        viewList.add(fragmentMain);
        viewList.add(new FragmentAdd());
        viewList.add(new FragmentSetting());
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(viewList);
        mainPager.setAdapter(myPagerAdapter);
        mainPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                navigation.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    public void SwitchToAdd()
    {
        navigation.getMenu().getItem(1).setChecked(true);
        mainPager.setCurrentItem(1);
    }
    public void SwitchToMain()
    {
        navigation.getMenu().getItem(0).setChecked(true);
        mainPager.setCurrentItem(0);
    }
}
