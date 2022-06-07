package com.example.btlinktestingapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class BTLinkLeServiceCode extends Service {
    private final static String TAG = BTLinkLeServiceCode.class.getSimpleName();

    public static int count_relayOff;
    public static int count_infocmd;

    public byte[] valueChunk = null;

    private String UUID_service = "725e0bc8-6f00-4d2d-a4af-96138ce599b6"; //first service UUID
    private String UUID_char = "e49227e8-659f-4d7e-8e23-8c6eea5b9173"; //first characteristic UUID
    private String UUID_service_BT = "725e0bc8-6f00-4d2d-a4af-96138ce599b9"; //BT LINK NEW service UUID


    private String UUID_service_file = "725e0bc8-6f00-4d2d-a4af-96138ce599b7";//7
    private String UUID_char_file = "e49227e8-659f-4d7e-8e23-8c6eea5b9174";//4

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private int gt_notify_status = 0;
    BluetoothGatt gatt_notify;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    public final static String ACTION_GATT_CONNECTED =
            "com.TrakEngineering.FluidSecureHubTest.QRLe.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.TrakEngineering.FluidSecureHubTest.QRLe.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.TrakEngineering.FluidSecureHubTest.QRLe.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.TrakEngineering.FluidSecureHubTest.QRLe.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.TrakEngineering.FluidSecureHubTest.QRLe.EXTRA_DATA";

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            gt_notify_status = status;
            gatt_notify = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestMtu(512);
                }
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);

                PulsarTestActivity.btLinkResponse = "STATE_DISCONNECTED";
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            gt_notify_status = status;
            gatt_notify = gatt;
            System.out.println("ACTION_GATT onServicesDiscovered");

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {



                List<BluetoothGattService> services = gatt.getServices();
                AppCommon.IsNewBTFirmware = false;
                for (BluetoothGattService service : services) {
                    String suuid = String.valueOf(service.getUuid());
                    if (!suuid.equals(UUID_service) && !suuid.equals(UUID_service_BT))
                        continue;

                    if (suuid.equals(UUID_service_BT)) {
                        UUID_service = UUID_service_BT;
                        AppCommon.IsNewBTFirmware = true;
                    }
                    List<BluetoothGattCharacteristic> gattCharacteristics =
                            service.getCharacteristics();

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        String cuuid = String.valueOf(gattCharacteristic.getUuid());
                        if (!cuuid.equals(UUID_char))
                            continue;

                        final int charaProp = gattCharacteristic.getProperties();

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            setCharacteristicNotification(gattCharacteristic, true);

                            BluetoothGattCharacteristic init_gatt = gatt.getService(UUID.fromString(UUID_service)).getCharacteristic(UUID.fromString(UUID_char));
                            for (BluetoothGattDescriptor descriptor : init_gatt.getDescriptors()) {
                                Log.e(TAG, "BluetoothGattDescriptor 1: " + descriptor.getUuid().toString());
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }

                        } else {
                            Log.w(TAG, "Characteristic does not support notify");
                        }
                    }
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            gt_notify_status = status;
            gatt_notify = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

                //System.out.println("MJ--Data avail1");

            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //System.out.println("MJ--Data avail2");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "New MTU is " + mtu);
            Log.i(TAG, "Attempting to start service discovery:" +
                    mBluetoothGatt.discoverServices());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            gt_notify_status = status;
            gatt_notify = gatt;
        }



        /*@Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (valueChunk != null)
                if (characteristic.getValue() != valueChunk) {
                    //gatt.abortReliableWrite();
                    System.out.println("aaaa-  abortReliableWrite");
                } else {
                    gatt.executeReliableWrite();
                    System.out.println("aaaa-  executeReliableWrite");
                }
        }*/
    };

    private void broadcastUpdate(final String action) {

        System.out.println("aaaa-" + action);
        if (action.contains("DISCONNECTED"));
            PulsarTestActivity.btLinkResponse = "DISCONNECTED";

        //final Intent intent = new Intent(action);
        //sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();

        String str1 = bytesToHex(data);


        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);

            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            System.out.println("QR data2----" + stringBuilder.toString());
            if (AppCommon.IsNewBTFirmware) {
                intent.putExtra(EXTRA_DATA, new String(data));
            } else {
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }

        sendBroadcast(intent);
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public class LocalBinder extends Binder {
        public BTLinkLeServiceCode getService() {
            return BTLinkLeServiceCode.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new BTLinkLeServiceCode.LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {

        try {

            if (mBluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return false;
            }

            // Previously connected device.  Try to reconnect.
            if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                    && mBluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (mBluetoothGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }

            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return false;
            }
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback, TRANSPORT_LE);
            } else {
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            }

            Log.d(TAG, "Trying to create a new connection.");

            mBluetoothDeviceAddress = address;
            mConnectionState = STATE_CONNECTING;


        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        //mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public void readCustomCharacteristic(boolean bleLFUpdateFlag) {
        try {

            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(UUID_service));

            if (mCustomService == null) {
                Log.w(TAG, "Custom BLE Service not found");
                return;
            }

            BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(UUID_char));

            if (mBluetoothGatt.readCharacteristic(mReadCharacteristic) == false) {
                Log.w(TAG, "Failed to read characteristic");


            } else {
                Log.w(TAG, "Read Characteristics successfully");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeCustomCharacteristic(String bleCommand) {


        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(UUID_service));

        if (mCustomService == null) {
            //2AppConstants.WriteinFile(BTLinkLeServiceCode.this, BTCommandActivity.btLinkResponse+" LeServiceCode ~~~~~~~~~"+bleCommand + " writeCustomCharacteristic Char Not found:" + UUID_char);
            return;
        }

        byte[] strBytes = bleCommand.getBytes();

        BluetoothGattCharacteristic mWriteCharacteristic = null;
        mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(UUID_char));
        mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mWriteCharacteristic.setValue(strBytes);

        if (mBluetoothGatt != null && mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
            //2AppConstants.WriteinFile(BTLinkLeServiceCode.this,"LeServiceCode ~~~~~~~~~"+bleCommand  + " Write Characteristics successfully!");

            if(bleCommand.contains("OFF"))
            {
                BTLinkLeServiceCode.count_relayOff=0;
            }

            if(bleCommand.contains("info"))
            {
                BTLinkLeServiceCode.count_infocmd=0;
            }

        } else {
            //2AppConstants.WriteinFile(BTLinkLeServiceCode.this,"LeServiceCode ~~~~~~~~~"+bleCommand + " Failed to write Characteristics");

            if(bleCommand.contains("OFF"))
            {
                count_relayOff--;
            }

            if(bleCommand.contains("info"))
            {
                count_infocmd--;
            }

        }


    }

    public void writeFileCharacteristic() {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(UUID_service_file));

        if (mCustomService == null) {
            //2AppConstants.WriteinFile(BTLinkLeServiceCode.this, "getService  Not found:" + UUID_service_file);
            return;
        }

        try {


            //String inputFile = Environment.getExternalStorageDirectory().toString() + "/" + "FSBin" + "/LINK_BLUE.bin";
            String inputFile = getApplicationContext().getExternalFilesDir(AppCommon.FOLDER_BIN) + "/LINK_BLUE.bin";

            long fileSize = new File(inputFile).length();
            long tempfileSize = fileSize;

            InputStream inputStream = new FileInputStream(inputFile);
            int BUFFER_SIZE = 490; // mtu
            //int BUFFER_SIZE = 16384; // 16KB buffer size
            //int BUFFER_SIZE = 8192; // 8KB buffer size

            byte[] bufferBytes = new byte[BUFFER_SIZE];

            Thread.sleep(2000);

            while (inputStream.read(bufferBytes) != -1) {

                valueChunk = bufferBytes;

                BluetoothGattCharacteristic mWriteCharacteristic = null;
                mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(UUID_char_file));
                mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mWriteCharacteristic.setValue(bufferBytes);


                if (mBluetoothGatt != null && mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)) {
                    Log.w(TAG, "Write File Characteristics successfully!");

                    //AppConstants.WriteinFile(BTLinkLeServiceCode.this, "writeFileCharacteristic ~~~~File~~~~~ Write Characteristics successfully!");
                } else {

                    break;
                    // AppConstants.WriteinFile(BTLinkLeServiceCode.this, "writeFileCharacteristic ~~~File~~~~~~ Failed to write Characteristics");
                }


                tempfileSize = tempfileSize - 490;
                if (tempfileSize < 490){
                    Integer i = (int) (long) tempfileSize;
                    bufferBytes = new byte[i];
                }

                Thread.sleep(300);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void notifyfun(boolean b) {

        try {
            List<BluetoothGattService> services = getSupportedGattServices();
            for (BluetoothGattService service : services) {
                String suuid = String.valueOf(service.getUuid());
                if (!suuid.equals(UUID_service))
                    continue;

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        service.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    String cuuid = String.valueOf(gattCharacteristic.getUuid());
                    if (!cuuid.equals(UUID_char))
                        continue;

                    final int charaProp = gattCharacteristic.getProperties();

                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        setCharacteristicNotification(gattCharacteristic, b);

                        BluetoothGattCharacteristic init_gatt = gatt_notify.getService(UUID.fromString(UUID_service)).getCharacteristic(UUID.fromString(UUID_char));
                        for (BluetoothGattDescriptor descriptor : init_gatt.getDescriptors()) {
                            Log.e(TAG, "BluetoothGattDescriptor 2: " + descriptor.getUuid().toString());
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt_notify.writeDescriptor(descriptor);
                        }

                    } else {
                        Log.w(TAG, "Characteristic does not support notify");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyClear() {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = null;

        mCustomService = mBluetoothGatt.getService(UUID.fromString(UUID_service));

        if (mCustomService == null) {
            //2AppConstants.WriteinFile(BTLinkLeServiceCode.this, "LeServiceQRCard ~~~~~~~~~" + "writeCustomCharacteristic notifyClear Not found:" + UUID_char);
            return;
        }

        BluetoothGattCharacteristic mWriteCharacteristic = null;
        mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(UUID_char));

        final int charaProp = mWriteCharacteristic.getProperties();

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            setCharacteristicNotification(mWriteCharacteristic, false);

            BluetoothGattCharacteristic init_gatt = mBluetoothGatt.getService(UUID.fromString(UUID_service)).getCharacteristic(UUID.fromString(UUID_char));
            for (BluetoothGattDescriptor descriptor : init_gatt.getDescriptors()) {
                Log.e(TAG, "BluetoothGattDescriptor 3: " + descriptor.getUuid().toString());
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }

        } else {
            Log.w(TAG, "Characteristic does not support notify");
        }
    }

}

