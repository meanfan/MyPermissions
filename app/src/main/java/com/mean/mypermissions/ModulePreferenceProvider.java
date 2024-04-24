package com.mean.mypermissions;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
import com.mean.mypermissions.utils.PreferenceUtils;

public class ModulePreferenceProvider extends RemotePreferenceProvider {
    public ModulePreferenceProvider(){
        super(PreferenceUtils.PACKAGE_NAME, new String[] {PreferenceUtils.MODULE_CONFIG_NAME});
    }
}
