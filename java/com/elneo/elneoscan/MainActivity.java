package com.elneo.elneoscan;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elneo.elneoscan.ProductQueries.GetProductDetails;
import com.elneo.elneoscan.ProductQueries.SendNoUpdate;
import com.elneo.elneoscan.ProductQueries.SendUpdatedValues;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.util.ArrayList;
import java.util.HashMap;


// Callback Interfaces
// Callback product fetched from database
interface ProductFetch {
    void onProductFetched(HashMap<String, Object> product);
}

// Callback quantity updated in database
interface QuantityUpdate {
    void onQuantityUpdated(HashMap<String, Object> results);
}

// Callback quantity validated (no change) in database
interface QuantityValidate {
    void onQuantityValidated(HashMap<String, Object> results);
}

public class MainActivity extends AppCompatActivity implements EMDKManager.EMDKListener,
        Scanner.StatusListener, Scanner.DataListener, ProductFetch, QuantityUpdate,
        QuantityValidate, EditQuantityDialogFragment.EditQuantityDialogListener {

    private EMDKManager emdkManager = null;
    private EMDKResults results = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;

    public HashMap<String, Object> product = null;

    // App lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(ElneoScan.LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);

        if(!ElneoScan.hasCredentials(getApplicationContext())) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    // -> Menu (set layout)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // -> Menu (set buttons onclick)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.btn_barcode:
                onBarcodeButtonPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Callback product fetched from database
    @Override
    public void onProductFetched(HashMap<String, Object> product) {
        if(product != null) {
            TextView tv_barcode =
                    (TextView) findViewById(R.id.tv_barcode);
            TextView tv_product_code =
                    (TextView) findViewById(R.id.tv_product_code);
            TextView tv_product_description =
                    (TextView) findViewById(R.id.tv_product_description);
            TextView tv_product_qty =
                    (TextView) findViewById(R.id.tv_product_qty);

            tv_barcode.setText(product.get("barcode").toString());
            tv_product_code.setText(product.get("code").toString());
            tv_product_description.setText(product.get("name").toString());
            tv_product_qty.setText(product.get("qty").toString());

            enableFloatingActionButtons();

            this.product = product;
        } else {
            // TODO : Toast no product
        }

        // Prepare for next scan
        try {
            scanner.cancelRead(); // ADDED ON 03/10 @ 14:46
            scanner.read();
        } catch (Exception e) {
            Log.d("Fuckinscanner", "Post_Execute_eX");
            e.printStackTrace();
            // TODO : Cannot read
        }
    }

    // Callback on (set quantity) dialog close
    @Override
    public void onFinishEditDialog(String input) {
        if(input != null) {}

        //TODO: Success or not
        new SendUpdatedValues(this, input, this.product).execute();
    }

    // Callback on quantity updated in database
    @Override
    public void onQuantityUpdated(HashMap<String, Object> results) {
        if(results != null && results.get("success").toString().equals("true")) {
            TextView textView = (TextView)findViewById(R.id.tv_product_qty);
            textView.setText(results.get("new_value").toString());
        } else {
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onQuantityValidated(HashMap<String, Object> results) {
        if(results != null && results.get("success").toString().equals("true")) {
            //TODO: Save
        } else {
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
        }

    }

    public void onBarcodeButtonPressed() {
        // TODO: Re-init scanner - Aprem

        resetView();
        disableFloatingActionButtons();
        this.product = null;

        try {
            //TODO: CancelRead
            scanner.cancelRead();
            scanner.read();
        } catch (ScannerException se) {
            Log.e(ElneoScan.LOG_TAG, "Scanner exception", se);
            Toast.makeText(this, "Error while initializing scanner",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(ElneoScan.LOG_TAG, "Generic exception", e);
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
        }
    }

    public void resetView() {
        TextView tv_barcode =
                (TextView)findViewById(R.id.tv_barcode);
        TextView tv_product_code =
                (TextView)findViewById(R.id.tv_product_code);
        TextView tv_product_description =
                (TextView)findViewById(R.id.tv_product_description);
        TextView tv_product_qty =
                (TextView)findViewById(R.id.tv_product_qty);

        tv_barcode.setText("");
        tv_product_code.setText("");
        tv_product_description.setText("");
        tv_product_qty.setText("");

        disableFloatingActionButtons();
    }

    // When a barcode is scanned and the product is loaded
    public void enableFloatingActionButtons(){
        FloatingActionButton.OnClickListener qty_listener =
            new FloatingActionButton.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditDialog();
                }
            };
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_qty);
        fab.setOnClickListener(qty_listener);

        final MainActivity mainActivity = this;

        FloatingActionButton.OnClickListener validate_listener =
            new FloatingActionButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SendNoUpdate(mainActivity, product).execute();
                }
            };
        fab = (FloatingActionButton)findViewById(R.id.fab_validate);
        fab.setOnClickListener(validate_listener);
    }

    // When there is no product
    public void disableFloatingActionButtons() {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_qty);
        fab.setOnClickListener(null);

        fab = (FloatingActionButton)findViewById(R.id.fab_validate);
        fab.setOnClickListener(null);
    }

    // Show edit quantity dialog
    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        EditQuantityDialogFragment editQuantityDialogFragment =
                EditQuantityDialogFragment.newInstance("Set new quantity", this);
        editQuantityDialogFragment.show(fm, "fragment_edit_qty");
    }

    // App lifecycle
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(ElneoScan.LOG_TAG, "onStart");
        this.results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        if(this.results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Toast.makeText(this, "Scanner failed to init", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Scanner init successfully", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(ElneoScan.LOG_TAG, "onStop");
        try {
            this.scanner.cancelRead();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(ElneoScan.LOG_TAG, "onDestroy");
        try {
            if(scanner != null) {
                scanner.cancelRead();
                scanner.disable();
                Log.e(ElneoScan.LOG_TAG, "Scanner disabled");
                scanner = null;
            }

            if(emdkManager !=null) {
                emdkManager.release();
                emdkManager = null;
            }
        } catch (ScannerException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Interfaces scanner
    @Override
    public void onOpened(EMDKManager emdkManager) {
        Log.e(ElneoScan.LOG_TAG, "onOpen");
        this.emdkManager = emdkManager;

        try {
            initializeScanner();
        }
        catch (ScannerException se) {
            Log.e(ElneoScan.LOG_TAG, "ScannerException :" + se.getMessage(), se);
            Toast.makeText(this, "Error while initializing scanner", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(ElneoScan.LOG_TAG, "Exception :" + e.getMessage(), e);
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClosed() {
        Log.e(ElneoScan.LOG_TAG, "onClose");

        deInitializeScanner();

        if(this.emdkManager != null) {
            this.emdkManager.release();
            this.emdkManager = null;
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        new AsyncDataUpdate(this).execute(scanDataCollection);
    }

    @Override
    public void onStatus(StatusData statusData) {
        new AsyncStatusUpdate().execute(statusData);
    }

    // Methods
    private void initializeScanner() throws ScannerException {
        if(this.scanner == null) {
            barcodeManager =
                    (BarcodeManager)this.emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            scanner.addDataListener(this);
            scanner.addStatusListener(this);
            scanner.triggerType = com.symbol.emdk.barcode.Scanner.TriggerType.HARD;
            scanner.cancelRead();

            if(!scanner.isEnabled()) {
                scanner.enable();
            }

            if(scanner.isEnabled() && !scanner.isReadPending()) {
                scanner.read();
            }
        }
    }

    private void deInitializeScanner() {
        if(scanner != null) {
            try {
                scanner.cancelRead();
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
                scanner.disable();
            } catch (ScannerException se) {
                Log.e(ElneoScan.LOG_TAG, "ScannerException :" + se.getMessage(), se);
                Toast.makeText(this, "Error while disabling scanner", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(ElneoScan.LOG_TAG, "Exception :" + e.getMessage(), e);
                Toast.makeText(this, "Unexpected error", Toast.LENGTH_LONG).show();
            }
            scanner = null;
        }
    }

    // Fetch barcode, then call GetProductDetails in post execute
    // When GetProductDetails is executed, post execute calls callback "onProductFetched"
    private class AsyncDataUpdate extends AsyncTask<ScanDataCollection, Void, String> {
        private MainActivity main;

        public AsyncDataUpdate(MainActivity main) {
            this.main = main;
        }

        @Override
        protected String doInBackground(ScanDataCollection... scanDataCollections) {
            String status = "";
            try {
                ScanDataCollection scanDataCollection = scanDataCollections[0];

                if(scanDataCollection != null
                        && scanDataCollection.getResult() == ScannerResults.SUCCESS) {

                    ArrayList<ScanDataCollection.ScanData> scanData =
                            scanDataCollection.getScanData();

                    for(ScanDataCollection.ScanData data : scanData) {
                        status = data.getData();
                    }
                }
            } catch (Exception e) {
                Log.e(ElneoScan.LOG_TAG, "Exception :" + e.getMessage(), e);
            }
            return status;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView tv = (TextView)findViewById(R.id.tv_barcode);
            tv.setText(s);

            new GetProductDetails(this.main, s).execute();
        }
    }

    // Get status and update textview
    private class AsyncStatusUpdate extends AsyncTask<StatusData, Void, String> {
        @Override
        protected String doInBackground(StatusData... statusDatas) {
            String status = "";
            StatusData statusData = statusDatas[0];
            StatusData.ScannerStates state = statusData.getState();

            switch(state) {
                case IDLE:
                    status = "The scanner is enabled and idle";
                    break;
                case SCANNING:
                    status = "Scanning ...";
                    break;
                case WAITING:
                    status = "Waiting for trigger press ...";
                    break;
                case DISABLED:
                    status = "Scanner is not enabled";
                    break;
                default:
                    break;
            }
            return status;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView tv = (TextView)findViewById(R.id.tv_status);
            tv.setText(s);
        }
    }
}