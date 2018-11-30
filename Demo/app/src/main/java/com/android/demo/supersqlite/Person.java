package com.android.demo.supersqlite;

import com.android.jack.supersqlite.*;
import com.android.jack.supersqlite.constraint.*;

/**
 * Created by Administrator on 2018/8/6.
 * The document in project Demo.
 *
 * @author Jack
 */
@Binding("Person")
public class Person {
    @Binding("ID")
    @PrimaryKey
    @NotNull
    @AutoIncrement
    private int id;
    @Binding("Name")
    @NotNull
    private String name;
    @Binding("Age")
    private int age;
    @Binding("Tel")
    private String phone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s, %s}", id, name, age, phone);
    }
}
