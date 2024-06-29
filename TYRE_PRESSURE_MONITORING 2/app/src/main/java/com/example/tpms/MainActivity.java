package com.example.tpms;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    // GUI Components
    private TextView tyre1,tyre2,tyre3,tyre4;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button onbtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private ListView mDevicesListView;
    private CheckBox mLED1;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path


    private BluetoothLeScanner bluetoothLeScanner =
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private boolean mScanning;
    private Handler handler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mBluetoothLeScanner;
    private TextView textView;
    Context context;
    UniversalHelper helper;



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        helper = new UniversalHelper(context);

        tyre1 = (TextView)findViewById(R.id.tyre1);
        tyre2 = (TextView)findViewById(R.id.tyre2);
        tyre3 = (TextView)findViewById(R.id.tyre3);
        tyre4 = (TextView)findViewById(R.id.tyre4);

        onbtn = (Button) findViewById(R.id.on);
      //  mBluetoothStatus = (TextView)findViewById(R.id.bluetooth_status);
       // mReadBuffer = (TextView) findViewById(R.id.read_buffer);
      //  mScanBtn = (Button)findViewById(R.id.scan);
      //  mOffBtn = (Button)findViewById(R.id.off);
       // mDiscoverBtn = (Button)findViewById(R.id.discover);
        //mListPairedDevicesBtn = (Button)findViewById(R.id.paired_btn);
       // mLED1 = (CheckBox)findViewById(R.id.checkbox_led_1);

        onbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
        {

        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

       // mDevicesListView = (ListView)findViewById(R.id.devices_list_view);
       // mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
       // mDevicesListView.setOnItemClickListener(mDeviceClickListener);


        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();

        if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
            Toast.makeText(this, "Multiple advertisement not supported", Toast.LENGTH_SHORT).show();
        }

        try {
            advertise();
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(scanCallback);
        }catch (Exception e){

        }




        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        mHandler = new Handler(Looper.getMainLooper()){
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void handleMessage(Message msg){

                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    mReadBuffer.setText(readMessage);
                    String[] separated = readMessage.split("\\s");
                    Log.d("==>",separated[0]);
                }
                if(msg.what == CONNECTING_STATUS){

                        //mBluetoothStatus.setText("Connected to Device: " + msg.obj);

                       // mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            //mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {





        }
    }

    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            //mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();


        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {


                advertise();
                BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(scanCallback);
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
               // mBluetoothStatus.setText("Enabled");
            }

              //  mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(){
        mBTAdapter.disable(); // turn off
       // mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){

                BluetoothLeAdvertiser advertiser =
                        BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

                Log.d("h==>", advertiser.toString());


                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                Log.d("h==>", String.valueOf( device));

               //ScanRecord record = device.getScanRecord();
