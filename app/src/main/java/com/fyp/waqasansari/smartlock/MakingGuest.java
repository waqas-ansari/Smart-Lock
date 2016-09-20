package com.fyp.waqasansari.smartlock;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.loopj.android.http.HttpGet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;


public class MakingGuest extends ActionBarActivity {
    String UserNamesString;
    String guestUser = null;
    String currentUser;
    String[] UserNames;
    ListView lstUsernames;
    Button btnSendKey;

    List<NameValuePair> nameValuePairs = new ArrayList<>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_making_guest);
        Intent intent = getIntent();
        currentUser = intent.getStringExtra("USERNAME");
        btnSendKey = (Button) findViewById(R.id.btnMakeGuest);


        MakingGuest.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet("http://homeiot.herokuapp.com/api/users");
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                try {
                    UserNamesString = httpClient.execute(httpGet, responseHandler);
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        });


        JSONObject NamesObject;
        if (UserNamesString != null) {
            try {
                Log.d("",UserNamesString);
                NamesObject = new JSONObject(UserNamesString);
                UserNames = new String[NamesObject.getJSONArray("users").length()-1];
                int count=0;
                for (int i = 0; i < NamesObject.getJSONArray("users").length(); i++) {
                    Log.d("Count:" + Integer.toString(count), "i:" + Integer.toString(i));
                    if(NamesObject.getJSONArray("users").getString(i).equals(currentUser)) {
                        Log.d("", Integer.toString(i));
                        continue;
                    }
                    UserNames[count] = NamesObject.getJSONArray("users").getString(i);
                    count++;
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            lstUsernames = (ListView) findViewById(R.id.lstUsers);
            lstUsernames.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice, UserNames));
            lstUsernames.setItemsCanFocus(true);
            lstUsernames.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            //GuestSelection adapter = new GuestSelection(MakingGuest.this, UserNames);
            //lstUsernames.setAdapter(adapter);
            lstUsernames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView txtGuest = (TextView) findViewById(R.id.txtGuest);
                    txtGuest.setText("Make " + UserNames[position] + " a guest and Send Key");
                    guestUser = UserNames[position];
                }
            });


        }

        btnSendKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] valid = new String[11];
                valid[0] = "Now";
                String[] exp = new String[10];
                final View dialogView = View.inflate(MakingGuest.this, R.layout.make_guest_dialog, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(MakingGuest.this).create();
                final Spinner validFrom = (Spinner) dialogView.findViewById(R.id.edtValidFrom);
                final Spinner expires = (Spinner) dialogView.findViewById(R.id.edtExpires);
                for(int i=0; i<10; i++){
                    valid[i+1] = String.valueOf(i+1) + " hour from now";
                    exp[i] = String.valueOf(i+1) + " hour";
                }

                validFrom.setAdapter(new ArrayAdapter<>(MakingGuest.this, android.R.layout.simple_spinner_dropdown_item, valid));
                expires.setAdapter(new ArrayAdapter<>(MakingGuest.this, android.R.layout.simple_spinner_dropdown_item, exp));


                dialogView.findViewById(R.id.btnSendKey).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        calendar.add(Calendar.HOUR, validFrom.getSelectedItemPosition() + 1);
                        nameValuePairs.add(new BasicNameValuePair("validFrom", new SimpleDateFormat("yyyyMMddhhmmss").format(calendar.getTime())));
                        calendar.add(Calendar.HOUR,expires.getSelectedItemPosition()+1);
                        nameValuePairs.add(new BasicNameValuePair("expires", new SimpleDateFormat("yyyyMMddhhmmss").format(calendar.getTime())));
                        nameValuePairs.add(new BasicNameValuePair("guest", guestUser));

                        MakingGuest.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                org.apache.http.client.HttpClient httpClient = new org.apache.http.impl.client.DefaultHttpClient();
                                HttpPost httpPost = new HttpPost("http://homeiot.herokuapp.com/api/" + currentUser + "/lock/guests");
                                try {
                                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                    HttpResponse httpResponse = httpClient.execute(httpPost);
                                    HttpEntity httpEntity = httpResponse.getEntity();
                                    String _response = EntityUtils.toString(httpEntity);
                                    JSONObject jsonObject = new JSONObject(_response);
                                    if(jsonObject.getString("Status").equals("Success"))
                                        Toast.makeText(getApplicationContext(), "Successfully Updated", Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(getApplicationContext(), "Something went wrong. Try again.", Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (ClientProtocolException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                });
                alertDialog.setView(dialogView);
                alertDialog.show();

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http:\\homeiot.herokuapp.com/api/username/lock/guests");
                //username, validFrom, expires, guest
            }
        });
    }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_making_guest, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
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
