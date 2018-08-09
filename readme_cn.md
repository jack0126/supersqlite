android sqlite database 框架。

优点：

本框架使用面向对象的方式来操作 android SQLiteDatabase数据库，使对数据库的操作简单易懂，可以使用对象来操作数据库的一切。

支持自定义表结构，需要定义如下 String[]COLUMNS_NAME，表结构字段名必需是数据变量名，如：

private static final COLUMNS_NAME = {"name", "sex", "age"};

缺点：

只支持单表数据库。

集成方法：
作为模块添加或者AAR包添加

repositories {

    flatDir {
    
        dirs 'libs' //this way we can find the .aar file in libs folder
        
    }
    
}


    implementation(name: 'supersqlite-20180806', ext: 'aar')
    
    
示例：

//数据模型定义，数据模型必需是 BaseRow的子类

public class DataRow extends BaseRow {

    //自定义数据表结构
    
    private static final COLUMNS_NAME = {"name", "sex", "age", "tel", "createTime"};
    
    String name;
    
    boolean sex;
    
    int age;
    
    String tel;
    
    Date createTime;
    
}

SuperSQLite<DataRow> superSQLite = new SuperSQLite<>(context, "database.db", DataRow.class);

superSQLite.insertOrUpdate(new DataRow());

superSQLite.query(new int[]{1});

欢迎一起改进学习。可以联系：final.hsx@qq.com

