android sqlite database ��ܡ�


����ʹ�ö������������ݿ��һ�С�

���ɷ�����

repositories {

    flatDir {
    
        dirs 'libs' //this way we can find the .aar file in libs folder
        
    }
    
}


    implementation(name: 'supersqlite-20180806', ext: 'aar')
    
ʾ����

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
