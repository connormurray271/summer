package com.example.connormurray.cdcapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SeekBar slider;
    TextView sliderText;
    TextView picText;
    int progress;
    UsbManager usbManager;
    UsbDevice device;
    UsbDeviceConnection connection;
    UsbSerialDevice serialPort;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if(serialPort != null){
                if(serialPort.open()){
                    serialPort.setBaudRate(9600);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);

                }else{

                }
            }else{

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
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        Controller();

        IntentFilter filter = new IntentFilter();

        registerReceiver(broadcastReceiver, filter);

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if(!usbDevices.isEmpty()){
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()){
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if(deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003)){
                    connection = usbManager.openDevice(device);
                    keep = false;
                }else{
                    connection = null;
                    device = null;
                }
                if(!keep) {
                    break;
                }
            }
        }
    }

    private void Controller(){
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                sliderText.setText("Slider: " +progress);
                String sendString = String.valueOf(i) + '\n';
                serialPort.write(sendString.getBytes());
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