//                byte[] dataByteArray = device.getBytes();

                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }


    private void listPairedDevices(){
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

           // mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                @Override
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail) {
                        mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
    private void advertise() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();
        Log.i("==>BLE","start of advertise data after settings");
        ParcelUuid pUuid = new ParcelUuid( UUID.fromString("b161c53c-0715-11e6-b512-3e1d05defe78"));

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .setIncludeTxPowerLevel(true)
                .addServiceUuid( pUuid )
                //.addServiceData( pUuid, "Data".getBytes(Charset.forName("UTF-8") ) )
                .build();
        Log.i("==>BLE","before callback");
        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Log.i("BLE==>", "LE Advertise success.");

            }


            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onStartFailure(int errorCode) {
                Log.e("BLE ==>", "Advertising onStartFailure: " + errorCode);
                super.onStartFailure(errorCode);
            }
        };

            advertiser.startAdvertising(settings, data, advertisingCallback);


        Log.i("BLE ==>", "start advertising");
    }



    private final ScanCallback scanCallback = new ScanCallback() {

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("BLE ==>", "start advertising2");
            SparseArray<byte[]> scanRecord = result.getScanRecord().getManufacturerSpecificData();
           // Log.i("BLE3 ==>", toString(scanRecord));
//            mBluetoothStatus.setText(scanRecord);
           // printScanResult(result);
            byte[] scanRecordBytes = result.getScanRecord().getBytes();



          //  String hexaString = new BigInteger(1, scanRecordBytes).toString(16);

            ScanRecord scanRecord2 = result.getScanRecord();
            SparseArray<byte[]> manufacturerData = scanRecord2.getManufacturerSpecificData();
            for(int i = 0; i < manufacturerData .size(); i++){
                int manufacturerId = manufacturerData.keyAt(i);
                Log.i("BLE5 ==>", String.valueOf(manufacturerId));

                if(manufacturerId == 741){
                    String data = byteToHex(scanRecordBytes);
                  //  Log.i("dummy ==>",data );
                    data=data.substring(0,44);
                   // Log.i("BLE8 ==>",data );

                  //  String dummy = data.substring(data.length() - 2);

                    String substring = data.substring(Math.max(data.length() - 2, 0));

                    int decimal=Integer.parseInt(substring,16);

                    Log.i("dummy ==>",data );
                   // Log.i("substring ==>",substring );
                    Log.i("decimal ==>", String.valueOf(decimal));


                    String substring2 = data.substring(Math.max(data.length() - 4, 0));

                    substring2= substring2.substring(0, substring2.length() - 2);

                   // int decimal2=Integer.parseInt(substring2,16);

                  //  Log.i("dummy2 ==>",data );
                    // Log.i("substring ==>",substring );
                    Log.i("decimal2 ==>", String.valueOf(substring2));

                    String temp = String.valueOf(decimal);



                   // tyre4.setText("tyre4:  "+temp);
                    if(substring2.equals("34"))
                    {
                        tyre4.setText("tyre4:  "+temp+ "psi");

                        if(decimal > 20 || decimal <30) {
                            tyre4.setTextColor(Color.parseColor("#FF0000"));

                            if(helper.loadPreferences("tyre4").equals("false")){

                                helper.savePreferences("tyre4","true");
                                sendSMS("8898480707","hi "+temp);
                            }

                        }else {
                            helper.savePreferences("tyre4","false");
                            tyre4.setTextColor(Color.parseColor("#000000"));
                        }

                    }

                    if(substring2.equals("33"))
                    {
                        tyre3.setText("tyre3:  "+temp + "psi");

                        if(decimal > 20 || decimal <30) {
                            if(helper.loadPreferences("tyre3").equals("false")){

                                helper.savePreferences("tyre3","true");
                                sendSMS("8898480707","hi "+temp);
                            }
                            tyre3.setTextColor(Color.parseColor("#FF0000"));
                        }else {
                            helper.savePreferences("tyre3","false");
                            tyre3.setTextColor(Color.parseColor("#000000"));
                        }

                    }

                    if(substring2.equals("32"))
                    {
                        tyre2.setText("tyre2:  "+temp+ "psi");

                        if(decimal > 20 || decimal <30) {
                            if(helper.loadPreferences("tyre2").equals("false")){

                                helper.savePreferences("tyre2","true");
                                sendSMS("8898480707","hi "+temp);
                            }

                            tyre2.setTextColor(Color.parseColor("#FF0000"));
                        }else {
                            helper.savePreferences("tyre2","false");
                            tyre2.setTextColor(Color.parseColor("#000000"));
                        }

                    }
                    if(substring2.equals("31"))
                    {
                        tyre1.setText("tyre1:  "+temp+ "psi");

                        if(decimal > 20 || decimal <30) {
                            if(helper.loadPreferences("tyre1").equals("false")){

                                helper.savePreferences("tyre1","true");
                                sendSMS("8898480707","hi "+temp);
                            }
                            tyre1.setTextColor(Color.parseColor("#FF0000"));
                        }else {
                            helper.savePreferences("tyre1","false");
                            tyre1.setTextColor(Color.parseColor("#000000"));
                        }

                    }




                  // Log.i("BLE4 ==>",byteToHex(scanRecordBytes) );
                }

            }


            //extractBytes(result.getScanRecord().getBytes());

            //Log.i("BLE3 ==>", String.valueOf(asList(scanRecord)));

            Map<ParcelUuid, byte[]> Uuidmap = result.getScanRecord().getServiceData();

           // Log.i(TAG, "onScanResult- Name: " + device.getName() + " rssi: " + result.getRssi() + " Mfg data: " + Arrays.toString(result.getScanRecord().getManufacturerSpecificData(0)));
            for (Map.Entry<ParcelUuid, byte[]> entry : Uuidmap.entrySet()) {
                String key = entry.getKey().toString();
                byte[] value = entry.getValue();
              //
                //  Log.i(TAG, "UUID:==> " + key + " Data: " + Arrays.toString(value));
            }
//            Log.i(TAG, "Data==> " + Arrays.toString(scanRecord));

        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            textView.append("Received " + results.size() + " batch results:\n");
            for (ScanResult r : results) {
                printScanResult(r);
              //  Log.i("BLE ==>", String.valueOf(results));
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onScanFailed(int errorCode) {
            switch (errorCode) {
                case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                    textView.append("Scan failed: already started.\n");
                    break;
                case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    textView.append("Scan failed: app registration failed.\n");
                    break;
                case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                    textView.append("Scan failed: feature unsupported.\n");
                    break;
                case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                    textView.append("Scan failed: internal error.\n");
                    break;
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private void printScanResult(ScanResult result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i("BLE ==>", String.valueOf(result));
                Log.d("==>", String.valueOf(result.getScanRecord().getBytes()));
                Log.i("BLE ==>", String.valueOf(result.getRssi()));

                byte[] scanRecordBytes = result.getScanRecord().getBytes();

                String str = new String(scanRecordBytes, StandardCharsets.UTF_8);
             //   Log.i("BLE ==>", String.valueOf(scanRecordBytes));

                ScanRecord scanRecord = result.getScanRecord();

                SparseArray<byte[]> manufacturerData = scanRecord.getManufacturerSpecificData();
                for(int i = 0; i < manufacturerData .size(); i++){
                    int manufacturerId = manufacturerData.keyAt(i);
                    Log.i("BLE2 ==>", String.valueOf(manufacturerId));
                }

                Log.i("BLE ==>", String.valueOf(manufacturerData));

                String data = String.valueOf(manufacturerData);
                Log.i("BLE ==>", data);
                byte[] scanRecord2 = result.getScanRecord().getBytes();

            }
           // SparseArray<byte[]> manufacturerData = result.getScanRecord().getManufacturerSpecificData();

            //{741=[B@58b08ee}


            //String str = new String(manufacturerData, StandardCharsets.UTF_8);


           // Log.i("BLE ==>", String.valueOf(str));


            String id = result.getDevice() != null ? result.getDevice().getAddress() : "unknown";
            int tx = result.getScanRecord() != null ? result.getScanRecord().getTxPowerLevel() : 0;

        }
        public  int hexToByte(char ch) {
            if ('0' <= ch && ch <= '9') return ch - '0';
            if ('A' <= ch && ch <= 'F') return ch - 'A' + 10;
            if ('a' <= ch && ch <= 'f') return ch - 'a' + 10;
            return -1;
        }

        private  final String[] byteToHexTable = new String[]
                {
                        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F",
                        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1A", "1B", "1C", "1D", "1E", "1F",
                        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C", "2D", "2E", "2F",
                        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3A", "3B", "3C", "3D", "3E", "3F",
                        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4A", "4B", "4C", "4D", "4E", "4F",
                        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5A", "5B", "5C", "5D", "5E", "5F",
                        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6A", "6B", "6C", "6D", "6E", "6F",
                        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7A", "7B", "7C", "7D", "7E", "7F",
                        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8A", "8B", "8C", "8D", "8E", "8F",
                        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9A", "9B", "9C", "9D", "9E", "9F",
                        "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "AA", "AB", "AC", "AD", "AE", "AF",
                        "B0", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "BA", "BB", "BC", "BD", "BE", "BF",
                        "C0", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "CA", "CB", "CC", "CD", "CE", "CF",
                        "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "DA", "DB", "DC", "DD", "DE", "DF",
                        "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "EA", "EB", "EC", "ED", "EE", "EF",
                        "F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "FA", "FB", "FC", "FD", "FE", "FF"
                };

        private  final String[] byteToHexTableLowerCase = new String[]
                {
                        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
                        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
                        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
                        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
                        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
                        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
                        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
                        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
                        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
                        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
                        "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
                        "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
                        "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
                        "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df",
                        "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
                        "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff"
                };

        public  String byteToHex(byte b){
            return byteToHexTable[b & 0xFF];
        }

        public  String byteToHex(byte[] bytes){
            if(bytes == null) return null;
            StringBuilder sb = new StringBuilder(bytes.length*2);
            for(byte b : bytes) sb.append(byteToHexTable[b & 0xFF]);
            return sb.toString();
        }

        public  String byteToHex(short[] bytes){
            StringBuilder sb = new StringBuilder(bytes.length*2);
            for(short b : bytes) sb.append(byteToHexTable[((byte)b) & 0xFF]);
            return sb.toString();
        }

        public  String byteToHexLowerCase(byte[] bytes){
            StringBuilder sb = new StringBuilder(bytes.length*2);
            for(byte b : bytes) sb.append(byteToHexTableLowerCase[b & 0xFF]);
            return sb.toString();
        }

        public  byte[] hexToByte(String hexString) {
            if(hexString == null) return null;
            byte[] byteArray = new byte[hexString.length() / 2];
            for (int i = 0; i < hexString.length(); i += 2) {
                byteArray[i / 2] = (byte) (hexToByte(hexString.charAt(i)) * 16 + hexToByte(hexString.charAt(i+1)));
            }
            return byteArray;
        }

        public  byte hexPairToByte(char ch1, char ch2) {
            return (byte) (hexToByte(ch1) * 16 + hexToByte(ch2));
        }

        public String toString(SparseArray<byte[]> array) {
            if (array == null) {
                return "null";
            }
            if (array.size() == 0) {
                return "{}";
            }
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            for (int i = 0; i < array.size(); ++i) {
                buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
            }
            buffer.append('}');
            return buffer.toString();
        }

//        public  <T> String toString(Map<T, byte[]> map) {
//            if (map == null) {
//                return "null";
//            }
//            if (map.isEmpty()) {
//                return "{}";
//            }
//            StringBuilder buffer = new StringBuilder();
//            buffer.append('{');
//            Iterator<Map.Entry<T, byte[]>> it = map.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<T, byte[]> entry = it.next();
//                Object key = entry.getKey();
//                buffer.append(key).append("=").append(Arrays.toString(map.get(key)));
//                if (it.hasNext()) {
//                    buffer.append(", ");
//                }
//            }
//            buffer.append('}');
//            return buffer.toString();
//        }

        public  <C> List<C> asList(SparseArray<C> sparseArray) {
            if (sparseArray == null) return null;
            List<C> arrayList = new ArrayList<C>(sparseArray.size());
            for (int i = 0; i < sparseArray.size(); i++)
                arrayList.add(sparseArray.valueAt(i));
            return arrayList;
        }


    };









}
