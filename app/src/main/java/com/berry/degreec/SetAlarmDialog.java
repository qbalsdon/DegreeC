package com.berry.degreec;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;


public class SetAlarmDialog extends Dialog {
    private Context context;
    private EditText time;
    private Spinner condition;
    private EditText temperature;

    public SetAlarmDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public SetAlarmDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected SetAlarmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alarm);
        setTitle(R.string.set_alarm);

        time = findViewById(R.id.time);
        condition = findViewById(R.id.conditional);
        temperature = findViewById(R.id.temperature);

        int selection = PreferenceUtil.getIntPreference(PreferenceUtil.CONDITION);
        float temperatureValue = PreferenceUtil.getFloatPreference(PreferenceUtil.TEMPERATURE);
        time.setText(PreferenceUtil.getStringPreference(PreferenceUtil.TIME));
        condition.setSelection((selection < 0) ? 0 : selection);
        temperature.setText((temperatureValue == Float.MIN_VALUE) ? "" : String.valueOf(temperatureValue));

        time.addTextChangedListener(new TextWatcher() {
            CharSequence previousText;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousText = charSequence;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                time.setError(null);
                time.removeTextChangedListener(this);
                charSequence = charSequence.toString().replace(":", "");
                if (charSequence.length() > 4) {
                    time.setText(previousText);
                } else if (charSequence.length() > 2) {
                    CharSequence suffix = charSequence.subSequence(charSequence.length() - 2, charSequence.length());
                    CharSequence prefix = charSequence.subSequence(0, charSequence.length() - 2);

                    time.setText(String.format("%s:%s", prefix, suffix));
                } else {
                    time.setText(charSequence);
                }
                time.setSelection(time.getText().length(), time.getText().length());
                time.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        temperature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temperature.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validTime = validTime(time.getText().toString());
                boolean validTemperature = validTemperature(temperature.getText().toString());

                if (!validTime) {
                    time.setError(context.getString(R.string.error_time));
                    return;
                }

                if (!validTemperature) {
                    temperature.setError(context.getString(R.string.error_temperature));
                    return;
                }

                setAlarm(time.getText().toString(), temperature.getText().toString(), condition.getSelectedItemPosition());
            }
        });
    }

    private boolean validTime(String time) {
        if (time == null || time.trim().length() == 0 || time.trim().length() > 5) return false;

        String[] split = time.split(":");

        if (split.length != 2) return false;

        int hours = 0;
        int minutes = 0;

        try {
            hours = Integer.parseInt(split[0]);
            minutes = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        return (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59);
    }

    private boolean validTemperature(String temperature) {
        if (temperature == null || temperature.trim().length() == 0 || temperature.trim().length() > 5)
            return false;

        float temperatureValue = -11;
        try {
            temperatureValue = Float.parseFloat(temperature);
        } catch (NumberFormatException e) {
            return false;
        }

        return (temperatureValue >= -10 && temperatureValue <= 30);
    }

    private void setAlarm(String time, String temperature, int condition) {
        Toast.makeText(context, context.getString(R.string.alarm_set), Toast.LENGTH_SHORT).show();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 98254, intent, 0);

        String[] timeSplit = time.split(":");
        int[] timeValues = new int[]{Integer.parseInt(timeSplit[0]), Integer.parseInt(timeSplit[1])};

        PreferenceUtil.savePreference(PreferenceUtil.TEMPERATURE, Float.parseFloat(temperature));
        PreferenceUtil.savePreference(PreferenceUtil.CONDITION, condition);
        PreferenceUtil.savePreference(PreferenceUtil.TIME, time);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, timeValues[0]);
        calendar.set(Calendar.MINUTE, timeValues[1]);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        dismiss();
    }
}
