package com.berry.degreec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;


public class AlarmReceiver extends BroadcastReceiver {
    private Context context;
    private DatabaseReference databaseReference;

    private void message(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        android.util.Log.d("Quintin", message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        message("Received");
        if (!PreferenceUtil.isValid()) {
            PreferenceUtil.init(context);
        }

        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
                checkAlarm();
                break;
            default:

        }
    }

    private void checkAlarm() {
        message("Check Alarm");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("bb9a0749-63a7-4d04-8e47-d40be76f2012");
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            message("Value changed");
            GenericTypeIndicator<List<Float>> genericTypeIndicator = new GenericTypeIndicator<List<Float>>() {
            };
            List<Float> temperatures = dataSnapshot.child("readings").getValue(genericTypeIndicator);

            boolean conditionMet = false;
            switch (PreferenceUtil.getIntPreference(PreferenceUtil.CONDITION)) {
                case Constants.ABOVE:
                    conditionMet = temperatures.get(temperatures.size() - 1) > PreferenceUtil.getFloatPreference(PreferenceUtil.TEMPERATURE);
                    break;
                case Constants.BELOW:
                    conditionMet = temperatures.get(temperatures.size() - 1) < PreferenceUtil.getFloatPreference(PreferenceUtil.TEMPERATURE);
                    break;
                case Constants.EQUAL:
                    conditionMet = temperatures.get(temperatures.size() - 1) == PreferenceUtil.getFloatPreference(PreferenceUtil.TEMPERATURE);
                    break;
                default:
                    conditionMet = false;
            }
            message(String.format("Condition: [%s] Met: [%s]", PreferenceUtil.getIntPreference(PreferenceUtil.CONDITION), (conditionMet) ? "TRUE" : "FALSE"));
            if (conditionMet) {
                soundAlarm();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void soundAlarm() {
        message("Alarm sounded");
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        MediaPlayer thePlayer = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        try {
            thePlayer.setVolume(
                    (float) (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) / 7.0),
                    (float) (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) / 7.0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        thePlayer.start();
    }
}
