package com.example.compass_navigateyourcompany;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class head_profile_Activity extends AppCompatActivity {

    private AppDatabase db;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView departmentTextView;
    private TextView yearsTextView;
    private TextView numOfEmployees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.head_profile);

        db = AppDatabase.getInstance(this);

        nameTextView = findViewById(R.id.headName);
        emailTextView = findViewById(R.id.email);
        phoneTextView = findViewById(R.id.phone);
        departmentTextView = findViewById(R.id.department);
        yearsTextView = findViewById(R.id.years);
        numOfEmployees = findViewById(R.id.emps);


        findViewById(R.id.back_button).setOnClickListener(v -> {
            Intent intent = new Intent(head_profile_Activity.this, home_head_Activity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.home_button).setOnClickListener(v -> {
            Intent intent = new Intent(head_profile_Activity.this, home_head_Activity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.logout_button).setOnClickListener(v -> {
            // Optionally handle logout
            finish();
        });


        loadProfile();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                //String headLoginName = getIntent().getStringExtra("loginName");
                String headLoginName = "Giota";
                Head head = db.headDao().getHeadByName(headLoginName);
                if (head == null) {
                    runOnUiThread(() -> Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                    return;
                }

                Department department = db.departmentDao().findById(head.departmentId);
                String departmentName = department != null ? department.Name : "Unknown Department";


                Integer number = db.employeeDao().countEmployees(head.authToken,head.departmentId);

                // Update UI
                runOnUiThread(() -> {
                    nameTextView.setText(head.name);
                    emailTextView.setText(head.mail);
                    phoneTextView.setText(head.phone);
                    departmentTextView.setText(departmentName);
                    yearsTextView.setText(String.valueOf(head.years));
                    numOfEmployees.setText(String.valueOf(number));
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
