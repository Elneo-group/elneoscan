package com.elneo.elneoscan;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.elneo.elneoscan.ProductQueries.GetUserDetails;

import java.net.URL;
import java.util.Arrays;

import de.timroes.axmlrpc.XMLRPCClient;

/**
 * LoginActivity + Asynctask LoginCall to authenticate
 */

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void signInButton(View v) {
        View parent = (View) v.getParent();
        EditText login = (EditText) parent.findViewById(R.id.login);
        EditText password = (EditText) parent.findViewById(R.id.password);

        if (login.getText().toString().length() > 0
                && password.getText().toString().length() > 0) {
            Object call = (Object) new LoginCall(this,
                    login.getText().toString(),
                    password.getText().toString()).execute();
        } else {
            Toast.makeText(getApplicationContext(),
                    "Fill both fields", Toast.LENGTH_LONG).show();
        }
    }

    private class LoginCall extends AsyncTask<Object, Integer, Integer> {
        final String SERVER_URL = "https://odooo.elneo.com/xmlrpc/2/common";
        final String DB_NAME = "elneo";
        final String METHOD_NAME = "authenticate";

        private LoginActivity callerActivity;
        private ProgressDialog progressDialog;

        private String in_login = "";
        private String in_password = "";

        public LoginCall(LoginActivity caller, String login, String password) {
            this.callerActivity = caller;
            this.in_login = login;
            this.in_password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Start progressDialog - Will stop it in postexecute
            this.progressDialog = new ProgressDialog(LoginActivity.this);
            this.progressDialog.setTitle("Login");
            this.progressDialog.setMessage("Login in progress");


            //TODO: Bettah
            this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: Implement

                    }
                }
            );
            this.progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Object... objects) {
            Integer res = null;

            try {
                XMLRPCClient client = new XMLRPCClient(new URL(SERVER_URL));
                res = (Integer) client.call(
                    METHOD_NAME,
                    new Object[]{
                            DB_NAME, in_login, in_password,
                            Arrays.asList()
                    }
                );
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: Handle shit if it hits the fan
            }
            return res;
        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);

            if (res != -1) {
                SharedPreferences sp =
                    PreferenceManager.getDefaultSharedPreferences(
                            this.callerActivity.getApplicationContext());

                EditText login = (EditText)this.callerActivity.findViewById(R.id.login);
                EditText pwd = (EditText)this.callerActivity.findViewById(R.id.password);

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(ElneoScan.USER_LOGIN, login.getText().toString());
                editor.putString(ElneoScan.USER_PASSWORD, pwd.getText().toString());
                editor.putInt(ElneoScan.USER_UID, res);
                editor.commit();
            }

            new GetUserDetails(this.callerActivity, this.progressDialog).execute();
        }
    }
}