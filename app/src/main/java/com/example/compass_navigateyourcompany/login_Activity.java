package com.example.compass_navigateyourcompany;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class login_Activity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword, editTextToken;
    private Button buttonLogin, backButton;
    private TextView textViewSignup;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        editTextUsername = findViewById(R.id.textUsername);
        editTextPassword = findViewById(R.id.textPassword);
        editTextToken = findViewById(R.id.texttoken);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignup = findViewById(R.id.sign_up);


        db = AppDatabase.getInstance(this);


        // Login Button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String token = editTextToken.getText().toString();

                // Highlight required fields
                highlightRequiredFields(editTextPassword);
                highlightRequiredFields(editTextToken);
                highlightRequiredFields(editTextUsername);

                if (validateInputs(username, password, token)) {
                    new LoginTask().execute(username, password, token);
                }
                else {
                    Toast.makeText(login_Activity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //  Signup
        textViewSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(login_Activity.this, select_type_Activity.class);
                startActivity(intent);
            }
        });
    }

    // validate input fields
    private boolean validateInputs(String username, String password, String token) {
        return !username.isEmpty() && !password.isEmpty() && !token.isEmpty();
    }


    // login
    private class LoginTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String token = params[2];

            User user = db.userDao().getUser(username, password, token);
            Log.d("LoginTest", "User retrieved: " + (user != null ? user.loginName : "null"));

            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                Toast.makeText(login_Activity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                goToHomePage(user);
            }
            else {
                Toast.makeText(login_Activity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Go to the corresponding activity based on user type
    private void goToHomePage(User user) {
        Intent intent;
        switch (user.type) {
            case "Employer":
                intent = new Intent(login_Activity.this, home_employer_Activity.class);
                break;
            case "Head":
                intent = new Intent(login_Activity.this, home_head_Activity.class);
                break;
            case "Employee":
                intent = new Intent(login_Activity.this, home_employee_Activity.class);
                break;
            default:
                Toast.makeText(login_Activity.this, "Unknown user type", Toast.LENGTH_SHORT).show();
                return;
        }
        // Pass userID and other information to the next activity
        intent.putExtra("Name",user.loginName);
        startActivity(intent);
        finish();

    }

    private void highlightRequiredFields(TextView textView) {
        if (textView == null) return;

        CharSequence hint = textView.getHint();
        if (hint == null) return;

        String hintString = hint.toString();
        SpannableString spannableString = new SpannableString(hintString);
        ForegroundColorSpan redColorSpan = new ForegroundColorSpan(Color.RED);
        int asteriskPosition = hintString.indexOf("*");
        if (asteriskPosition != -1) {
            spannableString.setSpan(redColorSpan, asteriskPosition, asteriskPosition + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setHint(spannableString);
    }
}
