
# Module documentation - Android

[aar package download address](https://github.com/elinkthings/AILinkSdkDemoAndroid/releases)

[key registered address](http://sdk.aicare.net.cn)

[中文文档](README_CN.md)

This document is to guide Android developers to integrate AILink-SDK-Android in Android 4.4 and above systems, and it is mainly for some key usage examples.

## 1. Conditions of use:
1. Android SDK minimum version android4.4 (API 19).
2. The Bluetooth version used by the device needs 4.0 and above.
3. Configure java1.8
4. The project depends on the androidx library

## 二, import SDK


```
repositories {
    flatDir {
        dirs 'libs'
    }
}


1. Add the JitPack repository to your build file
Add this to the root build.gradle at the end of the repository:
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

2. Add dependencies
	dependencies {
	        ...
	        implementation 'com.github.elinkthings:AILinkSDKRepositoryAndroid:1.2.9'//Bluetooth library
	        implementation 'com.github.elinkthings:AILinkSDKParsingLibraryAndroid:1.2.9'//Parsing library
	}

3.Configure java1.8 in gradle

    android {
        ...
        compileOptions {
            sourceCompatibility 1.8
            targetCompatibility 1.8
        }
    }


You can also use the aar package dependency. Please download it to the project's libs yourself.
The download address is at the top of the document.


```

## 二、Permission settings

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

> 6.0 and above systems must locate permissions, and need to obtain permissions manually

## 三, start integration

> Add below AndroidManifest.xml application tag
```
<application>
    ...

    <service android:name="com.pingwang.bluetoothlib.server.ELinkBleServer"/>

</application>

```
> > Initialize  [key registered address](http://sdk.aicare.net.cn)

```
   // Call in application
   AILinkSDK.getInstance().init(this, key, secret);

```

> You can use the BleBeseActivity class provided in the library,
and inherit the implementation method

### 1, Bind service:
```
ps:Bind services where you need to handle Bluetooth, get Bluetooth device objects to handle, or comprehensively handle them in one place.
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
            //Establish a connection with the service
            mBluetoothService = ((ELinkBleServer.BluetoothBinder) service).getService();
            onServiceSuccess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Disconnect from the service
            mBluetoothService = null;
            onServiceErr();
        }
    };

```
### 2, Set the mBluetoothService.setOnCallback ();
```
Implement OnCallbackBle interface to get search, connect, disconnect status and data
/ **
  * Bluetooth search, connection and other operating interfaces
  * /
public interface OnCallbackBle extends OnCallback {
     / **
      * Start scanning device
      * /
     default void onStartScan () ()
     / **
      * Called back every time a device is scanned
      * /
     default void onScanning (BleValueBean data) ()
     / **
      * Scan timeout (completed)
      * /
    default void onScanTimeOut () ()
     / **
      * connecting
      * /
    default void onConnecting (String mac) ()
   / **
      * Connection disconnected in UI thread
      * /
    default void onDisConnected (String mac, int code) {}

     / **
      * Successful connection (discover service), in UI thread
      * /
   default void onServicesDiscovered (String mac) {}

     / **
      * Bluetooth is turned on, triggering thread
      * /
    default void bleOpen () {}

     / **
      * Bluetooth is not turned on, triggering thread
      * /
    default void bleClose () {}
}
```
### 3, search for mBluetoothService.scanLeDevice (long timeOut);//timeOut milliseconds
```
    / **
      * Search device
      * Scanning too often will cause scanning to fail
      * Need to ensure that the total duration of 5 scans exceeds 30s
      * @param timeOut timeout time, milliseconds (how long to search to get data, 0 means always searching)
      * /
      scanLeDevice (long timeOut)

   / **
     * Search device
     * Scanning too often will cause scanning to fail
     * Need to ensure that the total duration of 5 scans exceeds 30s
     * @param timeOut timeout time, milliseconds (how long to search to get data, 0 means always searching)
     * @param scanUUID filtered UUID (empty / null code does not filter)
     * /
     scanLeDevice (long timeOut, UUID scanUUID)

The discovered device will be returned in onScanning (BleValueBean data) in the OnCallbackBle interface
```
### 4, connect mBluetoothService.connectDevice (String mAddress);
```
Note: It is recommended to stop searching for mBluetoothService.stopScan () before connecting,
so the connection process will be more stable After the connection is successful and the service is successfully obtained,
it will be returned in onServicesDiscovered (String mac) in the OnCallbackBle interface
```
### 5,Disconnect
```
 Disconnect all connections mBluetoothService.disconnectAll (),
 since this library supports multiple connections, only the method of disconnecting the device is provided in the service

```

### 6, Get connected device object
```
BleDevice bleDevice = mBluetoothService.getBleDevice (mAddress);
The BleDevice object has all operations on this device, including operations such as disconnecting, sending instructions, receiving instructions, etc.
BleDevice.disconnect (); // Disconnect
BleDevice.sendData (SendDataBean sendDataBean) // Send instructions, the content needs to be encapsulated with SendDataBean


    / **
     * @param hex content
     * @param uuid feature uuid to operate
     * @param type operation type (1 = read, 2 = write, 3 = signal strength) {@link BleConfig}
     * @param uuidService service uuid (Generally, no setting is needed, just use the default one)
     * /
    public SendDataBean (byte [] hex, UUID uuid, int type, UUID uuidService) {
        this.hex = hex;
        this.uuid = uuid;
        this.type = type;
        if (uuidService! = null)
            this.uuidService = uuidService;
 }
Under normal circumstances, you can use a subclass of SendDataBean;
Since there is a sending queue for sending data, the SendDataBean object is not recommended to be reused to avoid data overwriting;
SendBleBean is used to interact with the Bluetooth module;
SendMcuBean is used to interact with MCU;
If there is a need for custom transparent data transmission, please inherit SendDataBean or send it using SendDataBean object.

```
## 四, more commonly used interface introduction
### 1, setOnBleVersionListener (OnBleVersionListener bleVersionListener) in BleDevice // device version number, unit interface
```
  public interface OnBleVersionListener {
    / **
     * BM module software and hardware version number
     * /
    default void onBmVersion (String version) ()
    / **
     * mcu supported units (all are supported by default)
     * @param list null or empty means support all
     * /
    default void onSupportUnit (List <SupportUnitBean> list) {}
}
```
### 2, setOnMcuParameterListener (OnMcuParameterListener mcuParameterListener) in BleDevice // Power, time interface
```
public interface OnMcuParameterListener {
    / **
     * mcu battery status
     * @param status current battery status (0 = no charge, 1 = charging, 2 = full charge, 3 = charging abnormality)
     * @param battery current battery percentage (0 ~ 100), default: 0xFFFF
     * /
   default void onMcuBatteryStatus (int status, int battery) ()

    / **
     * system time
     * @param status time status (0 = invalid, 1 = valid)
     * @param times time array (year, month, day, hour, minute, second)
     * /
   default void onSysTime (int status, int [] times) {}

}
```
### 3, setOnBleOtherDataListener (OnBleOtherDataListener onBleOtherDataListener) in BleDevice // Transparent data interface, if the data format does not conform to the protocol, this interface will return data
```
public interface OnBleOtherDataListener {

    / **
     * Transparent data
     * @param data does not support protocol transparent data
     * /
    void onNotifyOtherData (byte [] data);

}
```

## 五, matters needing attention

#### 1.The Bluetooth library only provides data, and analyzes some ble data. The data connected to the MCU module is not parsed.
#### 2, Please use the AILinkBleParsingAndroid library for module data analysis, which provides analysis templates for each module
#### 3, AILinkBleParsingAndroid library needs to rely on AILinkSDKRepositoryAndroid library, it is not recommended to use it alone
#### 4, The BaseBleDeviceData object is the base class object of the module device. It is recommended to inherit the implementation operation. For more details, please refer to the template in the AILinkBleParsingAndroid library.
#### 5, AILinkBleParsingAndroid library has source code provided, you can find start on github
#### 6, For more operations, please refer to the demo, you can clone this project


## 六, [AILinkBleParsingAndroid library overview](https://elinkthings.github.io/AILinkSDKAndroidDoc/)

#### 1, [baby scale](https://elinkthings.github.io/AILinkSDKAndroidDoc/babyscale/en/index.html)
```
BabyDeviceData parsing class
BabyBleConfig directive configuration class
```
#### 2, [height gauge](https://elinkthings.github.io/AILinkSDKAndroidDoc/height/en/index.html)
```
HeightDeviceData Parsing Class
HeightBleConfig directive configuration class
```
#### 3, [sphygmomanometer](https://elinkthings.github.io/AILinkSDKAndroidDoc/sphygmomanometer/en/index.html)
```
SphyDeviceData parsing class
SphyBleConfig instruction configuration class
```
#### 4, [thermometer](https://elinkthings.github.io/AILinkSDKAndroidDoc/thermometer/en/index.html)
```
TempDeviceData parsing class
TempBleConfig instruction configuration class
```
#### 5, [forehead gun](https://elinkthings.github.io/AILinkSDKAndroidDoc/foreheadgun/en/index.html)
```
TempGunDeviceData parsing class
TempGunBleConfig instruction configuration class
```
#### 6, [TPMS (Smart Tire Pressure)](https://elinkthings.github.io/AILinkSDKAndroidDoc/tpms/en/index.html)
```
TPMS transfer board:
TpmsDeviceData Parsing Class
TpmsBleConfig directive configuration class
```

#### 7, [Body Fat Scale](https://elinkthings.github.io/AILinkSDKAndroidDoc/BodyFatScale/en/index.html)
```
BodyFatBleUtilsData Body Fat Scale Object
BodyFatDataUtil Body fat scale analysis and instruction configuration class
BodyFatRecord Body Fat Record Object (Measured Return)
McuHistoryRecordBean history object
User user information object
```

