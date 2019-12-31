package com.mean.mypermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mean.mypermissions.adapter.PermissionRVAdapter;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.utils.AppConfigDBUtil;
import com.mean.mypermissions.utils.AppUtil;

public class PermissionActivity extends AppCompatActivity {
    public static final String TAG = "PermissionActivity";
    private ImageView iv_icon;
    private TextView tv_name;
    private Switch sw_enable;
    private RecyclerView rv_permission_list;
    private TextView tv_none_hint;
    private AppConfig appConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisson);
        iv_icon = findViewById(R.id.iv_icon);
        tv_name = findViewById(R.id.tv_name);
        sw_enable = findViewById(R.id.sw_enable);
        rv_permission_list = findViewById(R.id.rv_permission_list);
        tv_none_hint = findViewById(R.id.tv_none_hint);

        // 初始化appConfig
        appConfig = (AppConfig) getIntent().getSerializableExtra("appConfig");
        if(appConfig == null){
            Toast.makeText(this,"参数错误",Toast.LENGTH_SHORT).show();
            finish();
        }
        AppConfig appConfigFromDB = AppConfigDBUtil.query(appConfig.getAppPackageName());
        if(appConfigFromDB!=null){
            appConfig = appConfigFromDB;
            Log.d(TAG, "onCreate: load appConfig from db success");
        }else {
            Log.d(TAG, "onCreate: not found appConfig from db");
        }

        // 设置UI
        Drawable icon = AppUtil.getAppIcon(this, appConfig.getAppPackageName());
        if(icon != null) {
            iv_icon.setImageDrawable(icon);
        }
        tv_name.setText(appConfig.getAppName());
        sw_enable.setChecked(appConfig.isEnabled());
        sw_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                appConfig.setEnabled(isChecked);
                AppConfigDBUtil.insert(appConfig);
            }
        });

        if(appConfig.getPermissionConfigs() == null || appConfig.getPermissionConfigs().size() == 0){
            rv_permission_list.setVisibility(View.GONE);
            tv_none_hint.setVisibility(View.VISIBLE);
        }else {
            rv_permission_list.setVisibility(View.VISIBLE);
            tv_none_hint.setVisibility(View.GONE);
        }
        rv_permission_list.setLayoutManager(new LinearLayoutManager(PermissionActivity.this));
        rv_permission_list.setAdapter(new PermissionRVAdapter(appConfig));
    }
}
