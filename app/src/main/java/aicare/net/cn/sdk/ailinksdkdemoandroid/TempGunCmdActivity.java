package aicare.net.cn.sdk.ailinksdkdemoandroid;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pingwang.bluetoothlib.device.BleDevice;
import com.pingwang.bluetoothlib.device.BleSendCmdUtil;
import com.pingwang.bluetoothlib.device.SendBleBean;
import com.pingwang.bluetoothlib.device.SendMcuBean;
import com.pingwang.bluetoothlib.listener.CallbackDisIm;
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
import cn.net.aicare.modulelibrary.module.foreheadgun.TempGunDeviceData;


/**
 * xing<br>
 * 2019/4/25<br>
 * 显示数据
 */
public class TempGunCmdActivity extends BleBaseActivity implements OnCallbackDis, OnBleVersionListener, TempGunDeviceData.onNotifyData , OnBleCompanyListener, OnMcuParameterListener, View.OnClickListener {

    private static String TAG = TempGunCmdActivity.class.getName();
    private final int REFRESH_DATA = 3;
    private List<String> mList;
    private ArrayAdapter listAdapter;

    /**
     * 服务Intent
     */
    private Context mContext;
    private EditText et_type;
    private TempGunDeviceData mBleDevice;
    private String mAddress;
    private BleSendCmdUtil mBleSendCmdUtil;
    private int type;
    private byte unit = 0;
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
        setContentView(R.layout.activity_temp_gun);
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

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btnVersion).setOnClickListener(this);
        findViewById(R.id.btnBattery).setOnClickListener(this);
        findViewById(R.id.btn_get_did).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        et_type = findViewById(R.id.et_type);

        cmdBtn();
    }


    private void cmdBtn() {
        Button btn_set_unit = findViewById(R.id.btn_set_unit);
        btn_set_unit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBleDevice != null) {
                    if (unit == 0) {
                        unit=1;
                        mBleDevice.setUnit((byte) 1);
                    } else {
                        unit=0;
                        mBleDevice.setUnit((byte) 0);
                    }
                }
            }
        });
    }



    @Override
    public void onClick(View v) {
        SendBleBean sendBleBean = new SendBleBean();
        switch (v.getId()){
            case R.id.btnVersion:
                sendBleBean.setHex(mBleSendCmdUtil.getBleVersion());
                mBleDevice.sendData(sendBleBean);
                break;
            case R.id.btnBattery:
                sendBleBean.setHex(mBleSendCmdUtil.getMcuBatteryStatus());
                mBleDevice.sendData(sendBleBean);
                break;
            case R.id.btn_get_did:
                sendBleBean.setHex(mBleSendCmdUtil.getDid());
                mBleDevice.sendData(sendBleBean);
                break;
            case R.id.btn1:
                String cmd = et_type.getText().toString().trim();
                SendMcuBean sendDataBean = new SendMcuBean();
                sendDataBean.setHex(type,cmd.getBytes());
                mBleDevice.sendData(sendDataBean);
                break;
            case R.id.clear:
                if (mList != null)
                    mList.clear();
                mHandler.sendEmptyMessage(REFRESH_DATA);
                break;
        }
    }

    //---------------------------------服务---------------------------------------------------


    @Override
    public void onServiceSuccess() {
        BleLog.i(TAG, "服务与界面建立连接成功");
        CallbackDisIm.getInstance().addListListener(this);
        if (mBluetoothService != null) {
            BleDevice bleDevice = mBluetoothService.getBleDevice(mAddress);
            if (bleDevice != null) {
                mBleDevice = TempGunDeviceData.getInstance(bleDevice);
                mBleDevice.setOnNotifyData(TempGunCmdActivity.this);
                mBleDevice.setOnBleVersionListener(TempGunCmdActivity.this);
                mBleDevice.setOnMcuParameterListener(TempGunCmdActivity.this);
                mBleDevice.setOnCompanyListener(TempGunCmdActivity.this);
            } else {
                finish();
                Toast.makeText(mContext, "连接获取对象失败", Toast.LENGTH_SHORT).show();
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
        CallbackDisIm.getInstance().removeListener(this);
        if (mBleDevice != null) {
            mBleDevice.disconnect();
            mBleDevice.clear();
            mBleDevice = null;
        }
    }


    //-----------------状态-------------------


    @Override
    public void onConnecting(@NonNull String mac) {
        //TODO 连接中
        if (mAddress.equals(mac))
            BleLog.i(TAG, "连接中");
    }

    @Override
    public void onDisConnected(@NonNull String mac, int code) {
        //TODO 连接断开
        BleLog.i(TAG, "连接断开");
        finish();
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
        finish();
    }

    //-----------------通知-------------------
    @Override
    public void onBmVersion(String version) {
        mList.add(TimeUtils.getTime() + "版本号:" + version);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void onData(byte[] status,int type) {
        String data = "";
        if (status != null)
            data = BleStrUtils.byte2HexStr(status);
        if (type == 100) {
            mList.add(TimeUtils.getTime() + "send->" + data);
        } else {
            mList.add(TimeUtils.getTime() + "notify->" + data);
        }
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }



    @Override
    public void tempNow(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "实时温度:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void tempEar(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "稳定耳温:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void tempEarNow(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "实时耳温:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void tempSurrounding(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "稳定环境温:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void tempSurroundingNow(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "实时环境温:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void tempBody(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "稳定物温:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void tempBodyNow(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "实时物温:" + tempStr + "||unit:" + unit);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getErr(byte status) {
        mList.add(TimeUtils.getTime() + "错误:" + status);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void getUnit(byte status) {
        mList.add(TimeUtils.getTime() + "单位结果:" + status);
        mHandler.sendEmptyMessage(REFRESH_DATA);
    }

    @Override
    public void temp(int temp, int decimal, byte unit) {
        String tempStr= BleDensityUtil.getInstance().holdDecimals(temp,decimal);
        mList.add(TimeUtils.getTime() + "稳定温度:" + tempStr + "||unit:" + unit);
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
    protected void onDestroy() {
        super.onDestroy();
        BleLog.i(TAG, "onDestroy");
    }
}
