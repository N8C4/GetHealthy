package se.bolzyk.gethealthy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


public class SettingActivity extends AppCompatActivity {

    private Spinner activitySpinner;
    private Spinner hourSpinner;
    private Spinner minSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        activitySpinner = setSpinner(R.id.activity_spinner, R.array.spinner_activity_array);
        hourSpinner = setSpinner(R.id.time_hour_spinner, R.array.spinner_time_hours_array_goal);
        minSpinner = setSpinner(R.id.time_min_spinner, R.array.spinner_time_min_array_goal);

        // This is a button to add content
        final Button saveGoal = (Button) findViewById(R.id.saveGoal);
        setSaveButton(saveGoal);
        // This is a button to add content
        final Button showsGoal = (Button) findViewById(R.id.showGoal);
        setShowButton(showsGoal);
    }


    /**
     * Open dialog to present actual goal value
     * @param saveActivity button layout
     */
    private void setShowButton(Button saveActivity){
        saveActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openGoalSettings(getGoalValue());
            }
        });
    }


    /**
     * Get goal values and cal them to hours and min
     * @return string of the goals that are in the database
     */
    private String getGoalValue() {
        SharedPreferences sp = getSharedPreferences("GoalSettings", Context.MODE_PRIVATE);
        int intW = sp.getInt("dbWalking", 0)/60;
        int intR = sp.getInt("dbRunning", 0)/60;
        int intT = sp.getInt("dbWeightTraining", 0)/60;
        int intG = sp.getInt("dbGroupWorkout", 0)/60;

        int intModW = sp.getInt("dbWalking", 0) % 60;
        int intModR = sp.getInt("dbRunning", 0) % 60;
        int intModT = sp.getInt("dbWeightTraining", 0) % 60;
        int intModG = sp.getInt("dbGroupWorkout", 0) % 60;

        if(intModG <= 1) {
            intModG = 0;
        }

        if(intModT <= 1) {
            intModT = 0;
        }

        if(intModR <= 1) {
            intModR = 0;
        }

        if(intModW <= 1) {
            intModW = 0;
        }

        String row1 = "Walking Goal : "+intW+"h "+intModW+"min\n";
        String row2 = "Running Goal : "+intR+"h "+intModR+"min\n";
        String row3 = "Weight Training Goal : "+intT+"h "+intModT+"min\n";
        String row4 = "Group Workout Goal : "+intG+"h "+intModG+"min\n";

        return row1 + row2 + row3 + row4;
    }


    /**
     * This method create and set a alertDialog box with content
     * The content that presents is a list of goal value
     */
    private void openGoalSettings(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Actual Goals Values");
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                closeContextMenu();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Set my save to database button
     * @param saveActivity button layout
     */
    private void setSaveButton(Button saveActivity){
        saveActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                chooseMyDatabase();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getBaseContext(), "Activity is saved!", duration);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        });
    }


    /**
     * Create an ArrayAdapter using the string array and a default spinner layout
     * Specify the layout to use when the list of choices appears
     * Apply the adapter to the spinner
     * @param myId the specify spinner layout object
     * @param myArray the spinner data the will presented
     * @return the set spinner
     */
    private Spinner setSpinner(int myId, int myArray){
        Spinner spinner = (Spinner) findViewById(myId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getBaseContext(), myArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinner != null) {
            spinner.setAdapter(adapter);
        }
        return spinner;
    }


    /**
     * This method save the data the users choose
     * Each activity has his own database
     */
    private void chooseMyDatabase(){

        String myHour = hourSpinner.getSelectedItem().toString();
        String myMin = minSpinner.getSelectedItem().toString();
        myHour = myHour.replace("h", "");
        myMin = myMin.replace("min", "");

        int intHour = Integer.parseInt(myHour);
        int intMin =  Integer.parseInt(myMin);

        int goal = (intHour*60)+intMin;

        switch (activitySpinner.getSelectedItem().toString()) {
            case "Walking":
                addToMyDatabase("dbWalking", goal );
                break;
            case "Running":
                addToMyDatabase("dbRunning", goal);
                break;
            case "Weight Training":
                addToMyDatabase("dbWeightTraining", goal);
                break;
            case "Group Workout":
                addToMyDatabase("dbGroupWorkout", goal);
                break;
        }
    }


    /**
     * This method add users activity to the respective database
     *
     */
    private void addToMyDatabase(String set, int value) {
        SharedPreferences sp = getSharedPreferences("GoalSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(set,value);
        editor.apply();
    }
}