package com.entropy.csc.evs;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FinishedActivity extends AppCompatActivity {
    Button logoutButton;
    TextView course_name_tv,course_code_tv,no_students_tv;

    Boolean loginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        logoutButton= (Button) findViewById(R.id.logout_button);
        course_code_tv= (TextView) findViewById(R.id.course_code_tv);
        course_name_tv= (TextView) findViewById(R.id.course_name_tv);
        no_students_tv= (TextView) findViewById(R.id.no_students);

        Intent intent=getIntent();
        String courseName=intent.getStringExtra("courseName");
        String courseCode=intent.getStringExtra("courseCode");
        String noStudents=intent.getStringExtra("noStudents");





        course_code_tv.setText(courseCode);
        course_name_tv.setText(courseName+" Exam");
        no_students_tv.setText("Number of students:\n"+noStudents);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginStatus=Boolean.FALSE;
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                intent.putExtra("loginStatus",loginStatus);

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
        }
        return super.onOptionsItemSelected(item);
    }
}
