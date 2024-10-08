package com.example.compass_navigateyourcompany;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class home_employee_Activity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private CalendarView calendarView;
    private Spinner typeSpinner;
    private EditText fromDateEditText, toDateEditText;
    private Button selectImageButton, clearButton, submitButton;
    private ImageView profileButton, homeButton, logoutButton;
    private String selectedFilePath;
    private AppDatabase db;
    private String login_name;
    private boolean isSelectingFromDate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_employee);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        calendarView = findViewById(R.id.calendar_view);
        typeSpinner = findViewById(R.id.type_spinner);
        fromDateEditText = findViewById(R.id.from_date);
        toDateEditText = findViewById(R.id.to_date);
        selectImageButton = findViewById(R.id.select_image_button);
        clearButton = findViewById(R.id.clear_button);
        submitButton = findViewById(R.id.submit_button);
        profileButton = findViewById(R.id.profile_button);
        homeButton = findViewById(R.id.home_button);
        logoutButton = findViewById(R.id.logout_button);
        ImageView settingsButton = findViewById(R.id.settings_button);
        NavigationView navigationView = findViewById(R.id.nav_view);

        db = AppDatabase.getInstance(this);

        // Get user login name from Intent
        Intent intent = getIntent();
        if (intent != null) {
            login_name = intent.getStringExtra("Name");

        }

        settingsButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_settings) {

                    Intent intentSettings = new Intent(home_employee_Activity.this, settings_Activity.class);
                    intentSettings.putExtra("Name", login_name);
                    intentSettings.putExtra("sourceActivity", "home_employee");
                    startActivity(intentSettings);
                } else if (id == R.id.nav_profile) {
                    Intent intentProfile = new Intent(home_employee_Activity.this, employee_profile_Activity.class);
                    intentProfile.putExtra("sourceActivity", "home_employee");
                    intentProfile.putExtra("loginName", login_name);
                    startActivity(intentProfile);
                    finish();
                } else if (id == R.id.nav_logout) {
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Clear all data
                    editor.apply();

                    Intent intentLogin = new Intent(home_employee_Activity.this, login_Activity.class);
                    startActivity(intentLogin);
                    finish();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        profileButton.setOnClickListener(v -> {
            String loginName = login_name;
            Intent intentProfile = new Intent(home_employee_Activity.this, employee_profile_Activity.class);
            intentProfile.putExtra("loginName", loginName);
            startActivity(intentProfile);
            finish();
        });

        homeButton.setOnClickListener(v -> {
            String loginName = login_name;
            Intent intentProfile = new Intent(home_employee_Activity.this, home_employee_Activity.class);
            intentProfile.putExtra("Name", loginName);
            startActivity(intentProfile);
            finish();
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear(); // Clear all data
            editor.apply();

            Intent intentLogin = new Intent(home_employee_Activity.this, login_Activity.class);
            startActivity(intentLogin);
            finish();
        });

        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if (selectedType.equals("Sick leave")) {
                    selectImageButton.setVisibility(View.VISIBLE);
                } else {
                    selectImageButton.setVisibility(View.GONE);
                    selectedFilePath = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Date Picker
        fromDateEditText.setOnClickListener(v -> showDatePickerDialog(fromDateEditText));
        toDateEditText.setOnClickListener(v -> showDatePickerDialog(toDateEditText));

        // CalendarView Listener for selecting dates
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            if (isSelectingFromDate) {
                fromDateEditText.setText(selectedDate);
                isSelectingFromDate = false;
            } else {
                toDateEditText.setText(selectedDate);
                isSelectingFromDate = true;
            }
        });

        // Select File
        selectImageButton.setOnClickListener(v -> openFileChooser());

        // Clear Button
        clearButton.setOnClickListener(v -> clearForm());

        // Submit Button
        submitButton.setOnClickListener(v -> submitForm());
    }

    private void showDatePickerDialog(EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = (dayOfMonth) + "/" + (monthOfYear + 1) + "/" + year1;
            editText.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = getRealPathFromURI(uri);

                if (filePath != null) {
                    selectedFilePath = filePath;
                    Log.d("FileSelection", "Selected file path: " + selectedFilePath);
                    Toast.makeText(this, "Selected: " + selectedFilePath, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("FileSelection", "Failed to get file path from URI.");
                    Toast.makeText(this, "Failed to get file path", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("FileSelection", "URI is null.");
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("FileSelection", "Result code: " + resultCode + ", Intent data: " + data);
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String path = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    path = cursor.getString(column_index);
                }
            }
        } catch (Exception e) {
            Log.e("getRealPathFromURI", "Failed to get real path from URI", e);
        }

        if (path == null) {
            // Fall back to using the URI path
            path = uri.getPath();
        }

        return path;
    }

    private void clearForm() {
        fromDateEditText.setText("");
        toDateEditText.setText("");
        typeSpinner.setSelection(0);
        selectedFilePath = null;
        isSelectingFromDate = true;
    }

    private void submitForm() {
        String selectedType = typeSpinner.getSelectedItem().toString();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            User user;
            try {
                user = db.userDao().findByLoginName(login_name);
                if (user == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(home_employee_Activity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        Log.e("SubmitForm", "User not found for login_name: " + login_name);
                    });
                    return;
                }

                // Check if file is required but not selected
                if (selectedType.equals("Sick leave") && selectedFilePath == null) {
                    runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "Please choose a file for Sick leave", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Parse Dates
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                sdf.setLenient(false);

                String fromDateString = fromDateEditText.getText().toString();
                String toDateString = toDateEditText.getText().toString();

                if (fromDateString.isEmpty() || toDateString.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "Please select dates", Toast.LENGTH_SHORT).show());
                    return;
                }
                Date fromDate, toDate;
                try {
                    fromDate = sdf.parse(fromDateString);
                    toDate = sdf.parse(toDateString);

                    if (fromDate == null || toDate == null) {
                        throw new Exception("Invalid date format");
                    }

                    Date currentDate = new Date();
                    if (fromDate.before(currentDate)) {
                        runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "From date cannot be before the current date", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    if (fromDate.after(toDate)) {
                        runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "From date must be before To date", Toast.LENGTH_SHORT).show());
                        return;
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "Error parsing dates", Toast.LENGTH_SHORT).show());
                    Log.e("SubmitForm", "Date parsing error", e);
                    return;
                }

                Pass pass = new Pass();
                pass.userID = user.id;
                pass.type = selectedType;
                pass.fromDate = fromDate;
                pass.toDate = toDate;
                pass.approved = 0;
                pass.filePath = selectedFilePath;

                // Insert pass
                try {
                    db.passDao().insert(pass);
                    runOnUiThread(() -> {
                        Toast.makeText(home_employee_Activity.this, "Form Submitted", Toast.LENGTH_SHORT).show();
                        clearForm();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "Error submitting form", Toast.LENGTH_SHORT).show());
                    Log.e("SubmitForm", "Error inserting pass", e);
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(home_employee_Activity.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
                Log.e("SubmitForm", "Error fetching user", e);
            }
        });
    }
}