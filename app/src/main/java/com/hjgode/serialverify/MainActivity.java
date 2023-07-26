package com.hjgode.serialverify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {

    AidcManager manager;
    private com.honeywell.aidc.BarcodeReader barcodeReader;
    Context context=this;
    Database database;
    String TAG="SerialVerify Main";
    TextView textviewSerial;
    TextView textViewModel;
    TextView textViewMandant;
    TextView textViewBezeichnung;
    TextView textViewBemerkung;

     TextView textviewError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textviewSerial=(TextView)findViewById(R.id.textViewSerial);
        textViewModel =(TextView)findViewById(R.id.textViewModel);
        textViewMandant =(TextView)findViewById(R.id.textview_mandant);
        textViewBezeichnung=(TextView)findViewById(R.id.txtViewBezeichnung);
        textViewBemerkung=(TextView)findViewById(R.id.txtViewBemerkung);

        textviewError=(TextView) findViewById(R.id.textviewError);
        textviewError.setVisibility(View.INVISIBLE);

        try {
            // create the AidcManager providing a Context and a
            // CreatedCallback implementation.
            AidcManager.create(this, new AidcManager.CreatedCallback() {
                @Override
                public void onCreated(AidcManager aidcManager) {

                    manager = aidcManager;
                    try{
                        Log.d(TAG, "createBarcodeReader... ");
                        barcodeReader = manager.createBarcodeReader();
                        try {
                            barcodeReader.claim();
                            barcodeReader.addBarcodeListener(MainActivity.this);
                            Log.d(TAG, "BarcodeReader ready!");
                        }catch(Exception e){
                            Log.e(TAG, "Scanner Claim Exception: " + e.getMessage());
                        }                    }
                    catch (InvalidScannerNameException e){
                        Toast.makeText(context, "Invalid Scanner Name Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Invalid Scanner Name Exception: " + e.getMessage());
                    }
                    catch (Exception e){
                        Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Scanner Exception: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "AidcManager exception: " + e.getMessage());
        }

        if (barcodeReader != null) {

            // register bar code event listener
//            barcodeReader.addBarcodeListener(this);
            // set the trigger mode to client control
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);

            } catch (UnsupportedPropertyException e) {
                Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to apply properties: " + e.getMessage());
            }
            // register trigger state change listener
            barcodeReader.addTriggerListener(this);
            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Disable bad read response, handle in onFailureEvent
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
            // Sets time period for decoder timeout in any mode
            properties.put(BarcodeReader.PROPERTY_DECODER_TIMEOUT,  400);
            // Apply the settings
            barcodeReader.setProperties(properties);

        }


        database=new Database(this.context);
        //database.readCSV();
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        Log.d(TAG, "onBarcodeEvent: " + event.getBarcodeData());
        textviewError.setVisibility(View.INVISIBLE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String data = event.getBarcodeData();
                if(data.startsWith("S41")){
                    data=data.substring(1);
                }
                textviewSerial.setText(data);
                if (database.findData(data).length()>0){          // serial;model;bezeichnung;auftrag;bemerkung
                    List<DataModel> datarows=database.getData(data); // "41B8690","6201-265","Hellweg_123","202230450","-"

                    Log.d(TAG, "Found serial data:" + data);
                    List<String> list = new ArrayList<String>();
                    list.add("Found Serial: " + data);
                    //String m = database.getAuftrag(data);
                    //textViewAuftrag.setText(m);
                    DataModel dataModel=datarows.get(0);
                    textViewMandant.setText(dataModel.getAuftrag());
                    textViewModel.setText(dataModel.getModel());
                    textViewBezeichnung.setText(dataModel.getBezeichnung());
                    textViewBemerkung.setText(dataModel.getBemerkung());
                }
                else{
                    //Serial not found
                    textviewError.setVisibility(View.VISIBLE);
/*                    new AlertDialog.Builder(context)
                            .setTitle("Unbekannte Seriennummer")
                            .setMessage("Bitte überprüfen")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Yes

                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    //No
                                }
                            })
                            .show();*/
                }                // update UI to reflect the data

            }
        });
    }

    // When using Automatic Trigger control do not need to implement the
    // onTriggerEvent function
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        boolean bState=event.getState();
        startstopScan(bState);
    }

    void startstopScan(boolean start){

        try {
            // only handle trigger presses
            // turn on/off aimer, illumination and decoding
            barcodeReader.aim(start);
            barcodeReader.light(start);
            barcodeReader.decode(start);

        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
        } catch (ScannerUnavailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context, "No data", Toast.LENGTH_SHORT).show();
            }
        });
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
            startstopScan(false);
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);            // close BarcodeReader to clean up resources.
            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
        Log.d(TAG, "datenbank anzahl: " + Long.toString(database.getDataCount()));
        database.close();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_data:
                Intent dbviewIntent = new Intent(context, DataBaseView.class);
                startActivity(dbviewIntent);
                break;
            case R.id.export_csv:
                database.exportCSV();
                break;
            case R.id.read_csv:
                //async call
                Boolean bAns = showDialog("CSV importieren", "Wirklich alle Daten löschen und neu lesen?");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    boolean showDialog(String title, String question){
        final boolean[] bRet = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(question);

        builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                bRet[0] =true;
                database.readCSV();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NEIN", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                bRet[0]=false;
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
        return bRet[0];
    }
}