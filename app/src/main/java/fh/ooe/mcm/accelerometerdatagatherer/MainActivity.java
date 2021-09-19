package fh.ooe.mcm.accelerometerdatagatherer;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.PowerManager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    DecimalFormat format = new DecimalFormat("00.00");

    boolean activityOn = false;

    String activity;

    String label;

    String filename = "data.txt";
    FileOutputStream outputStream;
    File file;

    ToggleButton sittingToggle;
    ToggleButton standingToggle;
    ToggleButton walkingToggle;
    ToggleButton joggingToggle;
    ToggleButton downstairsToggle;
    ToggleButton upstairsToggle;

    ToggleButton currentlyToggled;

    ArrayList<String> gatheredData;

    LockScreenReceiver lockScreenReceiver;
    SensorService sensorService;

    long previousTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        file  = new File(getExternalFilesDir(null) + "/" +  filename);
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) {
                    Toast.makeText(getApplicationContext(), "File not created.", Toast.LENGTH_SHORT);
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        gatheredData = new ArrayList<>();

        sittingToggle = findViewById(R.id.sittingToggle);
        standingToggle = findViewById(R.id.standingToggle);
        walkingToggle = findViewById(R.id.walkingToggle);
        joggingToggle = findViewById(R.id.joggingToggle);
        downstairsToggle = findViewById(R.id.downstairsToggle);
        upstairsToggle = findViewById(R.id.upstairsToggle);

        sittingToggle.setOnCheckedChangeListener(this);
        standingToggle.setOnCheckedChangeListener(this);
        walkingToggle.setOnCheckedChangeListener(this);
        joggingToggle.setOnCheckedChangeListener(this);
        downstairsToggle.setOnCheckedChangeListener(this);
        upstairsToggle.setOnCheckedChangeListener(this);

        lockScreenReceiver = new LockScreenReceiver(this);
        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(lockScreenReceiver, lockFilter);

        sensorService = new SensorService(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if(currentlyToggled != null) {
                currentlyToggled.setChecked(false);
            }
            currentlyToggled = (ToggleButton) buttonView;
            lockScreenReceiver.setCurrentlyToggled(currentlyToggled);
            activityOn = true;
            activity = buttonView.getText().toString();
            sensorService.sleep(3000);
            gatheredData.clear();
            String startLine = " " + "," + "START"  + "," + 0  + "," +  0  + "," +  0 + "\n";
            gatheredData.add(startLine);
        } else {
            activityOn = false;
            currentlyToggled = null;
            gatheredData.clear();
        }
    }

    private class DataWriter extends AsyncTask<ArrayList<String>, Integer, Void> {
        protected Void doInBackground(ArrayList<String>... arrayLists) {
            ArrayList<String> lines = arrayLists[0];
            try {
                outputStream = new FileOutputStream(file, true);
               for (String line : lines) {
                    outputStream.write(line.getBytes());
                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void addData(double x, double y, double z) {
        Long time = System.currentTimeMillis();
        Long timediff = time - previousTime;

        if(timediff > 45 && previousTime != 0) {
            gatheredData.add(time + "," + activity + "," + format.format(x) + "," + format.format(y) + "," + format.format(z) + "\n");

            if (gatheredData.size() >= 201) {
                new MainActivity.DataWriter().execute((ArrayList<String>) gatheredData.clone());
                gatheredData.clear();
            }

        }
        previousTime = time;
    }

    public boolean isActivityOn() {
        return activityOn;
    }

}

