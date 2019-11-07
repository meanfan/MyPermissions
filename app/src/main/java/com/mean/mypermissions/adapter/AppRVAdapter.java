package com.mean.mypermissions.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.mean.mypermissions.R;
import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.utils.PermissionUtil;

import java.io.IOException;
import java.io.InputStream;
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
    public void onBindViewHolder(VH holder, int position) {
        final AppConfig appConfig = appConfigs.get(position);
        //设置name
        holder.name.setText(appConfig.getAppName());
        //设置icon
        Drawable icon = PermissionUtil.getAppIcon(holder.itemView.getContext(),appConfig.getAppPackageName());
        if(icon != null) {
            holder.icon.setImageDrawable(icon);
        }
        holder.status.setText(appConfig.getIsEnabled());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 跳转到单个app配置页
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
