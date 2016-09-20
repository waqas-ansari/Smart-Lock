package com.fyp.waqasansari.smartlock;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.HttpGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


public class AvailableKeys extends ActionBarActivity {
    String[] key;
    String[] validFrom;
    String[] Senders;
    String[] Expires;
    HashMap<String,String> temp;
    String AllKeys;
    String Username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_keys);

        Intent intent = getIntent();
        Username = intent.getStringExtra("USERNAME");

        AvailableKeys.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("http://homeiot.herokuapp.com/api/" + Username +"/lock/available-keys");
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                try {
                    AllKeys = httpClient.execute(httpGet, responseHandler);
                } catch (IOException e) {
                    Log.d("", e.toString());
                    e.printStackTrace();
                }
            }
        });

        JSONArray KeysArray;
        if (AllKeys != null) {
            try {
                Log.d("", AllKeys);
                KeysArray = new JSONObject(AllKeys).getJSONArray("keys");
                key = new String[KeysArray.length()];
                Senders = new String[KeysArray.length()];
                Expires = new String[KeysArray.length()];
                validFrom = new String[KeysArray.length()];
                for (int i = 0; i < KeysArray.length(); i++) {
                    JSONObject value = new JSONObject(KeysArray.getString(i));
                    Senders[i] = value.getString("sender");
                    Expires[i] = value.getString("expires");
                    key[i] = value.getString("key");
                    validFrom[i] = value.getString("validFrom");

                }
            } catch (JSONException e) {
                Log.d("", e.toString());
                Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            Log.d("", "Putting in Adapter");
            ListView lstAvailableKeys = (ListView) findViewById(R.id.lstAvailableKeys);
            Log.d("", "Set an Adapter");
            lstAvailableKeys.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Senders));
            lstAvailableKeys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String expired = "Not Expired";
                    Log.d("Sender: ", Senders[position]);
                    Log.d("Key: ", key[position]);
                    Log.d("Expiry: ", Expires[position]);
                    Log.d("Valid From: ", validFrom[position]);
                    java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMddhhmmss");
                    Date date1 = null;
                    Date date2 = null;
                    try {
                        date1 = df.parse(Expires[position]);
                        date2 = df.parse(validFrom[position]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long diff = (date2.getTime() - date1.getTime());
                    if(diff < 0)
                        expired="Expired";

                    String[] ToPutExtra = new String[3];
                    ToPutExtra[0] = Senders[position];
                    ToPutExtra[1] = key[position];
                    ToPutExtra[2] = expired;

                    Intent intent1 = new Intent(AvailableKeys.this, AvailableKeyDialog.class);
                    intent1.putExtra("sender-key", ToPutExtra);
                    startActivity(intent1);

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_available_keys, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
