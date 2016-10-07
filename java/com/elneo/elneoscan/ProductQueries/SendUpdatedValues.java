package com.elneo.elneoscan.ProductQueries;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.elneo.elneoscan.ElneoScan;
import com.elneo.elneoscan.MainActivity;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

public class SendUpdatedValues extends AsyncTask<Void, Integer, Object> {
    //final String SERVER_URL  = "https://odooo.elneo.com/xmlrpc/2/object";
    final String SERVER_URL  = "http://10.0.0.106/xmlrpc/2/object";
    final String DB_NAME     = "elneo";
    final String METHOD_NAME = "execute_kw";

    private MainActivity mainActivity;
    private ProgressDialog progressDialog;

    final private HashMap<String, Object> product;

    private String new_value = null;

    private Exception e = null;

    public SendUpdatedValues(MainActivity mainActivity, String new_value,
                                            HashMap<String, Object> product) {
        this.mainActivity = mainActivity;
        this.new_value = new_value;
        this.product = product;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // Start progressDialog - Will stop it in postexecute
        this.progressDialog = new ProgressDialog(this.mainActivity);
        this.progressDialog.setTitle("Updating");
        this.progressDialog.setMessage("Sending data");
        this.progressDialog.setCancelable(true);
        this.progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                cancel(true);
            }
        });
        this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        this.progressDialog.show();
    }

    @Override
    protected Object doInBackground(Void... voids) {
        String password = ElneoScan.getUserPassword(this.mainActivity.getApplicationContext());
        Integer uid = ElneoScan.getUserUid(this.mainActivity.getApplicationContext());

        Object res = null;
        final Context context = this.mainActivity.getApplicationContext();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        final String date = df.format(Calendar.getInstance().getTime());
        final String val = this.new_value;

        try {
            XMLRPCClient client = new XMLRPCClient(new URL(SERVER_URL));
            res = client.call(
                METHOD_NAME, DB_NAME, uid, password,
                "inventory.entry", "setUpdatedValues",
                Arrays.asList(
                    Arrays.asList(),
                    new HashMap() {{
                        put("user_id", ElneoScan.getUserUid(context));
                        put("product_id", product.get("product_id"));
                        put("old_value", product.get("qty"));
                        put("new_value", val);
                        put("date", date);
                        put("aisle", ElneoScan.getUserAisle(context));
                        put("warehouse_id", ElneoScan.getUserWarehouse(context));
                    }}
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
    protected void onPostExecute(Object o) {
        HashMap<String, Object> results = null;

        if(this.e == null) {
            try {
                results = ((HashMap<String, Object>)(Object)o);
            } catch (Exception e) {
                Log.e(ElneoScan.LOG_TAG, "Unexpected error", e);
                this.e = e;
            }
        }

        // Will return results or null
        this.progressDialog.dismiss();
        this.mainActivity.onQuantityUpdated(results);
    }
}
