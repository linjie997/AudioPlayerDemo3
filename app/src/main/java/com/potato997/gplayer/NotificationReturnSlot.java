package com.potato997.gplayer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by linjie on 22/10/2017.
 */

public class NotificationReturnSlot extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        String action = (String) getIntent().getExtras().get("DO");

        Toast.makeText(getApplicationContext(), action, Toast.LENGTH_SHORT).show();

        if (action.equals("play")) {

        } else if (action.equals("back")) {

        }
        else if (action.equals("forward")) {

        }

        finish();
    }
}