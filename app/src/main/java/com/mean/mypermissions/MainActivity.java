package com.mean.mypermissions;

import android.content.Intent;
import android.os.Bundle;

import com.mean.mypermissions.adapter.AppRVAdapter;
import com.mean.mypermissions.utils.AppUtil;
import com.mean.mypermissions.utils.SuUtil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initRefreshLayout();
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
                App.appConfigs = AppUtil.getAllUserAppConfigs(MainActivity.this);
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
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private boolean isModuleActive(){
        return false;
    }
}
