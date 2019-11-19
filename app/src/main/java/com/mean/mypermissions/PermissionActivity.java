package com.mean.mypermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mean.mypermissions.adapter.PermissionRVAdapter;
import com.mean.mypermissions.bean.AppConfig;

public class PermissionActivity extends AppCompatActivity {
    public static final String TAG = "PermissionActivity";
    private RecyclerView rv_permission_list;
    private AppConfig appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisson);
        rv_permission_list = findViewById(R.id.rv_permission_list);
        int appPos = getIntent().getIntExtra("appPos",0);
        appConfig = App.appConfigs.get(appPos);
        if(appConfig == null){
            Toast.makeText(this,"参数错误",Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.d(TAG, "onCreate: "+appConfig.toString());
        rv_permission_list.setLayoutManager(new LinearLayoutManager(PermissionActivity.this));
        rv_permission_list.setAdapter(new PermissionRVAdapter(appConfig.getPermissionConfigs()));
    }
}
