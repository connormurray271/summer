package com.example.connormurray.cdc;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SeekBar slider;
    TextView sliderText;
    TextView comText;

    int progressChanged;

    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    UsbDevice device;
    UsbDeviceConnection connection;

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback()
    {
        @Override
        public void onReceivedData(byte[] arg0){
            comText.setText(""+arg0);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider = (SeekBar) findViewById(R.id.slider);

        sliderText = (TextView) findViewById(R.id.sliderText);
        comText = (TextView) findViewById(R.id.comText);

        setMyControlListener();
        findSerialPortDevice();


    }

    @Override
    protected  void onResume(){
        super.onResume();

        UsbSerialDevice serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if(serialPort != null)
        {
            if(serialPort.open())
            {
                // Devices are opened with default values, Usually 9600,8,1,None,OFF
                // CDC driver default values 115200,8,1,None,OFF
                serialPort.setBaudRate(115200);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);

                String sendString = String.valueOf(100) + '\n';
                serialPort.write(sendString.getBytes());
            }else
            {
                // Serial port could not be opened, maybe an I/O error or it CDC driver was chosen it does not really fit
            }
        }else
        {
            // No driver for given device, even generic CDC driver could not be loaded
        }
    }


    private void setMyControlListener(){
        slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChanged = i;
                sliderText.setText("Value: " +i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void findSerialPortDevice() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if(!usbDevices.isEmpty())
        {
            boolean keep = true;
            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
            {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if(deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003))
                {
                    // We are supposing here there is only one device connected and it is our serial device
                    connection = usbManager.openDevice(device);
                    keep = false;
                }else
                {
                    connection = null;
                    device = null;
                }

                if(!keep)
                    break;
            }
        }
    }
}
