package com.potato997.audioplayerdemo3;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by linjie on 22/10/2017.
 */

public class NotificationReturnSlot extends Activity {

    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        String action = (String) getIntent().getExtras().get("DO");
        Toast.makeText(getApplicationContext(), action, Toast.LENGTH_LONG).show();
        if (action.equals("play")) {
            ((MainActivity)ctx).play();
        } else if (action.equals("back")) {
            ((MainActivity)ctx).back();
        }
        else if (action.equals("forward")) {
            ((MainActivity)ctx).forward();
        }

        finish();
    }


}