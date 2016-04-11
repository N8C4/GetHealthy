package se.bolzyk.gethealthy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import me.itangqi.waveloadingview.WaveLoadingView;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private final ArrayList<String> allActivityData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        //Create and set float buttons for fast navigation
        createAndSetFloatingButton(fab1, MainActivity.class);
        createAndSetFloatingButton(fab2, AddActivity.class);
        createAndSetFloatingButton(fab3, MapsActivity.class);

        //Create and set process bars
        updateAndSetWave(R.id.waveLoadingView1, loadAndCalcToWave("dbWalking"), "dbWalking");
        updateAndSetWave(R.id.waveLoadingView2, loadAndCalcToWave("dbRunning"), "dbRunning");
        updateAndSetWave(R.id.waveLoadingView3, loadAndCalcToWave("dbWeightTraining"),"dbWeightTraining");
        updateAndSetWave(R.id.waveLoadingView4, loadAndCalcToWave("dbGroupWorkout"),"dbGroupWorkout");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
     * Method that set values for the process bars
     * @param id is the layout for the specific wave circle that should be modified
     * @param processValue a int that represent the percent the used succeeded
     * @param myDatabase name of the database the should be used
     */
    private void updateAndSetWave(int id, int processValue, final String myDatabase) {
        WaveLoadingView mWaveLoadingView = (WaveLoadingView) findViewById(id);
        if (mWaveLoadingView != null) {
            mWaveLoadingView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
            mWaveLoadingView.setBottomTitle(Integer.toString(processValue) + "%");
            mWaveLoadingView.setProgressValue(processValue);
            mWaveLoadingView.setAmplitudeRatio(processValue / 4);
        }
        // Set onClick function for process bar
        if (mWaveLoadingView != null) {
            mWaveLoadingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getAllActivityThisMonth(myDatabase);
                    open();
                }
            });
        }
    }


    /**
     * Get actual month and choose the respective data set from that
     * Call the my parse method and get goal method
     * @param myDatabase a string of the from database set
     * @return int value that represent the users process this month
     */
    private int loadAndCalcToWave(String myDatabase) {
        int process = 0;
        Calendar cal = Calendar.getInstance();
        String[] myDate = dateFormat.format(cal.getTime()).split("/");
        //myDate [1] contains the month value
        String monthString = myDate[1];
        int monthInt = Integer.parseInt(monthString);

        // Access database and get set depending on number of month to display the right data process to the user
        // Save the set of data and pars it to calculate the time spent for a specific month
        SharedPreferences sp = getSharedPreferences(myDatabase, Context.MODE_PRIVATE);
        if(!sp.getAll().isEmpty()) {
            Set<String> mySet;
            mySet = sp.getStringSet(""+monthInt, null);
            if (mySet != null) {
                process = getGoalSetting(parsDataStringGetInt(mySet.toString()), myDatabase);
            }
            return process;
        } else {
            process = getGoalSetting(0, myDatabase);
            return process;
        }
    }


    /**
     * The parser for my data set that filter out some chars and split string into string array for more process
     * @param myString a string of the from database set
     * @return int value that represent the users total activity time in min
     */
    private int parsDataStringGetInt(String myString) {
        // example of dataSet [2h:30min:6/4/2016:3, 2h:15min:6/4/2016:2]
        int myHour = 0;
        int myMin = 0;

        myString = myString.replace("[", "");
        myString = myString.replace("]", "");
        myString = myString.replace(" ", "");
        myString = myString.replace("h", "");
        myString = myString.replace("min", "");

        String[] myActivates = myString.split(",");
        for (String ma : myActivates) {
            // example of String mt = 2:30:6/4/2016:3
            String[] myComponents = ma.split(":");
            // example of String mc = 2 or 30 or 6/4/2016 or 3
            myHour = myHour + Integer.parseInt(myComponents[0]);
            myMin = myMin + Integer.parseInt(myComponents[1]);
        }
        // calc to hours to min
        myHour = myHour * 60;
        return myHour+myMin;
    }


    /**
     * Gets the goal for a activity and calc the percent from users total activity time in min
     * Math formula "(TotalUserInMin) divided by the (GoalInMin)" to get the percent
     * @param userProcess a int of total time spent on a activity in min
     * @param myDatabase a string that get the right goal for the right activity from the database
     * @return int value that represent the users process this month
     */
    private int getGoalSetting(int userProcess, String myDatabase) {
        // If no goal set catch the error and ask user to add goals
        SharedPreferences sp = getSharedPreferences("GoalSettings", Context.MODE_PRIVATE);
        int myProcess = 0;
        try {
            int myGoal = sp.getInt(myDatabase, 0);
            if(1 > myGoal) {
                myProcess = 0;
                openGoalSettings(myDatabase);
            } else if(1 < myGoal) {
                myProcess = (userProcess * 100) / sp.getInt(myDatabase, 0);
            }
        } catch(Exception ex){
            //show only when there is not goal set
            myProcess = 0;
            openGoalSettings(myDatabase);
        }
        return myProcess;
    }


    /**
     * This method create and set a alertDialog box with content
     * The content that presents is a list of all activity
     */
    private void openGoalSettings(final String myDatabase) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(myDatabase.substring(2)+" Goal!");
        alertDialogBuilder.setMessage("Do you want to use the standard goal for "+myDatabase.substring(2)+"?"+"\n(Standard is 15hours per month)");

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setGoalSetting(myDatabase, false);
                closeContextMenu();
            }
        });
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setGoalSetting(myDatabase, true);
                closeContextMenu();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Get all data for a specify month and create a arrayList with the data
     * This method is call when the user press the process bar
     * @param myDatabase string that get the right goal for the right activity from the database
     */
    private void getAllActivityThisMonth(String myDatabase) {
        // Get actual month to get the right set of data
        Calendar cal = Calendar.getInstance();
        String[] myDate = dateFormat.format(cal.getTime()).split("/");
        //myDate [1] contains the month value
        String monthString = myDate[1];
        int monthInt = Integer.parseInt(monthString);

        // Access database and get set depending on number of month to display the right data process to the user
        // Save the set of data and pars it to calculate the time spent for a specific month
        SharedPreferences sp = getSharedPreferences(myDatabase, Context.MODE_PRIVATE);
        if(!sp.getAll().isEmpty()) {
            Set<String> mySet;
            mySet = sp.getStringSet("" + monthInt, null);

            String myString = null;
            if (mySet != null) {
                myString = mySet.toString();
                // example of dataSet [2h:30min:6/4/2016:3, 2h:15min:6/4/2016:2]
                myString = myString.replace("[", "");
                myString = myString.replace("]", "");
                myString = myString.replace(" ", "");
            }
            allActivityData.clear();

            String[] myActivates = new String[0];
            if (myString != null) {
                myActivates = myString.split(",");
            }
            for (String ma : myActivates) {
                // example of String mt = 2:30:6/4/2016:3
                String[] myComponents = ma.split(":");
                allActivityData.add(myComponents[2] + " " + myComponents[0] + " " + myComponents[1] );
            }
        } else {
            allActivityData.clear();
            allActivityData.add("There is no activity");
        }
    }


    /**
     * Convert arrayList to string array
     * @return a string array that the alertDialog
     */
    private String[] getAllActivityData(){
        String[] dataArray = new String[allActivityData.size()];
        for (int i=0; i<allActivityData.size(); i++){
            dataArray[i] = allActivityData.get(i);
        }
        return dataArray;
    }


    /**
     * This method create and set a alertDialog box with content
     * The content that presents is a list of all activity
     */
    private void open() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.class_process_popup_main).setItems(getAllActivityData(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                closeContextMenu();
                // The 'which' argument contains the index position
                // of the selected item
            }
        });
        //alertDialogBuilder.setMessage(R.string.class_process_popup_main);
        alertDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                closeContextMenu();
            }
        });
        //alertDialogBuilder.setMessage(R.string.class_process_popup_main);
        alertDialogBuilder.setPositiveButton("Add activity", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                closeContextMenu();
                Intent intent = new Intent(getBaseContext(), AddActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Set goal value first time the user use the application
     * @param set the id of the goal database
     * @param myBoolean take the users answer
     */
    private void setGoalSetting(String set, Boolean myBoolean) {
        SharedPreferences st = getSharedPreferences("GoalSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = st.edit();
        if(myBoolean) {
            editor.putInt(set,900);
            editor.apply();
        } else {
            editor.putInt(set,1);
            editor.apply();
        }
    }
}
