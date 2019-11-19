package com.mean.mypermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mean.mypermissions.bean.PermissionConfigs;

public class PermissionModeActivity extends AppCompatActivity {
    private PermissionConfigs permissionConfigs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_mode);
        permissionConfigs = (PermissionConfigs) getIntent().getSerializableExtra("permissionConfig");
    }
}
