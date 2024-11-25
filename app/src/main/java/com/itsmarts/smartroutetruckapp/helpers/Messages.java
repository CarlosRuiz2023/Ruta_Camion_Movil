package com.itsmarts.smartroutetruckapp.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.itsmarts.smartroutetruckapp.R;

public class Messages {
    Activity mainActivity;
    Context context;

    public Messages(Activity mainActivity,Context context) {
        this.mainActivity = mainActivity;

    }

    public void showCustomToast(String message) {
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, mainActivity.findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
    }
}
