package com.fyp.waqasansari.smartlock;

import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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


public class Register extends ActionBarActivity {
    List<NameValuePair> nameValuePairs = new ArrayList<>(3);
    ProgressDialog prgDialog;
    TextView errorMsg;
    EditText usernameET;
    EditText emailET;
    EditText pwdET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        errorMsg = (TextView)findViewById(R.id.register_error);
        usernameET = (EditText)findViewById(R.id.registerUsername);
        emailET = (EditText)findViewById(R.id.registerEmail);
        pwdET = (EditText)findViewById(R.id.registerPassword);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);
        Button btnLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigatetoLoginActivity();
            }
        });

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = usernameET.getText().toString();
                String email = emailET.getText().toString();
                String password = pwdET.getText().toString();
                if(Utility.isNotNull(name) && Utility.isNotNull(email) && Utility.isNotNull(password)){
                    if(Utility.validate(email)){
                        nameValuePairs.add(new BasicNameValuePair("username", name));
                        nameValuePairs.add(new BasicNameValuePair("password", password));
                        nameValuePairs.add(new BasicNameValuePair("email", email));
                        prgDialog.show();
                        Register.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                HttpClient httpClient = new DefaultHttpClient();
                                HttpPost httpPost = new HttpPost("https://homeiot.herokuapp.com/api/signup/");
                                try {
                                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                    HttpResponse httpResponse = httpClient.execute(httpPost);
                                    HttpEntity httpEntity = httpResponse.getEntity();
                                    String _response = EntityUtils.toString(httpEntity);
                                    JSONObject jsonObject = new JSONObject(_response);
                                    if (jsonObject.getString("Status").equals("New user created")) {

                                        prgDialog.dismiss();
                                        navigatetoLoginActivity();
                                    }
                                    else if(jsonObject.getString("Status").equals("Username taken")){
                                        prgDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Username exists. Choose a different name", Toast.LENGTH_LONG).show();
                                        setDefaultValues();
                                        return;
                                    }
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
                    else{
                        Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    //*******************************************************************************************************************************//
    public void navigatetoLoginActivity(){
        Intent loginIntent = new Intent(getApplicationContext(),Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
    public void setDefaultValues(){
        usernameET.setText("");
        emailET.setText("");
        pwdET.setText("");
    }


}
