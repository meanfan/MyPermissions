package com.mean.mypermissions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mean.mypermissions.bean.AppConfig;
import com.mean.mypermissions.bean.FakeContact;
import com.mean.mypermissions.bean.PermissionConfigs;
import com.mean.mypermissions.bean.RestrictMode;
import com.mean.mypermissions.utils.AppConfigDBUtil;
import com.mean.mypermissions.utils.AppUtil;
import com.mean.mypermissions.utils.SuUtil;

public class PermissionModeActivity extends AppCompatActivity {
    public static final String TAG = "PermissionModeActivity";
    private AppConfig appConfig;
    private String packageName;
    private String[] permissionNames;
    private String permissionNameCheck;
    private int[] permissionModes;
    private int rawRequestCode;

    private PermissionConfigs permissionConfigs;
    private int currentSelectedMode;
    private int currentHandlingPermissionPos = 0;
    private boolean isRequestMode = false;
    private boolean isCheckMode = false;

    private RadioGroup rg_mode;
    private Button btn_config,btn_cancel,btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_mode);
        appConfig = (AppConfig) getIntent().getSerializableExtra("appConfig");
        packageName = getIntent().getStringExtra("packageName");

        permissionNames = getIntent().getStringArrayExtra("permissionNames");
        permissionModes = getIntent().getIntArrayExtra("permissionModes");
        rawRequestCode = getIntent().getIntExtra("rawRequestCode",-1);

        // 第三方应用权限请求调用
        Intent receivedIntent = getIntent();
        String action = receivedIntent.getAction();
        if(action!=null){       //外部调用进行权限请求
            if (packageName == null) {  //packageName不可为null
                intentDataError();
            }
            appConfig = AppConfigDBUtil.query(packageName);  //查找数据库
            if(action.equals("com.mean.mypermissions.intent.permissions.REQUEST")) {  //权限申请
                isRequestMode = true;
                if (appConfig == null) {  //数据库中无记录，则初始化一个appConfig
                    appConfig = new AppConfig();
                    appConfig.setAppPackageName(packageName);
                    try {
                        PackageManager pm = getPackageManager();
                        PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
                        AppUtil.initAppConfigByPackageInfo(this, appConfig, packageInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        appConfig = null;
                        Log.e(TAG, "onCreate: ERROR: packageInfo not found");
                    }
                }
            }else if(action.equals("com.mean.mypermissions.intent.permissions.CHECK")) {  // 权限检查
                isCheckMode = true;
                permissionNameCheck = receivedIntent.getStringExtra("permissionNameCheck");
                //TODO 权限检查
                int rstMode;
                if (appConfig != null && appConfig.getPermissionConfigs()!=null && appConfig.getPermissionConfigs().get(permissionNameCheck)!=null) {  //数据库中有记录
                    rstMode = appConfig.getPermissionConfigs().get(permissionNameCheck);
                }else {
                    rstMode = RestrictMode.DEFAULT;
                }
                Intent rstIntentPermissionCheck = new Intent();
                rstIntentPermissionCheck.putExtra("permissionNameCheck",permissionNameCheck);
                rstIntentPermissionCheck.putExtra("permissionMode",rstMode);
                setResult(RESULT_OK,rstIntentPermissionCheck);
                finish();
                return;
            }else if (action.equals("com.mean.mypermissions.intent.permissions.fake.CONTACT")) {
               /* 获取假联系人数据库Url
                File fakeContactPath = new File(getExternalFilesDir(null), "fakedb");
                File newFile = new File(fakeContactPath, "contacts_fake.db");
                Uri uri = getUriForFile(this, "com.mean.mypermissions.fileProvider", newFile);
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                Intent rstIntentFakeContact = new Intent();
                rstIntentFakeContact.setData(uri);
                setResult(RESULT_OK,rstIntentFakeContact);
                finish();
                */
                FakeContact fakeContact = generateDefaultFakeContact(0);
                Intent rstIntentFakeContact = new Intent();
                rstIntentFakeContact.putExtra("fakeContactName",fakeContact.getName());
                rstIntentFakeContact.putExtra("fakeContactPhone",fakeContact.getPhone());
                setResult(RESULT_OK,rstIntentFakeContact);
                finish();
                return;
            }
        }

        if(appConfig == null || permissionNames == null || appConfig.getPermissionConfigs() == null){
            intentDataError();
        }else {
            packageName = appConfig.getAppPackageName();
        }

        // 初始化 permissionConfigs
        permissionConfigs = appConfig.getPermissionConfigs();
        appConfig.setEnabled(true);

        // 初始化 permissionModes
        permissionModes = new int[permissionNames.length];
        for(int i=0;i<permissionNames.length;i++){
            Integer mode = appConfig.getPermissionConfigs().get(permissionNames[i]);
            if(mode == null){
                intentDataError();
            }else {
                permissionModes[i] = mode;
            }
        }

        rg_mode = findViewById(R.id.rg_mode);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_config = findViewById(R.id.btn_config);

        rg_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_allow:
                        uiSelectMode(RestrictMode.ALLOW);
                        break;
                    case R.id.rb_allow_but_fake:
                        uiSelectMode(RestrictMode.ALLOW_BUT_FAKE);
                        break;
                    case R.id.rb_allow_but_null:
                        uiSelectMode(RestrictMode.ALLOW_BUT_NULL);
                        break;
                    case R.id.rb_deny:
                        uiSelectMode(RestrictMode.DENY);
                        break;
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionConfigs.add(permissionNames[currentHandlingPermissionPos],currentSelectedMode);
                permissionModes[currentHandlingPermissionPos] = currentSelectedMode;
                //TODO 系统授权
                /*
                if(currentSelectedMode == RestrictMode.ALLOW){
                    SuUtil.grantPermission(packageName, permissionNames[currentHandlingPermissionPos]);
                }else{
                    SuUtil.revokePermission(packageName,permissionNames[currentHandlingPermissionPos]);
                }
                 */

                currentHandlingPermissionPos++;
                if(currentHandlingPermissionPos<permissionNames.length){ //不是最后一项权限
                    handlePermissionAtPos(currentHandlingPermissionPos);
                }else {
                    //更新配置数据库
                    AppConfigDBUtil.insert(appConfig);
                    //返回结果
                    Intent intent = new Intent();
                    intent.putExtra("permissionNames",permissionNames);
                    intent.putExtra("permissionModes",permissionModes);
                    intent.putExtra("rawRequestCode",rawRequestCode);
                    setResult(RESULT_OK,intent);
                    finish();
                }

            }
        });
        if(isRequestMode){
            btn_cancel.setVisibility(View.GONE);
        }else {
            btn_cancel.setVisibility(View.VISIBLE);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }

        btn_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 自定义fake功能
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handlePermissionAtPos(currentHandlingPermissionPos);
    }

    private void handlePermissionAtPos(int pos){
        setTitle(permissionNames[pos]);
        currentSelectedMode = permissionModes[pos];
        if(isRequestMode && currentSelectedMode!=RestrictMode.DEFAULT){
            btn_confirm.performClick(); //已配置，无需用户操作
            return;
        }
        switch (currentSelectedMode){
            case RestrictMode.ALLOW:
                updateUIMode(R.id.rb_allow);
                break;
            case RestrictMode.ALLOW_BUT_FAKE:
                updateUIMode(R.id.rb_allow_but_fake);
                break;
            case RestrictMode.ALLOW_BUT_NULL:
                updateUIMode(R.id.rb_allow_but_null);
                break;
            case RestrictMode.DEFAULT:
                updateUIMode(R.id.rb_allow); //DEFAULT则不可再选，默认选择ALLOW
                break;
            case RestrictMode.DENY:
                updateUIMode(R.id.rb_deny);
                break;
        }
    }

    private void uiSelectMode(int mode){
        currentSelectedMode = mode;
        setConfigButtonVisibility();
    }

    private void updateUIMode(int id){
        rg_mode.check(id);
        setConfigButtonVisibility();
    }

    private void setConfigButtonVisibility(){
        if(currentSelectedMode == RestrictMode.ALLOW_BUT_FAKE){
            btn_config.setVisibility(View.VISIBLE);
        }else {
            btn_config.setVisibility(View.GONE);
        }
    }

    private void intentDataError(){
        Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
        finish();
    }

    public FakeContact generateDefaultFakeContact(int key){
        char firstNameChar = 'A';
        firstNameChar = (char)((int)firstNameChar+key%26);
        String name = String.format("%c Fake",firstNameChar);
        String phone = String.valueOf(1234567890+key);
        FakeContact fakeContact = new FakeContact();
        fakeContact.setName(name);
        fakeContact.setPhone(phone);
        fakeContact.setPhoneType("phone");
        return fakeContact;

    }
}
