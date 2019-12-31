package com.mean.mypermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.bean.PermissionConfigs;
import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.utils.AppConfigDBUtil;

public class PermissionModeActivity extends AppCompatActivity {
    private AppConfig appConfig;
    private String permissionName;
    private Integer permissionMode;
    private PermissionConfigs permissionConfigs;

    private RadioGroup rg_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_mode);

        appConfig = (AppConfig) getIntent().getSerializableExtra("appConfig");
        permissionName = getIntent().getStringExtra("permissionName");
        permissionMode = getIntent().getIntExtra("permissionMode", -1);
        if(appConfig == null || permissionName==null || permissionMode<0 || appConfig.getPermissionConfigs() == null || appConfig.getPermissionConfigs().size() == 0){
            Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
            finish();
        }
        permissionConfigs = appConfig.getPermissionConfigs();

        rg_mode = findViewById(R.id.rg_mode);
        switch (permissionMode){
            case RestrictMode.ALLOW:
                rg_mode.check(R.id.rb_allow);
                break;
            case RestrictMode.ALLOW_BUT_FAKE:
                rg_mode.check(R.id.rb_allow_but_fake);
                break;
            case RestrictMode.ALLOW_BUT_NULL:
                rg_mode.check(R.id.rb_allow_but_null);
                break;
            case RestrictMode.DEFAULT:
                rg_mode.check(R.id.rb_default);
                break;
            case RestrictMode.DENY:
                rg_mode.check(R.id.rb_deny);
                break;
        }
        rg_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_allow:
                        setMode(RestrictMode.ALLOW);
                        break;
                    case R.id.rb_allow_but_fake:
                        setMode(RestrictMode.ALLOW_BUT_FAKE);
                        break;
                    case R.id.rb_allow_but_null:
                        setMode(RestrictMode.ALLOW_BUT_NULL);
                        break;
                    case R.id.rb_default:
                        setMode(RestrictMode.DEFAULT);
                        break;
                    case R.id.rb_deny:
                        setMode(RestrictMode.DENY);
                        break;
                }
            }
        });
    }

    private void setMode(int mode){
        permissionConfigs.add(permissionName,mode);
        AppConfigDBUtil.insert(appConfig);
    }
}
