package com.entropy.csc.evs;

import android.content.Intent;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class RegistrarActivity extends AppCompatActivity {
    Button writeCardsButton,readCardsButton;
    Boolean loginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        writeCardsButton= (Button) findViewById(R.id.writeCardsButton);
        readCardsButton= (Button) findViewById(R.id.readCardsButton);

        writeCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),CardWriter.class);

                startActivity(intent);
            }
        });

        readCardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),CardReader.class);

                startActivity(intent);
            }
        });
        
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator=getMenuInflater();
        inflator.inflate(R.menu.my_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout_menu_button:
                loginStatus=Boolean.FALSE;
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                intent.putExtra("loginStatus",loginStatus);

                startActivity(intent);

                return true;
            case R.id.setIPbutton:
                intent = new Intent(getApplicationContext(), ConnectToIp.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

