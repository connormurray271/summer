package com.example.connormurray.cdcapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    public final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    SeekBar slider;
    TextView sliderText;
    TextView picText;
    TextView connectionText;
    TextView checkText;
    TextView dataText;
    TextView readText;
    TextView checkText2;
    TextView checkText3;
    TextView checkText4;
    int progress;
    UsbManager usbManager;
    UsbDevice device;
    UsbDeviceConnection connection;
    UsbSerialDevice serialPort;
    int i = 0;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
//                    checkText4.setText("Granted" + serialPort.open());
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    checkText4.setText("Granted");
                    if(serialPort != null){
                        checkText2.setText("Serial port != null");
                        if(serialPort.open()){
                            checkText3.setText("Serialport.open");
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            readText.setText("Reading");

                        }else{
                            checkText3.setText("Serial is not open");
                        }
                    }else{
                        checkText2.setText("Serial port is null (bad)");

                    }
                }else{
                    checkText4.setText("Not granted");
                }
            }

        }
    };

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback(){

        @Override
        public void onReceivedData(byte[] bytes) {
            String data = null;
            try{
                data = new String(bytes, "UTF-8");
                data.concat("/n");
                picText.setText(data);
                dataText.setText("Getting data");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider = (SeekBar) findViewById(R.id.slider);
        sliderText = (TextView) findViewById(R.id.sliderText);
        picText = (TextView) findViewById(R.id.picText);
        connectionText = (TextView) findViewById(R.id.connectionText);
        checkText = (TextView) findViewById(R.id.checkText);
        dataText = (TextView) findViewById(R.id.dataText);
        readText = (TextView) findViewById(R.id.readText);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        checkText2 = (TextView) findViewById(R.id.checkText2);
        checkText3 = (TextView) findViewById(R.id.checkText3);
        checkText4 = (TextView) findViewById(R.id.checkText4);

        Controller();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if(!usbDevices.isEmpty()){
            checkText.setText("USB connected");
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()){
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if(deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003)){
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device,pi);
                    keep = false;
                    connectionText.setText("Accepted");
                }else{
                    connection = null;
                    device = null;
                    connectionText.setText("Not Accepted");
                }
                if(!keep) {
                    break;
                }
            }
        }else{
            checkText.setText("USB empty");
        }
    }

    private void Controller(){
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                sliderText.setText("Slider: " +progress);
                String sendString = String.valueOf(i) + '\n';
                try{
                    serialPort.write(sendString.getBytes());
                }
                catch (Throwable e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
