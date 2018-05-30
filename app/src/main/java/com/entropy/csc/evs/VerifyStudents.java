package com.entropy.csc.evs;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

public class VerifyStudents extends AppCompatActivity {
    String courseCode;
    String program;
    String academicYear;
    String examid,courseName;
    int noStudents;
    Boolean loginStatus;

    ImageView studentImageView;
    TextView std_name_view,std_number_view,reg_number_view,program_view,account_balance_view;
    EditText searchStudent;
    Button searchButton,verifDoneButton;

    JSONObject student;
    Hashtable<Integer,JSONObject> studs;
    JSONArray logs;

    Context context;

    JSONParser jsonParser=new JSONParser();

    NfcAdapter nfcAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_students);
        context = this;

        verifDoneButton= (Button) findViewById(R.id.verifDone_btn);
        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

        //Getting values passed to the activity
        Intent i=getIntent();
        courseCode=i.getStringExtra("courseCode");
        program=i.getStringExtra("program");
        academicYear=i.getStringExtra("academicYear");

       // Toast.makeText(getApplicationContext(),courseCode+" "+program+" "+academicYear,Toast.LENGTH_LONG).show();

        new GetData().execute();
        verifDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new sendLogs().execute();
                Intent intent=new Intent(getApplicationContext(),FinishedActivity.class);
                intent.putExtra("courseName",courseName);
                intent.putExtra("courseCode",courseCode);
                intent.putExtra("noStudents",String.valueOf(noStudents));



                startActivity(intent);
            }
        });


    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onNewIntent(Intent intent) {

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            Toast.makeText(getApplicationContext(),"NFCIntent",Toast.LENGTH_LONG).show();

            Parcelable[] parcelables=intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if(parcelables!=null && parcelables.length>0){
                readTextFromMessage((NdefMessage)parcelables[0]);
            }else{
                Toast.makeText(getApplicationContext(),"NO NDEF messages",Toast.LENGTH_LONG).show();
            }

        }
        super.onNewIntent(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void readTextFromMessage(NdefMessage ndefMessage) {
        NdefRecord[] ndefRecords= ndefMessage.getRecords();
        if(ndefRecords!=null & ndefRecords.length>0){
            NdefRecord ndefRecord=ndefRecords[0];
            String tagContent=getTextFromNdefRecord(ndefRecord);


            try{
                Toast.makeText(getApplicationContext(),tagContent,Toast.LENGTH_LONG).show();
                if(tagContent!=null){
                    findStudent(Integer.parseInt(tagContent));
                }else{
                    Toast.makeText(getApplicationContext(),"Nothing found",Toast.LENGTH_LONG).show();
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"NO NDEF records found",Toast.LENGTH_LONG).show();
        }
    }

    class GetData extends AsyncTask<String,String,String>{
        JSONArray students;



        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params=new ArrayList<>();
            params.add(new BasicNameValuePair("courseCode",courseCode));
            params.add(new BasicNameValuePair("program",program));
            params.add(new BasicNameValuePair("academicYear",academicYear));

            SharedPreferences sharedPreferences = context.getSharedPreferences("ipPreference",MODE_PRIVATE);
            final String ipAddress = sharedPreferences.getString("IP", "");
            final String DATAURL="http://"+ipAddress+":8080/EVS/get_data.php";

            JSONObject jsonObject=jsonParser.makeHttpRequest(DATAURL,"POST",params);
            Log.d("JSONStatus","Request made");

            try{
                int success=jsonObject.getInt("success");
                if(success==1){
                    students=jsonObject.getJSONArray("students");
                    examid=jsonObject.getString("exam_id");
                    courseName=jsonObject.getString("course_name");
                    Log.d("JSONStatus","Successful"+success);
                }else{
                    Toast.makeText(getApplicationContext(),"Couldn't get student data",Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            JSONObject student=null;
            int std_no=0;
            studs=new Hashtable();
            logs=new JSONArray();
            noStudents=0;
            try {
                //Filling the hashtable with student data
                for(int i=0;i<students.length();i++){
                    student=students.getJSONObject(i);
                    std_no=student.getInt("student_no");
                    studs.put(std_no,student);

                }
                //Toast.makeText(getApplicationContext(),examid.toString()+courseName,Toast.LENGTH_LONG).show();
                //student=studs.get(215001201);
                searchStudent= (EditText) findViewById(R.id.search_std);
                searchButton= (Button) findViewById(R.id.search_btn);

                searchButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        if(!searchStudent.getText().toString().isEmpty()){
                            int std_no=Integer.parseInt(searchStudent.getText().toString().trim());
                            findStudent(std_no);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Please,fill in the student number",Toast.LENGTH_LONG).show();
                        }
                    }
                });


                //Toast.makeText(getApplicationContext(),"Student balance for"+std_no+" is:"+student.getInt("account_balance"),Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void findStudent(final int student_no){
        student=studs.get(student_no);
        if(student!=null){

            studentImageView= (ImageView) findViewById(R.id.student_image);
            std_name_view= (TextView) findViewById(R.id.std_name_view);
            std_number_view= (TextView) findViewById(R.id.std_number_view);
            reg_number_view= (TextView) findViewById(R.id.reg_number_view);
            program_view= (TextView) findViewById(R.id.program_view);
            account_balance_view= (TextView) findViewById(R.id.account_balance_view);
            try{
                //Creating log entry
               if(student.getInt("account_balance")>0){

                   AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
                   alertDialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                          createLog(student_no);
                       }
                   });
                   alertDialogBuilder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {

                       }
                   });
                   alertDialogBuilder.setMessage("Fees balance: "+student.getInt("account_balance"));


                   AlertDialog alertDialog=alertDialogBuilder.create();
                   alertDialog.show();



               }
               else createLog(student_no);

           //Displaying the values on the screen

                std_name_view.setText(student.getString("first_name")+" "+student.getString("last_name"));
                std_number_view.setText(student.get("student_no").toString());
                reg_number_view.setText(student.getString("reg_no"));
                program_view.setText(student.getString("program"));
                account_balance_view.setText("ACCOUNT BALANCE: \n"+student.get("account_balance").toString());
                if(student.getInt("account_balance")>0)account_balance_view.setTextColor(Color.RED);
                else account_balance_view.setTextColor(Color.GREEN);
                byte[] imageData=Base64.decode(student.getString("picture"),Base64.DEFAULT);
                Bitmap decodedArray= BitmapFactory.decodeByteArray(imageData,0,imageData.length);
                studentImageView.setImageBitmap(decodedArray);
            } catch (JSONException e){e.printStackTrace();}
        }else{
           // Toast.makeText(getApplicationContext(),"Student not registered for paper",Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(this);
            alertDialogBuilder.setNeutralButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    createLog(student_no);
                }
            });
            alertDialogBuilder.setPositiveButton("View student", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent=new Intent(getApplicationContext(),CardReader.class);

                    intent.putExtra("studentNo",String.valueOf(student_no));

                    startActivity(intent);

                }
            });
            alertDialogBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            try {
                alertDialogBuilder.setMessage("Student " + student_no + " has not registered for paper");
            }catch (Exception e){
                e.printStackTrace();
                Log.d("JSONStatus","Couldn't get student number from JSON object");
            }

            AlertDialog alertDialog=alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    class sendLogs extends AsyncTask<String,String,String>{
        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params=new ArrayList<>();
            params.add(new BasicNameValuePair("logs",logs.toString()));
            params.add(new BasicNameValuePair("exam_id",examid));

            SharedPreferences sharedPreferences = context.getSharedPreferences("ipPreference",MODE_PRIVATE);
            final String ipAddress = sharedPreferences.getString("IP", "");
            final String LOGSURL="http://"+ipAddress+":8080/EVS/create_logs.php";

            jsonObject=jsonParser.makeHttpRequest(LOGSURL,"POST",params);
            Log.d("JSONStatus","Request made");

            try{
                int success=jsonObject.getInt("success");
                if(success==1)
                    {
                    Log.d("Logstatus","Logs created successfully");
                        Log.d("Logstatus",jsonObject.getString("message"));

                }
                else{
                    Log.d("Logstatus","Logs not created");
                    Log.d("Logstatus",jsonObject.getString("message"));
                }
            }catch (JSONException e){
                e.printStackTrace();
                Log.d("JSONStatus","Failed to create logs");
            }catch (NullPointerException e){
                Log.d("JSONStatus","Null object");
                e.printStackTrace();
            }
            return null;
        }
    }

    private void createLog(int studentNo){
       try{
            long myTime=Calendar.getInstance().getTime().getTime()+TimeZone.getDefault().getRawOffset();
            myTime=myTime/1000;

            JSONObject log_entry=new JSONObject();
            log_entry.put("student_no",studentNo);
            log_entry.put("timestamp",myTime);

            logs.put(log_entry);
            noStudents++;
       } catch (JSONException e) {
           e.printStackTrace();
           Log.d("JSONStatus",e.getMessage());

       }
    }

    @Override
    protected void onResume() {
        nfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null && nfcAdapter.isEnabled()){
           // Toast.makeText(this,"NFC is available",Toast.LENGTH_LONG).show();
            enableForegroundDispatchSystem();
        }else{
            Toast.makeText(this,"NFC is not available!",Toast.LENGTH_LONG).show();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(nfcAdapter!=null){
            disableForegroundDispatchSystem();
        }
        super.onPause();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent=new Intent(this,VerifyStudents.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters=new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }

    private String getTextFromNdefRecord(NdefRecord ndefRecord){
        String tagContent=null;
        try{
            byte[] payload=ndefRecord.getPayload();
            String textEncoding=((payload[0] & 128) ==0) ? "UTF-8" : "UTF-16";
            int languageSize=payload[0] & 0063;
            tagContent=new String(payload,languageSize+1,payload.length-languageSize-1,textEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return tagContent;

    }

    @Override
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
        }
        return super.onOptionsItemSelected(item);
    }
}
