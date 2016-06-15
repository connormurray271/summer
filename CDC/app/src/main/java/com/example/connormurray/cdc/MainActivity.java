package com.example.connormurray.cdc;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private UsbManager manager;
    private UsbSerialPort sPort;

    private SeekBar slider;
    private TextView sliderText;
    private TextView picText;
    private TextView deviceCheck;
    private TextView connectionCheck;
    private TextView driverCheck;
    private TextView dataCheck;
    int progress;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {

                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                            dataCheck.setText("Receiving Data");
                        }
                    });
                }
            };

    private void setMyControlListener() {
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                sliderText.setText("Slider: " +progress);

                String sendString = String.valueOf(progress) + '\n';
                try {
                    sPort.write(sendString.getBytes(), 10);
                } catch (IOException e) {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider = (SeekBar) findViewById(R.id.slider);
        sliderText = (TextView) findViewById(R.id.sliderText);
        picText = (TextView) findViewById(R.id.picText);
        deviceCheck = (TextView) findViewById(R.id.deviceCheck);
        connectionCheck = (TextView) findViewById(R.id.connectionCheck);
        driverCheck = (TextView) findViewById(R.id.driverCheck);
        dataCheck = (TextView) findViewById(R.id.dataCheck);

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        setMyControlListener();

        }



    @Override
    protected void onPause(){
        super.onPause();
        stopIoManager();
        if(sPort != null){
            try{
                sPort.close();
            } catch (IOException e){

            }
            sPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x04D8,0x000A, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);

        final List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);

        if(availableDrivers.isEmpty()) {
            driverCheck.setText("Driver detected");
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        sPort = driver.getPorts().get(0);

        if (sPort == null){
            deviceCheck.setText("No device detected");
        }else{

            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
            if (connection == null){
                connectionCheck.setText("Unable to open device");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                String sendString = String.valueOf(progress) + '\n';
                try {
                    sPort.write(sendString.getBytes(), 10);
                } catch (IOException e) {

                }
            }catch (IOException e) {
                connectionCheck.setText("Serial port didn't open: " + e.getMessage());
                try{
                    sPort.close();
                } catch (IOException e1) {

                }
                sPort = null;
                return;
            }
        }
        onDeviceStateChange();
    }

    private void stopIoManager(){
        if(mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if(sPort != null){
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange(){
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        String sendString = null;
        try {
            sendString = new String(data, "UTF-8");
            picText.setText(sendString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
