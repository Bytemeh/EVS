package com.entropy.csc.evs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import java.util.ArrayList;
import java.util.List;

public class CardReader extends AppCompatActivity {
    TextView txtTagContent;
    ImageView studentImageView;
    TextView std_name_view,std_number_view,reg_number_view,program_view,account_balance_view,subjects_registered,subjects_header,subjects;

    Boolean loginStatus;

    Context context;

    NfcAdapter nfcAdapter;

    JSONParser jsonParser=new JSONParser();

    private static final String STUDENTDATAURL="http://192.168.43.25:8080/EVS/get_student_data.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);
        context = this;

        txtTagContent= (TextView) findViewById(R.id.txtTagContent);

        nfcAdapter=NfcAdapter.getDefaultAdapter(this);

    }

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

    private void readTextFromMessage(NdefMessage ndefMessage) {
       NdefRecord[] ndefRecords= ndefMessage.getRecords();
        if(ndefRecords!=null & ndefRecords.length>0){
            NdefRecord ndefRecord=ndefRecords[0];
            String tagContent=getTextFromNdefRecord(ndefRecord);


            try{
                Toast.makeText(getApplicationContext(),tagContent,Toast.LENGTH_LONG).show();
                txtTagContent.setText(tagContent);
                new GetStudentData(tagContent).execute();
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"NO NDEF records found",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        nfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter!=null && nfcAdapter.isEnabled()){
            //Toast.makeText(this,"NFC is available",Toast.LENGTH_LONG).show();
            enableForegroundDispatchSystem();
        }else{
            Toast.makeText(this,"NFC is not available!",Toast.LENGTH_LONG).show();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {

        disableForegroundDispatchSystem();
        super.onPause();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent=new Intent(this,CardReader.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        IntentFilter[] intentFilters=new IntentFilter[]{};

        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }
    private void disableForegroundDispatchSystem(){
        if(nfcAdapter!=null){
            nfcAdapter.disableForegroundDispatch(this);
        }
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

    class GetStudentData extends AsyncTask<String,String,String>{
        String student_no,message;
        int success;
        JSONObject student;
        JSONArray courses;
        public GetStudentData(String student_no) {
            this.student_no=student_no;
        }

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params=new ArrayList<>();
            params.add(new BasicNameValuePair("student_no",student_no));

            SharedPreferences sharedPreferences = context.getSharedPreferences("ipPreference",MODE_PRIVATE);
            final String ipAddress = sharedPreferences.getString("IP", "");
            final String LOGIN_URL="http://"+ipAddress+":8080/EVS/get_student_data.php";

            JSONObject jsonObject=jsonParser.makeHttpRequest(STUDENTDATAURL,"POST",params);
            Log.d("JSONStatus","this Request made");

            if(jsonObject!=null){
               try{
                   success=jsonObject.getInt("success");


                   if(success==1){
                     student=jsonObject;
                       courses=jsonObject.getJSONArray("subjects");

                   }

               }catch (JSONException e){
                   e.printStackTrace();
                   Log.d("JSONStatus",e.getMessage());
               }
            }else{
                success=0;
                message="Couldn't connect to server";
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("JSONStatus","Executed");
            if(success==1) {
                studentImageView = (ImageView) findViewById(R.id.student_image_cr);
                std_name_view = (TextView) findViewById(R.id.std_name_view_cr);
                std_number_view = (TextView) findViewById(R.id.std_number_view_cr);
                reg_number_view = (TextView) findViewById(R.id.reg_number_view_cr);
                program_view = (TextView) findViewById(R.id.program_view_cr);
                account_balance_view = (TextView) findViewById(R.id.account_balance_view_cr);

                subjects_registered= (TextView) findViewById(R.id.subjects_registered);
                subjects_header= (TextView) findViewById(R.id.subjects_header);
                subjects= (TextView) findViewById(R.id.subjects);

                try {
                    std_name_view.setText(student.getString("first_name") + " " + student.getString("last_name"));
                    std_number_view.setText(student.get("student_no").toString());
                    reg_number_view.setText(student.getString("reg_no"));
                    program_view.setText(student.getString("program"));
                    account_balance_view.setText("ACCOUNT BALANCE: \n" + student.get("account_balance").toString());
                    if (student.getInt("account_balance") > 0)account_balance_view.setTextColor(Color.RED);
                    else account_balance_view.setTextColor(Color.GREEN);
                    byte[] imageData = Base64.decode(student.getString("picture"), Base64.DEFAULT);
                    Bitmap decodedArray = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    studentImageView.setImageBitmap(decodedArray);

                    subjects_registered.setText("Subjects registered");
                    subjects_header.setText("Subject \t Retake");

                    for(int i=0;i<courses.length();i++){

                        subjects.append(courses.getJSONObject(i).getString("course_code")+" \t "+
                                courses.getJSONObject(i).getString("retake") +"\n");

                    }





                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(s);
        }
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
