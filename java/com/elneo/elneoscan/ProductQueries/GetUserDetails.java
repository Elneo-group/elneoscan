package com.elneo.elneoscan.ProductQueries;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.elneo.elneoscan.ElneoScan;
import com.elneo.elneoscan.LoginActivity;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

/**
 * Created by elneo on 9/27/16.
 */
// AsyncTask to retrieve user details from uid
public class GetUserDetails extends AsyncTask<Void, Integer, Object> {
    final String SERVER_URL = "http://10.0.0.106/xmlrpc/2/object";

    final String DB_NAME     = "elneo";
    final String METHOD_NAME = "execute_kw";

    private LoginActivity loginActivity;
    private ProgressDialog progressDialog;

    private Exception e = null;

    public GetUserDetails(LoginActivity loginActivity, ProgressDialog progressDialog) {
        this.loginActivity = loginActivity;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Void... objects) {
        Object res = null;
        String password = ElneoScan.getUserPassword(this.loginActivity.getApplicationContext());
        Integer uid = ElneoScan.getUserUid(this.loginActivity.getApplicationContext());

        try {
            XMLRPCClient client = new XMLRPCClient(new URL(SERVER_URL));
            res = client.call(
                METHOD_NAME, DB_NAME, uid, password,
                "res.users", "search_read",
                Arrays.asList(
                    Arrays.asList(
                        Arrays.asList("id","=",uid)
                    )
                ),
                new HashMap() {{
                    put("fields", Arrays.asList("id", "name", "default_warehouse_id"));
                }}
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

                SharedPreferences sp =
                        PreferenceManager.getDefaultSharedPreferences(
                                this.loginActivity.getApplicationContext());

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(ElneoScan.KEY_PREF_SELECTED_WAREHOUSE,
                        results.get("default_warehouse_id").toString());

                editor.commit();

            } catch (Exception e) {
                Log.e(ElneoScan.LOG_TAG, "Unexpected error", e);
                this.e = e;
            }
        }

        this.progressDialog.dismiss();
        this.loginActivity.finish();
    }
}
