package com.entropy.csc.evs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static EditText usernameET;
    private static EditText passwordEt;
    private static Button loginButton;

    Context context;

    JSONParser jsonParser=new JSONParser();


   // SharedPreferences sharedPreferences = context.getSharedPreferences("ipPreference",MODE_PRIVATE);
//    SharedPref pref = new SharedPref();
//    SharedPreferences sharedPreferences = pref.getPreference();
//    String ipAddress = sharedPreferences.getString("IP", "");
//    private final String LOGIN_URL="http://"+ipAddress+":8080/EVS/app_login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        usernameET= (EditText) findViewById(R.id.userNameET);
        passwordEt= (EditText) findViewById(R.id.passwordET);
        loginButton= (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=usernameET.getText().toString().trim();
                String password=passwordEt.getText().toString().trim();

                //Checking if all fields have been filled
                if(!username.isEmpty() && !password.isEmpty()){
                    new Login(username,password).execute();
                    Toast.makeText(getApplicationContext(),"Logging in",Toast.LENGTH_LONG).show();
                    Log.d("Request status","Request made");
                }
                else {
                    Toast.makeText(getApplicationContext(),"Please fill in all fields",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkLogin(final String username,final String password){

    }
    class Login extends AsyncTask<String,String,String>{
        String username,password,designation;
        Boolean loginStatus=Boolean.FALSE;
        String message;



        Login(String username, String password){
            this.username=username;
            this.password=password;
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params=new ArrayList<>();
            params.add(new BasicNameValuePair("username",username));
            params.add(new BasicNameValuePair("password",password));

            Log.d("JSONStatus","Prepared request");
            //send username and password to php server for authentication

            SharedPreferences sharedPreferences = context.getSharedPreferences("ipPreference",MODE_PRIVATE);
            final String ipAddress = sharedPreferences.getString("IP", "");
            final String LOGIN_URL="http://"+ipAddress+":8080/EVS/app_login.php";

            try{JSONObject json=jsonParser.makeHttpRequest(LOGIN_URL,"POST",params);
            Log.d("JSONStatus","Request made");

                try{
                    if(json!=null){
                        int success=json.getInt("success");

                        if(success==1){
                            JSONArray userArray=json.getJSONArray("user");
                            JSONObject user=userArray.getJSONObject(0);
                            designation=user.getString("designation");
                            loginStatus=Boolean.TRUE;
                            Log.d("JSONStatus","Login success");

                        }else{
                            Log.d("JSONStatus","Login failure");
                            message=json.getString("message");
                            Log.d("JSONStatus",message);
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Couldn't connect to server",Toast.LENGTH_LONG).show();

                            }});

                        }
                }catch (JSONException e){
                    e.printStackTrace();
                    Log.d("JSON Exception",e.toString());
                }
                //TODO: fix Can't create handler inside thread that has not called Looper.prepare()
            }catch (Exception e){
                e.printStackTrace();
                Log.d("Connection Error",e.toString());
            }


            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            if(Boolean.TRUE){//loginStatus
                if(Boolean.TRUE){
                    Intent intent=new Intent(getApplicationContext(),ExamDetails.class);
                    intent.putExtra("designation",designation);
                    intent.putExtra("loginStatus",loginStatus);

                    startActivity(intent);
                }else if(designation.equals("registrar")){
                    Intent intent=new Intent(getApplicationContext(),RegistrarActivity.class);
                    intent.putExtra("desigantion",designation);
                    intent.putExtra("loginStatus",loginStatus);

                    startActivity(intent);
                }

                
            }else{
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
    }
}
