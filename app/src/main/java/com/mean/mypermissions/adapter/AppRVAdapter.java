package com.mean.mypermissions.adapter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mean.mypermissions.App;
import com.mean.mypermissions.MainActivity;
import com.mean.mypermissions.PermissionActivity;
import com.mean.mypermissions.R;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.utils.AppUtil;

import java.util.List;

public class AppRVAdapter extends RecyclerView.Adapter<AppRVAdapter.VH> {
    //创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder{
        public final TextView name;
        public final ImageView icon;
        public final TextView status;
        public VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            icon = v.findViewById(R.id.iv_icon);
            status = v.findViewById(R.id.tv_status);
        }
    }

    private List<AppConfig> appConfigs;
    public AppRVAdapter(List<AppConfig> appConfigs) {
        this.appConfigs = appConfigs;
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        final AppConfig appConfig = appConfigs.get(position);
        //设置name
        holder.name.setText(appConfig.getAppName());
        //设置icon
        Drawable icon = AppUtil.getAppIcon(holder.itemView.getContext(), appConfig.getAppPackageName());
        if(icon != null) {
            holder.icon.setImageDrawable(icon);
        }
        if(appConfig.getIsEnabled()){
            holder.status.setText("启用");
        }else {
            holder.status.setText("未启用");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PermissionActivity.class);
                intent.putExtra("appConfig",appConfig);
                intent.putExtra("appListPos",position);
                //intent.putExtra("appPackageName", appConfig.getAppPackageName());
                //intent.putExtra("appPermissionConfigs",appConfig.getPermissionConfigs());
                ((MainActivity)v.getContext()).startActivityForResult(intent,MainActivity.REQUESTCODE_CONFIG);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appConfigs.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_rv_item, parent, false);
        return new VH(v);
    }
}
