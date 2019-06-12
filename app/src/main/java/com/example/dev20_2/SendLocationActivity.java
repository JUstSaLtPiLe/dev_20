package com.example.dev20_2;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class SendLocationActivity extends AppCompatActivity {
    private Spinner accidentList;
    private Button btnSubmit;
    private int type;
    private String description;
    private long lat;
    private long lng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_location);

        accidentList = findViewById(R.id.accidents_list);
        String[] accidentListTxt = {"Traffic Jam", "Accident", "Blockage"};
        int[] accidenListValue = {1,2,3};
        SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, accidentListTxt);
        accidentList.setAdapter(spinnerAdapter);
        accidentList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = accidenListValue[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        description = findViewById(R.id.editText_description).toString();
        lat = getIntent().getExtras().getLong("lat");
        lng = getIntent().getExtras().getLong("lng");
    }
}
