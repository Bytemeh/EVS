package com.entropy.csc.evs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.R.layout.simple_dropdown_item_1line;
import static android.R.layout.simple_list_item_1;

public class ExamDetails extends AppCompatActivity {
    private static Spinner courseCodeSpinner;
    private static Spinner programSpinner;
    private static Spinner academicYearSpinner;
    private static Spinner yearOfStudySpinner;
    private static Button startVerificationButton;

    Boolean loginStatus;

    Context context;

    JSONParser jsonParser=new JSONParser();

    String[] courseList;
    ArrayList<String> stuff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_details);
        context = this;

        Intent i=getIntent();
        String designation=i.getStringExtra("designation");

        courseCodeSpinner= (Spinner) findViewById(R.id.courseCodeautoComplete);
        programSpinner= (Spinner) findViewById(R.id.programAutoComplete);
        academicYearSpinner= (Spinner) findViewById(R.id.academicyYearspinner);
        startVerificationButton= (Button) findViewById(R.id.startVerificicationButton);
        yearOfStudySpinner= (Spinner) findViewById(R.id.yearOfStudySpinner);

       int year= Calendar.getInstance().YEAR;
        int month=Calendar.getInstance().MONTH;

        Toast.makeText(this,"Year :"+year+" Month: "+month,Toast.LENGTH_LONG).show();
        new FillDetails().execute();

        String[] programs={"Computer Science","Software Engineering","Information systems","Information Technology"};
        ArrayAdapter progAdapter=new ArrayAdapter(getApplicationContext(),R.layout.pop_drop_item,programs);
        programSpinner.setAdapter(progAdapter);

        String [] yearsStudy={"1","2","3"};
        ArrayAdapter yearsAdapter=new ArrayAdapter(getApplicationContext(),R.layout.pop_drop_item,yearsStudy);
        yearOfStudySpinner.setAdapter(yearsAdapter);

        String[] years={"2017/18","2016/17","2015/16","2015/16"};
        ArrayAdapter yearAdapter=new ArrayAdapter(getApplicationContext(),R.layout.pop_drop_item,years);
        yearAdapter.setDropDownViewResource(R.layout.pop_drop_item);
        academicYearSpinner.setAdapter(yearAdapter);

        startVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String courseCode=courseCodeSpinner.getSelectedItem().toString().trim();
                String program=programSpinner.getSelectedItem().toString().trim();
                String academicYear=academicYearSpinner.getSelectedItem().toString();

                if(!courseCode.isEmpty() && !program.isEmpty() && !academicYear.isEmpty())
                    {
                    Intent intent=new Intent(getApplicationContext(),VerifyStudents.class);
                    intent.putExtra("courseCode",courseCode);
                    intent.putExtra("program",program);
                    intent.putExtra("academicYear",academicYear);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Please fill in all fields",Toast.LENGTH_LONG).show();
                }
            }
        });





    }
    class FillDetails extends AsyncTask<String,String,String>{
        JSONArray courses;
        JSONObject c;

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params=new ArrayList<>();
            params.add(new BasicNameValuePair("user_id","userid"));
            //Shared preference to access the server ip
            SharedPreferences sharedPreferences = context.getSharedPreferences("ipPreference",MODE_PRIVATE);
            final String ipAddress = sharedPreferences.getString("IP", "");
            final String DETAILSURL="http://"+ipAddress+":8080/EVS/fill_details.php";

            try {
                JSONObject jsonObject = jsonParser.makeHttpRequest(DETAILSURL, "POST", params);
                Log.d("JSONStatus", "Request made");
                // courseList=new String[]{};
                //TODO: Filter based on program and semester.
                stuff = new ArrayList<String>();
                try {
                    int success = jsonObject.getInt("success");
                    if (success == 1) {
                        courses = jsonObject.getJSONArray("courses");
                        for (int i = 0; i < courses.length(); i++) {
                            c = courses.getJSONObject(i);
                            String code = c.getString("course_code");
                            String cname = c.getString("course_name");
                            // courseList[0]=code;
                            stuff.add(code);
                        }

                        Log.d("JSONStatus", "Successful request");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d("Connection error", e.toString());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {

           // ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(), simple_list_item_1,courseList);
            ArrayAdapter<String> adapter2=new ArrayAdapter<String>(getApplicationContext(),R.layout.pop_drop_item,(List<String>) stuff);
            adapter2.setDropDownViewResource(R.layout.pop_drop_item);
            courseCodeSpinner.setAdapter(adapter2);

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
            case R.id.setIPbutton:
                intent = new Intent(getApplicationContext(), ConnectToIp.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
