package com.mean.mypermissions.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mean.mypermissions.PermissionActivity;
import com.mean.mypermissions.PermissionModeActivity;
import com.mean.mypermissions.R;
import com.mean.mypermissions.bean.PermissionConfigs;
import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.utils.AppUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionRVAdapter extends RecyclerView.Adapter<PermissionRVAdapter.VH> {
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

    private PermissionConfigs permissionConfigs;
    Map<String, RestrictMode> permissions;
    List<String> permissionNames;
    List<RestrictMode> permissionModes;

    private PermissionRVAdapter(){}

    public PermissionRVAdapter(PermissionConfigs configs) {
        this.permissionConfigs = configs;
        permissions=permissionConfigs.getAll();
        permissionNames = new ArrayList<>(permissions.keySet());
        permissionModes = new ArrayList<>(permissions.values());
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.name.setText(permissionNames.get(position));
        holder.status.setText(AppUtil.getRestrictModeName(holder.itemView.getContext(),permissionModes.get(position)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PermissionModeActivity.class);
                intent.putExtra("permissionConfig",permissionConfigs);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return permissions.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.permission_list_rv_item, parent, false);
        return new VH(v);
    }
}
