package com.mean.mypermissions.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.mean.mypermissions.PermissionActivity;
import com.mean.mypermissions.PermissionModeActivity;
import com.mean.mypermissions.R;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.bean.PermissionConfigs;
import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.utils.AppUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionRVAdapter extends RecyclerView.Adapter<PermissionRVAdapter.VH> {
    public static final String TAG = "PermissionRVAdapter";
    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder{
        public final TextView name;
        public final TextView status;
        public VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_permission_name);
            status = v.findViewById(R.id.tv_permission_config_status);
        }
    }

    private AppConfig appConfig;
    List<String> permissionNames;
    List<Integer> permissionModes;

    private PermissionRVAdapter(){}

    public PermissionRVAdapter(AppConfig config) {
        this.appConfig = config;
        updateData();
    }

    //在notify前调用
    public void updateData(){
        if(appConfig.getPermissionConfigs()!=null) {
            permissionNames = new ArrayList<>(appConfig.getPermissionConfigs().keySet());
            permissionModes = new ArrayList<>(appConfig.getPermissionConfigs().values());
        }else {
            Log.d(TAG, "PermissionRVAdapter: no permissions");
        }
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        holder.name.setText(permissionNames.get(position));
        holder.status.setText(RestrictMode.parse2String(holder.itemView.getContext(),permissionModes.get(position)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appConfig.isEnabled()) {
                    Intent intent = new Intent(v.getContext(), PermissionModeActivity.class);
                    intent.putExtra("appConfig", appConfig);
                    String[] permissionNamesIntent = new String[1];
                    int[] permissionModesIntent = new int[1];
                    permissionNamesIntent[0] = permissionNames.get(position);
                    permissionModesIntent[0] = permissionModes.get(position);
                    intent.putExtra("permissionNames",permissionNamesIntent);
                    intent.putExtra("permissionModes",permissionModesIntent);
                    ((PermissionActivity)v.getContext()).startActivityForResult(intent,PermissionActivity.REQUESTCODE_CONFIG);
                }else {
                    Toast.makeText(v.getContext(),"请先启用",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(appConfig.getPermissionConfigs()==null){
            return 0;
        }else {
            return appConfig.getPermissionConfigs().size();
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.permission_list_rv_item, parent, false);
        return new VH(v);
    }
}
