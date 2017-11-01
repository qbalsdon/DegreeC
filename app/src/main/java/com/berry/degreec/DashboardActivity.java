package com.berry.degreec;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.berry.degreec.view.Thermometer;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Thermometer thermometer;
    private LineChart graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        thermometer = findViewById(R.id.thermometer);

        graph = findViewById(R.id.graph);
        graph.setDrawGridBackground(false);
        graph.getDescription().setEnabled(false);
        graph.setTouchEnabled(true);
        graph.setDragEnabled(true);
        graph.setScaleEnabled(true);
        graph.setPinchZoom(true);
        graph.getAxisRight().setEnabled(false);
        Legend l = graph.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        findViewById(R.id.cancel_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();
            }
        });

        findViewById(R.id.set_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAlarm();
                SetAlarmDialog dialog = new SetAlarmDialog(DashboardActivity.this);
                dialog.show();
            }
        });
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            GenericTypeIndicator<List<Float>> genericTypeIndicator = new GenericTypeIndicator<List<Float>>() {
            };
            List<Float> temperatures = dataSnapshot.child("readings").getValue(genericTypeIndicator);
            thermometer.setCurrentTemp(temperatures.get(temperatures.size() - 1));
            updateGraph(temperatures);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("bb9a0749-63a7-4d04-8e47-d40be76f2012");
        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onPause() {
        databaseReference.removeEventListener(valueEventListener);
        super.onPause();
    }

    private void updateGraph(List<Float> temperatures) {
        List<Entry> values = new ArrayList<>();

        for (Float number : temperatures) {
            values.add(new Entry(values.size() + 1, number));
        }

        LineDataSet lineDataSet = new LineDataSet(values, getString(R.string.temperature));
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleColor(Color.RED);
        lineDataSet.setLineWidth(2.0f);
        lineDataSet.setColor(Color.DKGRAY);
        lineDataSet.setDrawFilled(true);

        lineDataSet.setValues(values);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        graph.setData(new LineData(dataSets));

        graph.getData().notifyDataChanged();
        graph.notifyDataSetChanged();

        graph.animateX(2500);
    }

    private void cancelAlarm() {
        Toast.makeText(this, getString(R.string.alarm_cancelled), Toast.LENGTH_SHORT).show();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 98254, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);


    }
}
