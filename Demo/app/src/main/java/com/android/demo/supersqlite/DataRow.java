package com.android.demo.supersqlite;

import com.android.jack.supersqlite.BaseRow;

/**
 * Created by Administrator on 2018/8/6.
 * The document in project Demo.
 *
 * @author Jack
 */
public class DataRow extends BaseRow {
    private String name;
    private int age;
    private String tel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
