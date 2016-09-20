package com.fyp.waqasansari.smartlock;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class KeyDialog extends ActionBarActivity {
    String sender;
    String address;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    ProgressDialog unlockingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_key_dialog);
        dialog.setTitle("Key Received");

        TextView txtSender = (TextView) dialog.findViewById(R.id.txtSender);
        txtSender.setText(intent.getStringArrayExtra("sender-key")[0]);

        sender = intent.getStringArrayExtra("sender-key")[0];
        final String key=intent.getStringArrayExtra("sender-key")[1];

        Button btnLater = (Button) dialog.findViewById(R.id.btnLater);
        Button btnUnlock = (Button) dialog.findViewById(R.id.btnUnlock);

        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(KeyDialog.this, "Go to your app to unlock later", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        unlockingDialog = new ProgressDialog(this);
        unlockingDialog.setMessage("Please Wait...");
        unlockingDialog.setCancelable(false);

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockingDialog.show();
                Home home = new Home();
                home.UpdateHistory(sender, home.username);

                try {
                    ConnectAndUnlock(key);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                unlockingDialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_key_dialog, menu);
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


    public void ConnectAndUnlock(String key) throws InterruptedException {
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            msg("Bluetooth Device Not Available");
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
                    catch (IOException e) { unlockingDialog.dismiss(); msg("Something goes wrong");  }

                    isBtConnected = true;
                    unlockTheDoor(key);
                }
            }
        }else {
            unlockingDialog.dismiss();
            msg("Please pair Lock to your Mobile Phone");
            return;
        }
    }



    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private void unlockTheDoor(String key) {
        String ToBeSend = "key~" + key;
        if (btSocket!=null) {
            try {
                unlockingDialog.setMessage("Unlocking...");
                btSocket.getOutputStream().write(ToBeSend.getBytes());
                Log.d("TAG", "Unlocking the door...");
                Thread.sleep(500);
            } catch (IOException e) {
                Log.d("ERROR", e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
