package fh.ooe.mcm.accelerometerdatagatherer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ToggleButton;

public class LockScreenReceiver extends BroadcastReceiver {

    ToggleButton currentlyToggled;

    MainActivity parent;

    public LockScreenReceiver() {
    }

    public LockScreenReceiver(MainActivity parent) {
        this.parent = parent;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
            {
                // Screen is on but not unlocked (if any locking mechanism present)
                currentlyToggled.setChecked(false);
                //parent.changeRate(NORMAL_READING_RATE);
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
            {
                //currentlyToggled.setChecked(false);
                //parent.changeRate(FASTER_READING_RATE);
            }
        }
    }

    public void setCurrentlyToggled(ToggleButton currentlyToggled) {
        this.currentlyToggled = currentlyToggled;
    }
}