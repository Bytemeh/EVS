package com.entropy.csc.evs;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectToIp extends Activity{

    private EditText ipAddressET;
    private Button connect;
    public String ipAddress;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipaddress);

        sharedPreferences = getSharedPreferences("ipPreference",MODE_PRIVATE);

        ipAddressET = (EditText)findViewById(R.id.ipAddressET);
        connect = (Button)findViewById(R.id.ip_button);

        if(sharedPreferences.contains("IP")){
            ipAddressET.setText(sharedPreferences.getString("IP", ""));
        }

        connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ipAddress = ipAddressET.getText().toString();
                Editor editor = sharedPreferences.edit();
                editor.putString("IP",ipAddress);
                editor.apply();

                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"Welcome", Toast.LENGTH_LONG).show();
            }
        });
    }
}
