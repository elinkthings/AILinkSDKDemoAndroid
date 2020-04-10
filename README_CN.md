
# AILink SDK使用说明 - Android

[aar包下载地址](https://github.com/elinkthings/AILinkSdkDemoAndroid/releases)

[key注册地址](http://sdk.aicare.net.cn)

[English documentation](README.md)

该文档为指导Android开发人员在Android 4.4及以上系统中集成AILink-SDK-Android，主要为一些关键的使用示例
## 一、使用条件：
1.Android SDK最低版本android4.4（API 19）。
2.设备所使用蓝牙版本需要4.0及以上。
3.配置java1.8
4.项目依赖androidx库

## 二、导入SDK


```
repositories {
    flatDir {
        dirs 'libs'
    }
}


1.将JitPack存储库添加到您的构建文件中
将其添加到存储库末尾的root build.gradle中：
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

2.添加依赖项
	dependencies {
	        implementation 'com.github.elinkthings:AILinkSDKRepositoryAndroid:1.2.9'//蓝牙库
	        implementation 'com.github.elinkthings:AILinkSDKParsingLibraryAndroid:1.2.9'//解析库
	}

3.在gradle中配置java1.8
    android {
        ...
        compileOptions {
            sourceCompatibility 1.8
            targetCompatibility 1.8
        }
    }

也可以使用aar包依赖,请自行下载放到项目的libs中,下载地址在文档顶部



```

## 二、权限设置

```
<!--In most cases, you need to ensure that the device supports BLE.-->
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true"/>

<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>

<!--Android 6.0 and above. Bluetooth scanning requires one of the following two permissions. You need to apply at run time.-->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

<!--Optional. If your app need dfu function.-->
<uses-permission android:name="android.permission.INTERNET"/>
```

>  6.0及以上系统必须要定位权限，且需要手动获取权限

## 三、开始集成

> 在AndroidManifest.xml application标签下面增加
```
<application>
    ...

    <service android:name="com.pingwang.bluetoothlib.server.ELinkBleServer"/>

</application>

```
> 初始化 [key注册地址](http://sdk.aicare.net.cn)

```
   //在application中调用
   AILinkSDK.getInstance().init(this, key, secret);

```
> 注:可使用库中提供的BleBeseActivity类,继承实现方法,
里面有绑定服务判断权限等相关操作,详细可参考demo

### 1,绑定服务:
```
ps:在需要处理蓝牙的地方绑定服务,拿到蓝牙设备对象来处理,也可在一个地方综合处理.

private void bindService() {
        if (bindIntent == null) {
            bindIntent = new Intent(this, ELinkBleServer.class);
            if (mFhrSCon != null)
                this.bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE);
        }
    }

    private ServiceConnection mFhrSCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //与服务建立连接
            mBluetoothService = ((ELinkBleServer.BluetoothBinder) service).getService();
            onServiceSuccess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //与服务断开连接
            mBluetoothService = null;
            onServiceErr();
        }
    };
```
### 2,绑定服务成功后设置监听mBluetoothService.setOnCallback();
```
实现OnCallbackBle接口可以获取搜索,连接,断开等状态和数据
/**
 * 蓝牙搜索,连接等操作接口
 */
public interface OnCallbackBle extends OnCallback {
    /**
     * 开始扫描设备
     */
    default void onStartScan(){}
    /**
     * 每扫描到一个设备就会回调一次
     */
    default void onScanning(BleValueBean data){}
    /**
     * 扫描超时(完成)
     */
   default void onScanTimeOut(){}
    /**
     * 正在连接
     */
   default void onConnecting(String mac){}
  /**
     * 连接断开,在UI线程
     */
   default void onDisConnected(String mac, int code){}

    /**
     * 连接成功(发现服务),在UI线程
     */
  default void onServicesDiscovered(String mac){}

    /**
     * 已开启蓝牙,在触发线程
     */
   default void bleOpen(){}

    /**
     * 未开启蓝牙,在触发线程
     */
   default void bleClose(){}
}
```
### 3,搜索 mBluetoothService.scanLeDevice(long timeOut);//timeOut毫秒
```
    /**
     * 搜索设备
     * 扫描过于频繁会导致扫描失败
     * 需要保证5次扫描总时长超过30s
     * @param timeOut 超时时间,毫秒(搜索多久去取数据,0代表一直搜索)
     */
     scanLeDevice(long timeOut)

   /**
     * 搜索设备
     * 扫描过于频繁会导致扫描失败
     * 需要保证5次扫描总时长超过30s
     * @param timeOut  超时时间,毫秒(搜索多久去取数据,0代表一直搜索)
     * @param scanUUID 过滤的UUID(空/null代码不过滤)
     */
     scanLeDevice(long timeOut, UUID scanUUID)

搜索到的设备会在OnCallbackBle接口中的onScanning(BleValueBean data)返回
```
### 4,连接mBluetoothService.connectDevice(String mAddress);
```
注:连接之前建议停止搜索mBluetoothService.stopScan(),这样连接过程会更稳定
连接成功并获取服务成功后会在OnCallbackBle接口中的onServicesDiscovered(String mac)返回
```
### 5,断开连接
```
mBluetoothService.disconnectAll()断开所有连接,由于此库支持多连接,
所以service中只提供断开设备的方法,可在BleDevice.disconnect();断开连接
```

### 6,获取连接的设备对象
```
BleDevice bleDevice = mBluetoothService.getBleDevice(mAddress);
BleDevice对象拥有对此设备的所有操作,包括断开连接,发送指令,接收指令等操作
BleDevice.disconnect();//断开连接
BleDevice.sendData(SendDataBean sendDataBean)//发送指令,内容需要用SendDataBean装载


    /**
     * @param hex         发送的内容
     * @param uuid        需要操作的特征uuid
     * @param type        操作类型(1=读,2=写,3=信号强度) {@link BleConfig}
     * @param uuidService 服务uuid(一般情况下不需要设置,使用默认的即可)
     */
    public SendDataBean(byte[] hex, UUID uuid, int type, UUID uuidService) {
        this.hex = hex;
        this.uuid = uuid;
        this.type = type;
        if (uuidService != null)
            this.uuidService = uuidService;
 }

正常情况下,使用SendDataBean的子类即可;
由于发送数据存在发送队列,SendDataBean对象不建议复用,避免数据给覆盖;
SendBleBean用于与蓝牙模块交互;
SendMcuBean用于与mcu交互;
如果有自定义透传数据需求,请自行继承SendDataBean或者使用SendDataBean对象发送即可.

```

## 四、较常用的接口介绍
### 1,BleDevice 中的setOnBleVersionListener(OnBleVersionListener bleVersionListener)//设备版本号,单位接口
```
  public interface OnBleVersionListener {
    /**
     * BM 模块软、硬件版本号
     */
    default void onBmVersion(String version){}
    /**
     * mcu 支持的单位(默认支持所有)
     * @param list null或者空代表支持所有
     */
    default void onSupportUnit(List<SupportUnitBean> list) {}
}
```
### 2,BleDevice 中的setOnMcuParameterListener(OnMcuParameterListener mcuParameterListener)//电量,时间接口
```
public interface OnMcuParameterListener {
    /**
     * mcu电池状态
     * @param status  当前电池状态(0=没充电,1=充电中,2=充满电,3=充电异常)
     * @param battery 当前电量百分比(0~100),默认为:0xFFFF
     */
   default void onMcuBatteryStatus(int status, int battery){}

    /**
     * 系统时间
     * @param status 时间状态(0=无效,1=有效)
     * @param times 时间数组(年,月,日,时,分,秒)
     */
   default void onSysTime(int status, int[] times){}

}
```
### 3,BleDevice 中的setOnBleOtherDataListener(OnBleOtherDataListener onBleOtherDataListener) //透传数据接口,数据格式不符合协议的才会走此接口返回数据
```
public interface OnBleOtherDataListener {

    /**
     * 透传数据
     * @param data 不支持协议的透传数据
     */
    void onNotifyOtherData(byte[] data);

}
```

## 五、注意事项

#### 1,蓝牙库只提供数据,解析部分ble数据,mcu模块对接的数据不解析
#### 2,模块数据解析请使用AILinkBleParsingAndroid 库,里面有提供各模块的解析模板
#### 3,AILinkBleParsingAndroid库需要依赖AILinkSDKRepositoryAndroid库,不建议单独使用
#### 4,BaseBleDeviceData对象为模块设备的基类对象,建议继承实现操作,更多请参考AILinkBleParsingAndroid库中的模板
#### 5,AILinkBleParsingAndroid库有源码提供,可在github上查找start
#### 6,更多操作请参考demo,将此项目clone下来即可


## 六、[AILinkBleParsingAndroid库概述](https://elinkthings.github.io/AILinkSDKAndroidDoc/README_CN.html)

#### 1,[婴儿秤](https://elinkthings.github.io/AILinkSDKAndroidDoc/babyscale/zh/index.html)
```
BabyDeviceData解析类
BabyBleConfig 指令配置类
```
#### 2,[身高仪](https://elinkthings.github.io/AILinkSDKAndroidDoc/height/zh/index.html)
```
HeightDeviceData解析类
HeightBleConfig指令配置类
```
#### 3,[血压计](https://elinkthings.github.io/AILinkSDKAndroidDoc/sphygmomanometer/zh/index.html)
```
SphyDeviceData解析类
SphyBleConfig指令配置类
```
#### 4,[体温计](https://elinkthings.github.io/AILinkSDKAndroidDoc/thermometer/zh/index.html)
```
TempDeviceData解析类
TempBleConfig指令配置类
```
#### 5,[额温枪](https://elinkthings.github.io/AILinkSDKAndroidDoc/foreheadgun/zh/index.html)
```
TempGunDeviceData解析类
TempGunBleConfig指令配置类
```
#### 6,[TPMS(智能胎压)](https://elinkthings.github.io/AILinkSDKAndroidDoc/tpms/zh/index.html)
```
TPMS转接板:
TpmsDeviceData解析类
TpmsBleConfig指令配置类
```
#### 7,[体脂秤](https://elinkthings.github.io/AILinkSDKAndroidDoc/BodyFatScale/zh/index.html)
```
BodyFatBleUtilsData 体脂秤对象
BodyFatDataUtil 体脂秤解析和指令配置类
BodyFatRecord 体脂记录对象(测量返回)
McuHistoryRecordBean 历史记录对象
User 用户信息对象
```