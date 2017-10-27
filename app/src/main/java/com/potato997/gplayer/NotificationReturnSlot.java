package com.potato997.gplayer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

/**
 * Created by linjie on 22/10/2017.
 */

public class NotificationReturnSlot extends Activity {

    ImageButton nPlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        nPlay = (ImageButton) findViewById(R.id.nPlay);

        String action = (String) getIntent().getExtras().get("DO");

        if (action.equals("play")) {
            //MainActivity.play();
        } else if (action.equals("back")) {
            //MainActivity.back();
        }
        else if (action.equals("forward")) {
            //MainActivity.forward();
        }

        finish();
    }
}