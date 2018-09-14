package com.layoutxml.applistmanager;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.layoutxml.applistmanagerlibrary.AppList;
import com.layoutxml.applistmanagerlibrary.interfaces.ActivitiesListener;
import com.layoutxml.applistmanagerlibrary.interfaces.AppListener;
import com.layoutxml.applistmanagerlibrary.interfaces.NewActivitiesListener;
import com.layoutxml.applistmanagerlibrary.interfaces.NewAppListener;
import com.layoutxml.applistmanagerlibrary.interfaces.SortListener;
import com.layoutxml.applistmanagerlibrary.interfaces.UninstalledActivitiesListener;
import com.layoutxml.applistmanagerlibrary.interfaces.UninstalledAppListener;
import com.layoutxml.applistmanagerlibrary.objects.AppData;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements ActivitiesListener, NewActivitiesListener, UninstalledActivitiesListener, AppListener, NewAppListener, UninstalledAppListener, SortListener {

    private static final String TAG = "ListActivity";

    private List<AppData> appDataList = new ArrayList<>();
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private Boolean apps;
    private Intent mainIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.listView);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(appDataList);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String data = intent.getStringExtra("appDataList");

        AppList.registerListeners(ListActivity.this,ListActivity.this,ListActivity.this,ListActivity.this,null,null,ListActivity.this);

        switch (data){
            default:
                apps = true;
                progressBar.setVisibility(View.VISIBLE);
                AppList.getAllApps(getApplicationContext(),0);
                break;
            case "apps":
                apps = true;
                progressBar.setVisibility(View.VISIBLE);
                AppList.getAllApps(getApplicationContext(),0);
                break;
            case "activities":
                apps = false;
                progressBar.setVisibility(View.VISIBLE);
                mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                AppList.getAllActivities(getApplicationContext(),mainIntent,1);
                break;
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            registerReceiver(new AppList(),AppList.intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppList.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppList.registerListeners(ListActivity.this,ListActivity.this,ListActivity.this,ListActivity.this,ListActivity.this,ListActivity.this,ListActivity.this);
        if (appDataList!=null && apps!=null) {
            if (apps) {
                AppList.getAllNewApps(getApplicationContext(), appDataList, 2);
                AppList.getAllUninstalledApps(getApplicationContext(),appDataList,4);
            }
            else {
                AppList.getAllNewActivities(getApplicationContext(), appDataList, mainIntent, 3);
                AppList.getAllUninstalledActivities(getApplicationContext(),appDataList, mainIntent,5);
            }
        }
    }

    @Override
    public void activitiesListener(List<AppData> appDataList, Intent intent, Integer activitiesFlags, Integer applicationFlags, Boolean applicationFlagsMatch, Integer uniqueIdentifier) {
        AppList.sort(appDataList,AppList.BY_APPNAME,AppList.IN_ASCENDING,uniqueIdentifier);
    }

    @Override
    public void appListener(List<AppData> appDataList, Integer applicationFlags, Boolean applicationFlagsMatch, Integer uniqueIdentifier) {
        AppList.sort(appDataList,AppList.BY_APPNAME,AppList.IN_ASCENDING,uniqueIdentifier);
    }

    @Override
    public void sortListener(List<AppData> appDataList, Integer sortBy, Integer inOrder, Integer uniqueIdentifier) {
        this.appDataList.clear();
        this.appDataList.addAll(appDataList);
        if (uniqueIdentifier==0 || uniqueIdentifier==1) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
        else if (uniqueIdentifier==2 || uniqueIdentifier==3) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void newActivitiesListener(List<AppData> appDataList, Intent intent, Integer activitiesFlags, Integer applicationFlags, Boolean applicationFlagsMatch, Boolean fromReceiver, Integer uniqueIdentifier) {
        if (!apps) {
            appDataList.addAll(this.appDataList);
            AppList.sort(appDataList,AppList.BY_APPNAME,AppList.IN_ASCENDING,3);
        }
    }

    @Override
    public void newAppListener(List<AppData> appDataList, Integer applicationFlags, Boolean applicationFlagsMatch, Boolean fromReceiver, Integer uniqueIdentifier) {
        if (apps) {
            appDataList.addAll(this.appDataList);
            AppList.sort(appDataList,AppList.BY_APPNAME,AppList.IN_ASCENDING,2);
        }
    }

    @Override
    public void uninstalledActivitiesListener(List<AppData> appDataList, Intent intent, Boolean fromReceiver, Integer uniqueIdentifier) {
        if (this.appDataList!=null){
            this.appDataList.removeAll(appDataList);
        }
    }

    @Override
    public void uninstalledAppListener(List<AppData> appDataList, Boolean fromReceiver, Integer uniqueIdentifier) {
        if (this.appDataList!=null){
            this.appDataList.removeAll(appDataList);
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private List<AppData> appDataList0;

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            TextView packageName;
            ImageView logo;

            MyViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.itemName);
                packageName = v.findViewById(R.id.itemPackageName);
                logo = v.findViewById(R.id.itemLogo);
            }
        }

        MyAdapter(List<AppData> appDataList) {
            appDataList0 = appDataList;
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            AppData app = appDataList0.get(position);
            holder.name.setText(app.getName());
            holder.packageName.setText(app.getPackageName());
            holder.logo.setImageDrawable(app.getIcon());
        }

        @Override
        public int getItemCount() {
            return appDataList0.size();
        }
    }
}