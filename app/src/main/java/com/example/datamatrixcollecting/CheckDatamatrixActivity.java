package com.example.datamatrixcollecting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.honeywell.aidc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckDatamatrixActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener,
        BarcodeReader.TriggerListener{

    private com.honeywell.aidc.BarcodeReader barcodeReader;
    private ListView barcodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_datamatrix);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        barcodeReader = MainActivity.getBarcodeObject();

        if (barcodeReader != null) {

            // register bar code event listener
            barcodeReader.addBarcodeListener(this);

            // set the trigger mode to client control
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
            } catch (UnsupportedPropertyException e) {
                Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }
            // register trigger state change listener
            barcodeReader.addTriggerListener(this);

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, false);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Disable bad read response, handle in onFailureEvent
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

        // get initial list
        barcodeList = (ListView) findViewById(R.id.listViewBarcodeData);
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI to reflect the data
                List<String> list = new ArrayList<String>();

                String RawData = event.getBarcodeData();

                MarkingCode Code;

                try {
                    Code = new MarkingCode(RawData);
                    list.add("Полный код:");
                    list.add(Code.getFullCode());
                    list.add("Код маркировки:");
                    list.add(Code.getUniqueCode());
                    list.add("GTIN:");
                    list.add(Code.getGTIN());
                    list.add("Серийный номер:");
                    list.add(Code.getSerial());
                    // list.add("Barcode data: " + event.getBarcodeData());
                    //list.add("Character Set: " + event.getCharset());
                    //list.add("Code ID: " + event.getCodeId());
                    //list.add("AIM ID: " + event.getAimId());
                    //list.add("Timestamp: " + event.getTimestamp());

                } catch (Exception e) {

                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, "Некорректный код маркировки!", duration);
                    toast.show();
                }

                final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                        CheckDatamatrixActivity.this, android.R.layout.simple_list_item_1, list);

                barcodeList.setAdapter(dataAdapter);
            }
        });
    }

    @Override
    public void onFailureEvent(final BarcodeFailureEvent event) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(CheckDatamatrixActivity.this, "Нет данных!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onTriggerEvent(final TriggerStateChangeEvent event) {
        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader.aim(event.getState());
            barcodeReader.light(event.getState());
            barcodeReader.decode(event.getState());

        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (barcodeReader != null) {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
        }
    }
}