package com.fyp.waqasansari.smartlock;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



public class Login extends ActionBarActivity {
    List<NameValuePair> nameValuePairs = new ArrayList<>(2);
    ProgressDialog prgDialog;
    TextView errorMsg;
    EditText usernameET;
    EditText pwdET;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);

        errorMsg = (TextView)findViewById(R.id.login_error);
        usernameET = (EditText)findViewById(R.id.loginUsername);
        pwdET = (EditText)findViewById(R.id.loginPassword);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);



        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgDialog.show();
                username = usernameET.getText().toString();
                String password = pwdET.getText().toString();
                if(Utility.isNotNull(username) && Utility.isNotNull(password)){
                    nameValuePairs.add(new BasicNameValuePair("username", username));
                    nameValuePairs.add(new BasicNameValuePair("password", password));
                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpPost httpPost = new HttpPost("https://homeiot.herokuapp.com/api/login/");
                            try {
                                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                HttpResponse httpResponse = httpClient.execute(httpPost);
                                HttpEntity httpEntity = httpResponse.getEntity();
                                String _response = EntityUtils.toString(httpEntity);
                                Log.d("", _response);
                                JSONObject jsonObject = new JSONObject(_response);
                                if(jsonObject.getString("Status").equals("Success")){
                                    prgDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_LONG).show();
                                    navigatetoHomeActivity();
                                } else {
                                    prgDialog.dismiss();
                                    errorMsg.setText(jsonObject.getString("Status"));
                                    Toast.makeText(getApplicationContext(), "Login" + jsonObject.getString("Status"), Toast.LENGTH_LONG).show();
                                    setDefaults();
                                }

                            } catch (UnsupportedEncodingException e) {
                                Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();
                                prgDialog.dismiss();
                                setDefaults();
                                e.printStackTrace();
                            } catch (ClientProtocolException e) {
                                Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();
                                prgDialog.dismiss();
                                setDefaults();
                                e.printStackTrace();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_LONG).show();
                                prgDialog.dismiss();
                                setDefaults();
                                e.printStackTrace();
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                                prgDialog.dismiss();
                                setDefaults();
                                e.printStackTrace();
                            }
                        }
                    });
                } else{
                    Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
                    setDefaults();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Login/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void navigatetoHomeActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),Home.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.putExtra("USERNAME", username);
        startActivity(homeIntent);
    }

    public void navigatetoRegisterActivity(View view){
        Intent registerIntent = new Intent(getApplicationContext(),Register.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(registerIntent);
    }

    public void setDefaults(){
        usernameET.setText("");
        pwdET.setText("");
    }

}
