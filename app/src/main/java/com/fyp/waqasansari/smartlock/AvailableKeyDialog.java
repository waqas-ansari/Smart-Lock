package com.fyp.waqasansari.smartlock;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class AvailableKeyDialog extends ActionBarActivity {
    String sender;
    ProgressDialog unlockingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final Intent intent = getIntent();


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_available_key_dialog);
        dialog.setTitle("Key Received");

        TextView txtSender = (TextView) dialog.findViewById(R.id.txtSender);
        txtSender.setText(intent.getStringArrayExtra("sender-key")[0]);
        TextView txtExpiry = (TextView) dialog.findViewById(R.id.txtExpiry);
        txtExpiry.setText(intent.getStringArrayExtra("sender-key")[2]);
        TextView txtKey = (TextView) dialog.findViewById(R.id.txtKey);
        txtKey.setText(intent.getStringArrayExtra("sender-key")[1]);

        sender = intent.getStringArrayExtra("sender-key")[0];
        final String key=intent.getStringArrayExtra("sender-key")[1];

        Button btnLater = (Button) dialog.findViewById(R.id.btnLater);
        Button btnUnlock = (Button) dialog.findViewById(R.id.btnUnlock);

        if(intent.getStringArrayExtra("sender-key")[2] == "Expired")
            btnUnlock.setClickable(false);

        btnLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(AvailableKeyDialog.this, "Go to your app to unlock later", Toast.LENGTH_SHORT).show();
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

                KeyDialog keyDialog = new KeyDialog();
                try {
                    keyDialog.ConnectAndUnlock(key);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_available_key_dialog, menu);
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
