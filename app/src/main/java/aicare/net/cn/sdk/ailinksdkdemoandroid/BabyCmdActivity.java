package aicare.net.cn.sdk.ailinksdkdemoandroid;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.pingwang.bluetoothlib.bean.SupportUnitBean;
import com.pingwang.bluetoothlib.device.BleDevice;
import com.pingwang.bluetoothlib.device.BleSendCmdUtil;
import com.pingwang.bluetoothlib.device.SendBleBean;
import com.pingwang.bluetoothlib.listener.OnBleCompanyListener;
import com.pingwang.bluetoothlib.listener.OnBleVersionListener;
import com.pingwang.bluetoothlib.listener.OnCallbackDis;
import com.pingwang.bluetoothlib.listener.OnMcuParameterListener;
import com.pingwang.bluetoothlib.utils.BleDensityUtil;
import com.pingwang.bluetoothlib.utils.BleLog;
import com.pingwang.bluetoothlib.utils.BleStrUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.net.aicare.modulelibrary.module.babyscale.BabyBleConfig;
import cn.net.aicare.modulelibrary.module.babyscale.BabyDeviceData;


/**
 * xing<br>
 * 2019/4/25<br>
 * 显示数据
 */
public class BabyCmdActivity extends BleBaseActivity implements OnCallbackDis, OnBleVersionListener
        , OnMcuParameterListener, OnBleCompanyListener, View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private static String TAG = BabyCmdActivity.class.getName();
    private final int REFRESH_DATA = 3;
    private List<String> mList;
    private ArrayAdapter listAdapter;
    private Context mContext;
    private BabyDeviceData babyDevice;
    private String mAddress;
    private BleSendCmdUtil mBleSendCmdUtil;
    private int type;
    private int weightUnit = 0, heightUnit = 0;
    private RadioButton mRadioButtonKg, mRadioButtonLb, mRadioButtonOz, mRadioButtonG,
            mRadioButtonLbLb, mRadioButtonCm, mRadioButtonFoot;
    private List<RadioButton> mListWeight, mListHeight;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {


                case REFRESH_DATA:
                    if (listAdapter != null)
                        listAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_cmd);
        mContext = this;
        mAddress = getIntent().getStringExtra("mac");
        type = getIntent().getIntExtra("type", -1);
        mBleSendCmdUtil = BleSendCmdUtil.getInstance();
        init();
    }

    private void init() {
        mList = new ArrayList<>();
        ListView listView = findViewById(R.id.listview);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mList);
        listView.setAdapter(listAdapter);

        findViewById(R.id.btn_get_did).setOnClickListener(this);
        findViewById(R.id.btnVersion).setOnClickListener(this);
        findViewById(R.id.btnBattery).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.btn_set_unit).setOnClickListener(this);
        findViewById(R.id.btn_set_tare).setOnClickListener(this);
        findViewById(R.id.btn_set_hold).setOnClickListener(this);
        findViewById(R.id.getUnit).setOnClickListener(this);
        ((RadioGroup) findViewById(R.id.radio_weight)).setOnCheckedChangeListener(this);
        ((RadioGroup) findViewById(R.id.radio_height)).setOnCheckedChangeListener(this);

        mListWeight = new ArrayList<>();
        mListHeight = new ArrayList<>();

        mRadioButtonKg = findViewById(R.id.radio_weight_kg);
        mRadioButtonLb = findViewById(R.id.radio_weight_lb);
        mRadioButtonOz = findViewById(R.id.radio_weight_oz);
        mRadioButtonG = findViewById(R.id.radio_weight_g);
        mRadioButtonLbLb = findViewById(R.id.radio_weight_lb_lb);
        mListWeight.add(mRadioButtonKg);
        mListWeight.add(mRadioButtonLb);
        mListWeight.add(mRadioButtonOz);
        mListWeight.add(mRadioButtonG);
        mListWeight.add(mRadioButtonLbLb);

        mRadioButtonCm = findViewById(R.id.radio_height_cm);
        mRadioButtonFoot = findViewById(R.id.radio_height_foot);
        mListHeight.add(mRadioButtonCm);
        mListHeight.add(mRadioButtonFoot);

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (group.getCheckedRadioButtonId()) {
            case R.id.radio_weight_kg:
                weightUnit = BabyBleConfig.BABY_KG;
                break;
            case R.id.radio_weight_lb:
                weightUnit = BabyBleConfig.BABY_LB;
                break;
            case R.id.radio_weight_lb_lb:
                weightUnit = BabyBleConfig.BABY_LB_LB;
                break;
            case R.id.radio_weight_oz:
                weightUnit = BabyBleConfig.BABY_OZ;
                break;
            case R.id.radio_weight_g:
                weightUnit = BabyBleConfig.BABY_G;
                break;
            case R.id.radio_height_cm:
                heightUnit = BabyBleConfig.BABY_CM;
                break;
            case R.id.radio_height_foot:
                heightUnit = BabyBleConfig.BABY_FEET;
                break;


        }
    }


    private void showWeightUnit(int unit) {
        for (RadioButton radioButton : mListWeight) {
            radioButton.setChecked(false);
        }
        switch (unit) {
            case BabyBleConfig.BABY_KG:
                mRadioButtonKg.setChecked(true);
                break;
            case BabyBleConfig.BABY_LB:
                mRadioButtonLb.setChecked(true);
                break;
            case BabyBleConfig.BABY_OZ:
                mRadioButtonOz.setChecked(true);
                break;
            case BabyBleConfig.BABY_G:
                mRadioButtonG.setChecked(true);
                break;
            case BabyBleConfig.BABY_LB_LB:
                mRadioButtonLbLb.setChecked(true);
                break;
        }
    }

    private void showHeightUnit(int unit) {
        for (RadioButton radioButton : mListHeight) {
            radioButton.setChecked(false);
        }
        switch (unit) {
            case BabyBleConfig.BABY_CM:
                mRadioButtonCm.setChecked(true);
                break;
            case BabyBleConfig.BABY_FEET:
                mRadioButtonFoot.setChecked(true);
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVersion:
                babyDevice.sendData(new SendBleBean(BleSendCmdUtil.getInstance().getBleVersion()));
                break;
            case R.id.btnBattery:
                babyDevice.sendData(new SendBleBean(BleSendCmdUtil.getInstance().getMcuBatteryStatus()));
                break;
            case R.id.btn_get_did:
                babyDevice.sendData(new SendBleBean(BleSendCmdUtil.getInstance().getDid()));
                break;

            case R.id.clear:
                if (mList != null)
                    mList.clear();
                mHandler.sendEmptyMessage(REFRESH_DATA);
                break;
            case R.id.btn_set_unit:
                //cm=0,inch=1
                //kg=0,斤=1,lb=2,oz=3,st=4,g=5
                babyDevice.setUnit(weightUnit, weightUnit);
                break;
            case R.id.btn_set_tare:
                babyDevice.setTare();
                break;
            case R.id.btn_set_hold:
                babyDevice.setHold();
                break;
            case R.id.getUnit:
                babyDevice.sendData(new SendBleBean(BleSendCmdUtil.getInstance().getSupportUnit()));
                break;


        }
    }

    //---------------------------------服务---------------------------------------------------


    @Override
    public void onServiceSuccess() {
        BleLog.i(TAG, "服务与界面建立连接成功");
        //与服务建立连接
        if (mBluetoothService != null) {
            BleDevice bleDevice = mBluetoothService.getBleDevice(mAddress);
            if (bleDevice != null) {
                babyDevice = BabyDeviceData.getInstance(bleDevice);
                babyDevice.setOnNotifyData(new babyNotifyData());
                babyDevice.setOnBleVersionListener(BabyCmdActivity.this);
                babyDevice.setOnMcuParameterListener(BabyCmdActivity.this);
                babyDevice.setOnCompanyListener(BabyCmdActivity.this);
            }
        }
    }

    @Override
    public void onServiceErr() {
        BleLog.i(TAG, "服务与界面连接断开");
        //与服务断开连接
        mBluetoothService = null;
    }

    @Override
    public void unbindServices() {
        if (babyDevice != null) {
            babyDevice.disconnect();
            babyDevice.clear();
            babyDevice = null;
        }

    }


    //-----------------状态-------------------


    @Override
    public void onConnecting(@NonNull String mac) {
        //TODO 连接中
        BleLog.i(TAG, "连接中");
    }

    @Override
    public void onDisConnected(@NonNull String mac, int code) {
        //TODO 连接断开
        BleLog.i(TAG, "连接断开");
        finish();
        Toast.makeText(mContext, "连接断开", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServicesDiscovered(@NonNull String mac) {
        //TODO 连接成功(获取服务成功)
        BleLog.i(TAG, "连接成功(获取服务成功)");
    }


    @Override
    public void bleOpen() {

    }

    @Override
    public void bleClose() {
        BleLog.i(TAG, "蓝牙未开启,可请求开启");
    }

    //-----------------通知-------------------

    private class babyNotifyData implements BabyDeviceData.onNotifyData {

        @Override
        public void onData(byte[] hex, int type) {
            String data = "";
            if (hex != null)
                data = BleStrUtils.byte2HexStr(hex);
            if (type == 100) {
                mList.add(TimeUtils.getTime() + "send->" + data);
            } else {
                mList.add(TimeUtils.getTime() + "notify->" + data);
            }
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getWeight(int weight, int decimal, byte unit) {
            String weightStr = BleDensityUtil.getInstance().holdNumber(weight, decimal);
            String showStr="稳定体重:" + weightStr + "|小数:" + decimal + "|单位:" + unit;
            mList.add(TimeUtils.getTime() + showStr);
            if (weightUnit != unit) {
                weightUnit = unit;
                showWeightUnit(weightUnit);
            }
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getWeightNow(int weight, int decimal, byte unit) {
            String weightStr = BleDensityUtil.getInstance().holdNumber(weight, decimal);
            mList.add(TimeUtils.getTime() + "实时体重:" + weightStr + "|小数:" + decimal + "|单位:" + unit);//10.00,2,0
            if (weightUnit != unit) {
                weightUnit = unit;
                showWeightUnit(weightUnit);
            }
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getHeight(int height, int decimal, byte unit) {
            String heightStr = BleDensityUtil.getInstance().holdNumber(height, decimal);
            mList.add(TimeUtils.getTime() + "稳定身高:" + heightStr + "|小数:" + decimal + "|单位:" + unit);
            if (heightUnit != unit) {
                heightUnit = unit;
                showHeightUnit(heightUnit);
            }
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getUnit(byte status) {
            mList.add(TimeUtils.getTime() + "单位结果:" + status);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getTare(byte status) {
            mList.add(TimeUtils.getTime() + "去皮:" + status);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getHold(byte status) {
            mList.add(TimeUtils.getTime() + "锁定:" + status);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }

        @Override
        public void getErr(byte status) {
            mList.add(TimeUtils.getTime() + "错误指令:" + status);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }


    @Override
    public void onBmVersion(String version) {
        mList.add(TimeUtils.getTime() + "版本号:" + version);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }


    @Override
    public void OnDID(int cid, int vid, int pid) {
        String didStr = "cid:" + cid + "||vid:" + vid + "||pid:" + pid;
        mList.add(TimeUtils.getTime() + "ID:" + didStr);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onMcuBatteryStatus(int status, int battery) {
        mList.add(TimeUtils.getTime() + "电量:" + battery + "%");
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onSysTime(int status, int[] times) {
        String time =
                times[0] + "-" + times[1] + "-" + times[2] + "  " + times[3] + ":" + times[4] +
                        ":" + times[5];
        mList.add(TimeUtils.getTime() + "系统时间:" + time);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }


    @Override
    public void onSupportUnit(List<SupportUnitBean> list) {
        StringBuilder unitStr = new StringBuilder();
        unitStr.append(TimeUtils.getTime());

        for (SupportUnitBean supportUnitBean : list) {
            unitStr.append("单位类型:").append(supportUnitBean.getType());
            StringBuilder units = new StringBuilder();
            units.append("[");
            for (Integer integer1 : supportUnitBean.getSupportUnit()) {
                units.append(integer1).append(",");
            }
            units.append("]");
            unitStr.append("单位列表:").append(units);
            unitStr.append("\n");
        }


        mList.add(unitStr.toString());
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleLog.i(TAG, "onDestroy");
    }
}
