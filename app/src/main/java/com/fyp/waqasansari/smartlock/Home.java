package com.fyp.waqasansari.smartlock;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class Home extends ActionBarActivity {
    String username;
    private ArrayList<HashMap<String, String>> list;
    String HistoryData=null;
    HashMap<String,String> temp;
    ListView listView;
    TextView txtStatus;
    ProgressDialog unlockingDialog;


    //**********************************************************************************************
    String address;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Intent KeyServiceIntent = new Intent(this, KeyNotificationService.class);
        //KeyServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //KeyServiceIntent.putExtra("current-user", username);
        //this.startService(KeyServiceIntent);



        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        String firebaseURL = "https://homeiot.firebaseio.com/" + username + "/lock/history";

        TextView txtUsername = (TextView) findViewById(R.id.txtUsername);
        txtUsername.setText(username);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        Firebase.setAndroidContext(this);

        Firebase myFirebaseRef = new Firebase(firebaseURL);
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    HistoryData = dataSnapshot.getValue().toString();
                } catch (Exception e){
                    HistoryData=null;
                }
                JSONObject HistoryObject;
                try {
                    if (HistoryData != null){
                        list = new ArrayList<>();
                        HistoryObject = new JSONObject(HistoryData);
                        for (int i = 0; i < HistoryObject.names().length(); i++) {
                            JSONObject value = HistoryObject.getJSONObject(HistoryObject.names().getString(i));
                            temp = new HashMap<>();
                            temp.put("name", value.getString("person"));
                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
                            Date date = format.parse(value.getString("time"));
                            temp.put("date", date.toString());
                            list.add(temp);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                listView = (ListView) findViewById(R.id.lstHistory);
                ListDetail adapter = null;
                if(HistoryData != null)
                    adapter = new ListDetail(Home.this, list);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        int pos = position + 1;
                        Toast.makeText(Home.this, Integer.toString(pos) + " Clicked", Toast.LENGTH_SHORT).show();
                    }

                });


            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        unlockingDialog = new ProgressDialog(this);
        unlockingDialog.setMessage("Please Wait...");
        unlockingDialog.setCancelable(false);
        Button btnUnlock = (Button) findViewById(R.id.btnUnlock);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockingDialog.show();
                txtStatus.setText("Unlocking...");
                txtStatus.setTextColor(Color.GREEN);
                try {
                    ConnectAndUnlock();
                } catch (InterruptedException e) {
                    txtStatus.setText("Locked");
                    txtStatus.setTextColor(Color.BLACK);
                    e.printStackTrace();
                }
                unlockingDialog.dismiss();
                txtStatus.setText("Locked");
                txtStatus.setTextColor(Color.BLACK);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_sendkey){
            Intent sendKeyIntent = new Intent(Home.this, MakingGuest.class);
            sendKeyIntent.putExtra("USERNAME", username);
            startActivity(sendKeyIntent);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if( id == R.id.action_available_keys){
            Intent AvailableKeysIntent = new Intent(Home.this, AvailableKeys.class);
            AvailableKeysIntent.putExtra("USERNAME", username);
            startActivity(AvailableKeysIntent);
            return true;
        }


        if (id == R.id.action_start_service) {


            Intent KeyServiceIntent = new Intent(this, KeyNotificationService.class);
            KeyServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            KeyServiceIntent.putExtra("current-user", username);
            this.startService(KeyServiceIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void UpdateHistory(String username, String unlocker){
        Firebase reference = new Firebase("https://homeiot.firebaseio.com/" + username + "/lock/history");

        Map<String, String> values = new HashMap<String, String>();
        values.put("person", unlocker);
        values.put("time", new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault()).format(new Date()));
        reference.push().setValue(values);
    }



    public void ConnectAndUnlock() throws InterruptedException {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            msg("Bluetooth Device Not Available");
            txtStatus.setText("Locked");
            txtStatus.setTextColor(Color.BLACK);
        }
        else
            if (!myBluetooth.isEnabled())
            { Log.d("Bluetooth", "Enabled");  myBluetooth.enable(); unlockingDialog.setMessage("Enabling Bluetooth..."); }
        Thread.sleep(1000);
        pairedDevices = myBluetooth.getBondedDevices();
        unlockingDialog.setMessage("Getting Paied Devices...");
        if(pairedDevices.size() > 0){
            for(BluetoothDevice bt : pairedDevices){
                if(bt.getAddress().equals("98:D3:31:90:49:B3")){
                    address = bt.getAddress();
                    try {
                        if (btSocket == null || !isBtConnected) {
                            unlockingDialog.setMessage("Connecting to " + bt.getName());
                            myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                            BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                            Thread.sleep(500);
                            btSocket.connect();//start connection
                            unlockingDialog.setMessage("Connected");
                            Log.d("Connected", "To device");
                        }
                    }
                    catch (IOException e) {
                        unlockingDialog.dismiss();
                        msg("Something goes wrong");
                        txtStatus.setText("Locked");
                        txtStatus.setTextColor(Color.BLACK);  }

                    isBtConnected = true;
                    unlockTheDoor();
                }
            }
        }else {
            unlockingDialog.dismiss();
            msg("Please pair Lock to your Mobile Phone");
            txtStatus.setText("Locked");
            txtStatus.setTextColor(Color.BLACK);
            return;
        }
    }


    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private void unlockTheDoor() {
        if (btSocket!=null) {
            try {
                unlockingDialog.setMessage("Unlocking...");
                btSocket.getOutputStream().write("TO".getBytes());
                Log.d("TAG", "Unlocking the door...");
                Thread.sleep(500);
                UpdateHistory(username, username);
                txtStatus.setText("Unlocked");
                txtStatus.setTextColor(Color.RED);
            } catch (IOException e) {
                txtStatus.setText("Locked");
                txtStatus.setTextColor(Color.BLACK);
                Log.d("ERROR", e.toString());
            } catch (InterruptedException e) {
                txtStatus.setText("Locked");
                txtStatus.setTextColor(Color.BLACK);
                e.printStackTrace();
            }
        }
    }

}
