package com.elneo.elneoscan.ProductQueries;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.elneo.elneoscan.ElneoScan;
import com.elneo.elneoscan.MainActivity;
import com.elneo.elneoscan.R;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

// AsyncTask to retrieve product details from barcode
public class GetProductDetails extends AsyncTask<Void, Integer, Object> {
    final String SERVER_URL = "http://10.0.0.106/xmlrpc/2/object";

    final String DB_NAME     = "elneo";
    final String METHOD_NAME = "execute_kw";

    private String barcode;

    // Need a ref to update UI
    private MainActivity mainActivity;
    private ProgressDialog progressDialog;

    public Exception e = null;

    public GetProductDetails(MainActivity main, String barcode) {
        this.mainActivity = main;
        this.barcode = barcode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Start progressDialog - Will stop it in postexecute
        this.progressDialog = new ProgressDialog(this.mainActivity);
        this.progressDialog.setTitle("Fetching data");
        this.progressDialog.setMessage("Fetch in progress");
        this.progressDialog.setCancelable(true);
        this.progressDialog.setOnCancelListener(
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            }
        );
        this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }
        );
        this.progressDialog.show();
    }

    @Override
    protected Object doInBackground(Void... objects) {
        Object res = null;
        String password = ElneoScan.getUserPassword(this.mainActivity.getApplicationContext());
        Integer uid = ElneoScan.getUserUid(this.mainActivity.getApplicationContext());
        String warehouse_id = ElneoScan.getUserWarehouse(this.mainActivity.getApplicationContext());

        try {
            XMLRPCClient client = new XMLRPCClient(new URL(SERVER_URL));
            res = client.call(
                METHOD_NAME, DB_NAME, uid, password,
                "product.product",
                "getWrappedProductForBarcode",
                Arrays.asList(
                    Arrays.asList(),
                    Arrays.asList(
                        Arrays.asList("barcode_number","=",this.barcode),
                        Arrays.asList("warehouse_id","=",warehouse_id)
                    )
                )
            );
        } catch (XMLRPCException xe) {
            Log.e(ElneoScan.LOG_TAG, "XMLRPC Error", xe);
            this.e = xe;
        } catch (Exception e) {
            Log.e(ElneoScan.LOG_TAG, "Unexpected error", e);
            this.e = e;
        }
        return res;
    }

    @Override
    protected void onPostExecute(Object s) {
        HashMap<String, Object> results = null;

        if(this.e == null) {
            try {
                results = (HashMap<String, Object>)((Object)s);
            } catch(Exception e) {
                Log.e(ElneoScan.LOG_TAG, "Unexpected error", e);
                this.e = e;
            }
        }

        this.mainActivity.onProductFetched(results);
        this.progressDialog.dismiss();
    }
}
