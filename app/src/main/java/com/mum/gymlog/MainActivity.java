package com.mum.gymlog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.mum.gymlog.database.GymLogRepository;
import com.mum.gymlog.database.entities.GymLog;
import com.mum.gymlog.database.entities.User;
import com.mum.gymlog.databinding.ActivityMainBinding;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String MAIN_ACTIVITY_USER_ID = "com.mum.gymlog.MAIN_ACTIVITY_USER_ID";
    static final String SHARED_PREFERENCE_USERID_KEY = "com.mum.gymlog.SHARED_PREFERENCE_USERID_KEY";
    static final String SHARED_PREFERENCE_USERID_VALUE = "com.mum.gymlog.SHARED_PREFERENCE_USERID_KVALUE";
    private static final String SAVED_INSTANCE_STATE_USERID_KEY = "com.mum.gymlog.SAVED_INSTANCE_STATE_USERID_KEY";
    private static final int LOGGED_OUT = -1;
    private ActivityMainBinding binding;
    private GymLogRepository repository;
    public static final String TAG = "GYMLOG";

    String mExercise = "";
    double mWeight = 0.0;
    int mReps = 0;

    private int loggedInUserId = -1;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.mum.gymlog.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // gets instance of repository then used to retrieve info from database
        repository = GymLogRepository.getRepository(getApplication());
        loginUser(savedInstanceState);

        if (loggedInUserId == -1) {
            Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
            startActivity(intent);
        }

        binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());
        updateDisplay();

        binding.logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Takes entered info from user and displays
                getInformationFromDisplay();
                insertGymLogRecord();
                updateDisplay();
            }
        });

        binding.exerciseInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDisplay();
            }
        });
    }

    private void loginUser(Bundle savedInstanceState) {
        // checks shared preference for logged in user
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_USERID_KEY,
                Context.MODE_PRIVATE);

        if (sharedPreferences.contains(SHARED_PREFERENCE_USERID_VALUE)) {
            loggedInUserId = sharedPreferences.getInt(SHARED_PREFERENCE_USERID_KEY, LOGGED_OUT);
        }
        if (loggedInUserId == LOGGED_OUT & savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_STATE_USERID_KEY)) {
            loggedInUserId = savedInstanceState.getInt(SAVED_INSTANCE_STATE_USERID_KEY, LOGGED_OUT);
        }
        if (loggedInUserId == LOGGED_OUT) {
            // checks intent for logged in user
            loggedInUserId = getIntent().getIntExtra(MAIN_ACTIVITY_USER_ID, LOGGED_OUT);
        }
        if (loggedInUserId == LOGGED_OUT) {
            return;
        }
        LiveData<User> userObserver = repository.getUserByUserId(loggedInUserId);
        // watches object for updates
        userObserver.observe(this, user -> {
            if (user != null) {
                invalidateOptionsMenu();
            }
            else {
                // TODO: verify if this was an issue
//                logout();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_INSTANCE_STATE_USERID_KEY, loggedInUserId);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_USERID_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putInt(MainActivity.SHARED_PREFERENCE_USERID_KEY, loggedInUserId);
        sharedPrefEditor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    // Gets references to menu items
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.logoutMenuItem);
        item.setVisible(true);

        if (user == null) {
            return false;
        }
        //TODO: get username from user parameter.

        // example
        item.setTitle(user.getUsername());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                showLogoutDialog();
                return false;
            }
        });
        return true;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        // instantiate memory for alert dialog so only one alert dialog at a time
        final AlertDialog alertDialog = alertBuilder.create();

        alertBuilder.setTitle("Logout?");

        // Proceed logout
        alertBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logout();
            }
        });

        // Cancel logout
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });

        alertBuilder.create().show();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_USERID_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor= sharedPreferences.edit();
        sharedPrefEditor.putInt(SHARED_PREFERENCE_USERID_KEY, LOGGED_OUT);
        sharedPrefEditor.apply();

        getIntent().putExtra(MAIN_ACTIVITY_USER_ID, LOGGED_OUT);

        startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
    }

    static Intent mainActivityIntentFactory(Context context , int userId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MAIN_ACTIVITY_USER_ID, userId);
        return intent;
    }


    private void insertGymLogRecord() {
        if (mExercise.isEmpty()) {
            return;
        }
        GymLog log = new GymLog(mExercise, mWeight, mReps, loggedInUserId);
        repository.insertGymLog(log);
    }
    private void updateDisplay() {
        ArrayList<GymLog> allLogs = repository.getAllLogsByUserId(loggedInUserId);

        if (allLogs.isEmpty()) {
            binding.logDisplayTextView.setText(R.string.nothing_to_show_time_to_hit_the_gym);
        }

        StringBuilder sb = new StringBuilder();
        for (GymLog log : allLogs) {
            sb.append(log);
        }
        binding.logDisplayTextView.setText(sb.toString());
    }

    private void getInformationFromDisplay() {
        mExercise = binding.exerciseInputEditText.getText().toString();

        try {
            mWeight = Double.parseDouble(binding.weightInputEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            Log.d(TAG, "Error reading value from Weight edit text.");
        }

        try {
            mReps = Integer.parseInt(binding.repInputEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            Log.d(TAG, "Error reading value from Reps edit text.");
        }

    }

}