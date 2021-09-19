package fh.ooe.mcm.accelerometerdatagatherer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;

public class SensorService extends Service implements SensorEventListener {

    int READING_RATE = 50000;

    private SensorManager sensorManager;
    private Sensor sensor;

    HandlerThread sensorThread;
    Handler sensorHandler;

    MainActivity parent;

    PowerManager.WakeLock wakeLock;

    public SensorService(MainActivity parent) {
        this.parent = parent;

        sensorManager = (SensorManager) parent.getSystemService(Context.SENSOR_SERVICE);
        sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorThread = new HandlerThread("Sensor thread", Process.THREAD_PRIORITY_BACKGROUND);
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
        sensorManager.registerListener(this, sensor, READING_RATE, sensorHandler);


        PowerManager pm = (PowerManager) parent.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myApp:myWakeTag");
        wakeLock.acquire();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && parent.isActivityOn()) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            parent.addData(x, y, z);
        }
    }

    public void sleep(int time) {
        try {
            sensorThread.sleep(time); //why does this block UI thread?!
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing here.
    }

    //@androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
