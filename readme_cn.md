android sqlite database 框架。


可以使用对象来操作数据库的一切。

集成方法：

repositories {

    flatDir {
    
        dirs 'libs' //this way we can find the .aar file in libs folder
        
    }
    
}


    implementation(name: 'supersqlite-20180806', ext: 'aar')
    
示例：

public class DataRow extends BaseRow {

    String name;
    
    boolean sex;
    
    int age;
    
    String tel;
    
    Date createTime;
    
}

SuperSQLite<DataRow> superSQLite = new SuperSQLite<>(context, "database.db", DataRow.class);

superSQLite.insertOrUpdate(new DataRow());

superSQLite.query(new int[]{1});
