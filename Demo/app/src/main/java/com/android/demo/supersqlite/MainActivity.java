package com.android.demo.supersqlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.jack.supersqlite.SuperSQLite;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SuperSQLite<DataRow>superSQLite = new SuperSQLite<>(this, "database.db", DataRow.class);

        DataRow data1 = new DataRow();
        data1.setName("张三");
        data1.setAge(10);
        data1.setTel("13312345678");

        DataRow data2 = new DataRow();
        data2.setName("李四");
        data2.setAge(15);
        data2.setTel("13412345678");

        superSQLite.insertOrUpdate(data1);
        superSQLite.insertOrUpdate(data2);

        List<DataRow> list = superSQLite.query("name = '张三'");
        DataRow dataRow = list.get(0);
        textView1.setText(dataRow.getName() + ", " + dataRow.getAge() + ", " + dataRow.getTel());
    }


}
