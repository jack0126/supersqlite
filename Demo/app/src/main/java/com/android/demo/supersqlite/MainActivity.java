package com.android.demo.supersqlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.jack.supersqlite.SuperSQLite;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        textView1 = findViewById(R.id.textView1);

        SuperSQLite superSQLite = new SuperSQLite(this, "test.db");

        Person data1 = new Person();
        data1.setName("aa");
        data1.setAge(10);
        data1.setPhone("13312345678");

        Person data2 = new Person();
        data2.setName("bb");
        data2.setAge(15);
        data2.setPhone("13412345678");

        superSQLite.insert(Person.class, data1, data2);

        Log.d(TAG, "onCreate: " + superSQLite.getCount(Person.class));
        Log.d(TAG, "onCreate: " + superSQLite.getCount(Person.class, "Name = 'aa'"));
        Log.d(TAG, "onCreate: " + superSQLite.query(Person.class, "Name = 'aa'"));
    }


}
