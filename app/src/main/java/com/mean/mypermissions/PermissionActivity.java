package com.mean.mypermissions;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mean.mypermissions.adapter.PermissionRVAdapter;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.utils.AppConfigDBUtil;
import com.mean.mypermissions.utils.AppUtil;

public class PermissionActivity extends AppCompatActivity {
    public static final String TAG = "PermissionActivity";
    public static final int REQUESTCODE_CONFIG = 0;
    private ImageView iv_icon;
    private TextView tv_name;
    private Switch sw_enable;
    private RecyclerView rv_permission_list;
    private TextView tv_none_hint;
    private FloatingActionButton fab_restore;

    private AppConfig appConfig;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUESTCODE_CONFIG){
            if(resultCode == RESULT_OK){
                if(data!=null){
                    String [] permissionNames =  data.getStringArrayExtra("permissionNames");
                    int[] permissionModes = data.getIntArrayExtra("permissionModes");
                    if(permissionNames!=null && permissionNames.length>0 && permissionModes!=null && permissionModes.length>0) {
                        String permissionName = permissionNames[0];
                        int permissionMode = permissionModes[0];
                        appConfig.getPermissionConfigs().add(permissionName,permissionMode);
                        ((PermissionRVAdapter)rv_permission_list.getAdapter()).updateData();
                        rv_permission_list.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisson);
        iv_icon = findViewById(R.id.iv_icon);
        tv_name = findViewById(R.id.tv_name);
        sw_enable = findViewById(R.id.sw_enable);
        rv_permission_list = findViewById(R.id.rv_permission_list);
        tv_none_hint = findViewById(R.id.tv_none_hint);
        fab_restore = findViewById(R.id.fab_restore);

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
                Intent intent = new Intent();
                int appListPos = getIntent().getIntExtra("appListPos",-1);
                intent.putExtra("appListPos",appListPos);
                intent.putExtra("isEnabled",isChecked);
                setResult(RESULT_OK,intent);
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

        fab_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PermissionActivity.this);
                builder.setTitle("提示")
                        .setMessage("确认重置该应用所有配置？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AppConfigDBUtil.delete(appConfig); //直接从数据库删除
                                appConfig.getPermissionConfigs().restoreAllMode();
                                sw_enable.setChecked(false);
                                ((PermissionRVAdapter)rv_permission_list.getAdapter()).updateData();
                                rv_permission_list.getAdapter().notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }
}
