package aicare.net.cn.sdk.ailinksdkdemoandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.pingwang.bluetoothlib.bean.BleValueBean;
import com.pingwang.bluetoothlib.config.CmdConfig;
import com.pingwang.bluetoothlib.device.BleDevice;
import com.pingwang.bluetoothlib.device.BleSendCmdUtil;
import com.pingwang.bluetoothlib.device.SendBleBean;
import com.pingwang.bluetoothlib.listener.OnBleCompanyListener;
import com.pingwang.bluetoothlib.listener.OnBleSettingListener;
import com.pingwang.bluetoothlib.listener.OnBleVersionListener;
import com.pingwang.bluetoothlib.listener.OnCallbackBle;
import com.pingwang.bluetoothlib.listener.OnMcuParameterListener;
import com.pingwang.bluetoothlib.utils.BleDensityUtil;
import com.pingwang.bluetoothlib.utils.BleLog;
import com.pingwang.bluetoothlib.utils.BleStrUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleBleConfig;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleBodyFatData;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleBodyFatDataRecord;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleDeviceData;
import cn.net.aicare.modulelibrary.module.ADWeight.ADWeightScaleUserData;


/**
 * xing<br>
 * 2019/7/12<br>
 * 显示数据
 */
public class ADWeightScaleCmdActivity extends BleBaseActivity implements OnCallbackBle, OnBleVersionListener, OnMcuParameterListener, OnBleCompanyListener, OnBleSettingListener,
        ADWeightScaleDeviceData.onNotifyData, View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static String TAG = ADWeightScaleCmdActivity.class.getName();
    private final int REFRESH_DATA = 3;
    private TextView user_id_tv, user_sex_tv, user_age_tv, user_height_tv, user_weight_tv,
            user_adc_tv;
    private List<String> mList;
    private ArrayAdapter listAdapter;
    private Context mContext;
    private ADWeightScaleDeviceData mDevice;
    private String mAddress;
    private BleSendCmdUtil mBleSendCmdUtil;
    private int type;
    private int weightUnit = 0;
    private RadioGroup radio_weight;
    private RadioButton mRadioButtonKg, mRadioButtonLb, mRadioButtonStLb, mRadioButtonJin;
    private List<RadioButton> mListWeight;
    /**
     * 去衣模式状态
     */
    private boolean mUndressing = false;
    /**
     * 阻抗值开关
     */
    private boolean mImpedance = false;
    public static List<ADWeightScaleUserData> sADWeightScaleUserDataList;
    private ADWeightScaleUserData mADWeightScaleUserData;
    public static int sCheckedUserId = 1;
    public static int mUserId = 1;

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
        setContentView(R.layout.activity_ad_weight_scale_cmd);
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

        findViewById(R.id.user).setOnClickListener(this);
        findViewById(R.id.btnVersion).setOnClickListener(this);
        findViewById(R.id.Undressing).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.getRecord).setOnClickListener(this);
        findViewById(R.id.synUserAll).setOnClickListener(this);
        findViewById(R.id.synUser).setOnClickListener(this);
        findViewById(R.id.Impedance).setOnClickListener(this);
        findViewById(R.id.synTime).setOnClickListener(this);
        findViewById(R.id.btnDis).setOnClickListener(this);
        findViewById(R.id.btnConnect).setOnClickListener(this);
        ((RadioGroup) findViewById(R.id.radio_weight)).setOnCheckedChangeListener(this);

        user_id_tv = findViewById(R.id.user_id_tv);
        user_sex_tv = findViewById(R.id.user_sex_tv);
        user_age_tv = findViewById(R.id.user_age_tv);
        user_height_tv = findViewById(R.id.user_height_tv);
        user_weight_tv = findViewById(R.id.user_weight_tv);
        user_adc_tv = findViewById(R.id.user_adc_tv);


        mListWeight = new ArrayList<>();

        radio_weight = findViewById(R.id.radio_weight);
        mRadioButtonKg = findViewById(R.id.radio_weight_kg);
        mRadioButtonJin = findViewById(R.id.radio_weight_jin);
        mRadioButtonStLb = findViewById(R.id.radio_weight_st_lb);
        mRadioButtonLb = findViewById(R.id.radio_weight_lb_lb);
        mListWeight.add(mRadioButtonKg);
        mListWeight.add(mRadioButtonLb);
        mListWeight.add(mRadioButtonStLb);
        mListWeight.add(mRadioButtonJin);
        if (sADWeightScaleUserDataList == null) {
            sADWeightScaleUserDataList = new ArrayList<>();
            ADWeightScaleUserData adWeightScaleUserData = new ADWeightScaleUserData();
            adWeightScaleUserData.setUserId(1);
            adWeightScaleUserData.setSex(ADWeightScaleBleConfig.SEX.MALE);
            adWeightScaleUserData.setAge(20);
            adWeightScaleUserData.setHeight(170);
            adWeightScaleUserData.setWeight(50);
            adWeightScaleUserData.setAdc(500);
            sADWeightScaleUserDataList.add(adWeightScaleUserData);
        }
        mADWeightScaleUserData = sADWeightScaleUserDataList.get(0);
        initUserData(mADWeightScaleUserData);

    }

    @SuppressLint("SetTextI18n")
    private void initUserData(ADWeightScaleUserData adWeightScaleUserData) {
        user_id_tv.setText("用户ID:" + adWeightScaleUserData.getUserId());
        user_sex_tv.setText("性别:" + (adWeightScaleUserData.getSex() == ADWeightScaleBleConfig.SEX.MALE ? "男" : "女"));
        user_age_tv.setText("年龄:" + adWeightScaleUserData.getAge());
        user_height_tv.setText("身高:" + adWeightScaleUserData.getHeight());
        user_weight_tv.setText("体重:" + adWeightScaleUserData.getWeight());
        user_adc_tv.setText("阻抗:" + adWeightScaleUserData.getAdc());

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == -1)
            return;//不是人为点击不触发
        switch (group.getCheckedRadioButtonId()) {
            case R.id.radio_weight_kg:
                weightUnit = ADWeightScaleBleConfig.WEIGHT_KG;
                break;
            case R.id.radio_weight_lb_lb:
                weightUnit = ADWeightScaleBleConfig.WEIGHT_LB_LB;
                break;
            case R.id.radio_weight_st_lb:
                weightUnit = ADWeightScaleBleConfig.WEIGHT_ST;
                break;
            case R.id.radio_weight_jin:
                weightUnit = ADWeightScaleBleConfig.WEIGHT_JIN;
                break;
        }
        BleLog.i(TAG, "weightUnit:" + weightUnit);
        mDevice.setUnit(weightUnit);
    }


    private void showWeightUnit(int unit) {
        switch (unit) {
            case ADWeightScaleBleConfig.WEIGHT_KG:
                mRadioButtonKg.setChecked(true);
                break;
            case ADWeightScaleBleConfig.WEIGHT_LB:
                mRadioButtonLb.setChecked(true);
                break;
            case ADWeightScaleBleConfig.WEIGHT_ST:
                mRadioButtonStLb.setChecked(true);
                break;
            case ADWeightScaleBleConfig.WEIGHT_JIN:
                mRadioButtonJin.setChecked(true);
                break;

        }
    }


    @Override
    public void onClick(View v) {
        SendBleBean sendBleBean = new SendBleBean();
        switch (v.getId()) {
            case R.id.btnVersion:
                sendBleBean.setHex(mBleSendCmdUtil.getBleVersion());
                mDevice.sendData(sendBleBean);
                break;
            case R.id.Undressing:
                mUndressing = !mUndressing;
                mDevice.setUndressing(mUndressing);
                break;
            case R.id.Impedance:
                mImpedance = !mImpedance;
                mDevice.setBleImpedanceDiscern(mImpedance, 50);
                break;

            case R.id.clear:
                if (mList != null)
                    mList.clear();
                mHandler.sendEmptyMessage(REFRESH_DATA);
                break;
            case R.id.getRecord:
                List<Integer> list = new ArrayList<>();
                for (ADWeightScaleUserData adWeightScaleUserData : sADWeightScaleUserDataList) {
                    list.add(adWeightScaleUserData.getUserId());
                }
                mDevice.setBleSynUserHistoryRecord(list);
                break;
            case R.id.synUserAll:
                synUserAll();
                break;
            case R.id.synUser:
                mDevice.setBleUpdateUser(mADWeightScaleUserData);
                break;
            case R.id.user:
                startActivityForResult(new Intent(mContext, ADWeightScaleUserActivity.class), 1);
                break;
            case R.id.synTime:
                if (mDevice != null) {
                    mDevice.setSynTime();
                    mList.add(TimeUtils.getTime() + "同步时间");
                    mHandler.sendEmptyMessage(REFRESH_DATA);
                }
                break;
            case R.id.btnDis:
                if (mDevice != null) {
                    mDevice.disconnect();
                }
                break;
            case R.id.btnConnect:
                startScanBle(0);
                break;


        }
    }

    /**
     * 同步用户
     */
    private void synUserAll() {
        mList.add(TimeUtils.getTime() + "开始同步用户列表");
        mHandler.sendEmptyMessage(REFRESH_DATA);
        for (ADWeightScaleUserData adWeightScaleUserData : sADWeightScaleUserDataList) {
            mDevice.setBleSynUser(adWeightScaleUserData);
        }
        mDevice.setBleSynUserSuccess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            for (ADWeightScaleUserData adWeightScaleUserData : sADWeightScaleUserDataList) {
                if (adWeightScaleUserData.getUserId() == sCheckedUserId) {
                    mADWeightScaleUserData = adWeightScaleUserData;
                    initUserData(mADWeightScaleUserData);
                }
            }

        }
    }


    //---------------------------------服务---------------------------------------------------


    @Override
    public void onServiceSuccess() {
        BleLog.i(TAG, "服务与界面建立连接成功");
        //与服务建立连接
        if (mBluetoothService != null) {
            mBluetoothService.setOnCallback(this);
            BleDevice bleDevice = mBluetoothService.getBleDevice(mAddress);
            if (bleDevice != null) {
                mDevice = ADWeightScaleDeviceData.getInstance(bleDevice);
                mDevice.setOnNotifyData(this);
                mDevice.setOnBleVersionListener(ADWeightScaleCmdActivity.this);
                mDevice.setOnMcuParameterListener(ADWeightScaleCmdActivity.this);
                mDevice.setOnCompanyListener(ADWeightScaleCmdActivity.this);
                mDevice.setOnBleSettingListener(ADWeightScaleCmdActivity.this);
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
        if (mDevice != null) {
            mDevice.disconnect();
            mDevice.clear();
            mDevice = null;
        }

    }

    //-----------------状态-------------------


    @Override
    public void onScanning(BleValueBean data) {
        if (data.getMac().equals(mAddress)) {
            connectBle(mAddress);
            mList.add(TimeUtils.getTime() + "开始连接");
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    @Override
    public void onConnecting(@NonNull String mac) {
        //TODO 连接中
        BleLog.i(TAG, "连接中");
        mList.add(TimeUtils.getTime() + "连接中");
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onDisConnected(@NonNull String mac, int code) {
        //TODO 连接断开
        BleLog.i(TAG, "连接断开");
        mList.add(TimeUtils.getTime() + "连接断开");
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onServicesDiscovered(@NonNull String mac) {
        //TODO 连接成功(获取服务成功)
        BleLog.i(TAG, "连接成功(获取服务成功)");
        mList.add(TimeUtils.getTime() + "连接成功");
        mHandler.sendEmptyMessage(REFRESH_DATA);
        onServiceSuccess();
    }


    @Override
    public void bleOpen() {
        mList.add(TimeUtils.getTime() + "蓝牙打开");
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void bleClose() {
        BleLog.i(TAG, "蓝牙未开启,可请求开启");
        mList.add(TimeUtils.getTime() + "蓝牙关闭");
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    //-----------------通知-------------------


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
    public void getWeight(int weight, int decimal, int unit) {
        String weightStr = BleDensityUtil.getInstance().holdNumber(weight, decimal);

        mList.add(TimeUtils.getTime() + "稳定体重=" + weightStr + ";小数位=" + decimal + ";单位=" + unit);
        if (weightUnit != unit) {
            weightUnit = unit;
            showWeightUnit(weightUnit);
        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getWeightNow(int weight, int decimal, int unit) {
        String weightStr = BleDensityUtil.getInstance().holdNumber(weight, decimal);
        mList.add(TimeUtils.getTime() + "实时体重=" + weightStr + ";小数位=" + decimal + ";单位=" + unit);
        //10.00,2,0
        if (weightUnit != unit) {
            weightUnit = unit;
            showWeightUnit(weightUnit);
        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getTemperature(double temp) {

    }

    @Override
    public void getImpedance(int status, double impedance) {
        switch (status) {
            //测阻抗中
            case ADWeightScaleBleConfig.GET_IMPEDANCE_ING:
                BleLog.i(TAG, "测阻抗中");
                mList.add(TimeUtils.getTime() + "测阻抗中,阻抗值:" + impedance);
                break;
            //测阻抗成功，带上阻抗数据
            case ADWeightScaleBleConfig.GET_IMPEDANCE_SUCCESS:
                BleLog.i(TAG, "测阻抗成功");
                mList.add(TimeUtils.getTime() + "测阻抗成功,阻抗值:" + impedance);
                break;
            //测阻抗失败
            case ADWeightScaleBleConfig.GET_IMPEDANCE_FAILURE:
                BleLog.i(TAG, "测阻抗失败");
                mList.add(TimeUtils.getTime() + "测阻抗失败,阻抗值:" + impedance);
                break;
            //测阻抗成功，带上阻抗数据，并使用 APP 算法，APP 会根据 VID,PID 来识别对应算法
            case ADWeightScaleBleConfig.GET_IMPEDANCE_SUCCESS_APP:
                BleLog.i(TAG, "测阻抗成功,使用app算法");
                mList.add(TimeUtils.getTime() + "测阻抗成功,使用app算法,阻抗值:" + impedance);
                break;
        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getUnit(int status) {
        String msg = "";
        switch (status) {
            case CmdConfig.SETTING_SUCCESS:
                msg = "设置单位成功";
                break;
            case CmdConfig.SETTING_FAILURE:
                msg = "设置单位失败";

                break;
            case CmdConfig.SETTING_ERR:
                msg = "设置单位错误";

                break;
        }
        mList.add(TimeUtils.getTime() + msg);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getUndressing(int status) {
        switch (status) {
            case 0:
                mList.add(TimeUtils.getTime() + "去衣设置成功:" + mUndressing);
                break;
            case 1:
                mList.add(TimeUtils.getTime() + "去衣设置失败" + status);
                break;
            case 2:
                mList.add(TimeUtils.getTime() + "去衣不支持设置" + status);
                break;

        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getImpedance(int status) {
        switch (status) {
            case 0:
                mList.add(TimeUtils.getTime() + "阻抗设置成功:" + mImpedance);
                break;
            case 1:
                mList.add(TimeUtils.getTime() + "阻抗设置失败" + status);
                break;
            case 2:
                mList.add(TimeUtils.getTime() + "阻抗不支持设置" + status);
                break;

        }

        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void requestSynUser() {
        mList.add(TimeUtils.getTime() + "请求同步用户");
        mHandler.sendEmptyMessage(REFRESH_DATA);
        mDevice.setSynUserData(mADWeightScaleUserData);
    }

    @Override
    public void getSynUser(boolean status) {
        mList.add(TimeUtils.getTime() + "同步用户:" + status);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getBodyFatData(ADWeightScaleBodyFatData adWeightScaleBodyFatData) {
        if (adWeightScaleBodyFatData == null) {
            mList.add(TimeUtils.getTime() + "没有体脂数据");
        } else {
            mList.add(TimeUtils.getTime() + "体脂数据:" + adWeightScaleBodyFatData.toString());

        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getBodyFatDataRecord(ADWeightScaleBodyFatDataRecord adWeightScaleBodyFatDataRecord) {
        if (adWeightScaleBodyFatDataRecord != null) {
            mList.add(TimeUtils.getTime() + "历史数据:" + adWeightScaleBodyFatDataRecord.toString());
        } else {
            mList.add(TimeUtils.getTime() + "历史数据:null");

        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getBodyFatDataRecordResult(int status) {
        switch (status) {
            case 0:
                mList.add(TimeUtils.getTime() + "无历史记录");
                break;

            case 1:
                mList.add(TimeUtils.getTime() + "开始发送历史记录");
                break;

            case 2:
                mList.add(TimeUtils.getTime() + "结束发送历史记录");
                break;

        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getErr(byte status) {
        mList.add(TimeUtils.getTime() + "错误指令:" + status);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getAppUpdateUser(int status) {
//00：更新列表成功
        //01：更新个人用户成功
        //02：更新列表失败
        //03：更新个人用户失败
        switch (status) {
            case 0:
                mList.add(TimeUtils.getTime() + "更新列表成功");
                break;
            case 1:
                mList.add(TimeUtils.getTime() + "更新个人用户成功");
                break;
            case 2:
                mList.add(TimeUtils.getTime() + "更新列表失败");
                break;
            case 3:
                mList.add(TimeUtils.getTime() + "更新个人用户失败");
                break;
        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
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
    public void OnSettingReturn(byte cmdType, byte cmdData) {
        String msg = "";
        switch (cmdType) {
            case CmdConfig.SET_SYS_TIME://设置系统当前时间返回
                msg = "设置系统当前时间";
                break;
            case CmdConfig.SET_DEVICE_TIME://同步时间返回
                msg = "同步时间";
                break;
        }
        String cmdDataMsg="";
        switch (cmdData){

            case 0:
                cmdDataMsg="设置成功";
                break;
            case 1:

                cmdDataMsg="设置失败";
                break;
            case 2:

                cmdDataMsg="不支持设置";
                break;

        }
        if (msg.isEmpty())
            mList.add(TimeUtils.getTime() + "设置指令:" + cmdType + ";" + cmdDataMsg + ";");
        else
            mList.add(TimeUtils.getTime() + "设置指令:" + cmdType + ";" + cmdDataMsg + ";" + msg);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleLog.i(TAG, "onDestroy");
    }
}
