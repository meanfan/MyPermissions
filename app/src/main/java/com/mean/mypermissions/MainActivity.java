package com.mean.mypermissions;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.mean.mypermissions.adapter.AppRVAdapter;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.utils.AppUtil;
import com.mean.mypermissions.utils.PreferenceUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static final int REQUESTCODE_CONFIG = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initRefreshLayout();
        swipeRefreshLayout.setRefreshing(true);
        forceRefresh();
        checkXposed();
    }

    private void initRefreshLayout(){
        swipeRefreshLayout = findViewById(R.id.app_list_sfl);
        swipeRefreshLayout.setProgressViewOffset(true,50,200);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        forceRefresh();
                    }
                }
        );
    }

    // use thread
    private void forceRefresh(){
        new Thread(){
            @Override
            public void run() {
                App.initAppConfig(MainActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView = findViewById(R.id.app_list_rv);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        recyclerView.setAdapter(new AppRVAdapter(App.appConfigs));
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }.start();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Get the action view used in your toggleservice item
        final MenuItem toggleService = menu.findItem(R.id.action_toggle);
        final Switch actionView = (Switch) toggleService.getActionView();
        actionView.setChecked(isModuleActive());
        actionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                moduleToggle(isChecked);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void checkXposed(){
        if(isModuleActive()){
            Toast.makeText(this,"模块已启用",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Module Enabled");
        }else {
            Toast.makeText(this,"模块未启用",Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Module Not Enabled");
        }
    }

    void moduleToggle(boolean status){
        SharedPreferences prefs = new RemotePreferences(this, PreferenceUtils.AUTHORY, PreferenceUtils.MODULE_CONFIG_NAME);
        SharedPreferences.Editor mEditor  = prefs.edit();
        mEditor.putBoolean(PreferenceUtils.ENABLE, status).commit();
        Log.d(TAG, "moduleToggle: "+status);
    }

    private boolean isModuleActive(){
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUESTCODE_CONFIG){
            if(resultCode == RESULT_OK){
                boolean isEnabled = data.getBooleanExtra("isEnabled",false);
                int pos = data.getIntExtra("appListPos",-1);
                if(pos>=0){
                    App.appConfigs.get(pos).setEnabled(isEnabled);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        }
    }
}
