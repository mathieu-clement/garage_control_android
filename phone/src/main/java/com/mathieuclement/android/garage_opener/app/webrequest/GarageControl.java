package com.mathieuclement.android.garage_opener.app.webrequest;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.mathieuclement.android.garage_opener.app.MyApplication;
import com.mathieuclement.android.garage_opener.app.R;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mathieu Clément
 * @since 25.07.2013
 */
public class GarageControl {

    private static final String SERVER_URL = "https://YOUR-SERVER:1234";
    private static final String WEBAPP_URL = SERVER_URL + "/garage/";
    private static final String BASIC_AUTH_CODE = "XXXXXX4UWpSNXc1Zg==";

    public void open() {
        Log.i("GARAGE_CONTROL_PHONE", "GarageControl.open()");
        fireOpenCommandSent();
        sendCommand("open");
    }

    public void close() {
        Log.i("GARAGE_CONTROL_PHONE", "GarageControl.close()");
        fireCloseCommandSent();
        sendCommand("close");
    }

    private void sendCommand(String cmd) {
        if (cmd == null || cmd.length() < 1) {
            throw new IllegalArgumentException("Command is null or empty.");
        }

        try {
            doSendCommand(cmd);
        } catch (Exception e) {
            alertStatus("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void doSendCommand(final String cmd) {
        final AsyncTask<String, Exception, Boolean> task = new AsyncTask<String, Exception, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();

                String phoneNo = getUserPhoneNumber();
                if (!"".equals(phoneNo)) {
                    formEncodingBuilder.add("phone", phoneNo);
                }
                String account = getGoogleAccount();
                if (!"".equals(account)) {
                    formEncodingBuilder.add("account", account);
                }

                RequestBody requestBody = formEncodingBuilder.build();

                Request request = new Request.Builder()
                        .url(WEBAPP_URL + params[0])
                        .addHeader("Authorization", "Basic " + BASIC_AUTH_CODE)
                        .addHeader("User-Agent",
                                "GarageControl " + ((MyApplication) MyApplication.getContext()).getVersion() + " for Android")
                        .post(requestBody)
                        .build();

                OkHttpClient httpClient = new CustomTrustHttpClientBuilder(MyApplication.getContext()).build();
                httpClient.setRetryOnConnectionFailure(true);

                int responseCode;
                try {
                    Response response = httpClient.newCall(request).execute();
                    responseCode = response.code();

                    // Read response
                    //httpResponse.getEntity().consumeContent();
                    CommandResponse commandResponse = null;
                    try {
                        String responseStr = response.body().string();
                        Log.d(this.getClass().getName(), "Response from server:" + responseStr);

                        Gson gson = new Gson();
                        commandResponse = gson.fromJson(responseStr, CommandResponse.class);
                    } catch (Exception e) {
                        publishProgress(e);
                    }

                    if (commandResponse == null) {
                        return false;
                    }

                    if (responseCode >= 200 && responseCode < 300) {
                        if (commandResponse.isSuccess()) {
                            fireCommandSuccess(cmd);
                            return true;
                        } else {
                            publishProgress(new IOException("Has status code " + responseCode + ", which is fine, " +
                                    "but server said request was not successful, which is weird."));
                        }
                    } else {
                        publishProgress(new IOException("Bad status code: " + responseCode + ". Error message: " +
                                (commandResponse.getErrorMessage() == null ? "Null" : commandResponse.getErrorMessage
                                        ())));
                        return false;
                    }
                } catch (Exception e) {
                    publishProgress(e);
                }

                return false;
            }

            @Override
            protected void onProgressUpdate(Exception... values) {
                Log.e(GarageControl.this.getClass().getName(), values[0].getMessage(), values[0]);
                alertStatus(values[0].getLocalizedMessage());
            }

            @Override
            protected void onPostExecute(Boolean commandSuccessful) {
                if (commandSuccessful != null && commandSuccessful) {
                    alertStatus(MyApplication.getContext().getString(R.string.cmd_received));
                } else {
                    fireCommandFailure(cmd);
                }
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cmd);
    }

    private String inputStreamToString(InputStream is, int estimatedLength) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(is);
        StringBuilder sb = new StringBuilder(estimatedLength);
        int c;
        while ((c = inputStreamReader.read()) != -1) {
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static void alertStatus(String text) {
        Toast.makeText(MyApplication.getContext(), text, Toast.LENGTH_SHORT).show();
        Log.d(GarageControl.class.getName(), "Status: " + text);
    }

    private final List<Listener> listeners = new LinkedList<Listener>();

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    private void fireOpenCommandSent() {
        for (Listener listener : listeners) {
            listener.onOpenCommandSent();
        }
    }

    private void fireCloseCommandSent() {
        for (Listener listener : listeners) {
            listener.onCloseCommandSent();
        }
    }

    private void fireCommandSuccess(String cmd) {
        for (Listener listener : listeners) {
            listener.onCommandSuccess(cmd);
        }
    }

    private void fireCommandFailure(String cmd) {
        for (Listener listener : listeners) {
            listener.onCommandFailure(cmd);
        }
    }

    public interface Listener {
        public void onOpenCommandSent();

        public void onCloseCommandSent();

        public void onCommandSuccess(String cmd);

        public void onCommandFailure(String cmd);
    }

    private String getUserPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) MyApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    private String getGoogleAccount() {
        // TODO Add setting to choose associated account
        Account[] accounts = AccountManager.get(MyApplication.getContext()).getAccounts();
        if (accounts.length > 0) {
            return accounts[0].name;
        } else {
            return "";
        }
    }
}
