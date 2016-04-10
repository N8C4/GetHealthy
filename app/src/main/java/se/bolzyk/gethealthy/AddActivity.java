package se.bolzyk.gethealthy;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;


public class AddActivity extends AppCompatActivity {

    private Spinner activitySpinner;
    private Spinner hourSpinner;
    private Spinner minSpinner;

    private TextView dateView;
    private int year, month, day;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Standard method that initial all parameters and listener
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        //Create and set float buttons for fast navigation
        createAndSetFloatingButton(fab1, MainActivity.class);
        createAndSetFloatingButton(fab2, AddActivity.class);
        createAndSetFloatingButton(fab3, MapsActivity.class);

        // Create and set spinners
        activitySpinner = setSpinner(R.id.activity_spinner, R.array.spinner_activity_array);
        hourSpinner = setSpinner(R.id.time_hour_spinner, R.array.spinner_time_hours_array);
        minSpinner = setSpinner(R.id.time_min_spinner, R.array.spinner_time_min_array);

        // This is a button to add content
        final Button saveActivity = (Button) findViewById(R.id.saveActivity);
        setSaveButton(saveActivity);

        // Set value for date picker
        dateView = (TextView) findViewById(R.id.textView3);
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month+1, day);
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
        String addData = hourSpinner.getSelectedItem().toString() +":"+
                minSpinner.getSelectedItem().toString() +":"+
                dateView.getText();

        // get det month to separate data and can call in a good way
        String[] set = dateView.getText().toString().split("/");

        switch (activitySpinner.getSelectedItem().toString()) {
            case "Walking":
                addToMyDatabase("dbWalking", addData, set[1]);
                break;
            case "Running":
                addToMyDatabase("dbRunning", addData, set[1]);
                break;
            case "Weight Training":
                addToMyDatabase("dbWeightTraining", addData, set[1]);
                break;
            case "Group Workout":
                addToMyDatabase("dbGroupWorkout", addData, set[1]);
                break;
        }
    }


    /**
     * This method add users activity to the respective database
     *
     * @param db the name of the database
     * @param mContent the content that should be added
     * @param set the id name of the set it should be added to
     */
    private void addToMyDatabase(String db, String mContent, String set) {
        SharedPreferences sp = getSharedPreferences(db, Context.MODE_PRIVATE);
        Set<String> hs = sp.getStringSet(set, new HashSet<String>());
        Set<String> in = new HashSet<>(hs);
        in.add(mContent + ":" + in.size());
        sp.edit().putStringSet(set, in).apply();
    }


    /**
     * Method that sets fast navigation float buttons by creating a button listener
     * @param fab the layout id of FloatingActionButton
     * @param myActivity the class that should be called when the button is pressed
     */
    private void createAndSetFloatingButton(FloatingActionButton fab, final Class myActivity) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), myActivity);
                startActivity(intent);
            }
        });
    }


    /**
     * present the date picker
     */
    @SuppressWarnings({"deprecation", "unused"})
    public void setDate(View view) {
        showDialog(999);
    }


    /**
     * Open date picker dialog when user press the button
     * @return DatePickerDialog
     */
    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return new DatePickerDialog(this, myDateListener, year, month, day);
    }


    /**
     * Listener to date picker dialog and safe the values the user choose
     */
    final private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        // arg1 = year; arg2 = month; arg3 = day;
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            showDate(arg1, arg2+1, arg3);
        }
    };


    /**
     * Present the chosen date to the user
     */
    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }
}
