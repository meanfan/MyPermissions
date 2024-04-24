package com.mean.mypermissions.utils;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SuUtil {
    public static final String TAG = "SuUtil";

     //use su shell to grant permission
     public static void grantPermission(String appPackageName, String permissionName) {
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            String shellCmd = String.format("pm grant %s %s\n",appPackageName,permissionName);
            os.writeBytes(shellCmd);
            os.writeBytes("exit\n");
            os.flush();
            suProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "grantPermission: ");
        }
    }

    //use su shell to revoke permission
    public static String revokePermission(String appPackageName, String permissionName) {
        try {
            Process proc = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(proc.getOutputStream());
            os.flush();
            String shellCmd = String.format("pm revoke %s %s\n",appPackageName,permissionName);
            os.writeBytes(shellCmd);
            os.flush();
            os.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String rst = reader.readLine();
            reader.close();
            proc.destroy();
            Log.d(TAG, appPackageName+ ": revokePermission: "+permissionName);
            Log.d(TAG, appPackageName + "revokePermissionResult: "+rst);
            return rst;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
