package com.example.connormurray.cdc;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static UsbSerialPort sPort = null;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {
//                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };


    SeekBar slider;
    TextView sliderText;
    TextView picText;
    TextView deviceCheck;
    TextView connectionCheck;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider = (SeekBar) findViewById(R.id.slider);
        sliderText = (TextView) findViewById(R.id.sliderText);
        picText = (TextView) findViewById(R.id.picText);
        deviceCheck = (TextView) findViewById(R.id.deviceCheck);
        connectionCheck = (TextView) findViewById(R.id.connectionCheck);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sPort == null){
            deviceCheck.setText("No device");
        }else{
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null){
                connectionCheck.setText("Opening device failed");
                return;
            }

            try{
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                connectionCheck.setText("Serial port didn't open");
                try{
                    sPort.close();
                } catch (IOException e1) {

                }
                sPort = null;
                return;
            }
        }
    }

    static void show(Context context, UsbSerialPort port) {
        sPort = port;
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }
}
